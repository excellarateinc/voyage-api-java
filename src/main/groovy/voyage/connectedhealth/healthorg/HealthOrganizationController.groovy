package voyage.connectedhealth.healthorg

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import voyage.connectedhealth.validation.Create
import voyage.connectedhealth.validation.Update
import voyage.security.user.UserService

@RestController
@RequestMapping(['/connectedhealth/v1/healthorg', '/connectedhealth/v1.0/healthorg'])
class HealthOrganizationController {
    private HealthOrganizationService healthOrganizationService
    private UserService userService

    @Autowired
    HealthOrganizationController(HealthOrganizationService healthOrganizationService,
                                 UserService userService) {
        this.healthOrganizationService = healthOrganizationService
        this.userService = userService
    }

    @GetMapping
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity findByCurrentUser() {
        List<HealthOrganization> healthOrganizations = healthOrganizationService.findByUser(userService.currentUser)
        return new ResponseEntity(healthOrganizations, HttpStatus.OK)
    }
    @GetMapping('/{healthOrgId}')
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity get(@PathVariable('healthOrgId') Long healthOrgId) {
        HealthOrganization healthOrganization = healthOrganizationService.get(healthOrgId)
        return new ResponseEntity(healthOrganization, HttpStatus.OK)
    }

    @GetMapping('/all')
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity findAll(Pageable pageable) {
        Page<HealthOrganization> healthOrganizations = healthOrganizationService.findAll(pageable)
        return new ResponseEntity(healthOrganizations.getContent(), HttpStatus.OK)
    }

    @PostMapping
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity create(@Validated(Create.class) @RequestBody HealthOrganization healthOrganizationIn) {
        HealthOrganization healthOrganization = healthOrganizationService.save(healthOrganizationIn)
        return new ResponseEntity(healthOrganization, HttpStatus.CREATED)
    }

    @PostMapping('/assign')
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity assign(@RequestBody HealthOrganization healthOrganizationIn) {
        HealthOrganizationUser healthOrganizationUser = healthOrganizationService.assign(healthOrganizationIn, userService.currentUser)
        return new ResponseEntity(healthOrganizationUser, HttpStatus.CREATED)
    }

    @PutMapping
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity update(@Validated(Update.class) @RequestBody HealthOrganization healthOrganizationIn) {
        healthOrganizationService.save(healthOrganizationIn)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @GetMapping('/usersettings')
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity findAllUserSettings() {
        List<HealthOrganizationUserSettings> healthOrganizationUserSettings = healthOrganizationService.findAllUserSettings(userService.currentUser)
        return new ResponseEntity(healthOrganizationUserSettings, HttpStatus.OK)
    }

    @PutMapping('/usersettings')
//    @PreAuthorize("hasAuthority('')")
    ResponseEntity updateUserSettings(@Validated @RequestBody HealthOrganizationUserSettings healthOrganizationUserSettingsIn) {
        healthOrganizationService.updateUserSettings(healthOrganizationUserSettingsIn, userService.currentUser)
        return new ResponseEntity(HttpStatus.NO_CONTENT)
    }

}
