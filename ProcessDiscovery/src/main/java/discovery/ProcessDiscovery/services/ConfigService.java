package discovery.ProcessDiscovery.services;

import discovery.ProcessDiscovery.models.Organization;
import discovery.ProcessDiscovery.repositories.AuthorizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

    private final AuthorizationRepository authorizationRepository;

    public ConfigService(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    public List<Organization> getConfig() {
        return authorizationRepository.findAll();
    }

    public Organization addOrUpdateConfig(Organization authorization) {
        System.out.println(authorization);
        return authorizationRepository.save(authorization);
    }
}
