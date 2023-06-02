package discovery.ProcessDiscovery.services;

import discovery.ProcessDiscovery.models.Organization;
import discovery.ProcessDiscovery.repositories.AuthorizationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class AuthenticationService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final AuthorizationRepository authorizationRepository;

    public AuthenticationService(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    public String getOrganizationIDFromJwtToken(String token) {
        System.out.println(token);
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateJwtToken() {

        return Jwts.builder()
                .setSubject(("CarManufacturerID"))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Organization getOrganizationFromJwtToken(String token) {
        String organizationID = getOrganizationIDFromJwtToken(token);
        return authorizationRepository.getReferenceById(organizationID);
    }
}
