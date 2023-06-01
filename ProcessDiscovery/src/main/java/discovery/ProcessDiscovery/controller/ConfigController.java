package discovery.ProcessDiscovery.controller;

import discovery.ProcessDiscovery.models.Organization;
import discovery.ProcessDiscovery.services.ConfigService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
public class ConfigController {


    private final ConfigService configService;


    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config/getconfig")
    public List<Organization> getConfig(){
        return configService.getConfig();
    }

    @PostMapping ("/config/setconfig")
    public Organization setConfig(@RequestBody Organization organization){
        return configService.addOrUpdateConfig(organization);
    }
}
