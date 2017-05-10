package voyage.security.bfa

import org.springframework.util.StopWatch
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SleepAfterFailureFilterSpec extends Specification {
    long minSleepMillis = 3000
    long maxSleepMillis = 5000

    SleepAfterFailureFilter filter

    HttpServletRequest request
    HttpServletResponse response
    FilterChain filterChain

    def setup() {
        filter = new SleepAfterFailureFilter()
        filter.isEnabled = true
        filter.httpStatusList = [401, 403, 404]
        filter.minSleepSeconds = minSleepMillis / 1000
        filter.maxSleepSeconds = maxSleepMillis / 1000

        request = Mock(HttpServletRequest)
        response = Mock(HttpServletResponse)
        filterChain = Mock(FilterChain)
    }

    def 'doFilterInternal is skipped if disabled'() {
        given:
            filter.isEnabled = false
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            filter.doFilter(request, response, filterChain)
            stopWatch.stop()

        then:
            1 * filterChain.doFilter(request, response)

            !withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    def 'doFilterInternal is skipped if the HTTP response status code does not match'() {
        given:
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            filter.doFilter(request, response, filterChain)
            stopWatch.stop()

        then:
            2 * response.status >> 200
            1 * filterChain.doFilter(request, response)

            !withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    def 'doFilterInternal is executed if the HTTP response status code matches'() {
        given:
            StopWatch stopWatch = new StopWatch()

        when:
            stopWatch.start()
            filter.doFilter(request, response, filterChain)
            stopWatch.stop()

        then:
            3 * response.status >> 401
            1 * filterChain.doFilter(request, response)

            withinSleepRange(stopWatch.lastTaskTimeMillis)
    }

    private boolean withinSleepRange(long milliseconds) {
        return milliseconds >= minSleepMillis && milliseconds <= maxSleepMillis
    }
}
