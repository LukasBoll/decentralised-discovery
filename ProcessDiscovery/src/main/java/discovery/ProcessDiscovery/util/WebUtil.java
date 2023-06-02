package discovery.ProcessDiscovery.util;

import discovery.ProcessDiscovery.models.Organization;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class WebUtil {

    public static HttpHeaders generateHeaders(Organization o) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+o.getToken());
        return headers;
    }
}
