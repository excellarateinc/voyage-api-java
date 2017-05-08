package voyage.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.ClientRegistrationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import voyage.security.client.Client
import voyage.security.client.ClientService
import voyage.security.permission.Permission
import voyage.security.permission.PermissionService

@Service
@Transactional(readOnly = true)
class PermissionBasedClientDetailsService implements ClientDetailsService {
    private final ClientService clientService
    private final PermissionService permissionService

    PermissionBasedClientDetailsService(ClientService clientService, PermissionService permissionService) {
        this.clientService = clientService
        this.permissionService = permissionService
    }

    @Override
    ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Client client = clientService.findByClientIdentifier(clientId)
        if (!client || !client.isEnabled) {
            throw new ClientRegistrationException("No client was found for the given username and password")
        } else if (client.isAccountLocked) {
            throw new ClientRegistrationException("The client account is locked")
        }
        return new PermissionBasedClientDetails(client, getAuthorities(client))
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Client client) {
        Collection<SimpleGrantedAuthority> authorities = [] as Set<SimpleGrantedAuthority>
        Iterable<Permission> permissions = permissionService.findAllByClient(client.id)
        permissions?.each { permission ->
            authorities.add(new SimpleGrantedAuthority(permission.name))
        }
        return authorities
    }
}
