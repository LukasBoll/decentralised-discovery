package discovery.ProcessDiscovery.repositories;

import discovery.ProcessDiscovery.models.Routing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoutingRepository extends JpaRepository<Routing, UUID> {

    public Routing findFirstByOrganizationId(String id);
}
