package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import java.util.*;

public class Communication {

    private Map<Integer, Set<CommEvent>> comms;

    private Map<String, CommType> commTypes = new HashMap<>();
    private Map<Integer,Map<String, MsgType>> msgTypes = new HashMap<>();
    private Map<String, MsgConnections> msgFlows = new HashMap<>();
    public Communication(){
        comms = new HashMap<>();
    }


    public Map<Integer, Set<CommEvent>> getCommunacations(){
        return comms;
    }

    public boolean add(int index, String activity, MsgType type, String msgInstanceId, String mFlow, String time, String msgEv, CommType commType, String sender, String receiver, String organizationId){
        if(!msgTypes.containsKey(index)) {
            msgTypes.put(index, new HashMap<>());
        }
        msgTypes.get(index).put(activity,type);
        if(!commTypes.containsKey(activity)) {
            commTypes.put(activity, commType);
        }
        if (!msgFlows.containsKey(mFlow)) {
            msgFlows.put(mFlow, new MsgConnections(null, null));
        }
        if (type == MsgType.SEND) {
            msgFlows.get(mFlow).setSource(activity);
        } else {
            msgFlows.get(mFlow).setTarget(activity);
        }
        if(!comms.containsKey(index)) comms.put(index, new HashSet<>());
        return comms.get(index).add(new CommEvent(index, activity, type, msgInstanceId, mFlow, time, msgEv, commType, sender, receiver, organizationId));
    }

    public void addAll(int i, Set<CommEvent> cEvents) {
        for (CommEvent cEv : cEvents) {
            add(i, cEv.getActivity(), cEv.getType(), cEv.getInstanceId(), cEv.getFlow(), cEv.getTime(), cEv.getActivity(), cEv.getCommType(), cEv.getSender(),cEv.getReceiver(),cEv.getOrganizationID());
        }
    }

        public Map<String, String> getMsgEvents () {
            Map<String, String> ret = new HashMap<>();
//        for (CommEvent c : comms){
//            if (c.isMsgEvent() != null){
//                ret.put(c.getEvent(), c.isMsgEvent());
//            }
//        }
            return ret;
        }

        public Map<Integer, Map<String, MsgType>> getMsgTypes () {
            return msgTypes;
        }

        public Map<String, CommType> getCommTypes () {
            return commTypes;
        }

        public Map<String, MsgConnections> getMsgFlows () {
            return msgFlows;
        }
    }
