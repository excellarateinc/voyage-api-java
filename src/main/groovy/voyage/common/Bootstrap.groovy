package voyage.common

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Bootstrap implements InitializingBean {

    private final BootstrapService bootstrapService

    @Autowired
    Bootstrap(BootstrapService bootstrapService) {
        this.bootstrapService = bootstrapService
    }

    @Override
    void afterPropertiesSet() throws Exception {
        bootstrapService.updateSuperUsersPassword()
    }
}
