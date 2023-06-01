package discovery.ProcessDiscovery.controller;

import discovery.ProcessDiscovery.models.CollaborationDiscoverResponse;
import discovery.ProcessDiscovery.models.CommunicationEvent;
import discovery.ProcessDiscovery.models.MessageFlow;
import discovery.ProcessDiscovery.repositories.MessageFlowRepository;
import discovery.ProcessDiscovery.services.DecentralisedDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
public class CollaborationDiscoveryController {

    private final DecentralisedDiscoveryService decentralisedDiscoveryService;
    private final MessageFlowRepository messageFlowRepository;

    @Autowired
    public CollaborationDiscoveryController(DecentralisedDiscoveryService decentralisedDiscoveryService, MessageFlowRepository messageFlowRepository){
        this.decentralisedDiscoveryService=decentralisedDiscoveryService;
        this.messageFlowRepository = messageFlowRepository;
    }

    @GetMapping("/collaboration/discover")
    public CollaborationDiscoverResponse discover(@RequestBody List<String> entryPoints) throws ParserConfigurationException, IOException, SAXException {
        System.out.println("Discover");
        Document model = decentralisedDiscoveryService.getPrivateModel();
        List<MessageFlow> messages = messageFlowRepository.findAll();
        return new CollaborationDiscoverResponse(model, messages);
    }

    @RequestMapping(value = "/collaboration/communicationevent/{decisionId}", method = RequestMethod.GET)
    public List<CommunicationEvent> getMessages(@PathVariable String decisionId) throws IOException {
        return decentralisedDiscoveryService.getCommunicationEvents(decisionId);
    }
}
