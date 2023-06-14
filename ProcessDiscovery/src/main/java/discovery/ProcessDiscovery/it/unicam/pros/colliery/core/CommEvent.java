/*
 *   This File is part of the Colliery_source Project from PROSLabTeam
 *   https://bitbucket.org/proslabteam/colliery_source/src/master/
 *   Thanks to Lorenzo Rossi for granting permissions
 * */


package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommEvent{
    private String activity, msgInstanceId, flow;
    private MsgType type;
    private Date date;
    private String isMsgEvent, time;
    private CommType commType;
    private String sender;
    private String receiver;
    private int pid;
    private String organizationID;

    public CommType getCommType() {
        return commType;
    }

    public void setCommType(CommType commType) {
        this.commType = commType;
    }

    public CommEvent(int pid, String activity, MsgType type, String msgInstanceId, String mFlow, String mTime, String isMsgEvent, CommType commType, String sender, String receiver, String organizationID){
        this.pid = pid;
        this.type = type;
        this.activity = activity;
        this.msgInstanceId = msgInstanceId;
        this.flow = mFlow;
        this.isMsgEvent = isMsgEvent;
        this.commType = commType;
        this.time = mTime;
        this.sender = sender;
        this.receiver = receiver;
        this.organizationID = organizationID;
        mTime = mTime.substring(0,mTime.indexOf("+")).replace("T", " ");
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(mTime);
        } catch (ParseException e) {
            this.date = new Date();
        }
    }

    public String getMsgInstanceId() {
        return msgInstanceId;
    }

    public void setMsgInstanceId(String msgInstanceId) {
        this.msgInstanceId = msgInstanceId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIsMsgEvent() {
        return isMsgEvent;
    }

    public void setIsMsgEvent(String isMsgEvent) {
        this.isMsgEvent = isMsgEvent;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(String organizationID) {
        this.organizationID = organizationID;
    }

    public int getPid() {return  this.pid;}
    public String getTime() {return this.time;}
    public Date getDate() {return this.date;}

    public String isMsgEvent() {
        return this.isMsgEvent;
    }

    public String getInstanceId() {
        return msgInstanceId;
    }

    public void setInstanceId(String content) {
        this.msgInstanceId = content;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
