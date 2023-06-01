package discovery.ProcessDiscovery.util;

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
}
