package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class CollaborationMiner{

    private String[] paths; DiscoveryAlgorithm a;
    private String resModel = null;
    private boolean isTerminated = false;
    private static CommunicationAnalysis results;
    private static Map<String, Object> miningParams;
    public boolean isTerminated(){ return isTerminated;}
    public String getResModel(){
        return resModel;
    }
    public CommunicationAnalysis getCommAnalysisResults(){
        return results;
    }

    public void setParams(String[] pathsToXESFiles, DiscoveryAlgorithm algo, Map<String, Object> params){
        resModel = "";
        paths = pathsToXESFiles;
        a = algo;
        isTerminated = false;
        setMiningParams(params);
    }


    private double heu_tres = 0.5;
    private double heu_and_tres = 0.65;
    private double heu_loop_tres = 1;
    private double ind_tres = 0.2;


    public Map<String, Object> getMiningParams() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("heu_tres", heu_tres);
        ret.put("heu_and_tres", heu_and_tres);
        ret.put("heu_loop_tres", heu_loop_tres);

        ret.put("ind_tres", ind_tres);

        return ret;
    }


    private void setMiningParams(Map<String, Object> params) {
        miningParams = params;
    }


    public static String mineLogs(String[] pathsToXESFiles, DiscoveryAlgorithm algo) throws Exception {
        List<String> xmlModels = new ArrayList<>(pathsToXESFiles.length);
        Communication communications = new Communication();
        for(int i  = 0; i < pathsToXESFiles.length; i++){
            BpmnModelInstance process = null;
            System.out.println("Discovery on "+pathsToXESFiles[i]+" with "+algo+"...");
            process = discovery(pathsToXESFiles[i], algo);
            System.out.println("Discovery on "+pathsToXESFiles[i]+" completed.\n");
            System.out.println("Extract communication from "+pathsToXESFiles[i]+"...");
            communications.addAll(i, XESUtils.extractCommunication(i, pathsToXESFiles[i]));
            System.out.println("Extraction from"+pathsToXESFiles[i]+" completed.\n");
            System.out.println("Convert communication events for "+pathsToXESFiles[i]+"...");
            String proc = Bpmn.convertToString(process);
            proc = convertCommunicationEvents(proc, communications, i);
            System.out.println("Convertion on "+pathsToXESFiles[i]+" completed.\n");
            System.out.println("Apply model fixes on "+pathsToXESFiles[i]+"...");
            proc = BPMNUtils.convertXorGateways(proc);
            System.out.println("Model fixing "+pathsToXESFiles[i]+" completed.\n");
            xmlModels.add(proc);
        }
        System.out.println("Group processes...");
        String finalCollaboration = BPMNUtils.groupProcesses(xmlModels);
        System.out.println("Processes correctly grouped.\n");

        System.out.println("Analyse communication...");
        results = new CommunicationAnalysis(communications);
        System.out.println("Analysis completed.\n");

        System.out.println("Decorate collaboration with communication aspects...");
        finalCollaboration = applyCommunication(finalCollaboration, communications);
        finalCollaboration = BPMNUtils.convertStartEndMessageEvents(finalCollaboration);
        System.out.println("Decoration completed.\n");
        return finalCollaboration;
    }


    public static String convertCommunicationEvents(String process, Communication communications, int pid){
        Map<Integer, Map<String, MsgType>> msgType = communications.getMsgTypes();
        Map<String, CommType> commType = communications.getCommTypes();
        for (String eventID : msgType.get(pid).keySet()) {
            if (commType.get(eventID) == CommType.P2P)
                process = BPMNUtils.makeMsgTask(process, eventID, msgType.get(pid).get(eventID));
            else if (commType.get(eventID) == CommType.BROADCAST)
                process = BPMNUtils.makeSignal(process, eventID, msgType.get(pid).get(eventID), pid);
        }
        return process;
    }

    private static String applyCommunication(String finalCollaboration, Communication communications) {
        Map<String, CommType> commType = communications.getCommTypes();
        Map<String, MsgConnections> msgFlows = communications.getMsgFlows();
        Map<String, String> events = communications.getMsgEvents();

        for (String msg : msgFlows.keySet()){
            if (commType.get(msgFlows.get(msg).getSource()) == CommType.P2P)
                finalCollaboration = BPMNUtils.makeMsgFlow(finalCollaboration, msg, msgFlows.get(msg).getSource(), msgFlows.get(msg).getTarget());
        }
//        for (String ev : events.keySet()){
//            if (commType.get(ev) == CommType.P2P)
//                finalCollaboration = BPMNUtils.convertMsgEvents(finalCollaboration, ev, events.get(ev));
//        }
//        finalCollaboration = BPMNUtils.convertXorGateways(finalCollaboration);
        return finalCollaboration;
    }


    public static BpmnModelInstance discovery(String filePath, DiscoveryAlgorithm algo) throws Exception {
        String rawProcess = AlgorithmsBridge.discovery(filePath, algo, miningParams);
        String processXML  = BPMNUtils.fixRawProcess(rawProcess);
        BpmnModelInstance collaboration = BPMNUtils.generateCollaborationInstance(processXML, filePath);
        return collaboration;
    }


}

