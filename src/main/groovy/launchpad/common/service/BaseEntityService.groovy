package launchpad.common.service

import launchpad.common.exception.EntityNotFoundException
import org.springframework.transaction.annotation.Transactional

@Transactional
abstract class BaseEntityService {

    def Class entityClass;

    BaseEntityService(){}

    BaseEntityService(Class entityClass) {
        this.entityClass = entityClass
    }

    def assertEntityNotNull(entity, entityClass, id) throws EntityNotFoundException {
        if (!entity) {
            throw new EntityNotFoundException(entityClass, id)
        }
        return entity
    }

    def getEntity(long id) throws EntityNotFoundException {
        def entity = entityClass.get(id)
        assertEntityNotNull(entity, entityClass, id)
    }

    def save(entity) {
        entity.save(flush:true, failOnError:true)
    }

    def update(entity) {
        getEntity(entity.id)
        return entity.save(flush:true, failOnError:true)?.id
    }

    def delete(long id) {
        def entity = get(id)
        entity.delete(flush:true, failOnError:true)
    }

    def get(long id) throws EntityNotFoundException {
        return getEntity(id)
    }

    def list() {
        entityClass.list()
    }
}
