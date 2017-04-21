package voyage.security.user

import org.hibernate.validator.constraints.NotBlank

/**
 * Created by user on 4/20/2017.
 */
class ResetPassword {
    @NotBlank
    String password

    @NotBlank
    String newPassword

    @NotBlank
    String confirmPassword
}
