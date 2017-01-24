package launchpad.security.user

import com.fasterxml.jackson.annotation.JsonIgnore
import launchpad.security.client.Client
import launchpad.security.client.ClientRedirectUri

import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.validation.constraints.NotNull

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
    User user

}
