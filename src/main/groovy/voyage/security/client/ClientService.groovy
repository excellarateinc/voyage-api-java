package voyage.security.client

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

import javax.transaction.Transactional
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Transactional
@Validated
class ClientService {
    private final ClientRepository clientRepository

    ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository
    }

    Client getCurrentClient() {
        String clientId
        Authentication authentication = SecurityContextHolder.context.authentication
        if (authentication && authentication instanceof OAuth2Authentication) {
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication)authentication
            clientId = oAuth2Authentication.OAuth2Request.clientId
        }

        if (clientId) {
            return findByClientIdentifier(clientId)
        }

        return null
    }

    Client findByClientIdentifier(@NotNull String clientIdentifier) {
        return clientRepository.findByClientIdentifier(clientIdentifier)
    }

    Client save(@Valid Client client) {
        clientRepository.save(client)
    }
}
