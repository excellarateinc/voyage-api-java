/*
 * Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
