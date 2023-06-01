package discovery.ProcessDiscovery.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class MessageFlow {
    @Id
    @GeneratedValue
    UUID id;

    String sender;
    String receiver;
    String name;
    String sendTask;
    String receiveTask;

    public MessageFlow(UUID id, String sender, String receiver, String name, String sendTask, String receiveTask) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.name = name;
        this.sendTask = sendTask;
        this.receiveTask = receiveTask;
    }

    public MessageFlow(String sender, String receiver, String name, String sendTask, String receiveTask) {
        this.sender = sender;
        this.receiver = receiver;
        this.name = name;
        this.sendTask = sendTask;
        this.receiveTask = receiveTask;
    }

    public MessageFlow() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSendTask() {
        return sendTask;
    }

    public void setSendTask(String sendTask) {
        this.sendTask = sendTask;
    }

    public String getReceiveTask() {
        return receiveTask;
    }

    public void setReceiveTask(String receiveTask) {
        this.receiveTask = receiveTask;
    }
}
