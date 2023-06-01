package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Plane;
import org.camunda.bpm.model.bpmn.instance.di.Shape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BPMNUtils {

    private static String Diagram = "bpmndi:BPMNDiagram";
    private static String Bounds = "omgdc:Bounds";
    private static String Waypoints = "omgdi:waypoint";
    private static String Plane = "bpmndi:BPMNPlane";
    private static String Process = "process";
    private static String Collaboration = "collaboration";
    private static String Participant = "participant";
    private static String Shape = "bpmndi:BPMNShape";
    private static String Edge = "bpmndi:BPMNEdge";

    public static String fixRawProcess(String rawProcess) {
        Document doc = XESUtils.convertStringToXMLDocument(rawProcess);
        Node definitions = getDefinitions(doc);
        Node processNode = null, defNode = null;
        for (int i = 0; i < definitions.getChildNodes().getLength(); i++){
            switch (definitions.getChildNodes().item(i).getNodeName()){
                case "bpmndi:BPMNDiagram" :
                    defNode = definitions.getChildNodes().item(i);
                    break;
                case "process" :
                    processNode = definitions.getChildNodes().item(i);
                    break;
                case "bpmn:process" :
                    processNode = definitions.getChildNodes().item(i);
                    break;
                default:
                    break;
            }
        }
        for (int i = 0; i < processNode.getChildNodes().getLength(); i++){
            Node child = processNode.getChildNodes().item(i);
            if (child.getNodeName().contains("Gateway")){
                int in = 0, out = 0;
                for (int j = 0; j < child.getChildNodes().getLength(); j++){
                    if (child.getChildNodes().item(j).getNodeName().contains("incoming")){
                        in ++;
                    } else if (child.getChildNodes().item(j).getNodeName().contains("outgoing")){
                        out ++;
                    }
                }
                if (in > out){
                    ((Element) child).setAttribute("gatewayDirection","Converging");
                } else if (out > in){
                    ((Element) child).setAttribute("gatewayDirection","Diverging");
                } else {
                    ((Element) child).setAttribute("gatewayDirection","Unspecified");
                }
            }
        }
        definitions.removeChild(processNode);
        definitions.removeChild(defNode);
        definitions.appendChild(processNode);
        definitions.appendChild(defNode);
        return XESUtils.convertXMLToString(doc);
    }

    private static String refreshIDs(String rawProcess) {
        List<String> oldIDs = getAllMatches(rawProcess, "id=\".*?\"");
        long unique = System.currentTimeMillis();
        for(String id : oldIDs){
            String newID = "id_"+ unique++;
            rawProcess = rawProcess.replaceAll(id, newID);
        }

        return rawProcess;
    }

    private static List<String> getAllMatches(String text, String regex) {
        List<String> matches = new ArrayList<>();
        Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text);
        while(m.find()) {
            matches.add(m.group(1).substring(4, m.group(1).length()-1));
        }
        return matches;
    }

    public static BpmnModelInstance generateCollaborationInstance(String processXML, String xesPath) {
        String procName = "";
        try{
            Document dom = XESUtils.convertFileToXMLDocument(xesPath);
            NodeList attrs = XESUtils.findByExpr(dom, "/log/trace/event/string[@key=\"org:group\"]");
            procName = attrs.item(0).getAttributes().getNamedItem("value").getNodeValue();
        } catch (Exception e){
            procName = "Participant";
        }

        processXML = refreshIDs(processXML);
        processXML = removeInterruptingStartEv(processXML);
        BpmnModelInstance mi = Bpmn.readModelFromStream(new ByteArrayInputStream(processXML.getBytes()));
        Process miProcess = mi.getModelElementsByType(Process.class).iterator().next();
        Definitions def = mi.getDefinitions();
        Collaboration coll = mi.newInstance(Collaboration.class);
        def.addChildElement(coll);
        String partyId = procName+"_"+System.currentTimeMillis();
        Participant party = createElement(coll, partyId, Participant.class);
        party.setProcess(miProcess);
        party.setName(procName);
        Plane plane = mi.getModelElementsByType(Plane.class).iterator().next();
        plane.setAttributeValue("bpmnElement", coll.getId());

        Shape collShape = createElement(plane, coll.getId()+"_di", BpmnShape.class);
        collShape.setAttributeValue("isHorizontal","true");
        collShape.setAttributeValue("bpmnElement",partyId);

        Bounds bounds = createBounds(processXML, mi);
        collShape.setBounds(bounds);
        return mi;
    }

    private static String removeInterruptingStartEv(String processXML) {
        return processXML.replaceAll("isInterrupting=\"false\"", "");
    }


    private static  <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
        T element = parentElement.getModelInstance().newInstance(elementClass);
        element.setAttributeValue("id", id, true);
        parentElement.addChildElement(element);
        return element;
    }

    private static Bounds createBounds(String model, BpmnModelInstance mi){
        Document doc = XESUtils.convertStringToXMLDocument(model);
        NodeList boundsNodes = doc.getElementsByTagName(Bounds);
        NodeList waypointNodes = doc.getElementsByTagName(Waypoints);
        double xLow = 0, xMax = 0, yLow = 0, yMax = 0;
        for (int i = 0; i< boundsNodes.getLength(); i++){
            double x = Double.valueOf(boundsNodes.item(i).getAttributes().getNamedItem("x").getNodeValue());
            double y = Double.valueOf(boundsNodes.item(i).getAttributes().getNamedItem("y").getNodeValue());
            if (x < xLow) xLow = x;
            if (x > xMax) xMax = x;
            if (y < yLow) yLow = y;
            if (y > yMax) yMax = y;
        }
        for (int i = 0; i< waypointNodes.getLength(); i++){
            double x = Double.valueOf(waypointNodes.item(i).getAttributes().getNamedItem("x").getNodeValue());
            double y = Double.valueOf(waypointNodes.item(i).getAttributes().getNamedItem("y").getNodeValue());
            if (x < xLow) xLow = x;
            if (x > xMax) xMax = x;
            if (y < yLow) yLow = y;
            if (y > yMax) yMax = y;
        }
        Bounds bounds = mi.newInstance(Bounds.class);
        bounds.setX(xLow);
        bounds.setY(yLow);
        bounds.setWidth((xMax - xLow)*1.3);
        bounds.setHeight((yMax - yLow)*1.3);
        return bounds;
    }

    public static String groupProcesses(List<String> processesXML) {
        String baseDoc = processesXML.iterator().next();
        processesXML.remove(baseDoc);
        Document collaboration = XESUtils.convertStringToXMLDocument(baseDoc);
        Boundaries firstPoolBoundaries = diToZero(collaboration);
        double lowerY = firstPoolBoundaries.yMax +50;
        Node definitions = getDefinitions(collaboration);
        for(String xml : processesXML){
            Document proc = XESUtils.convertStringToXMLDocument(xml);
            diToZero(proc);
            NodeList procNodes = proc.getElementsByTagName(Process) == null ? proc.getElementsByTagName(Process) : proc.getElementsByTagName("bpmn:process");
            for(int i = 0; i < procNodes.getLength(); i++){
                Node procNode = collaboration.importNode(procNodes.item(i), true);
                definitions.appendChild(procNode);
            }
            NodeList partyNodes = proc.getElementsByTagName(Participant);
            for(int i = 0; i < partyNodes.getLength(); i++){
                Node partyNode = collaboration.importNode(partyNodes.item(i), true);
                collaboration.getElementsByTagName(Collaboration).item(0).appendChild(partyNode);
            }
            Set<Node> shapeNodes = getShapeNodes(proc);
            Boundaries shapesBounds = getBoundaries(shapeNodes);
            for(Node n : shapeNodes){
                Node shapeNode = collaboration.importNode(n, true);
                shiftPosition(shapeNode,0, lowerY);
                collaboration.getElementsByTagName(Plane).item(0).appendChild(shapeNode);
            }
            lowerY += shapesBounds.getyMax() - shapesBounds.getyLow() +50;
        }
        NodeList nl = definitions.getChildNodes();
        List<Node> ordered = new ArrayList<>();
        for(int i = 0; i < nl.getLength(); i++) {
            System.out.println(nl.item(i).getNodeName());
            if(is(nl.item(i), Process) || is(nl.item(i), "bpmn:process") || is(nl.item(i), Collaboration)) ordered.add(0,nl.item(i));
            if(is(nl.item(i), Diagram)) ordered.add(nl.item(i));
            definitions.removeChild(nl.item(i));
        }

        for(int i = 0; i < ordered.size(); i++){
            definitions.appendChild(ordered.get(i));
        }
        return XESUtils.convertXMLToString(collaboration);
    }

    private static Node getDefinitions(Document doc) {
        if (doc.getElementsByTagName("definitions").item(0) == null)
            return doc.getElementsByTagName("bpmn:definitions").item(0);
        return doc.getElementsByTagName("definitions").item(0);
    }

    private static void shiftPosition(Node shapeNode, double xShift, double yShift) {
        Node bounds = null;
        NodeList shapeChilds = shapeNode.getChildNodes();
        for (int i = 0; i <shapeChilds.getLength(); i++){
            if (shapeChilds.item(i).hasAttributes()){
                bounds = shapeChilds.item(i);
                if(bounds.getAttributes().getNamedItem("y") != null) bounds.getAttributes().getNamedItem("y").setNodeValue(String.valueOf(Double.valueOf(bounds.getAttributes().getNamedItem("y").getNodeValue()) +yShift));
                if(bounds.getAttributes().getNamedItem("x") != null) bounds.getAttributes().getNamedItem("x").setNodeValue(String.valueOf(Double.valueOf(bounds.getAttributes().getNamedItem("x").getNodeValue()) +xShift));

            }
        }
    }

    private static Boundaries diToZero(Document collaboration) {
        Set<Node> shapeNodes = getShapeNodes(collaboration);
        Boundaries b = getBoundaries(shapeNodes);
        for(Node n : shapeNodes){
            shiftPosition(n, -b.getxLow(), -b.getyLow());
        }
        b.setxLow(0);
        b.setyLow(0);
        b.setxMax(b.getxMax()-b.getxLow());
        b.setyMax(b.getyMax()-b.getyLow());
        return b;
    }

    private static boolean is(Node n, String type){
        return n.getNodeName() == type;
    }

    private static Set<Node> getShapeNodes(Document xml) {
        NodeList docChilds = getDefinitions(xml).getChildNodes();
        Node di = null, plane = null;
        for(int i=0; i< docChilds.getLength(); i++){
            if (is(docChilds.item(i), Diagram)) di = docChilds.item(i);
        }
        NodeList diChilds = di.getChildNodes();
        for(int i=0; i< diChilds.getLength(); i++){
            if (is(diChilds.item(i), Plane)) plane = diChilds.item(i);
        }
        diChilds = plane.getChildNodes();
        Set<Node> ret = new HashSet<>();
        for(int i=0; i< diChilds.getLength(); i++){
            if (is(diChilds.item(i), Shape) || is(diChilds.item(i), Edge)) ret.add(diChilds.item(i));
        }
        return ret;
    }

    private static Boundaries getBoundaries(Set<Node> l){
        Double xLow = null, xMax = null, yLow = null, yMax = null;
        for(Node n : l){
            NodeList bounds = n.getChildNodes();
            for(int j = 0; j < bounds.getLength(); j++){
                if(bounds.item(j).getAttributes() == null) continue;
                Boundaries tmp = getBoundary(bounds.item(j));
                if (xLow == null || tmp.getxLow()<xLow)
                    xLow = tmp.getxLow();
                if (yLow == null || tmp.getyLow()<yLow)
                    yLow = tmp.getyLow();
                if (xMax == null || tmp.getxMax()>xMax)
                    xMax = tmp.getxMax();
                if (yMax == null || tmp.getyMax()>yMax) yMax = tmp.getyMax();
            }
        }
        return new Boundaries(xLow, xMax, yLow, yMax);
    }

    private static Boundaries getBoundary(Node n){
        double xLow = 0, xMax = 0, yLow = 0, yMax = 0;
        if(n.getAttributes().getNamedItem("x") != null){
            xLow =  Double.valueOf(n.getAttributes().getNamedItem("x").getNodeValue());
        }
        if(n.getAttributes().getNamedItem("width") != null){
            xMax =  Double.valueOf(n.getAttributes().getNamedItem("width").getNodeValue()) + xLow;
        }
        if(n.getAttributes().getNamedItem("y") != null){
            yLow =  Double.valueOf(n.getAttributes().getNamedItem("y").getNodeValue());
        }
        if(n.getAttributes().getNamedItem("height") != null){
            yMax =  Double.valueOf(n.getAttributes().getNamedItem("height").getNodeValue()) + yLow;
        }
        return new Boundaries(xLow, xMax, yLow, yMax);
    }

    public static String makeMsgTask(String finalCollaboration, String eventID, MsgType msgType) {

        Document dom  = XESUtils.convertStringToXMLDocument(finalCollaboration);

        Node n = XESUtils.findProcNode(dom, "task","name", eventID);

        if(n==null) {

        } else if(msgType.equals(MsgType.SEND)) {
            dom.renameNode (n, "", "bpmn:sendTask");
        } else {
            dom.renameNode(n, "", "bpmn:receiveTask");
        }
        String ret = XESUtils.convertXMLToString(dom);
        return ret.replaceAll("xmlns=\"\"", "");
    }

    public static String makeSignal(String process, String activity, MsgType msgType, int pid) {

        Document dom  = XESUtils.convertStringToXMLDocument(process);

        Node taskNode = XESUtils.findProcNode(dom, "task","name", activity);

        if(taskNode==null) {

        } else if(msgType.equals(MsgType.SEND)) {
            taskNode = dom.renameNode (taskNode, "", "bpmn:intermediateThrowEvent");
        } else {
            taskNode = dom.renameNode(taskNode, "", "bpmn:intermediateCatchEvent");
        }
        if (taskNode.getAttributes().getNamedItem("completionQuantity") != null) taskNode.getAttributes().removeNamedItem("completionQuantity");
        if (taskNode.getAttributes().getNamedItem("isForCompensation") != null) taskNode.getAttributes().removeNamedItem("isForCompensation");
        if (taskNode.getAttributes().getNamedItem("startQuantity") != null) taskNode.getAttributes().removeNamedItem("startQuantity");

        Node evDef = dom.createElement("bpmn:signalEventDefinition");
        ((Element) evDef).setAttribute("id", "signal_"+activity+"_"+pid);
        taskNode.appendChild(evDef);

       String ret = XESUtils.convertXMLToString(dom);
        return ret.replaceAll("xmlns=\"\"", "");
    }

    public static String makeMsgFlow(String xml, String msg, String source, String target) {
        Document dom  = XESUtils.convertStringToXMLDocument(xml);

        Node sou = XESUtils.findProcNode(dom, "*" ,"name", source);
        Node tar = XESUtils.findProcNode(dom, "*","name", target);
        if(sou == null || tar == null) return xml;
        String sID = sou.getAttributes().getNamedItem("id").getNodeValue();
        String tID = tar.getAttributes().getNamedItem("id").getNodeValue();
        sou = XESUtils.findByExpr(dom, "/definitions/BPMNDiagram/BPMNPlane/BPMNShape[@bpmnElement=\""+sID+"\"]/Bounds").item(0);
        tar = XESUtils.findByExpr(dom, "/definitions/BPMNDiagram/BPMNPlane/BPMNShape[@bpmnElement=\""+tID+"\"]/Bounds").item(0);
        Boundaries sBounds = getBoundary(sou);
        Boundaries tBounds = getBoundary(tar);

        String msgID = "Flow_"+System.currentTimeMillis();
        List<Waypoint> ways = getWaypointsBetween(sBounds, tBounds);
        Waypoint labelWay = getLabelWaypoint(ways.get(0), ways.get(ways.size()-1));

        Node message = XESUtils.string2node("<bpmn:messageFlow id=\""+msgID+"\" name=\""+msg+"\" sourceRef=\""+sID+"\" targetRef=\""+tID+"\" />");

        String edgeXML = "<bpmndi:BPMNEdge id=\""+msgID+"_di\" bpmnElement=\""+msgID+"\">";
        for (Waypoint w : ways){
            edgeXML +="<di:waypoint x=\""+w.getX()+"\" y=\""+w.getY()+"\" />";

        }
        edgeXML += "<bpmndi:BPMNLabel><dc:Bounds x=\""+labelWay.getX()+"\" y=\""+labelWay.getY()+"\" width=\"50\" height=\"50\" /></bpmndi:BPMNLabel></bpmndi:BPMNEdge>";
        Node edge = XESUtils.string2node(edgeXML);
        Node newMessage = dom.importNode(message, true);
        Node newEdge = dom.importNode(edge, true);
        XESUtils.findByExpr(dom, "/definitions/collaboration").item(0).appendChild(newMessage);
        XESUtils.findByExpr(dom, "/definitions/BPMNDiagram/BPMNPlane").item(0).appendChild(newEdge);

        return XESUtils.convertXMLToString(dom);
    }


    private static Waypoint getLabelWaypoint(Waypoint s, Waypoint t) {
        return new Waypoint( (s.getX()+t.getX())/2,  (s.getY()+t.getY())/2);
    }

    private static List<Waypoint> getWaypointsBetween(Boundaries sBounds, Boundaries tBounds) {
        boolean sourceIsLower = sBounds.getyMax() > tBounds.getyLow();
        List<Waypoint> ret = new ArrayList<>();
        double sX = (sBounds.getxMax()+sBounds.getxLow())/2;
        double sY = (sourceIsLower) ? sBounds.getyMax() : sBounds.getyLow();
        ret.add(new Waypoint(sX,sY));
        double tX = (tBounds.getxMax()+tBounds.getxLow())/2;
        double tY = (sourceIsLower) ? tBounds.getyMax() : tBounds.getyLow();
        ret.add(new Waypoint(tX,tY));
        return ret;
    }


