package voyage.security.bfa

import org.springframework.util.StopWatch
import spock.lang.Specification

class SleepAfterFailureEventListenerSpec extends Specification {
    long minSleepMillis = 3000
    long maxSleepMillis = 5000
    SleepAfterFailureEventListener listener

    def setup() {
        listener = new SleepAfterFailureEventListener()
        listener.minSleepSeconds = minSleepMillis / 1000
        listener.maxSleepSeconds = maxSleepMillis / 1000
    }

    def 'authenticationFailed is skipped if disabled'() {
        given:
            listener.isEnabled = false
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            listener.authenticationFailed(null)
            stopWatch.stop()

        then:
            !withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    def 'authenticationFailed sleeps between the min and max amount'() {
        given:
            listener.isEnabled = true
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            listener.authenticationFailed(null)
            stopWatch.stop()

        then:
            withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    private boolean withinSleepRange(long milliseconds) {
        return milliseconds >= minSleepMillis && milliseconds <= maxSleepMillis
    }
}
