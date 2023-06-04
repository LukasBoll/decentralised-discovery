package discovery.ProcessDiscovery.controller;

import discovery.ProcessDiscovery.models.*;
import discovery.ProcessDiscovery.repositories.AuthorizationRepository;
import discovery.ProcessDiscovery.repositories.MessageFlowRepository;
import discovery.ProcessDiscovery.services.AuthenticationService;
import discovery.ProcessDiscovery.services.DecentralisedDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class CollaborationDiscoveryController {

    private final DecentralisedDiscoveryService decentralisedDiscoveryService;
    private final MessageFlowRepository messageFlowRepository;
    private final AuthenticationService authenticationService;

    @Autowired
    public CollaborationDiscoveryController(DecentralisedDiscoveryService decentralisedDiscoveryService, MessageFlowRepository messageFlowRepository, AuthenticationService authenticationService) {
        this.decentralisedDiscoveryService = decentralisedDiscoveryService;
        this.messageFlowRepository = messageFlowRepository;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/collaboration/discover")
    public CollaborationDiscoverResponse discover(@RequestBody List<String> entryPoints, @RequestHeader(name = "Authorization") String token) throws ParserConfigurationException, IOException, SAXException {
        System.out.println("Discover");

        Organization organization = authenticationService.getOrganizationFromJwtToken(token);

        Document model = switch (organization.getAuthorizationEnum()) {
            case PRIVATE -> decentralisedDiscoveryService.getPrivateModel();
            case PUBLIC -> decentralisedDiscoveryService.getPublicModel();
            case MINIMUM -> decentralisedDiscoveryService.getMinimumModel();
            default -> null;
        };

        List<MessageFlow> messages = new ArrayList<>();
        if(organization.getAuthorizationEnum()== AuthorizationEnum.PRIVATE ||
                organization.getAuthorizationEnum()== AuthorizationEnum.PUBLIC){
            messages = messageFlowRepository.findAll();
        }

        decentralisedDiscoveryService.buildFragment(model,entryPoints);

        return new CollaborationDiscoverResponse(model, messages);
    }

    @RequestMapping(value = "/collaboration/communicationevent", method = RequestMethod.GET)
    public List<CommunicationEvent> degCommunicationEvent(@RequestHeader(name = "Authorization") String token) throws IOException {
        System.out.println(authenticationService.generateJwtToken());

        return decentralisedDiscoveryService.getCommunicationEvents(authenticationService.getOrganizationIDFromJwtToken(token));
    }
}
