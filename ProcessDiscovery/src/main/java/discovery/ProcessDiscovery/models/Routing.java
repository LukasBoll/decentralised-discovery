package discovery.ProcessDiscovery.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
public class Routing {

    @Id
    @GeneratedValue
    private UUID id;

    private String organizationId;

    private String address;

    public Routing(UUID id, String organizationId, String address) {
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
