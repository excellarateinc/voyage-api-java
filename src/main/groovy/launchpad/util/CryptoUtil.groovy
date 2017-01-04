package launchpad.util

class CryptoUtil {

    static String generateUniqueToken() {
        // Create a unique UUID without the dashes. This will work better on URLs
        // and wont necessary clue people to it being a UUID. Either way we
        return UUID.randomUUID().toString().replaceAll('-', '')
    }
}
