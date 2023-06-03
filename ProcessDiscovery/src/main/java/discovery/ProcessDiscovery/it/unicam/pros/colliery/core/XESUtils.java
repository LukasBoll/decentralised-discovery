package discovery.ProcessDiscovery.it.unicam.pros.colliery.core;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class XESUtils {

    public static Document loadXES(String path) {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = docBuilder.parse(path);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

//    public static Document preprocessLog(Document doc, CorrelationMechanism correlation){
//        Set<CorrelationMechanism.BoolCondition> is_send = correlation.getIs_send(), is_receive = correlation.getIs_receive();
//        NodeList events = doc.getElementsByTagName("event");
//        for (int i = 0; i < events.getLength(); i++){
//            NodeList eventChilds = events.item(i).getChildNodes();
//
//            boolean send_event = true;
//            for (CorrelationMechanism.BoolCondition con: is_send){
//                for(int j = 0; j < eventChilds.getLength(); j++) {
//                    Node child = eventChilds.item(j);
//                    if (child.getNodeName() != "#text" && child.hasAttributes() && child.getAttributes().getNamedItem("key").getNodeValue() == con.getVar()) {
//                        send_event &= con.evaluate(child.getAttributes().getNamedItem("value").getNodeValue());
//                    }
//                }
//            }
//            boolean receive_event = true;
//            for (CorrelationMechanism.BoolCondition con: is_receive){
//                for(int j = 0; j < eventChilds.getLength(); j++) {
//                    Node child = eventChilds.item(j);
//                    if (child.getNodeName() != "#text" && child.hasAttributes() && child.getAttributes().getNamedItem("key").getNodeValue() == con.getVar()) {
//                        receive_event &= con.evaluate(child.getAttributes().getNamedItem("value").getNodeValue());
//                    }
//                }
//            }
//
//            if (send_event) {
//                events.item(i).appendChild(new AttrImpl());
//            }
//            if (!send_event && !receive_event) continue;
//
//
//
//
//            for(int j = 0; j < eventChilds.getLength(); j++){
//                Node child = eventChilds.item(j);
//                if (child.getNodeName() != "#text" && child.hasAttributes()){
//                    switch (child.getAttributes().getNamedItem("key").getNodeValue()){
//                        case "concept:name":
//                            eventId = child.getAttributes().getNamedItem("value").getNodeValue();
//                            break;
//                        case "msgType" :
//                            msgType = (child.getAttributes().getNamedItem("value").getNodeValue().contains("send")) ? MsgType.SEND : MsgType.RECEIVE;
//                            break;
//                        case "msgFlow" :
//                            msgFlow = child.getAttributes().getNamedItem("value").getNodeValue();
//                            break;
//                        case "msgInstanceId" :
//                            msgInstanceId = child.getAttributes().getNamedItem("value").getNodeValue();
//                            break;
//                        case "eventType" :
//                            isMsgEvent = child.getAttributes().getNamedItem("value").getNodeValue();
//                            break;
//                        case "time:timestamp" :
//                            msgTime = child.getAttributes().getNamedItem("value").getNodeValue();
//                        default:
//                            continue;
//                    }
//                }
//            }
//            if (msgType != null  && msgFlow != "") comms.add(eventId,msgType,msgInstanceId,msgFlow, msgTime, isMsgEvent);
//        }
//    }

    public static Set<CommEvent> extractCommunication(int idx, String xesPath) throws IOException {
        return getCommunication(idx, loadXES(xesPath));
    }

    public static List extractEventAttrs(String xesPath) {
        return getEventAttrs(loadXES(xesPath));
    }

    private static List getEventAttrs(Document xes) {
        return null;
    }

    public static Set<CommEvent> getCommunication(int idx, Document xes) {
        Set<CommEvent> comms = new HashSet<>();
        NodeList events = xes.getElementsByTagName("event");
        retreiveMessage(idx, comms, events);
        return comms;
    }

    private static void retreiveMessage(int idx, Set<CommEvent> comms, NodeList events) {
        for (int i = 0; i < events.getLength(); i++) {
            NodeList eventChilds = events.item(i).getChildNodes();
            String activity = "", msgInstanceId = "", msgName = "", msgTime = "", sender = "", receiver = "", organizationId = "";
            MsgType msgRole = null;
            CommType commType = null;
            String isMsgEvent = null;
            for (int j = 0; j < eventChilds.getLength(); j++) {
                Node child = eventChilds.item(j);

                if (child.getNodeName() != "#text" && child.hasAttributes()) {
                    switch (child.getAttributes().getNamedItem("key").getNodeValue()) {
                        case "concept:name":
                            activity = child.getAttributes().getNamedItem("value").getNodeValue();
                            break;
                        case "msgRole":
                            msgRole = (child.getAttributes().getNamedItem("value").getNodeValue().contains("send")) ? MsgType.SEND : MsgType.RECEIVE;
                            break;
                        case "msgName":
                            msgName = child.getAttributes().getNamedItem("value").getNodeValue();
                            break;
                        case "msgInstanceId":
                            msgInstanceId = child.getAttributes().getNamedItem("value").getNodeValue();
                            break;
                        case "eventType":
                            isMsgEvent = child.getAttributes().getNamedItem("value").getNodeValue();
                            break;
                        case "sender":
                            sender = child.getAttributes().getNamedItem("value").getNodeValue();
                            break;
                        case "receiver":
                            receiver = child.getAttributes().getNamedItem("value").getNodeValue();
                            break;
                        case "org:group":
                            organizationId = child.getAttributes().getNamedItem("value").getNodeValue();
                            break;
                        case "time:timestamp":
                            msgTime = child.getAttributes().getNamedItem("value").getNodeValue();
                        case "msgProtocol":
                            commType = (child.getAttributes().getNamedItem("value").getNodeValue().contains("Pub/Sub")) ? CommType.BROADCAST : CommType.P2P;
                        default:
                            continue;
                    }
                }
            }
            if (msgRole != null && msgName != "")
                comms.add(new CommEvent(idx, activity, msgRole, msgInstanceId, msgName, msgTime, isMsgEvent, commType, sender, receiver, organizationId));
        }
    }

    public static Node findProcNode(Document doc, String type, String attrKey, String attrValue) {
        return findByExpr(doc, "/definitions/process/" + type + "[@" + attrKey + "=\"" + attrValue + "\"]").item(0);
    }

    public static Node findNode(Document doc, String path, String attrKey, String attrValue) {
        return findByExpr(doc, path + "[@" + attrKey + "=\"" + attrValue + "\"]").item(0);
    }

    public static Node findProcNode(Document doc, String type, String attrKey1, String attrValue1, String attrKey2, String attrValue2) {
        return findByExpr(doc, "/definitions/process/" + type + "[@" + attrKey1 + "=\"" + attrValue1 + "\" @" + attrKey1 + "=\"" + attrValue1 + "\"]").item(0);
    }


    public static java.util.List<Node> getIncoming(Node n) {
        java.util.List<Node> inC = new ArrayList<>();
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().contains("incoming")) {
                inC.add(nl.item(i));
            }
        }
        return inC;
    }

    public static java.util.List<Node> getOutgoing(Node n) {
        java.util.List<Node> ouG = new ArrayList<>();
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().contains("outgoing")) {
                ouG.add(nl.item(i));
            }
        }
        return ouG;
    }

    public static NodeList findByExpr(Document doc, String expression) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = null;
        try {
            expr = xpath.compile(expression);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        NodeList nl = null;
        try {
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return nl;
    }

    public static Node string2node(String s) {
        try {
            return DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(s.getBytes()))
                    .getDocumentElement();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String node2string(Node n) {
        StringWriter writer = new StringWriter();
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        try {
            transformer.transform(new DOMSource(n), new StreamResult(writer));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static String convertXMLToString(Document dom) {
        DOMSource domSource = new DOMSource(dom);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        try {
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static Document convertFileToXMLDocument(String filePath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            return db.parse(new File(filePath));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Document convertStringToXMLDocument(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static NodeList getNodesByTag(Document dom, String tag) {
        return dom.getElementsByTagName(tag);
    }
}

