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
    private final int maxLength = 30
    private final int minLength = 8
    private final int minimumLowerCaseChars = 1
    private final int minimumUpperCaseChars = 1
    private final int minimumSpecialChars = 1
    private final int minimumDigits = 1
    boolean validate(String password) {
        CharacterRule upperCaseCharacterRule =  new CharacterRule(EnglishCharacterData.UpperCase, minimumUpperCaseChars)
        CharacterRule lowerCaseCharacterRule =  new CharacterRule(EnglishCharacterData.LowerCase, minimumLowerCaseChars)
        CharacterRule numericCharacterRule =  new CharacterRule(EnglishCharacterData.Digit, minimumDigits)
        CharacterRule specialCharacterRule =    new CharacterRule(EnglishCharacterData.Special, minimumSpecialChars)
        LengthRule lengthRule = new LengthRule(minLength, maxLength)
        WhitespaceRule whitespaceRule = new WhitespaceRule()

        PasswordValidator policy = new PasswordValidator(Arrays.asList(lengthRule, upperCaseCharacterRule, lowerCaseCharacterRule,
                numericCharacterRule, specialCharacterRule, whitespaceRule))
        RuleResult result = policy.validate(new PasswordData(password))
        if (result.isValid()) {
            return true
        }
        throw new InvalidPasswordException('The password did not meet the requirements')
    }

}
