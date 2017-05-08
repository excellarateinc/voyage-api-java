package voyage.security.bfa

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import voyage.security.PermissionBasedUserDetails
import voyage.security.user.User
import voyage.security.user.UserService

@Component
class UserLockEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(UserLockEventListener)
    private UserService userService

    @Value('${security.brute-force-attack.user-lock-event-listener.enabled}')
    private boolean isEnabled
    
    @Value('${security.brute-force-attack.user-lock-event-listener.max-login-attempts}')
    private int maxLoginAttempts = 5

    UserLockEventListener(UserService userService) {
        this.userService = userService
    }

    @EventListener
    void authenticationSuccess(AuthenticationSuccessEvent event) {
        LOG.debug('User authentication successful')
        Authentication authentication = event.authentication
        if (authentication.principal instanceof PermissionBasedUserDetails) {
            String username = ((PermissionBasedUserDetails) authentication.principal).username
            User user = userService.findByUsername(username)
            if (user) {
                if (user.failedLoginAttempts > 0) {
                    if (LOG.debugEnabled) {
                        LOG.debug('Resetting the failed login attempts to 0 for username: ' + user.username)
                    }
                    user.failedLoginAttempts = 0
                    userService.saveDetached(user)
                } else if (LOG.debugEnabled) {
                    LOG.debug("User ${user.username} has no failed login attempts. Skipping update.")
                }
            }
        } else if (LOG.debugEnabled) {
            LOG.debug("Authentication principal is not a recognized type: ${authentication.principal}. Skipping.")
        }
    }

    @EventListener
    void authenticationFailed(AbstractAuthenticationFailureEvent event) {
        LOG.debug('User authentication failed')
        if (event.source instanceof UsernamePasswordAuthenticationToken) {
            String username = ((UsernamePasswordAuthenticationToken)event.source).principal
            User user = userService.findByUsername(username)
            if (user && user.isEnabled && !user.isAccountLocked && !user.isAccountExpired && !user.isCredentialsExpired) {
                if (LOG.debugEnabled) {
                    LOG.debug('Found User record in the database for username: ' + username)
                }

                if (!user.failedLoginAttempts) {
                    user.failedLoginAttempts = 0
                }

                user.failedLoginAttempts = user.failedLoginAttempts + 1
                if (LOG.debugEnabled) {
                    LOG.debug("User ${username} has ${user.failedLoginAttempts} failed login attempts.")
                }

                if (user.failedLoginAttempts >= maxLoginAttempts) {
                    if (LOG.debugEnabled) {
                        LOG.debug("User ${username} has hit their max failed login attempts of ${maxLoginAttempts}. Locking user account with ID=${user.id}.")
                    }
                    user.isAccountLocked = true
                }

                userService.saveDetached(user)

            } else if (LOG.debugEnabled) {
                if (!user) {
                    LOG.debug('No User record found in the database for username: ' + username)
                } else if (!user.isEnabled) {
                    LOG.debug('The User record is DISABLED for username: ' + username)
                } else if (user.isAccountLocked) {
                    LOG.debug('The User record is LOCKED for username: ' + username)
                } else if (user.isAccountExpired) {
                    LOG.debug('The User record is EXPIRED for username: ' + username)
                } else if (user.isCredentialsExpired) {
                    LOG.debug('The User record has its CREDENTIALS EXPIRED for username: ' + username)
                }
            }
        } else if (LOG.debugEnabled) {
            LOG.debug("Event source is not a recognized type: ${event.source}. Skipping.")
        }
    }
}
