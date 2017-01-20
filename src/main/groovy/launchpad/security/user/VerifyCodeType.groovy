package launchpad.security.user

enum VerifyCodeType {
    PASSWORD_RESET('password_reset'),
    ACCOUNT_VERIFICATION('account_verification')

    final String code

    VerifyCodeType(String code) {
        this.code = code
    }

    String toString() {
        return code
    }
}
