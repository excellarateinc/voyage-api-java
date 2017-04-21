package voyage.common

import org.passay.CharacterRule
import org.passay.EnglishCharacterData
import org.passay.LengthRule
import org.passay.PasswordData
import org.passay.PasswordValidator
import org.passay.RuleResult
import org.passay.WhitespaceRule
import org.springframework.stereotype.Service

import java.util.regex.Pattern

/**
 * Created by user on 4/19/2017.
 */
@Service
class PasswordValidationService {

    private boolean validate(String password){

        PasswordValidator policy = new PasswordValidator(Arrays.asList(new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase,1),new CharacterRule(EnglishCharacterData.Digit,1),
                new CharacterRule(EnglishCharacterData.Special,1),new WhitespaceRule()));
        RuleResult result = policy.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }
        throw new InvalidPasswordException("The password did not met the requirements")
    }

}
