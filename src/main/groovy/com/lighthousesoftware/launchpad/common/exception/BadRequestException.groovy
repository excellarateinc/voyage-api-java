package com.lighthousesoftware.launchpad.common.exception

class BadRequestException extends Exception {
    BadRequestException() {
    }

    BadRequestException(String message) {
        super(message)
    }

    BadRequestException(String message, Throwable cause) {
        super(message, cause)
    }

    BadRequestException(Throwable cause) {
        super(cause)
    }
}
