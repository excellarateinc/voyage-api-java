package voyage.connectedhealth.result

import groovy.transform.EqualsAndHashCode
import org.hibernate.sql.Update
import voyage.connectedhealth.validation.Create

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
@EqualsAndHashCode(includes=['id'])
class ResultType {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @NotNull(groups = Update.class)
    @Null(groups = Create.class)
    Long id

    @NotNull
    String name

    @NotNull
    String code

    @NotNull
    String unitOfMeasure
}
