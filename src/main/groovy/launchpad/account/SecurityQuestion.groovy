package launchpad.account

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull

@Entity
public class SecurityQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    @NotNull
    String question

    @NotNull
    Boolean isDeleted = Boolean.FALSE
}