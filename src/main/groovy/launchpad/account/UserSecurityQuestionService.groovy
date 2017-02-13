package launchpad.account

import groovy.time.TimeCategory
import launchpad.error.PasswordRecoveryFailedException
import launchpad.error.UnknownIdentifierException
import launchpad.security.user.User
import launchpad.security.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service
@Validated
class UserSecurityQuestionService {
    private final UserSecurityQuestionRepository userSecurityQuestionRepository
    private final UserRepository userRepository

    @Value('${acct-temp-suspended-message}')
    private String accTempSuspMsg

    @Value('${acct-locked-message}')
    private String accLockedMsg

    @Value('${acct-temp-susp-min-attempts}')
    private int acctTempSuspMinAttmpts

    @Value('${acct-temp-susp-max-attempts}')
    private int acctTempSuspMaxAttmpts

    @Value('${acct-temp-susp-duration}')
    private int acctTempSuspDuration

    @Value('${acct-extended-susp-max-attempts}')
    private int acctExtSuspMaxAttmpts

    @Value('${acct-extended-susp-duration}')
    private int acctExtSuspDuration

    @Autowired
    UserSecurityQuestionService(UserSecurityQuestionRepository userSecurityQuestionRepository, UserRepository userRepository) {
        this.userSecurityQuestionRepository = userSecurityQuestionRepository
        this.userRepository = userRepository
    }

    void delete(@NotNull Long id) {
        UserSecurityQuestion userSecurityQuestion = get(id)
        userSecurityQuestion.isDeleted = true
        userSecurityQuestionRepository.save(userSecurityQuestion)
    }

    UserSecurityQuestion get(@NotNull Long id) {
        UserSecurityQuestion userSecurityQuestion = userSecurityQuestionRepository.findOne(id)
        if (!userSecurityQuestion) {
            throw new UnknownIdentifierException('UserSecurityQuestion: ' + id + ' : Unknown record identifier provided')
        }
        return userSecurityQuestion
    }

    UserSecurityQuestion saveOrUpdate(@Valid UserSecurityQuestion userSecurityQuestionIn) {
        userSecurityQuestionIn.answer = encryptAnswer(userSecurityQuestionIn.answer)
        if (userSecurityQuestionIn.id) {
            UserSecurityQuestion userSecurityQuestion = get(userSecurityQuestionIn.id)
            userSecurityQuestion.with {
                userId = userSecurityQuestionIn.userId
                questionId = userSecurityQuestionIn.questionId
                answer = userSecurityQuestionIn.answer
            }
            return userSecurityQuestionRepository.save(userSecurityQuestion)
        }
        return userSecurityQuestionRepository.save(userSecurityQuestionIn)
    }

    List<UserSecurityQuestion> findSecurityQuestionsByUserId(@NotNull Long userId) {
        userSecurityQuestionRepository.findByUserId(userId)
    }

    /**
     * One-way password encryption using
     * @param passwordIn
     * @return String encrypted answer
     */
    String encryptAnswer(String passwordIn) {
        java.security.MessageDigest.getInstance('SHA-256')
                .digest(passwordIn.getBytes('UTF-8')).encodeBase64().toString()
    }

