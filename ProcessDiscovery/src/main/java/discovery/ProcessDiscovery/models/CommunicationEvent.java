package discovery.ProcessDiscovery.models;

import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.CommEvent;
import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.CommType;
import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.MsgType;
import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
public class CommunicationEvent {

    private String activity, msgInstanceId, flow;
    private MsgType type;
    private Date date;
    private String  time;
    private CommType commType;
    private String sender;
    private String receiver;
    private String organization;

    @Id
    @GeneratedValue
    private UUID id;

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public CommunicationEvent() {
    }

    public CommunicationEvent(String activity, String msgInstanceId, String flow, MsgType type, Date date, String time, CommType commType, String sender, String receiver, String organization) {
        this.activity = activity;
        this.msgInstanceId = msgInstanceId;
        this.flow = flow;
        this.type = type;
        this.date = date;
        this.time = time;
        this.commType = commType;
        this.sender = sender;
        this.receiver = receiver;
        this.organization =organization;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getMsgInstanceId() {
        return msgInstanceId;
    }

    public void setMsgInstanceId(String msgInstanceId) {
        this.msgInstanceId = msgInstanceId;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public CommType getCommType() {
        return commType;
    }

    public void setCommType(CommType commType) {
        this.commType = commType;
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


    public static CommunicationEvent fromCommEvent(CommEvent commEvent){
        return new CommunicationEvent(commEvent.getActivity(),commEvent.getInstanceId(),commEvent.getFlow(),commEvent.getType(),commEvent.getDate(),commEvent.getTime(),commEvent.getCommType(), commEvent.getSender(),commEvent.getReceiver(),commEvent.getOrganizationID());
    }
}
