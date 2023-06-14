/*
 *   This File is part of the Colliery_source Project from PROSLabTeam
 *   https://bitbucket.org/proslabteam/colliery_source/src/master/
 *   Thanks to Lorenzo Rossi for granting permissions
 * */


package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommunicationAnalysis {

    private Communication communication;
    private HashMap<String, Integer> msgQnt;
    private Map<String, CommEvent> msgIdtoSend;
    private Map<String, CommEvent> msgIdtoRec;
    private Set<CommEvent> hangingMsg = new HashSet<>();
    private Map<String, Set<Long>> msgDelays = new HashMap<>();
    public Map<String, CommEvent> getMsgIdtoSend() {
        return msgIdtoSend;
    }

    public Map<String, CommEvent> getMsgIdtoRec() {
        return msgIdtoRec;
    }

    public Communication getCommunication() {
        return communication;
    }

    public Set<CommEvent> getHangingMsg() {
        return hangingMsg;
    }

    public Map<String, Set<Long>> getMsgDelays() {
        return msgDelays;
    }

    public CommunicationAnalysis(Communication c){
        this.communication = c;

        analyseCommunication();
    }

    public HashMap<String, Integer> getMsgQnt() {
        return msgQnt;
    }

    private void analyseCommunication(){
        //First check
        hangingMsg = new HashSet<>();
        msgDelays = new HashMap<>();
        msgIdtoSend = new HashMap<>();
        msgIdtoRec = new HashMap<>();
        msgQnt = new HashMap<>();

        for(int i : communication.getCommunacations().keySet()){
            for (CommEvent c : communication.getCommunacations().get(i)) {
                if (c.getType().equals(MsgType.SEND)) {
                    msgIdtoSend.put(c.getInstanceId(), c);
                }
                if (c.getType().equals(MsgType.RECEIVE))
                    msgIdtoRec.put(c.getInstanceId(), c);


            }
        }
        for (String msgId : msgIdtoSend.keySet()) {
            if(!msgIdtoRec.containsKey(msgId)) {
                hangingMsg.add(msgIdtoSend.get(msgId));
            } else {
                if (!msgDelays.containsKey(msgIdtoSend.get(msgId).getFlow())) { msgDelays.put(msgIdtoSend.get(msgId).getFlow(), new HashSet<>()); }
                long d = Math.abs(msgIdtoSend.get(msgId).getDate().getTime() - msgIdtoRec.get(msgId).getDate().getTime());
                msgDelays.get(msgIdtoSend.get(msgId).getFlow()).add(d);
                if (!msgQnt.containsKey(msgIdtoSend.get(msgId).getFlow())) {
                    msgQnt.put(msgIdtoSend.get(msgId).getFlow(), 0);
                }
                msgQnt.put(msgIdtoSend.get(msgId).getFlow(), msgQnt.get(msgIdtoSend.get(msgId).getFlow()) +1);
            }
        }
        for (String msgId : msgIdtoRec.keySet()) {
            if(!msgIdtoSend.containsKey(msgId)) hangingMsg.add(msgIdtoRec.get(msgId));
        }

    }
}
