package discovery.ProcessDiscovery.repositories;

import discovery.ProcessDiscovery.models.CommunicationEvent;
import discovery.ProcessDiscovery.models.MessageFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageFlowRepository extends JpaRepository<MessageFlow, UUID> {
}
