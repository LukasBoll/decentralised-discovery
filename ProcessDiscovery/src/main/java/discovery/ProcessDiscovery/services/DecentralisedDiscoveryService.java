package discovery.ProcessDiscovery.services;

import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.*;
import discovery.ProcessDiscovery.models.*;
import discovery.ProcessDiscovery.repositories.AuthorizationRepository;
import discovery.ProcessDiscovery.repositories.CommunicationEventRepository;
import discovery.ProcessDiscovery.repositories.MessageFlowRepository;
import discovery.ProcessDiscovery.util.GatewayAndNodes;
import discovery.ProcessDiscovery.util.WebUtil;
import discovery.ProcessDiscovery.util.XmlUtil;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static discovery.ProcessDiscovery.ProcessDiscoveryApplication.applicationID;
import static discovery.ProcessDiscovery.it.unicam.pros.colliery.core.CollaborationMiner.convertCommunicationEvents;
import static discovery.ProcessDiscovery.it.unicam.pros.colliery.core.CollaborationMiner.discovery;
import static discovery.ProcessDiscovery.it.unicam.pros.colliery.core.MsgType.RECEIVE;
import static discovery.ProcessDiscovery.it.unicam.pros.colliery.core.MsgType.SEND;
import static discovery.ProcessDiscovery.util.XmlUtil.filterNonInteracting;

@Service
public class DecentralisedDiscoveryService {

    private static final String srcLogPath = "log.xes";
    private static final String interactingLogPath = "interactingLog.xes";
    private static final String privateModelPath = "privateModel.bpmn";
    private static final String publicModelPath = "publicModel.bpmn";


    private final CommunicationEventRepository communicationEventRepository;
    private final AuthorizationRepository authorizationRepository;
    private final RoutingService routingService;
    private final MessageFlowRepository messageFlowRepository;

    public DecentralisedDiscoveryService(CommunicationEventRepository communicationEventRepository, AuthorizationRepository authorizationRepository, RoutingService routingService, MessageFlowRepository messageFlowRepository) {
        this.communicationEventRepository = communicationEventRepository;
        this.authorizationRepository = authorizationRepository;
        this.routingService = routingService;
        this.messageFlowRepository = messageFlowRepository;
    }


