package voyage.common

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.EqualsAndHashCode
import org.hibernate.envers.Audited
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener

import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.validation.constraints.NotNull

@MappedSuperclass
@EntityListeners(AuditingEntityListener)
@EqualsAndHashCode(includes=['id'])
class AuditableEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    Long id

    @CreatedBy
    @Audited
    @JsonIgnore
    String createdBy

    @CreatedDate
    @Audited
    @JsonIgnore
    Date createdDate

    @LastModifiedBy
    @Audited
    @JsonIgnore
    String lastModifiedBy

    @LastModifiedDate
    @Audited
    @JsonIgnore
    Date lastModifiedDate

    @NotNull
    @Audited
    @JsonIgnore
    Boolean isDeleted = Boolean.FALSE
}
