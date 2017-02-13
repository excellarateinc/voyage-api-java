package launchpad.account

/**
 * POGO to expose UserSecurityAnswers to clients instead of Domain class UserSecurityQuestion
 */
class UserSecurityAnswers {

    Long userId

    List<SecurityAnswer> securityAnswers
}
