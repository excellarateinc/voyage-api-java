package voyage.common

import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.PasswordGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import voyage.security.PermissionBasedUserDetailsService
import voyage.security.crypto.CryptoService
import voyage.security.role.Role
import voyage.security.user.User
import voyage.security.user.UserService

@Service
class BootstrapService {
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapService)

    private final UserService userService
    private final CryptoService cryptoService
    private final PermissionBasedUserDetailsService permissionBasedUserDetailsService
    private final Environment environment

    @Autowired
    BootstrapService(UserService userService, CryptoService cryptoService, Environment environment,
                     PermissionBasedUserDetailsService permissionBasedUserDetailsService) {
        this.userService = userService
        this.cryptoService = cryptoService
        this.permissionBasedUserDetailsService = permissionBasedUserDetailsService
        this.environment = environment
    }

    void updateSuperUsersPassword() {
        if (Arrays.asList(environment.activeProfiles).contains('test')) {
            return //skip the change password in the test environment
        }
        Iterable<User> users = userService.findAllByRolesInList([Role.SUPER])
        StringBuilder superUsersInfo = new StringBuilder()
        List<CharacterRule> rules = passwordRules
        PasswordGenerator generator = new PasswordGenerator()
        users.each { user ->
            if (cryptoService.hashMatches('password', user.password)) {
                UserDetails userDetails = permissionBasedUserDetailsService.loadUserByUsername(user.username)
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails.username, userDetails.password, userDetails.authorities)
                SecurityContextHolder.context.setAuthentication(authentication)
                user.password = generator.generatePassword(12, rules)
                userService.saveDetached(user)
                superUsersInfo.append("User: ${user.username}, Password: ${user.password} \n")
            }
        }
        if (superUsersInfo.length() > 0 ) {
            LOG.info('Restricted Users found with default password. Generating new passwords:')
            LOG.info(superUsersInfo.toString())
        }

    }

    private static List<CharacterRule> getPasswordRules() {
        List<CharacterRule> rules = Arrays.asList(
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1)
        )
        return rules
    }
}
