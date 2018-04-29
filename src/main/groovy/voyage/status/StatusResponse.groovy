package voyage.status

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
class StatusResponse {
    @ApiModelProperty(allowableValues = 'alive')
    String status
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssXXX")
    Date datetime
}
