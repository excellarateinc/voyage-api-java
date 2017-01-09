package launchpad.security

import launchpad.security.client.Client
import launchpad.security.client.ClientService
import launchpad.security.permission.Permission
import launchpad.security.permission.PermissionService
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.ClientRegistrationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
            throw new ClientRegistrationException("Client ${clientId} was not found.")
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
