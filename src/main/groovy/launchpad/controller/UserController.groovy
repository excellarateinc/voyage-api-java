package launchpad.controller

import launchpad.domain.User
import launchpad.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController extends BaseController {

    @Autowired
    UserService userService;

    @GetMapping
    ResponseEntity list(){
        def users = userService.list()
        return new ResponseEntity(users, HttpStatus.OK);
    }

    @PostMapping("/save")
    ResponseEntity save(@RequestBody User user){
        //TODO: Add validations - Jagadeesh Manne - 12/09/2016
        userService.save(user)
        return new ResponseEntity(user, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity get(@PathVariable("id") long id){
        //TODO: Add validations to check path variable - Jagadeesh Manne - 12/09/2016
        User user = userService.get(id)
        return new ResponseEntity(user, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    ResponseEntity delete(@PathVariable("id") long id){
        //TODO: Add validations to check path variable - Jagadeesh Manne - 12/09/2016
        User user = userService.delete(id)
        //TODO: return valid response
        return new ResponseEntity(HttpStatus.OK);
    }

    @PutMapping("/update")
    ResponseEntity update(@RequestBody User user){
        //TODO: Add validations - Jagadeesh Manne - 12/09/2016
        userService.update(user)
        return new ResponseEntity(user, HttpStatus.OK);
    }
}
