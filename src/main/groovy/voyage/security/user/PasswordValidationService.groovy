package voyage.security.user

import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.LengthRule
import org.passay.PasswordData
import org.passay.PasswordValidator
import org.passay.RuleResult
import org.passay.WhitespaceRule
import org.springframework.stereotype.Service

@Service
class PasswordValidationService {
    private static final int MAX_LENGTH = 30
    private static final int MIN_LENGTH = 8
    private static final int MIN_LOWERCASE_CHARS = 1
    private static final int MIN_UPPERCASE_CHARS = 1
    private static final int MIN_SPL_CHARS = 1
    private static final int MIN_DIGITS = 1

    boolean validate(String password) {
        CharacterRule upperCaseCharacterRule =  new CharacterRule(EnglishCharacterData.UpperCase, MIN_UPPERCASE_CHARS)
        CharacterRule lowerCaseCharacterRule =  new CharacterRule(EnglishCharacterData.LowerCase, MIN_LOWERCASE_CHARS)
        CharacterRule numericCharacterRule =  new CharacterRule(EnglishCharacterData.Digit, MIN_DIGITS)
        CharacterRule specialCharacterRule =    new CharacterRule(EnglishCharacterData.Special, MIN_SPL_CHARS)
        LengthRule lengthRule = new LengthRule(MIN_LENGTH, MAX_LENGTH)
        WhitespaceRule whitespaceRule = new WhitespaceRule()

        PasswordValidator policy = new PasswordValidator(Arrays.asList(lengthRule, upperCaseCharacterRule, lowerCaseCharacterRule,
                numericCharacterRule, specialCharacterRule, whitespaceRule))
        RuleResult result = policy.validate(new PasswordData(password))
        if (!result.valid) {
            throw new WeakPasswordException()
        }

        return true
    }

}
