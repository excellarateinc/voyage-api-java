package launchpad.security

import launchpad.security.client.Client
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
        // Not implemented since security is governed by permissions of the client
        return null
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
        return scopes
    }

    @Override
    Set<String> getAuthorizedGrantTypes() {
        Set<String> grants = []
        client.clientGrants?.each { clientGrantType ->
            grants << clientGrantType.grantType.code
        }
        return grants
    }

    @Override
    Set<String> getRegisteredRedirectUri() {
        Set<String> uris = []
        client.clientRedirectUris?.each { clientRedirectUri ->
            uris << clientRedirectUri.uri
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
        return client.getRefreshTokenValiditySeconds()
    }

    @Override
    boolean isAutoApprove(String scope) {
        return client.isAutoApprove
    }

    @Override
    Map<String, Object> getAdditionalInformation() {
        // Not Implemented
        return null
    }
}
