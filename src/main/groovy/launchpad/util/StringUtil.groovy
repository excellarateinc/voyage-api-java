package launchpad.util

import org.apache.commons.lang3.RandomStringUtils

class StringUtil {

    static String generateUniqueCode(int length = 32) {
        RandomStringUtils.randomAlphanumeric(length).toUpperCase()
    }
}
