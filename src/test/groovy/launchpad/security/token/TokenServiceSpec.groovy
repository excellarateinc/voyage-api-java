package launchpad.security.token

import launchpad.security.user.User
import spock.lang.Specification

class TokenServiceSpec extends Specification {

    TokenRepository tokenRepository = Mock()
    TokenService tokenService = new TokenService(tokenRepository)

    def 'generate - returns a single token for given entity' () {
        setup:
            User user = new User(id:1, firstName:'LSS', lastName:'User')
            Token token = new Token(value:'random-token', entityId:user.id, entityType:'User', tokenType:TokenType.EMAIL_VERIFICATION)
            tokenRepository.save(_) >> token
        when:
            Token savedToken = tokenService.generate(user, TokenType.EMAIL_VERIFICATION)
        then:
            'random-token' == savedToken.value
            user.id == savedToken.entityId
            'User' == savedToken.entityType
            TokenType.EMAIL_VERIFICATION == savedToken.tokenType
    }
}
