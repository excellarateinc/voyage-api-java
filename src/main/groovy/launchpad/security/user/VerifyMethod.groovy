package launchpad.security.user

enum VerifyMethod {
    TEXT('text'),
    EMAIL('email')

    final String code

    VerifyMethod(String code) {
        this.code = code
    }

    String toString() {
        return code
    }
}
