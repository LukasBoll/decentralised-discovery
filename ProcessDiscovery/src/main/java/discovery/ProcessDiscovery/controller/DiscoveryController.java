package discovery.ProcessDiscovery.controller;

import discovery.ProcessDiscovery.services.DecentralisedDiscoveryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@CrossOrigin
@RestController
public class DiscoveryController {

    private final DecentralisedDiscoveryService discovery;

    public DiscoveryController(DecentralisedDiscoveryService discovery) {
        this.discovery = discovery;
    }

    @GetMapping("/discover/privatemodel")
    public Document getPrivateModel() throws ParserConfigurationException, IOException, SAXException {
        return discovery.getPrivateModel();
    }

    @GetMapping("/discover/publicmodel")
    public Document getPublicModel() throws ParserConfigurationException, IOException, SAXException {
        return discovery.getPublicModel();
    }

    @GetMapping("/discover/comodel")
    public Document getCOModel() throws ParserConfigurationException, IOException, SAXException {
        return discovery.getCOModel();
    }
}