    public void buildModels() {
        System.out.println("discover");
        try {
            Pm4PyBridge.checkScripts();
            Communication communications = new Communication();
            String privateModel = minePrivateModel(communications);

            saveCommunicationEvents(communications);
            saveMessageFlowM();

            communications = new Communication();
            generatePubLog();
            String publicModel = minePublicModel(communications);

            ArrayList<String> lostMessageFlows = getLostMessagesL(communications);
            GatewayAndNodes gatewayAndNodesPrivateModel = getRC(privateModel, lostMessageFlows);
            GatewayAndNodes gatewayAndNodesPublicModel = getRC(publicModel, lostMessageFlows);


            List<List<String>> raceConditions = gatewayAndNodesPrivateModel.getTasksInCompetition();
            privateModel = transformationB(privateModel, lostMessageFlows, raceConditions, gatewayAndNodesPrivateModel.getXorGateways());

            publicModel = transformationB(publicModel, lostMessageFlows, raceConditions, gatewayAndNodesPublicModel.getXorGateways());

            stringToFile(new File(Path.of(System.getProperty("user.dir"), privateModelPath).toString()), privateModel);
            stringToFile(new File(Path.of(System.getProperty("user.dir"), publicModelPath).toString()), publicModel);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String transformationB(String model, ArrayList<String> lostMessageFlows, List<List<String>> rcs, List<Node> xorGateways) {

        /*transform all tasks except:
        tasks in lost message list, that are NOT in the list of race conditions
         */
        List<String>tasksToNotTransform = new ArrayList<>();
        //
        for (String mf : lostMessageFlows) {
            tasksToNotTransform.add(messageFlowRepository.getFirstByName(mf).getReceiveTask());
        }
        tasksToNotTransform=tasksToNotTransform.stream().filter(task->
            rcs.stream().anyMatch(rc->rc.stream().anyMatch(receiveTask->receiveTask.equals(task)))).collect(Collectors.toList());

        return XmlUtil.convertXorGateways(model,xorGateways, tasksToNotTransform);
    }

    private GatewayAndNodes getRC(String model, ArrayList<String> lostMessageFlows) {
        GatewayAndNodes gatewayAndNodes = XmlUtil.getTasksInCompetition(model);
        List<List<String>> tasksInCompetition = gatewayAndNodes.getTasksInCompetition();
        tasksInCompetition= tasksInCompetition.stream().filter(this::isRaceCondition).collect(Collectors.toList());
        return new GatewayAndNodes(gatewayAndNodes.getXorGateways(),tasksInCompetition);

    }

    private boolean isRaceCondition(List<String> tasks) {
        List<CommunicationEvent> competingSendEvents = new ArrayList<>();
        List<CommunicationEvent> competingReceiveEvents = new ArrayList<>();

        for (String task : tasks) {
            MessageFlow msgFlow = messageFlowRepository.getFirstByReceiveTask(task);
            competingSendEvents.addAll(communicationEventRepository.findAllByFlowAndType(msgFlow.getName(), SEND));
            competingReceiveEvents.addAll(communicationEventRepository.findAllByFlowAndType(msgFlow.getName(), RECEIVE));
        }

        competingSendEvents.sort(Comparator.comparing(CommunicationEvent::getDate));
        competingReceiveEvents.sort(Comparator.comparing(CommunicationEvent::getDate));

        boolean isRC = true;

        while(competingSendEvents.size()>0 && competingReceiveEvents.size()>0){
            CommunicationEvent sendingEventWaitingForConsumption = competingSendEvents.get(0);
            CommunicationEvent nextReceiveEvent = competingReceiveEvents.get(0);
            if(sendingEventWaitingForConsumption.getFlow().equals(nextReceiveEvent.getFlow())){
                competingSendEvents = competingSendEvents.stream().filter(e->e.getDate().after(nextReceiveEvent.getDate())).collect(Collectors.toList());
                competingReceiveEvents.remove(0);
            }
            else{
                isRC = false;
                break;
            }
        }

        return isRC;
    }

    private ArrayList<String> getLostMessagesL(Communication communications) {
        Map<String, MsgConnections> comTypes = communications.getMsgFlows();

        ArrayList<String> setOfLostMessages = new ArrayList<>();
        comTypes.keySet().forEach(msgFlow-> {
                    System.out.println(communicationEventRepository.findAllByFlowAndType(msgFlow, SEND).size());
            System.out.println(communicationEventRepository.findAllByFlowAndType(msgFlow, RECEIVE).size());
            if(communicationEventRepository.findAllByFlowAndType(msgFlow, SEND).size()
                            != communicationEventRepository.findAllByFlowAndType(msgFlow, RECEIVE).size()){
                        setOfLostMessages.add(msgFlow);
                    }
                }
        );
        return setOfLostMessages;
    }

    private void generatePubLog() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document log = db.parse(new File(Path.of(System.getProperty("user.dir"), srcLogPath).toString()));

        filterNonInteracting(log);

        try (FileOutputStream output =
                     new FileOutputStream(Path.of(System.getProperty("user.dir"), interactingLogPath).toString())) {
            XmlUtil.writeXml(log, output);
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private String minePublicModel(Communication communications) throws Exception {

        System.out.println("Public Model: Start Mining");
        String publicModel = mineModel(Path.of(System.getProperty("user.dir"), interactingLogPath).toString(), DiscoveryAlgorithm.ALPHA, communications);
        publicModel = BPMNUtils.convertStartEndMessageEvents(publicModel);
        return publicModel;
    }


    private String minePrivateModel(Communication communications) throws Exception {

        System.out.println("Private Model: Start Mining");
        String privateModel = mineModel(Path.of(System.getProperty("user.dir"), srcLogPath).toString(), DiscoveryAlgorithm.ALPHA, communications);
        privateModel = BPMNUtils.convertStartEndMessageEvents(privateModel);

        return privateModel;
    }


    private void saveMessageFlowM() {
        List<CommunicationEvent> communicationEvents = communicationEventRepository.findAll();

        Map<String, MessageFlow> mappedEvents = communicationEvents.stream().collect(Collectors.toMap(CommunicationEvent::getFlow,
                comEvent ->
                        new MessageFlow(comEvent.getSender(), comEvent.getReceiver(), comEvent.getFlow(),
                                comEvent.getType() == SEND ? comEvent.getActivity() : null,
                                comEvent.getType() == RECEIVE ? comEvent.getActivity() : null),
                (oldValue, newValue) -> {
                    if (newValue.getSendTask() != null) {
                        oldValue.setSendTask(newValue.getSendTask());
                    }
                    if (newValue.getReceiveTask() != null) {
                        oldValue.setReceiveTask(newValue.getReceiveTask());
                    }
                    return oldValue;
                })
        );
        mappedEvents.forEach((id, mf)-> messageFlowRepository.save(mf));
    }

    private void saveCommunicationEvents(Communication communications) {
        //Own Events
        if (communicationEventRepository.count() == 0) {
            List<CommunicationEvent> communicationEventList = new ArrayList<>();
            communications.getCommunacations().forEach((integer, commEventSet) -> {
                commEventSet.forEach(commEvent -> communicationEventList.add(CommunicationEvent.fromCommEvent(commEvent)));
            });
            communicationEventRepository.saveAll(communicationEventList);
        }

        List<Organization> organizations = authorizationRepository.findAll();

        organizations.stream().filter(Objects::nonNull).forEach(
                (organization -> {
                    String address = routingService.getAddress(organization);
                    RestTemplate restTemplate = new RestTemplate();
                    HttpEntity<String> entity= new HttpEntity<>("", WebUtil.generateHeaders(organization));

                    ResponseEntity<CommunicationEvent[]> result = restTemplate.exchange(address + "/collaboration/communicationevent", HttpMethod.GET, entity, CommunicationEvent[].class);
                    if (result.getBody() != null) {
                        communicationEventRepository.saveAll(Arrays.stream(result.getBody()).toList());
                    }
                })
        );
    }

    private String mineModel(String pathToXESFile, DiscoveryAlgorithm algo, Communication communications) throws Exception {
        BpmnModelInstance process = null;
        System.out.println("Discovery on " + pathToXESFile + " with " + algo + "...");
        process = discovery(pathToXESFile, algo);
        System.out.println("Discovery on " + pathToXESFile + " completed.\n");
        System.out.println("Extract communication from " + pathToXESFile + "...");
        communications.addAll(0, XESUtils.extractCommunication(0, pathToXESFile));
        System.out.println("Extraction from" + pathToXESFile + " completed.\n");
        System.out.println("Convert communication events for " + pathToXESFile + "...");
        String proc = Bpmn.convertToString(process);
        proc = convertCommunicationEvents(proc, communications, 0);
        System.out.println("Convertion on " + pathToXESFile + " completed.\n");
        return proc;
    }


    private static void stringToFile(File file, String xml) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(xml);
        writer.close();
    }

    public List<CommunicationEvent> getCommunicationEvents(String id) throws IOException {
        if (communicationEventRepository.count() == 0) {
            Set<CommEvent> communication = XESUtils.extractCommunication(0, srcLogPath);
            communicationEventRepository.saveAll(communication.stream().map(CommunicationEvent::fromCommEvent).collect(Collectors.toList()));
        }
        return communicationEventRepository.findAllBySenderOrReceiverAndOrganization(id, id, applicationID);
    }

    public Document getPrivateModel() throws ParserConfigurationException, IOException, SAXException {
        return getLocalModel(privateModelPath);
    }

    public Document getPublicModel() throws ParserConfigurationException, IOException, SAXException {
        return getLocalModel(publicModelPath);
    }

    public Document getLocalModel(String path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document log = null;
        try {
            log = db.parse(new File(Path.of(System.getProperty("user.dir"), path).toString()));
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            buildModels();
            log = db.parse(new File(Path.of(System.getProperty("user.dir"), path).toString()));
        }
        return log;
    }

    public Document getCOModel() throws ParserConfigurationException, IOException, SAXException {
        Document coModel = getLocalModel(privateModelPath);
        List<MessageFlow> unconnectedMessages = messageFlowRepository.findAll();

        Map<String,List<MessageFlow>> unconnectedMessageMap = new HashMap<>();

        insertIfNotExistsInModel(unconnectedMessageMap,unconnectedMessages,coModel);

        while(!unconnectedMessageMap.isEmpty()){
            String organizationToRequestId = unconnectedMessageMap.keySet().iterator().next();
            List<MessageFlow> messageFromToOrganization = unconnectedMessageMap.get(organizationToRequestId);
            NodeList tasks = coModel.getElementsByTagName("bpmn:task");
            List<String> entryPoints = messageFromToOrganization.stream().map(mf->
                XmlUtil.isInTasks(mf.getReceiveTask(),tasks)?mf.getSendTask():mf.getReceiveTask()
                ).collect(Collectors.toList());

            Organization organizationToRequest = authorizationRepository.getReferenceById(organizationToRequestId);
            String address = routingService.getAddress(organizationToRequest);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<List<String>> entity= new HttpEntity<>(entryPoints, WebUtil.generateHeaders(organizationToRequest));

            CollaborationDiscoverResponse result = restTemplate.postForObject(address + "/collaboration/discover", entity ,CollaborationDiscoverResponse.class);
            if (result != null) {
                //combineBPM
                List<String> modelStringList = new LinkedList(List.of(XESUtils.convertXMLToString(coModel),XESUtils.convertXMLToString(result.getModel())));
                coModel = XESUtils.convertStringToXMLDocument(BPMNUtils.groupProcesses(modelStringList));
                //addMsgtoModel
                for (MessageFlow msg : messageFromToOrganization) {
                    BPMNUtils.makeMsgFlow(coModel, msg.getName(), msg.getSendTask(), msg.getReceiveTask());
                }
                //insert newmsg
                insertIfNotExistsInModel(unconnectedMessageMap,result.getMessageFlows(),coModel);
            }
            unconnectedMessageMap.remove(organizationToRequestId);
        }
        stringToFile(new File(Path.of(System.getProperty("user.dir"), "coModel.bpmn").toString()), XESUtils.convertXMLToString(coModel));
        System.out.println("CO Model: Serialization completed at " + Path.of(System.getProperty("user.dir"),"coModel.bpmn").toString() + "\n");
        return coModel;
    }

    public void insertIfNotExistsInModel(Map<String,List<MessageFlow>> map, List<MessageFlow> unconnectedMessages,Document model){
        NodeList tasks = model.getElementsByTagName("bpmn:task");
        unconnectedMessages.forEach(messageFlow -> {
            if(messageFlow.getReceiveTask()!=null && messageFlow.getSendTask()!=null) {
                if (!XmlUtil.isInTasks(messageFlow.getReceiveTask(), tasks) && !messageFlow.getReceiver().equals(applicationID)) {
                    if (map.get(messageFlow.getReceiver()) != null) {
                        map.get(messageFlow.getReceiver()).add(messageFlow);
                    } else {
                        map.put(messageFlow.getReceiver(), new LinkedList<>(List.of(messageFlow)));
                    }
                } else if (!XmlUtil.isInTasks(messageFlow.getSendTask(), tasks) && !messageFlow.getSender().equals(applicationID)) {
                    if (map.get(messageFlow.getSender()) != null) {
                        map.get(messageFlow.getSender()).add(messageFlow);
                    } else {
                        map.put(messageFlow.getSender(), new LinkedList<>(List.of(messageFlow)));
                    }
                }
            }
        });
    }
}
