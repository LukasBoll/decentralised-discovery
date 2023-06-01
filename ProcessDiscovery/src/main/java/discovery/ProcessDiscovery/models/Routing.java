package discovery.ProcessDiscovery.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Routing {

    @Id
    private String id;

    private String organizationId;

    private String address;

    public Routing(String id, String organizationId, String address) {
        this.id = id;
        this.organizationId = organizationId;
        this.address = address;
    }

    public Routing() {
    }

    public Routing(String organizationId, String address) {
        this.organizationId = organizationId;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
