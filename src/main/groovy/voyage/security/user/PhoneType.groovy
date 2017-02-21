package launchpad.security.user

enum PhoneType {
    MOBILE('mobile'),
    OFFICE('office'),
    HOME('home'),
    OTHER('other')

    final String code

    PhoneType(String code) {
        this.code = code
    }

    String toString() {
        return code
    }
}
