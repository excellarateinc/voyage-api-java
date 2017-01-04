package launchpad.security.token

import launchpad.mail.MailService
import launchpad.util.CryptoUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Transactional
@Service('tokenService')
@Validated
class TokenService {
    private final TokenRepository tokenRepository

    @Autowired
    MailService mailService

    TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository
    }

    Token generate(Object entity, TokenType tokenType, Date expiresOn = null) {
        Token token = new Token()
        token.entityType = entity.class.name
        token.entityId = entity.id
        token.expiresOn = expiresOn
        token.tokenType = tokenType
        token.value = CryptoUtil.generateUniqueToken()
        return tokenRepository.save(token)
    }

}
