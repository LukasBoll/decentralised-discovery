package discovery.ProcessDiscovery.util;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Set;

public class GatewayAndNodes {

    private List<Node> xorGateways;

    private List<List<String>> tasksInCompetition;

    public GatewayAndNodes(List<Node> xorGateway, List<List<String>> taskInCompetition){
        this.tasksInCompetition=taskInCompetition;
        this.xorGateways = xorGateway;

    }

    public List<Node> getXorGateways() {
        return xorGateways;
    }

    public void setXorGateways(List<Node> xorGateways) {
        this.xorGateways = xorGateways;
    }

    public List<List<String>> getTasksInCompetition() {
        return tasksInCompetition;
    }

    public void setTasksInCompetition(List<List<String>> tasksInCompetition) {
        this.tasksInCompetition = tasksInCompetition;
    }
}
