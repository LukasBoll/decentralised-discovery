package discovery.ProcessDiscovery.controller;

import discovery.ProcessDiscovery.models.CollaborationDiscoverResponse;
import discovery.ProcessDiscovery.models.CommunicationEvent;
import discovery.ProcessDiscovery.services.DecentralisedDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
public class CollaborationDiscoveryController {

    private final DecentralisedDiscoveryService decentralisedDiscoveryService;

    @Autowired
    public CollaborationDiscoveryController(DecentralisedDiscoveryService decentralisedDiscoveryService){
        this.decentralisedDiscoveryService=decentralisedDiscoveryService;
    }

    @GetMapping("/collaboration/discover")
    public CollaborationDiscoverResponse discover(@RequestBody List<String> entryPoints){
        return new CollaborationDiscoverResponse();
    }

    @GetMapping("/collaboration/communicationevent")
    public List<CommunicationEvent> getMessages(@RequestParam String id) throws IOException {
        return decentralisedDiscoveryService.getCommunicationEvents(id);
    }
}
