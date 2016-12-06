package com.lighthousesoftware.launchpad.controller

import com.lighthousesoftware.launchpad.domain.Greeting
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/hello")
class HelloWordController {
    private static final String template = "Hello %s!"

    @RequestMapping(method=RequestMethod.GET)
    @ResponseBody Greeting sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name){
        return new Greeting(String.format(template, name))
    }
}
