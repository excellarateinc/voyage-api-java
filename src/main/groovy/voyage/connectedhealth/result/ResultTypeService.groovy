package voyage.connectedhealth.result

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.transaction.Transactional

@Transactional
@Service
class ResultTypeService {
    private ResultTypeRepository resultTypeRepository

    @Autowired
    ResultTypeService(ResultTypeRepository resultTypeRepository) {
        this.resultTypeRepository = resultTypeRepository
    }

    ResultType get(long id) {
        return resultTypeRepository.findOne(id)
    }

    List<ResultType> findAll() {
        return resultTypeRepository.findAll()
    }
}
