package launchpad.account

enum VerifyType {
    TEXT('text'),
    EMAIL('email')

    final String code

    VerifyType(String code) {
        this.code = code
    }

    String toString() {
        return code
    }
}
