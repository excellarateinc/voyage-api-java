package voyage.security

import voyage.security.client.Client
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails

class PermissionBasedClientDetails implements ClientDetails {
    private final Client client
    private final Collection<GrantedAuthority> authorities

    PermissionBasedClientDetails(Client client, Collection<GrantedAuthority> authorities) {
        this.client = client
        this.authorities = authorities
    }

    @Override
    String getClientId() {
        return client.clientIdentifier
    }

    @Override
    Set<String> getResourceIds() {
        return Collections.EMPTY_SET
    }

    @Override
    boolean isSecretRequired() {
        return client.isSecretRequired
    }

    @Override
    String getClientSecret() {
        return client.clientSecret
    }

    @Override
    boolean isScoped() {
        return client.isScoped
    }

    @Override
    Set<String> getScope() {
        Set<String> scopes = []
        client.clientScopes?.each { clientScope ->
            scopes << clientScope.clientScopeType.name
        }
        return scopes.toSorted { a, b -> a <=> b }
    }

    @Override
    Set<String> getAuthorizedGrantTypes() {
        Set<String> grants = []
        client.clientGrants?.each { clientGrantType ->
            grants << clientGrantType.grantType.code
        }
        return grants.toSorted { a, b -> a <=> b }
    }

    @Override
    Set<String> getRegisteredRedirectUri() {
        Set<String> uris = []
        client.clientRedirects?.each { clientRedirect ->
            uris << clientRedirect.redirectUri
        }
        return uris
    }

    @Override
    Collection<GrantedAuthority> getAuthorities() {
        return authorities
    }

    @Override
    Integer getAccessTokenValiditySeconds() {
        return client.accessTokenValiditySeconds
    }

    @Override
    Integer getRefreshTokenValiditySeconds() {
        return client.refreshTokenValiditySeconds
    }

    @Override
    boolean isAutoApprove(String scope) {
        return client.isAutoApprove
    }

    @Override
    Map<String, Object> getAdditionalInformation() {
        return Collections.EMPTY_MAP
    }
}
