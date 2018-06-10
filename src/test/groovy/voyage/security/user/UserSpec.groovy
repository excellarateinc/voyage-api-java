package voyage.security.user

import spock.lang.Specification

class UserSpec extends Specification {
    def 'isNewUsername() is true on a new User object'() {
        given:
            User user = new User()
        when:
            user.username = 'test'
        then:
            user.newUsername

    }

    def 'isNewUsername() is true when a different username is set'() {
        given:
            User user = new User()
            user.username = 'current'
        when:
            user.username = 'new'
        then:
            user.newUsername
    }

    def 'isNewUsername() is always true when the User object has no ID'() {
        given:
            User user = new User()
            user.username = 'current'
        when:
            user.username = 'current'
        then:
            user.newUsername
    }

    def 'isNewUsername() is false when the username is never set'() {
        when:
            User user = new User()
        then:
            !user.newUsername
    }

    def 'isNewUsername() is false on an existing User with only 1 username set'() {
        given:
           User user = new User(id:1)
        when:
            user.username = 'test'
        then:
            !user.newUsername

    }

    def 'isNewUsername() is false on an existing User when the username is set twice'() {
        given:
            User user = new User(id:1, username:'test')
        when:
            user.username = 'test2'
        then:
           user.newUsername

    }

    def 'isNewUsername() is false on an existing User with no username set'() {
        when:
            User user = new User(id:1)
        then:
           !user.newUsername
    }

    def 'isNewPassword() is true on a new User object'() {
        given:
            User user = new User()
        when:
            user.password = 'test'
        then:
            user.newPassword

    }

    def 'isNewPassword() is true when a different password is set'() {
        given:
            User user = new User()
            user.password = 'current'
        when:
            user.password = 'new'
        then:
            user.newPassword
    }

    def 'isNewPassword() is always true for new User objects'() {
        given:
            User user = new User()
            user.password = 'current'
        when:
            user.password = 'current'
        then:
            user.newPassword
    }

    def 'isNewPassword() is false when the password is never set'() {
        when:
            User user = new User()
        then:
            !user.newPassword
    }

    def 'isNewPassword() is false on an existing User with only 1 password set'() {
        given:
            User user = new User(id:1)
        when:
            user.password = 'test'
        then:
            !user.newPassword

    }

    def 'isNewPassword() is false on an existing User when the password is set twice'() {
        given:
            User user = new User(id:1, password:'test')
        when:
            user.password = 'test2'
        then:
            user.newPassword

    }

    def 'isNewPassword() is false on an existing User with no password set'() {
        when:
            User user = new User(id:1)
        then:
            !user.newPassword
    }
}
