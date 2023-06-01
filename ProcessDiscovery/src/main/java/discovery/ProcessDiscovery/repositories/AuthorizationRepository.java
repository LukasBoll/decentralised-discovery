package discovery.ProcessDiscovery.repositories;

import discovery.ProcessDiscovery.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthorizationRepository extends JpaRepository<Organization, String> {
}
