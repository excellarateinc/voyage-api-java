package launchpad.security.token

import launchpad.error.TokenExpiredException
import launchpad.error.UnknownIdentifierException
import launchpad.util.CryptoUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Transactional
@Service('tokenService')
@Validated
class TokenService {
    private final TokenRepository tokenRepository

    TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository
    }

    Token generate(Object entity, TokenType tokenType, Date expiresOn = null) {
        Token token = tokenRepository.find(entity.id, entity.class.name, tokenType)
        token = token ?: new Token()
        token.entityType = entity.class.name
        token.entityId = entity.id
        token.expiresOn = expiresOn
        token.tokenType = tokenType
        token.value = CryptoUtil.generateUniqueToken()
        return tokenRepository.save(token)
    }

    Token findByValue(@NotNull String value) {
        Token token = tokenRepository.findByValue(value)
        if (!token) {
            throw new UnknownIdentifierException()
        }
        if (token.isExpired()) {
            throw new TokenExpiredException()
        }
        return token
    }

    Token save(@Valid Token token) {
        tokenRepository.save(token)
    }

    Token expire(@NotNull String value) {
        Token token = tokenRepository.findByValue(value)
        if (!token) {
            throw new UnknownIdentifierException()
        }
        token.expiresOn = new Date()
        tokenRepository.save(token)
    }
}
