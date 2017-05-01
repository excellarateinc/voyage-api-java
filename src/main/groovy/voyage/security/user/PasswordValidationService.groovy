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
    boolean validate(String password) {
        PasswordValidator policy = new PasswordValidator(passwordPolicyRules)
        RuleResult result = policy.validate(new PasswordData(password))
        if (!result.valid) {
            throw new WeakPasswordException()
        }
        return true
    }

    private static List<CharacterRule> getPasswordPolicyRules() {
        CharacterRule upperCaseCharacterRule =  new CharacterRule(EnglishCharacterData.UpperCase, 1)
        CharacterRule lowerCaseCharacterRule =  new CharacterRule(EnglishCharacterData.LowerCase, 1)
        CharacterRule numericCharacterRule =  new CharacterRule(EnglishCharacterData.Digit, 1)
        CharacterRule specialCharacterRule =    new CharacterRule(EnglishCharacterData.Special, 1)
        LengthRule lengthRule = new LengthRule(8, 30)
        WhitespaceRule whitespaceRule = new WhitespaceRule()
        [upperCaseCharacterRule, lowerCaseCharacterRule, numericCharacterRule, specialCharacterRule, lengthRule, whitespaceRule]
    }
}
