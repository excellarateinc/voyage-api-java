package voyage.connectedhealth.reminder

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.sql.Update
import voyage.connectedhealth.result.ResultType
import voyage.connectedhealth.validation.Create
import voyage.security.audit.AuditableEntity
import voyage.security.user.User

import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
@EqualsAndHashCode(includes=['id'])
class Reminder {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @NotNull(groups = Update.class)
    @Null(groups = Create.class)
    Long id

    @ManyToOne
    @JoinColumn(name='user_id')
    @JsonIgnore
    @NotNull
    User user

    @ManyToOne
    @JoinColumn(name='result_type_id')
    @NotNull
    ResultType resultType

    @NotNull
    Integer hour

    @NotNull
    Integer minute

    @NotNull
    Boolean am

    @NotNull
    @Enumerated(EnumType.STRING)
    RepeatInterval repeatInterval

    @NotNull
    Integer repeatCount

    @NotNull
    @Convert(converter = DayOfWeekListAttributeConverter.class)
    List<DayOfWeek> daysOfWeek

    @NotNull
    Boolean isNotify = Boolean.FALSE

    Date endDate

    String comment
}
