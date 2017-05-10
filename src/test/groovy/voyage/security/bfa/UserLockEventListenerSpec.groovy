package voyage.security.bfa

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.AuthenticationException
import spock.lang.Specification
import voyage.security.PermissionBasedUserDetails
import voyage.security.user.User
import voyage.security.user.UserService

class UserLockEventListenerSpec extends Specification {
    UserLockEventListener listener
    UserService userService
    UsernamePasswordAuthenticationToken authentication

    def setup() {
        userService = Mock(UserService)
        listener = new UserLockEventListener(userService)

        authentication = Mock(UsernamePasswordAuthenticationToken)
    }

    def 'authenticationFailed is skipped if disabled'() {
        given:
            listener.isEnabled = false
            AuthenticationException
            AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                    authentication,
                    new BadCredentialsException('test')
            )

        when:
            listener.authenticationFailed(event)

        then:
            0 * authentication.principal
            0 * userService.findByUsername(_ as String)
    }

    def 'authenticationFailed principal is not a UsernamePasswordAuthenticationToken'() {
        given:
            listener.isEnabled = true
            AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                    new TestingAuthenticationToken(null, null),
                    new BadCredentialsException('test')
            )

        when:
            listener.authenticationFailed(event)

        then:
            0 * userService.findByUsername(_ as String)
    }

    def 'authenticationFailed finds the user that is NOT ENABLED'() {
        given:
            listener.isEnabled = true
            AuthenticationException
            AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                    authentication,
                    new BadCredentialsException('test')
            )

            User user = new User(username: 'test', isEnabled: false, isAccountLocked: false, isAccountExpired: false, isCredentialsExpired: false)

        when:
           listener.authenticationFailed(event)

        then:
            1 * authentication.principal >> user.username
            1 * userService.findByUsername(user.username) >> user
            0 * userService.saveDetached(user)
    }

    def 'authenticationFailed finds the user with ACCOUNT LOCKED'() {
        given:
        listener.isEnabled = true
        AuthenticationException
        AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                authentication,
                new BadCredentialsException('test')
        )

        User user = new User(username: 'test', isEnabled: true, isAccountLocked: true, isAccountExpired: false, isCredentialsExpired: false)

        when:
        listener.authenticationFailed(event)

        then:
        1 * authentication.principal >> user.username
        1 * userService.findByUsername(user.username) >> user
        0 * userService.saveDetached(user)
    }

    def 'authenticationFailed finds the user with ACCOUNT EXPIRED'() {
        given:
            listener.isEnabled = true
            AuthenticationException
            AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                    authentication,
                    new BadCredentialsException('test')
            )

            User user = new User(username: 'test', isEnabled: true, isAccountLocked: false, isAccountExpired: true, isCredentialsExpired: false)

        when:
           listener.authenticationFailed(event)

        then:
            1 * authentication.principal >> user.username
            1 * userService.findByUsername(user.username) >> user
            0 * userService.saveDetached(user)
    }

    def 'authenticationFailed finds the user with CREDENTIALS EXPIRED'() {
        given:
            listener.isEnabled = true
            AuthenticationException
            AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                    authentication,
                    new BadCredentialsException('test')
            )

            User user = new User(username: 'test', isEnabled: true, isAccountLocked: false, isAccountExpired: false, isCredentialsExpired: true)

        when:
            listener.authenticationFailed(event)

        then:
            1 * authentication.principal >> user.username
            1 * userService.findByUsername(user.username) >> user
            0 * userService.saveDetached(user)
    }

    def 'authenticationFailed finds the active user and increments the failed login attempts'() {
        given:
            listener.isEnabled = true
            AuthenticationException
            AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                    authentication,
                    new BadCredentialsException('test')
            )

            User user = new User(username: 'test', failedLoginAttempts: 0)

        when:
            listener.authenticationFailed(event)

        then:
            1 * authentication.principal >> user.username
            1 * userService.findByUsername(user.username) >> user
            1 * userService.saveDetached(user)

            user.failedLoginAttempts == 1
            !user.isAccountLocked
    }

    def 'authenticationFailed finds the active user and locks the user account'() {
        given:
            listener.isEnabled = true
            AuthenticationException
            AuthenticationFailureBadCredentialsEvent event = new AuthenticationFailureBadCredentialsEvent(
                    authentication,
                    new BadCredentialsException('test')
            )

            User user = new User(username: 'test', failedLoginAttempts: 4)

        when:
           listener.authenticationFailed(event)

        then:
            1 * authentication.principal >> user.username
            1 * userService.findByUsername(user.username) >> user
            1 * userService.saveDetached(user)

            user.failedLoginAttempts == 5
            user.isAccountLocked
    }

    def 'authenticationSuccess is skipped if disabled'() {
        given:
            listener.isEnabled = false
            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication)

        when:
            listener.authenticationSuccess(event)

        then:
            0 * authentication.principal
            0 * userService.findByUsername(_ as String)
    }

    def 'authenticationSuccess principal is not a PermissionBasedUserDetails'() {
        given:
            listener.isEnabled = true
            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication)

        when:
            listener.authenticationSuccess(event)

        then:
            2 * authentication.principal >> new User()
            0 * userService.findByUsername(_ as String)
    }

    def 'authenticationSuccess user is found and failed attempts is null'() {
        given:
            listener.isEnabled = true
            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication)
            User user = new User(username: 'test', failedLoginAttempts: null)

        when:
            listener.authenticationSuccess(event)

        then:
            2 * authentication.principal >> new PermissionBasedUserDetails(user, [])
            1 * userService.findByUsername(user.username) >> user
            0 * userService.saveDetached(user)
    }

    def 'authenticationSuccess user is found and failed attempts is 0'() {
        given:
            listener.isEnabled = true
            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication)
            User user = new User(username: 'test', failedLoginAttempts: 0)

        when:
           listener.authenticationSuccess(event)

        then:
            2 * authentication.principal >> new PermissionBasedUserDetails(user, [])
            1 * userService.findByUsername(user.username) >> user
            0 * userService.saveDetached(user)
    }

    def 'authenticationSuccess user is found and failed attempts are reset'() {
        given:
            listener.isEnabled = true
            AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication)
            User user = new User(username: 'test', failedLoginAttempts: 3)

        when:
           listener.authenticationSuccess(event)

        then:
            2 * authentication.principal >> new PermissionBasedUserDetails(user, [])
            1 * userService.findByUsername(user.username) >> user
            1 * userService.saveDetached(user)

            user.failedLoginAttempts == 0
    }
}
