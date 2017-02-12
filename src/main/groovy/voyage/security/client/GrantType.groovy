package voyage.security.client

enum GrantType {
    AUTHORIZATION_CODE('authorization_code'),
    CLIENT_CREDENTIALS('client_credentials'),
    IMPLICIT('implicit'),
    PASSWORD('password'),
    REFRESH_TOKEN('refresh_token')

    final String code

    GrantType(String code) {
        this.code = code
    }

    String toString() {
        return code
    }
}
