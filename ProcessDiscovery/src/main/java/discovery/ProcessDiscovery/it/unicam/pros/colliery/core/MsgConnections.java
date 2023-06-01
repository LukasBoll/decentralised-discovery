package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

public class MsgConnections{

    private String source, target;
    public MsgConnections(String s, String t){
        this.source = s;
        this.target = t;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
