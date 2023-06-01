package discovery.ProcessDiscovery.services;

import discovery.ProcessDiscovery.models.Organization;
import discovery.ProcessDiscovery.models.Routing;
import discovery.ProcessDiscovery.repositories.RoutingRepository;
import org.springframework.stereotype.Service;

@Service
public class RoutingService {

    private final RoutingRepository routingRepository;

    public RoutingService(RoutingRepository routingRepository) {
        this.routingRepository = routingRepository;
    }

    public Routing getAddress(Organization o){
        return routingRepository.findFirstByOrganizationId(o.getId());
    }

}
