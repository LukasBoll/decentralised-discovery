package discovery.ProcessDiscovery.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@CrossOrigin
@RestController
public class CollaborationConfigController {

    @Value("${server.port}")
    private int serverPort =0;

    @GetMapping("/collaborationconfig/getaddress")
    public String getPrivateModel() throws ParserConfigurationException, IOException, SAXException {
        return "http://localhost:"+serverPort;
    }
}
