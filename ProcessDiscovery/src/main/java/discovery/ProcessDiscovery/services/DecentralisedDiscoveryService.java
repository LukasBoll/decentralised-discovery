package discovery.ProcessDiscovery.services;

import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.*;
import discovery.ProcessDiscovery.models.*;
import discovery.ProcessDiscovery.repositories.AuthorizationRepository;
import discovery.ProcessDiscovery.repositories.CommunicationEventRepository;
import discovery.ProcessDiscovery.repositories.MessageFlowRepository;
import discovery.ProcessDiscovery.util.XmlUtil;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
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

    public DecentralisedDiscoveryService(CommunicationEventRepository communicationEventRepository, AuthorizationRepository authorizationRepository, RoutingService routingService, MessageFlowRepository messageFlowRepository, MessageFlowRepository messageFlowRepository1) {
        this.communicationEventRepository = communicationEventRepository;
        this.authorizationRepository = authorizationRepository;
        this.routingService = routingService;
        this.messageFlowRepository = messageFlowRepository1;
    }


    public void buildModels() {
        System.out.println("discover");
        try {
            Pm4PyBridge.checkScripts();
            String privateModel = minePrivateModel();
            stringToFile(new File(Path.of(System.getProperty("user.dir"), privateModelPath).toString()), privateModel);
            System.out.println("Private Model: Serialization completed at " + Path.of(System.getProperty("user.dir"), privateModelPath).toString() + "\n");
            generatePubLog();
            String publicModel = minePublicModel();
            stringToFile(new File(Path.of(System.getProperty("user.dir"), publicModelPath).toString()), publicModel);
            System.out.println("Public Model: Serialization completed at " + Path.of(System.getProperty("user.dir"), publicModelPath).toString() + "\n");

            getLostMessagesL();//TODO
            getRC();//TODO
            transformB();//TODO

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private String minePublicModel() throws Exception {

        System.out.println("Public Model: Start Mining");
        Communication communications = new Communication();
        return mineModel(Path.of(System.getProperty("user.dir"), interactingLogPath).toString(), DiscoveryAlgorithm.ALPHA, communications);

    }


    private String minePrivateModel() throws Exception {

        System.out.println("Private Model: Start Mining");
        Communication communications = new Communication();
        String privateModel = mineModel(Path.of(System.getProperty("user.dir"), srcLogPath).toString(), DiscoveryAlgorithm.ALPHA, communications);

        saveCommunicationEvents(communications);
        saveMessageFlowM();

        return privateModel;
    }

    private void getRC() {
    }

    private void getLostMessagesL() {
    }

    private void transformB() {
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

        organizations.stream().map(routingService::getAddress).collect(Collectors.toList()).stream().filter(Objects::nonNull).forEach(
                (address -> {
                    RestTemplate restTemplate = new RestTemplate();
                    CommunicationEvent[] result = restTemplate.getForObject(address + "/collaboration/communicationevent/{id}", CommunicationEvent[].class, Map.of("id", applicationID));
                    if (result != null) {
                        communicationEventRepository.saveAll(Arrays.stream(result).toList());
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
            CollaborationDiscoverResponse result = restTemplate.postForObject(address + "/collaboration/discover", entryPoints,CollaborationDiscoverResponse.class);
            if (result != null) {
                //combineBPM
                insertIfNotExistsInModel(unconnectedMessageMap,result.getMessageFlows(),coModel);
            }
            unconnectedMessageMap.remove(organizationToRequestId);
        }
        return coModel;
    }

    public void insertIfNotExistsInModel(Map<String,List<MessageFlow>> map, List<MessageFlow> unconnectedMessages,Document model){
        NodeList tasks = model.getElementsByTagName("bpmn:task");
        unconnectedMessages.forEach(messageFlow -> {
            if(messageFlow.getReceiveTask()!=null && messageFlow.getSendTask()!=null) {
                if (!XmlUtil.isInTasks(messageFlow.getReceiveTask(), tasks)) {
                    if (map.get(messageFlow.getReceiver()) != null) {
                        map.get(messageFlow.getReceiver()).add(messageFlow);
                    } else {
                        map.put(messageFlow.getReceiver(), List.of(messageFlow));
                    }
                } else if (!XmlUtil.isInTasks(messageFlow.getSendTask(), tasks)) {
                    if (map.get(messageFlow.getSendTask()) != null) {
                        map.get(messageFlow.getSendTask()).add(messageFlow);
                    } else {
                        map.put(messageFlow.getSendTask(), List.of(messageFlow));
                    }
                }
            }
        });
    }
}
