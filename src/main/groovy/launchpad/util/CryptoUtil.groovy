package launchpad.util

import org.apache.commons.lang3.RandomStringUtils

class CryptoUtil {

    static String generateUniqueToken(int length = 32) {
        RandomStringUtils.randomAlphanumeric(length).toUpperCase()
    }
}
