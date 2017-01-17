package launchpad.security.client

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

    Client findByClientIdentifier(@NotNull String clientIdentifier) {
        return clientRepository.findByClientIdentifier(clientIdentifier)
    }

    Client save(@Valid Client client) {
        clientRepository.save(client)
    }
}