//    public static String convertMsgEvents(String xml, String ev, String type) {
//        Document dom  = XESUtils.convertStringToXMLDocument(xml);
//        Node evNode = XESUtils.findProcNode(dom, "*" ,"name", ev);
//        String evId = evNode.getAttributes().getNamedItem("id").getNodeValue();
//        List<Node> inC = XESUtils.getIncoming(evNode);
//        List<Node> ouG = XESUtils.getOutgoing(evNode);
//        NodeList nl = evNode.getChildNodes();
//        return makeEvent(dom, evNode, evId, inC, ouG, type);
//    }

//    private static String makeEvent(Document dom, Node evNode, String evId, List<Node> inC, List<Node> ouG, String type) {
//        String newDom = "";
//        switch (type){
//            case "start" :
//                newDom = makeStartEvent(dom, evNode, evId, inC );
//                break;
//            case "end" :
//                break;
//            case "notification" :
//                newDom = makeIntermediateEvent(dom, evNode, evId );
//                break;
//            default:
//                break;
//        }
//        return newDom;
//    }

    public static String convertStartEndMessageEvents(String proc) {

        Document dom = XESUtils.convertStringToXMLDocument(proc);

        NodeList startEvents = XESUtils.getNodesByTag(dom, "bpmn:startEvent");
        NodeList endEvents = XESUtils.getNodesByTag(dom, "bpmn:endEvent");

        for (int i = 0; i < startEvents.getLength(); i++) {
            List<Node> outG = XESUtils.getOutgoing(startEvents.item(i));
            if (outG.size() == 1) {
                Node outSeqFlow = XESUtils.findProcNode(dom, "*", "id", outG.get(0).getTextContent());
                Node target = XESUtils.findProcNode(dom, "*", "id", outSeqFlow.getAttributes().getNamedItem("targetRef").getNodeValue());

                Node seqDI = XESUtils.findNode(dom, "/definitions/BPMNDiagram/BPMNPlane/*", "bpmnElement", outSeqFlow.getAttributes().getNamedItem("id").getNodeValue());
                Node startDI = XESUtils.findNode(dom, "/definitions/BPMNDiagram/BPMNPlane/*", "bpmnElement", startEvents.item(i).getAttributes().getNamedItem("id").getNodeValue());

                if (target.getNodeName().contains("receiveTask") || target.getNodeName().contains("intermediateCatchEvent")) {
                    startEvents.item(i).getParentNode().removeChild(startEvents.item(i));
                    outSeqFlow.getParentNode().removeChild(outSeqFlow);
                    startDI.getParentNode().removeChild(startDI);
                    seqDI.getParentNode().removeChild(seqDI);
                    target.removeChild(XESUtils.getIncoming(target).get(0));
                    if (target.getNodeName().contains("receiveTask")){
                        Node evDef = dom.createElement("bpmn:messageEventDefinition");
                        ((Element) evDef).setAttribute("id", "message_"+System.currentTimeMillis()+i);
                        target.appendChild(evDef);
                        System.out.println(target);
                    }
                    dom.renameNode(target, "", "bpmn:startEvent");
                }
            }
        }

        for (int i = 0; i < endEvents.getLength(); i++) {
            List<Node> inC = XESUtils.getIncoming(endEvents.item(i));
            if (inC.size() == 1) {
                Node inSeqFlow = XESUtils.findProcNode(dom, "*", "id", inC.get(0).getTextContent());
                Node source = XESUtils.findProcNode(dom, "*", "id", inSeqFlow.getAttributes().getNamedItem("sourceRef").getNodeValue());

                Node seqDI = XESUtils.findNode(dom, "/definitions/BPMNDiagram/BPMNPlane/*", "bpmnElement", inSeqFlow.getAttributes().getNamedItem("id").getNodeValue());
                Node endDI = XESUtils.findNode(dom, "/definitions/BPMNDiagram/BPMNPlane/*", "bpmnElement", endEvents.item(i).getAttributes().getNamedItem("id").getNodeValue());

                if (source.getNodeName().contains("sendTask") || source.getNodeName().contains("intermediateThrowEvent")) {
                    endEvents.item(i).getParentNode().removeChild(endEvents.item(i));
                    inSeqFlow.getParentNode().removeChild(inSeqFlow);
                    endDI.getParentNode().removeChild(endDI);
                    seqDI.getParentNode().removeChild(seqDI);
                    source.removeChild(XESUtils.getOutgoing(source).get(0));
                    if (source.getNodeName().contains("sendTask")){
                        Node evDef = dom.createElement("bpmn:messageEventDefinition");
                        ((Element) evDef).setAttribute("id", "message_"+System.currentTimeMillis()+i);
                        source.appendChild(evDef);
                    }
                    dom.renameNode(source, "", "bpmn:endEvent");
                }
            }
        }

        return XESUtils.convertXMLToString(dom);
    }

    public static String convertXorGateways(String xml) {
        Document dom  = XESUtils.convertStringToXMLDocument(xml);

        Set<String> flowToRemove = new HashSet<>();
        Set<String> exclusiveToEventBased = new HashSet<>();

        NodeList exclusives = XESUtils.findByExpr(dom, "/definitions/process/exclusiveGateway[@gatewayDirection=\"Diverging\"]");

        for(int i = 0; i < exclusives.getLength(); i++){
            Node exclusive = exclusives.item(i);
            NodeList exChilds = exclusive.getChildNodes();
            Set<String> outgoing = new HashSet<>();
            for (int j = 0; j<exChilds.getLength(); j++){
                if(exChilds.item(j).getNodeName().equals("outgoing") || exChilds.item(j).getNodeName().equals("bpmn:outgoing")) outgoing.add(exChilds.item(j).getTextContent());
            }
            boolean isEventBased = true;
            for (String sF : outgoing){
                Node sFnode = XESUtils.findProcNode(dom, "*", "id", sF);
                String targetId = sFnode.getAttributes().getNamedItem("targetRef").getNodeValue();
                Node target = XESUtils.findProcNode(dom, "*", "id", targetId);
                String targetType = target.getNodeName();
                if (targetType.contains("receiveTask") || targetType.contains("intermediateCatchEvent")){
                }
                else {
                    isEventBased = false;
                }

            }
            if (isEventBased)
                exclusiveToEventBased.add(XESUtils.node2string(exclusive));
        }



        Set<String> toDelete = new HashSet<>();
        for (String sFId : flowToRemove){
            Node n = XESUtils.findProcNode(dom,"incoming", "text()", sFId);
            String s = XESUtils.node2string(n).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
            toDelete.add(s);
            n = XESUtils.findProcNode(dom,"outgoing", "text()", sFId);
            s = XESUtils.node2string(n).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
            toDelete.add(s);
            n = XESUtils.findProcNode(dom,"sequenceFlow", "id", sFId);
            s = XESUtils.node2string(n).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
            toDelete.add(s);
        }

        for (String del : toDelete){
            xml = xml.replace(del, "");
        }
        for (String excl : exclusiveToEventBased){
            excl = excl.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
            String evB = excl.replaceAll("exclusiveGateway", "eventBasedGateway");
            xml = xml.replace(excl,evB);
        }

        return xml;
    }



    static class Waypoint{
        private double x, y;
        Waypoint(double x, double y){
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }
    static class Boundaries{
        private double xLow, xMax, yLow, yMax;
        Boundaries(double xl, double xm, double yl, double ym){
            xLow = xl;
            xMax = xm;
            yLow = yl;
            yMax = ym;
        }

        public double getxLow() {
            return xLow;
        }

        public void setxLow(double xLow) {
            this.xLow = xLow;
        }

        public double getxMax() {
            return xMax;
        }

        public void setxMax(double xMax) {
            this.xMax = xMax;
        }

        public double getyLow() {
            return yLow;
        }

        public void setyLow(double yLow) {
            this.yLow = yLow;
        }

        public double getyMax() {
            return yMax;
        }

        public void setyMax(double yMax) {
            this.yMax = yMax;
        }

    }
}
