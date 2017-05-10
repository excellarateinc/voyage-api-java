package voyage.security.bfa

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.stereotype.Component

import java.security.SecureRandom

@Component
class SleepAfterFailureEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(SleepAfterFailureEventListener)

    @Value('${security.brute-force-attack.sleep-after-failure.enabled}')
    private boolean isEnabled

    @Value('${security.brute-force-attack.sleep-after-failure.min-sleep-seconds}')
    private int minSleepSeconds

    @Value('${security.brute-force-attack.sleep-after-failure.max-sleep-seconds}')
    private int maxSleepSeconds

    @EventListener
    void authenticationFailed(AbstractAuthenticationFailureEvent ignore) {
        if (!isEnabled) {
            LOG.debug('SleepAfterFailureEventListener is DISABLED. Skipping.')
            return
        }
        LOG.debug('User authentication failed. Sleeping the thread to slow down brute force attacks')

        SecureRandom random = new SecureRandom()
        int sleepSeconds = random.nextInt(maxSleepSeconds)
        if (sleepSeconds < minSleepSeconds) {
            sleepSeconds = minSleepSeconds
        }

        if (LOG.debugEnabled) {
            LOG.debug("Sleeping the thread for ${sleepSeconds} seconds")
        }

        sleep(sleepSeconds * 1000)

        LOG.debug('Resuming the thread')
    }
}
