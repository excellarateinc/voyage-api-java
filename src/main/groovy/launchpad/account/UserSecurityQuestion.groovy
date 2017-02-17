package launchpad.account

import launchpad.security.user.User

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.validation.constraints.NotNull

@Entity
class UserSecurityQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = 'user_id')
    User user

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = 'question_id')
    SecurityQuestion question

    @NotNull
    String answer

    @NotNull
    Boolean isDeleted = Boolean.FALSE
}
