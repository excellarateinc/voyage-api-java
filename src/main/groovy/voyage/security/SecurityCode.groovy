package launchpad.security

import org.apache.commons.lang3.RandomStringUtils

class SecurityCode {

    static String getUserVerifyCode() {
        RandomStringUtils.randomNumeric(6).toUpperCase()
    }
}
