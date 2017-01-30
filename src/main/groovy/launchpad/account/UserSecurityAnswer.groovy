package launchpad.account

import launchpad.security.user.User

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
public class UserSecurityAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    @ManyToOne
    @JoinTable(name='user', joinColumns=@JoinColumn(name='id'))
    User user_id

    @ManyToOne
    @JoinTable(name='security_questions', joinColumns=@JoinColumn(name='id'))
    SecurityQuestion question_id

    @NotNull
    String answer

    @NotNull
    Boolean isDeleted = Boolean.FALSE
}