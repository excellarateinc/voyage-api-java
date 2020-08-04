package voyage.connectedhealth.healthorg

import javax.validation.constraints.NotNull

class HealthOrganizationUserSettings {
    @NotNull
    HealthOrganization healthOrganization

    @NotNull
    Boolean isPrimary

    @NotNull
    Boolean isShareData
}
