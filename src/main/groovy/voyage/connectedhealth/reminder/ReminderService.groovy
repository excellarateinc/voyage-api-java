package voyage.connectedhealth.reminder


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import voyage.connectedhealth.result.ResultTypeService
import voyage.security.user.User
import voyage.security.user.UserService

import javax.transaction.Transactional

@Transactional
@Service
class ReminderService {
    private ReminderRepository reminderRepository
    private UserService userService
    private ResultTypeService resultTypeService

    @Autowired
    ReminderService(ReminderRepository reminderRepository, UserService userService, ResultTypeService resultTypeService) {
        this.reminderRepository = reminderRepository
        this.userService = userService
        this.resultTypeService = resultTypeService
    }

    Reminder get(long reminderId) {
        return reminderRepository.findOne(reminderId)
    }

    List<Reminder> findAll(User user, Pageable pageable) {
        return reminderRepository.findByUser(user, pageable)
    }

    Reminder save(Reminder reminderIn) {
        Reminder reminder
        if (reminderIn.id) {
            reminder = get(reminderIn.id)
        } else {
            reminder = new Reminder()
            reminder.user = userService.currentUser
            reminder.resultType = resultTypeService.get(reminderIn.resultType.id)
        }
        reminder.with {
            reminder.comment = reminderIn.comment
            reminder.isNotify = reminderIn.isNotify
            reminder.daysOfWeek = reminderIn.daysOfWeek
            reminder.repeatCount = reminderIn.repeatCount
            reminder.repeatInterval = reminderIn.repeatInterval
            reminder.minute = reminderIn.minute
            reminder.hour = reminderIn.hour
            reminder.am = reminderIn.am
            reminder.endDate = reminderIn.endDate
        }
        return reminderRepository.save(reminder)
    }

    void delete(Reminder reminder) {
        if (reminder.id) {
            reminderRepository.delete(reminder)
        }
    }
}
