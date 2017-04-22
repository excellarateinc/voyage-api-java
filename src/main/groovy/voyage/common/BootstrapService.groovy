package voyage.common

import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.PasswordGenerator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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

    private final UserService userService
    private final CryptoService cryptoService
    private final PermissionBasedUserDetailsService permissionBasedUserDetailsService

    @Autowired
    BootstrapService(UserService userService, CryptoService cryptoService, PermissionBasedUserDetailsService permissionBasedUserDetailsService) {
        this.userService = userService
        this.cryptoService = cryptoService
        this.permissionBasedUserDetailsService = permissionBasedUserDetailsService
    }

    void updateSuperUsersPassword() {
        Logger LOG = LoggerFactory.getLogger('PASSWORD_LOGGER')
        Iterable<User> users = userService.findAllByRolesInList([Role.SUPER])
        StringBuilder superUsersInfo = new StringBuilder()
        List<CharacterRule> passwordRules = getPasswordRules()
        PasswordGenerator generator = new PasswordGenerator()
        users.each { user ->
            if (cryptoService.hashMatches('password', user.password)) {
                UserDetails userDetails = permissionBasedUserDetailsService.loadUserByUsername(user.username)
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails.username, userDetails.password, userDetails.getAuthorities())
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String password = generator.generatePassword(12, passwordRules)
                userService.saveDetached(user)
                superUsersInfo.append("User: ${user.username}, Password: ${password} \n")
            }
        }
        if(superUsersInfo.length() > 0 ) {
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
