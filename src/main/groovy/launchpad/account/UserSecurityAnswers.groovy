package launchpad.account

import launchpad.security.user.User

/**
 * POGO to expose UserSecurityAnswers to clients instead of Domain class UserSecurityQuestion
 */
class UserSecurityAnswers {

    User user

    List<SecurityAnswer> securityAnswers
}
