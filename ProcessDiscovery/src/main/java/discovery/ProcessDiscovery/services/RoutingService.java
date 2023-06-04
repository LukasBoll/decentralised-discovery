package discovery.ProcessDiscovery.services;

import discovery.ProcessDiscovery.models.Organization;
import discovery.ProcessDiscovery.models.Routing;
import discovery.ProcessDiscovery.repositories.RoutingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RoutingService {

    private final RoutingRepository routingRepository;

    public RoutingService(RoutingRepository routingRepository) {
        this.routingRepository = routingRepository;
    }

    public String getAddress(Organization o) {
        Routing routing = routingRepository.findFirstByOrganizationId(o.getId());
        if (routing == null) {
            RestTemplate restTemplate = new RestTemplate();
            String result = null;
            try {
                result = restTemplate.getForObject(o.getAddress() + "/collaborationconfig/getaddress", String.class);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (result != null) {
                routing = routingRepository.save(new Routing(o.getId(),result));
            }
        }
        return routing!=null?routing.getAddress():null;
    }
}
