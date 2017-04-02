package voyage.status

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(['/api/status'])
class StatusController {

    /**
     * @api {get} /status Status
     * @apiVersion 1.0.0
     * @apiName Status
     * @apiGroup Status
     *
     * @apiPermission none
     *
     * @apiSuccess {String} status
     * @apiSuccess {String} datetime
     *
     * @apiSuccessExample Success-Response:
     *   HTTP/1.1 200 OK
     *   [
     *       {
     *           "status": "alive",
     *           "datetime": "2016-12-23 17:55:55 UTC",
     *       }
     *   ]
     **/
    @GetMapping
    ResponseEntity list() {
        Map<String, String> response = [status:'alive', datetime:currentDate]
        return new ResponseEntity(response, HttpStatus.OK)
    }

    private static String getCurrentDate() {
        new Date().format("yyyy-MM-dd'T'HH:mm:ssXXX")
    }
}
