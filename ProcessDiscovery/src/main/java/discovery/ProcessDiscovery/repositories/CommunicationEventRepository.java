package discovery.ProcessDiscovery.repositories;

import discovery.ProcessDiscovery.it.unicam.pros.colliery.core.MsgType;
import discovery.ProcessDiscovery.models.CommunicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommunicationEventRepository extends JpaRepository<CommunicationEvent, UUID> {

    public List<CommunicationEvent> findAllBySenderOrReceiverAndOrganization(String senderId, String reviverId,String id);

    public List<CommunicationEvent> findAllByFlowAndType(String flow, MsgType type);
}
