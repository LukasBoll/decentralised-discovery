package discovery.ProcessDiscovery.models;

import org.w3c.dom.Document;

import java.util.List;

public class CollaborationDiscoverResponse {

    private Document model;
    private List<MessageFlow> messageFlows;

    public CollaborationDiscoverResponse(Document model, List<MessageFlow> messageFlows) {
        this.model = model;
        this.messageFlows = messageFlows;
    }

    public CollaborationDiscoverResponse() {
    }

    public Document getModel() {
        return model;
    }

    public void setModel(Document model) {
        this.model = model;
    }

    public List<MessageFlow> getMessageFlows() {
        return messageFlows;
    }

    public void setMessageFlows(List<MessageFlow> messageFlows) {
        this.messageFlows = messageFlows;
    }
}
