package discovery.ProcessDiscovery.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


@Entity
public class Organization {

    @Id
    private String id;

    private String address;

    private String name;

    private AuthorizationEnum authorizationEnum;

    private String token;

    public Organization(String id, String address, String name, AuthorizationEnum authorizationEnum, String token) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.authorizationEnum = authorizationEnum;
        this.token = token;
    }

    public Organization() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AuthorizationEnum getOrganizationAuthorization() {
        return authorizationEnum;
    }

    public void setOrganizationAuthorization(AuthorizationEnum authorizationEnum) {
        this.authorizationEnum = authorizationEnum;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