    /**
     * Perform checks to suspend or lock user based on number of failed recovery attempts
     * @param user User object which needs to be checked for stats of invalid recovery attempts
     */
    void assertFailedAttemptRules(User user) {
         //if user is already locked
        if (user.isAccountLocked) {
            throw new PasswordRecoveryFailedException(accLockedMsg)
        } else {
            Date currentDateTime = new Date()
            if (user.noOfFailedRecoveryAttempts && user.failedRecoveryAttemptTime) {
                //temporarily suspend user account
                if (user.noOfFailedRecoveryAttempts >= acctTempSuspMinAttmpts && user.noOfFailedRecoveryAttempts < acctTempSuspMaxAttmpts) {
                    use(TimeCategory) {
                        if (currentDateTime < user.failedRecoveryAttemptTime + acctTempSuspDuration.minutes) {
                            throw new PasswordRecoveryFailedException(accTempSuspMsg)
                        }
                    }
                }
                //suspend account for extended duration
                else if (user.noOfFailedRecoveryAttempts >= acctTempSuspMaxAttmpts && user.noOfFailedRecoveryAttempts < acctExtSuspMaxAttmpts) {
                    use(TimeCategory) {
                        if (currentDateTime < user.failedRecoveryAttemptTime + acctExtSuspDuration.minutes) {
                            throw new PasswordRecoveryFailedException(accTempSuspMsg)
                        }
                    }
                }
                //lock user if number of failed recovery attempts exceeds configured max allowed
                else if (user.noOfFailedRecoveryAttempts >= acctExtSuspMaxAttmpts) {
                    throw new PasswordRecoveryFailedException(accLockedMsg)
                }
            }
        }
    }

    /**
     * Validates all the user security questions are answered in the recovery process.
     * @param userId
     * @param userSecurityAnswersList
     * @return boolean true for valid and false for invalid user input
     */
    boolean validateUserSecurityAnswers(Long userId, List<SecurityAnswer> userSecurityAnswersList) throws PasswordRecoveryFailedException {
        boolean isValid = true
        boolean exitLoop = true
        if (!userSecurityAnswersList) {
            throw new PasswordRecoveryFailedException('User security answers are required for password recovery')
        }
        User user = userRepository.findOne(userId)
        if (!user) {
            throw new UnknownIdentifierException('Requested user account not found')
        }
        assertFailedAttemptRules(user)
        Iterable<UserSecurityQuestion> userSecurityAnswersInDbList = userSecurityQuestionRepository.findByUserId(userId)
        if (!userSecurityAnswersInDbList) {
            throw new PasswordRecoveryFailedException('User security questions have not been set-up for the user. Please contact administrator')
        }
        userSecurityAnswersInDbList.any { userSecurityAnswerInDb ->
            //For each user security question from db, retrieve corresponding user entered answer from the input
            SecurityAnswer userEnteredSecurityAnswer = userSecurityAnswersList.find {
                it.questionId == userSecurityAnswerInDb.question.id
            }
            //if there is no corresponding user entered answer for this question then the validation should fail
            if (!userEnteredSecurityAnswer) {
                isValid = false
                return exitLoop
            }
            // Since the answers in db are stored in encrypted format, first encrypt the user entered answer before comparison
            String encryptedAnswer = encryptAnswer(userEnteredSecurityAnswer.answer)
            // if answer does not match validation must fail
            if (encryptedAnswer != userSecurityAnswerInDb.answer) {
                isValid = false
                return exitLoop
            }
        }
        performPostValidationUpdates(user, isValid)
        return isValid
    }
    /**
     * If the recovery validation is successful, set isPasswordResetRequired as true.
     * if recovery attempt fails, increment number of recovery attempts
     * @param user User object to update
     * @param isValid flag to indicate if the user answers were valid or not
     */
    void performPostValidationUpdates(User user, boolean isValid) {
        //no validation failures and all the user entered security answers from db are validated
        if (isValid) {
            user.isVerifyRequired = true
            user.isPasswordResetRequired = true
            user.noOfFailedRecoveryAttempts = null
            user.failedRecoveryAttemptTime = null
        } //user answers invalid, hence increment the failed recovery attempts
        else {
            user.failedRecoveryAttemptTime = user.failedRecoveryAttemptTime ?: new Date()
            user.noOfFailedRecoveryAttempts = user.noOfFailedRecoveryAttempts ?: 0
            user.noOfFailedRecoveryAttempts += 1
            //lock user account if failed recovery attempts exceed the max allowed attempts
            if (user.noOfFailedRecoveryAttempts >= acctExtSuspMaxAttmpts) {
                user.isAccountLocked = true
            }
        }
        userRepository.save(user)
    }
}
