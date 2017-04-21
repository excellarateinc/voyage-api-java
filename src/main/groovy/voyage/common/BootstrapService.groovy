package voyage.common

import org.apache.log4j.Appender
import org.apache.log4j.FileAppender
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.PasswordGenerator
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import voyage.security.crypto.CryptoService
import voyage.security.role.Role
import voyage.security.user.User
import voyage.security.user.UserService

@Service
class BootstrapService implements InitializingBean {

    private static final Logger LOG = Logger.getLogger(BootstrapService.class.name)
    private static final String PASSWORD_APPENDER_NAME = 'PASSWORD'
    private static final String PASSWORD_LOG_FILE_NAME = 'passwords.log'

    private final UserService userService
    private final CryptoService cryptoService

    @Autowired
    BootstrapService(UserService userService, CryptoService cryptoService) {
        this.userService = userService
        this.cryptoService = cryptoService
    }

    @Override
    void afterPropertiesSet() throws Exception {
        updateSuperUsersPassword()
    }

    void updateSuperUsersPassword() {
        Logger LOG = getPasswordLogger()
        Iterable<User> users = userService.findAllByRolesInList([Role.SUPER])
        StringBuilder superUsersInfo = new StringBuilder()
        List<CharacterRule> passwordRules = getPasswordRules()
        PasswordGenerator generator = new PasswordGenerator();
        users.each { user ->
            if (cryptoService.hashMatches('password', user.password)) {
                String password = generator.generatePassword(8, passwordRules)
                //userService.save(user)
                superUsersInfo.append("User: ${user.username}, Password: ${password} \n")
            }
        }
        if(superUsersInfo.length() > 0 ) {
            LOG.info('Restricted Users found with default password. Generating new passwords:')
            LOG.info(superUsersInfo.toString())
        }

    }

    private static Logger getPasswordLogger() {
        Appender appender = LOG.getAppender(PASSWORD_APPENDER_NAME);
        if (!appender) {
            appender = new FileAppender();
            appender.setFile(PASSWORD_LOG_FILE_NAME);
            appender.setName(PASSWORD_APPENDER_NAME);
            appender.setLayout(new PatternLayout(PatternLayout.DEFAULT_CONVERSION_PATTERN));
            appender.setAppend(true);
            appender.activateOptions();
            LOG.addAppender(appender);
        }
        return LOG
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
