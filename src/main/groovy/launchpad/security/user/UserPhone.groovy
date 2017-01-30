package launchpad.security.user

import com.fasterxml.jackson.annotation.JsonIgnore

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull

@Entity
class UserPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id

    @Enumerated(EnumType.STRING)
    PhoneType phoneType

    @NotNull
    @JsonIgnore
    Boolean isDeleted = Boolean.FALSE

    @NotNull
    String phoneNumber

    @ManyToOne
    @JoinColumn(name='user_id')
    @JsonIgnore
    User user

}
