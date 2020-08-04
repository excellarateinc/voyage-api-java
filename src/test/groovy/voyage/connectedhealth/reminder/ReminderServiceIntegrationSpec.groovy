package voyage.connectedhealth.reminder

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Specification
import voyage.connectedhealth.result.ResultType
import voyage.connectedhealth.result.ResultTypeService
import voyage.security.TestAuthentication
import voyage.security.TestSecurityContext
import voyage.security.user.User
import voyage.security.user.UserService

@SpringBootTest
class ReminderServiceIntegrationSpec extends Specification {
    @Autowired
    private ReminderService reminderService
    @Autowired
    private UserService userService
    @Autowired
    private ResultTypeService resultTypeService

    private TestAuthentication testAuthentication = new TestAuthentication(principal: 'super')

    def setup() {
        SecurityContextHolder.setContext(new TestSecurityContext(authentication: testAuthentication))
    }

    def 'get - get a reminder by ID'() {
        when:
        Reminder result = reminderService.get(1L)

        then:
        result.id == 1L
        result.comment == 'Coag check'
        result.hour == 8
        result.minute == 0
        result.am
        result.repeatInterval == RepeatInterval.WEEK
        result.repeatCount == 1
        result.daysOfWeek == [DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY]
        result.isNotify
    }

    def "findAll - find all reminders for the given user"() {
        given:
        User user = userService.get(1L)

        when:
        List<Reminder> reminders = reminderService.findAll(user, new PageRequest(0, 10))

        then:
        reminders.size() == 2
    }

    def "save - save a new reminder"() {
        given:
        ResultType resultType = resultTypeService.get(1L)
        Reminder reminder = new Reminder(resultType: new ResultType(id: 1L), hour: 12, minute: 30, am: false,
                repeatInterval: RepeatInterval.WEEK, repeatCount: 1, daysOfWeek: [DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY],
                isNotify: true, comment: 'this is a test')

        when:
        def result = reminderService.save(reminder)

        then:
        result.user.username == 'super'
        result.resultType == resultType
        result.hour == 12
        result.minute == 30
        !result.am
        result.repeatInterval == RepeatInterval.WEEK
        result.repeatCount == 1
        result.daysOfWeek == [DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY]
        result.isNotify
        result.comment == 'this is a test'
    }

    def "delete - delete a reminder"() {
        given:
        Reminder reminder = new Reminder(resultType: new ResultType(id: 1L), hour: 12, minute: 30, am: false,
                repeatInterval: RepeatInterval.WEEK, repeatCount: 1, daysOfWeek: [DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY],
                isNotify: true, comment: 'this is a test')
        reminder = reminderService.save(reminder)
        def reminderId = reminder.id

        when:
        reminderService.delete(reminder)

        then:
        !reminderService.get(reminderId)
    }
}
