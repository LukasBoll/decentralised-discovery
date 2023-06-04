package discovery.ProcessDiscovery.repositories;

import discovery.ProcessDiscovery.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationRepository extends JpaRepository<Organization, String> {
}
