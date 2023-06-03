package discovery.ProcessDiscovery.util;

import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.XESUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class XmlUtil {

    public static void writeXml(Document doc,
                                OutputStream output)
            throws TransformerException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);

    }

    public static void filterNonInteracting(Document log){

        NodeList eventNodes = log.getElementsByTagName("event");
        for(int i = 0; eventNodes.getLength()>i; i++) {
            Node eventNode = eventNodes.item(i);
            NodeList eventChildren = eventNode.getChildNodes();
            boolean isInteracting = false;
            for (int j = 0; eventChildren.getLength() > j; j++) {
                Node eventChild = eventChildren.item(j);
                NamedNodeMap attributes = eventChild.getAttributes();
                if (attributes != null && attributes.getNamedItem("key") != null && attributes.getNamedItem("key").getNodeValue() != null
                        && attributes.getNamedItem("key").getNodeValue().equalsIgnoreCase("msgName")) {
                    isInteracting = true;
                    break;
                }
            }
            if(!isInteracting) {
                eventNode.getParentNode().removeChild(eventNode);
            };
        }
    }

    public static boolean isInTasks(String task, NodeList tasks) {
        for(int i =0; i<tasks.getLength();i++){
            if(task.equals(tasks.item(i).getAttributes().getNamedItem("name").getNodeValue())){
                return true;
            }
        }
        return false;
    }

    //BY colliery
    public static GatewayAndNodes getTasksInCompetition(String xml) {
        Document dom = XESUtils.convertStringToXMLDocument(xml);

        List<Node> xorGateways = new ArrayList<>();
        List<List<String>> setOfNodesInCompetition = new ArrayList<>();

        NodeList exclusives = XESUtils.findByExpr(dom, "/definitions/process/exclusiveGateway[@gatewayDirection=\"Diverging\"]");

        for (int i = 0; i < exclusives.getLength(); i++) {
            Node exclusive = exclusives.item(i);
            NodeList exChilds = exclusive.getChildNodes();
            Set<String> outgoing = new HashSet<>();
            for (int j = 0; j < exChilds.getLength(); j++) {
                if (exChilds.item(j).getNodeName().equals("outgoing") || exChilds.item(j).getNodeName().equals("bpmn:outgoing"))
                    outgoing.add(exChilds.item(j).getTextContent());
            }
            boolean isEventBased = true;
            List<String> eventsInCompetition = new ArrayList<>();
            for (String sF : outgoing) {
                Node sFnode = XESUtils.findProcNode(dom, "*", "id", sF);
                String targetId = sFnode.getAttributes().getNamedItem("targetRef").getNodeValue();
                Node target = XESUtils.findProcNode(dom, "*", "id", targetId);
                String targetType = target.getNodeName();
                if (targetType.contains("receiveTask") || targetType.contains("intermediateCatchEvent")) {
                    if(target.getAttributes()!=null && target.getAttributes().getNamedItem("name")!=null){
                        eventsInCompetition.add(target.getAttributes().getNamedItem("name").getNodeValue());
                    }
                } else {
                    isEventBased = false;
                }

            }
            if (isEventBased){
                setOfNodesInCompetition.add(eventsInCompetition);
                xorGateways.add(exclusive);
            }
        }
        return new GatewayAndNodes(xorGateways,setOfNodesInCompetition);
    }

    //BY Colliery
    public static String convertXorGateways(String xml, List<Node> gateways, List<String> tasksToNotTransform) {

        Document dom = XESUtils.convertStringToXMLDocument(xml);
        gateways = gateways.stream().filter(gateway->convertGateway(gateway,dom,tasksToNotTransform)).collect(Collectors.toList());
        List<String> exclusiveToEventBased = gateways.stream().map(XESUtils::node2string).collect(Collectors.toList());

        for (String excl : exclusiveToEventBased) {
            excl = excl.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            String evB = excl.replaceAll("exclusiveGateway", "eventBasedGateway");
            xml = xml.replace(excl, evB);
        }

        return xml;
    }

    //BY colliery
    public static boolean convertGateway(Node gateway, Document dom, List<String> tasksToNotTransform){
        NodeList childNotes = gateway.getChildNodes();
        Set<String> outgoing = new HashSet<>();
        for (int j = 0; j<childNotes.getLength(); j++){
            if(childNotes.item(j).getNodeName().equals("outgoing") || childNotes.item(j).getNodeName().equals("bpmn:outgoing")) outgoing.add(childNotes.item(j).getTextContent());
        }
        boolean convertGateway = true;
        for (String sF : outgoing){
            Node sFnode = XESUtils.findProcNode(dom, "*", "id", sF);
            String targetId = sFnode.getAttributes().getNamedItem("targetRef").getNodeValue();
            Node target = XESUtils.findProcNode(dom, "*", "id", targetId);
            String targetType = target.getNodeName();
            if (targetType.contains("receiveTask") || targetType.contains("intermediateCatchEvent")){
                String name = target.getAttributes().getNamedItem("name").getNodeValue();
                if(tasksToNotTransform.stream().anyMatch(task->task.equals(name))){
                    convertGateway = false;
                }
            }}
        return convertGateway;
    }

    public static void removeProcess(Document document) {
        Node process = document.getElementsByTagName("bpmn:process").item(0);
        process.getParentNode().removeChild(process);
    }
}
