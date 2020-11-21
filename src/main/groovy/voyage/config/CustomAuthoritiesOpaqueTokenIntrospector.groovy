package voyage.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector

class CustomAuthoritiesOpaqueTokenIntrospector  implements OpaqueTokenIntrospector {

    OpaqueTokenIntrospector delegate

    static Collection<GrantedAuthority> extractAuthorities(OAuth2AuthenticatedPrincipal principal) {
        Collection<GrantedAuthority> authorities = []
        List<String> roles = principal.getAttribute('realm_access')?.roles
        roles.each { role -> authorities.add(new SimpleGrantedAuthority("ROLE_${role.toUpperCase()}")) }
        authorities
    }

    CustomAuthoritiesOpaqueTokenIntrospector(String introspectionUri, String clientId, String clientSecret) {
        delegate = new NimbusOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret)
    }
    
    @Override
    OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token)
        new DefaultOAuth2AuthenticatedPrincipal(
                principal.name, principal.attributes, extractAuthorities(principal))
    }

}
