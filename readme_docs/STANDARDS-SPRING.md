## Spring Framework Style Guide
The Spring Framework is the defacto standard for enterprise Java development. At the heart of the Spring Framework is
a dependency injection container that wires together loosely coupled objects at application startup. The primary purpose
of the Spring Framework was to allow for application components to be loosely coupled so that components could be
specifically isolated for testing purposes. There are many other dependency injection frameworks for Java, but the 
Spring Framework has evolved into the best-of-breed framework for all things enterprise Java. 

Spring Framework is included within this app by means of Spring Boot, which is a complete application framework that 
bundles together a base Spring Framework application with Maven/Gradle for build management. 

Any developer who will be writing code in a Spring Framework environment should invest the time to reach the excellent 
documentation provided by the Spring Framework. There is a lot to read, but it's incredibly valuable to read it from the
source! 

Resources: 
* [Spring Framework](https://projects.spring.io/spring-framework/)
* [Spring Boot](http://projects.spring.io/spring-boot/)


## Table of Contents
* [Use constructor injection vs field injection](#use-constructor-injection-vs-field-injection)
* [Use Java Configuration over XML Configuration](#use-java-configuration-over-xml-configuration)
* [Manage transactions only in the service layer](#manage-transactions-only-in-the-service-layer)
* [Throw Runtime exceptions instead of checked exceptions](#throw-runtime-exceptions-instead-of-checked-exceptions)
* [Use specific stereotype annotations as much as possible](#use-specific-stereotype-annotations-as-much-as-possible)
* [Spring MVC: Store all business logic in a service layer](#spring-mvc-store-all-business-logic-in-a-service-layer)

## Use Constructor Injection vs Field Injection
While this might hotly debatable, the application standard for dependency injection into an object for this
app is to go through the constructor vs directly injecting through a setter on a property. The primary reason is to 
make the constructor look bloated if too many dependencies are being injected. A bloated constructor is a bad code "smell"
that should tell the developer that they need to think about breaking down the class into a better, smaller, more modular
architecture. 

The following blog article does a good job summarizing the issue and includes a number of excellent reference articles
 that we also include below: 
* [Why I Changed My Mind About Field Injection?](https://www.petrikainulainen.net/software-development/design/why-i-changed-my-mind-about-field-injection/)
* [Repeat After Me: Setter Injection is a Symptom of Design Problems](http://blog.schauderhaft.de/2012/06/05/repeat-after-me-setter-injection-is-a-symptom-of-design-problems/)
* [The One Correct Way to do Dependency Injection](http://blog.schauderhaft.de/2012/01/01/the-one-correct-way-to-do-dependency-injection/)

### DO

```
@RestController
@RequestMapping(['/api/v1/users', '/api/v1.0/users'])
class UserController {
    private final UserService userService
    private final OtherService otherService
    private final AnotherService anotherService
    
    @Autowired
    UserController(UserService userService, OtherService otherService, AnotherService anotherService) {
        this.userService = userService
        this.otherService = otherService
        this.anotherService = anotherService
    }
}
```

### DON'T DO
```
@RestController
@RequestMapping(['/api/v1/users', '/api/v1.0/users'])
class UserController {
    @Autowired
    private UserService userService
    
    @Autowired
    private OtherService otherService
    
    @Autowired
    private AnotherService anotherService
}
```

## Use Java Configuration over XML Configuration
Spring initially started off with XML configuration files to register beans and where they should be injected (among other
things). Spring now supports configuration through Java annotations. This application has been configured to use Java
Spring configuration using annotations exclusively. No XML configuration should be used for a few basic reasons:
 * Java is type safe. Compiler will report issues if you are configuring right bean class qualifiers.
 * XML based on configuration can quickly grow big and hard to read/follow and generally be a pain in the rear-end
 * Search is much simpler, refactoring will be bliss. Finding a bean definition will be far easier. 
 
Articles that influenced our decisions: 
 * [Spring Dependency Injection Styles – Why I love Java based configuration](https://blog.codecentric.de/en/2012/07/spring-dependency-injection-styles-why-i-love-java-based-configuration/)
 * [Consider Replacing Spring XML Configuration with JavaConfig](https://dzone.com/articles/consider-replacing-spring-xml)
 
 ### DO
 Use @Service and other Spring annotations (@Component, @Controller, ...) that mark a class for inclusion in the 
 dependency container. Use @Autowired on constructors to have Spring match the type of class in the constructor with
 a dependency in the dependency container of the same type. 
 
 ```
 @Service
 class SomeService {
     private final SomeRepository someRepository
 
     @Autowired
     SomeService(SomeRepository someRepository) {
         this.someRepository = someRepository
     }
 }
 ```
  
### DON'T DO
 XML configuration files. Period. 
 
 
## Manage transactions only in the service layer
The place to demarcate transactions in a Spring enabled application is the service layer, nowhere else. You should only 
mark @Service beans as @Transactional or their public methods.

You can still place @Transactional with propagation=Propagation.MANDATORY over DAO classes so that they wouldn’t be 
accessed without an active transaction at all.

References:
* [Spring Framework: Chapter 17 Transaction Management](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/transaction.html)
* [How does Spring @Transactional Really Work?](http://blog.jhades.org/how-does-spring-transactional-really-work/)
* Learn more about @Transactional with Spring & JPA by doing a simple Google search "spring @transactional".

### DO

```
 @Service
 @Transactional
 class SomeService {
     private final SomeRepository someRepository
 
     @Autowired
     SomeService(SomeRepository someRepository) {
         this.someRepository = someRepository
     }
 }
```

```
 @Service
 class SomeService {
     private final SomeRepository someRepository
 
     @Autowired
     SomeService(SomeRepository someRepository) {
         this.someRepository = someRepository
     }
     
     @Transactional
     Some getSome(int id) {
         return someRepository.getSome(id)
     }
 }
```

### DONT'T DO
Don't put @Transactional on classes outside of the Service layer. Placing excessive logic into the Controller is a common
mistake and leads to business logic bleeding into the "switchboard" controller, which cannot be reused by other areas of the
app. AVOID THIS! 

```
 @Controller
 class SomeController {
     private final SomeService someService
     private final OtherService otherService
 
     @Autowired
     SomeService(SomeService someService) {
         this.someService = someServicee
     }
     
     @Transactional
     @PostMapping
     ResponseEntity create(Some some) {
         someService.save(some)
         otherService.applyOther(some)         
         return new ResponseEntity(some, HttpStatus.OK)
     }
 }
```

## Throw Runtime exceptions instead of checked exceptions
Spring Framework advocates for throwing Runtime exceptions and then letting interceptors watch for exceptions and
handle them appropriately. Checked exceptions can work fine in very specific cases where the consumer must think about
handling the exception for some reason. In most cases, Runtime exceptions are better as they do not put the burden
on the consumer to know how to handle them. There are a few key areas where Spring intercepts and handles exceptions:
 
 1. @Transactional - when inside of a method marked as @Transactional, then a Checked exception will inform Spring
 to trigger a transaction commit. Spring behaves differently with a Runtime exception in that it will trigger a
  transaction rollback!
  
 2. Spring MVC ExceptionHandler - Spring MVC provides a very nice feature in Spring MVC which will intercept all Runtime
  exceptions and handle them appropriately for the user. The ExceptionHandler can be overridden (like we've done in this
  app) and extended to transform the exceptions however necessary. See DefaultExceptionHandler within this project src. 
   
There are other reasons why one should avoid checked exceptions:
 * [Unchecked Exceptions - The Controversy](http://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html)
 * [Why should you use Unchecked exceptions over Checked exceptions in Java](https://www.javacodegeeks.com/2012/03/why-should-you-use-unchecked-exceptions.html)

### DO
Throw a Runtime exception if a negative situation occurs and the issue needs to be escalated to a process that is
watching for Runtime exceptions. 

```
@Service
class SomeService {
    private final SomeRepository someRepository

    @Autowired
    SomeService(SomeRepository someRepository) {
        this.someRepository = someRepository
    }

    Some get(@NotNull Long id) {
        Some some = someRepository.findOne(id)
        if (!some) {
            throw new UnknownIdentifierException()
        }
        return some
    }
}
```

## Use specific stereotype annotations as much as possible
Spring provides a number of stereotype annotations that can be placed on a class signature to 'register' the class
as a bean within the dependency injection container. 

Common Stereotypes:
* @Service
* @Controller
* @Repository (not used in this app)

All of these stereotypes extend from @Component, which can also be used as a way to annotate a class for inclusion
into the dependency injection container. Spring advises developers to use the more specific stereotype annotations 
so that Spring can decorate the class with more stereotype specific features. For example, @Controller notifies Spring
MVC that the class has @RequestMapping annotations among other MVC specific annotations.

One side benefit is from a code readability perspective. @Service simply tells a better story to the developer that 
this is a Service class and Spring might do something special because it's a Service. Technically @Component would do
 the same job.

References:
* [Spring @Component, @Service, @Repository, @Controller Difference](http://javapapers.com/spring/spring-component-service-repository-controller-difference/)

### DO
Use @Service when the class is a service!

```
@Service
class SomeService {
    boolean isSome(Object someObject) {
        if (someObject instanceof Some) {
            return true
        }
    }
}
```

### DON'T DO
Don't use @Component when the class is a service! Use the appropriate stereotype annotation if there is one that
fits the class you are creating. 

```
@Component
class SomeService {
    boolean isSome(Object someObject) {
        if (someObject instanceof Some) {
            return true
        }
    }
}
```
## Spring MVC: Store all business logic in a service layer
Spring MVC follows the Model-View-Controller pattern, which governs controlling in bound user requests and passing
the response data to a view of some sort. The hub of the MVC pattern is the Controller, which acts as a switchboard
for inbound requests and outbound responses. 

RULE: Under NO circumstances should any type of business logic or special logic beyond controlling the inbound request
and outbound response be contained within a controller class. 

ALL logic must be embedded within the Service layer so
that the view and "switchboard" controller are single focused and separated from the inner workings of the app. 
Separating these areas of concern will allow for testing the controllers for their 'switchboard' features without having
test unrelated work. Also, this rule will ensure that controller classes stay SMALL and focused only on request and
response handling. Finally, when the day comes to transform the view from JSP pages to JSON, then the business logic
wont need to be rewritten!

While this topic might not be related specifically to the Spring Framework, it's important enough to repeat it here to
 avoid abusing the Spring MVC Controllers. 
  
### DO
Make a single call to a Service to handle everything necessary for getting the data that the controller should
pass back to the response. Keep the methods as small as possible and ONLY focused on request/response handling. 

```
@RestController
@RequestMapping(['/api/v1/somes'])
class SomeController {
    private final SomeService someService
    
    @Autowired
    SomeController(SomeService someService) {
        this.someService = someService
    }

    @GetMapping
    ResponseEntity list() {
        Iterable<Some> someList = someService.listAllSorted()
        return new ResponseEntity(someList, HttpStatus.OK)
    }
    
    @PostMapping
    ResponseEntity create(@RequestBody Some some) {
        Some savedSome = someService.save(some)
        return new ResponseEntity(savedSome, HttpStatus.OK)
    }
```

### DON'T DO
Don't put any kind of logic in a controller other than handling the request and response! The following
example is BAD! Don't do!!

```
@RestController
@RequestMapping(['/api/v1/somes'])
class SomeController {
    private final SomeService someService
    private final OtherService otherService

    @Autowired
    SomeController(SomeService someService, OtherService otherService) {
        this.someService = someService
        this.otherService = otherService
    }

    @GetMapping
    ResponseEntity list() {
        Iterable<Some> someList = someService.listAll()
        Iterable<Some> sortedSomeList = otherService.sort(someList)
        return new ResponseEntity(sortedSomeList, HttpStatus.OK)
    }
    
    @PostMapping
    ResponseEntity create(@RequestBody Some some) {
        Some newSome = new Some()
        newSome.name = some.name
        newSome.description = some.description
        if (some.isLarge()) {
            newSome.setLarge(true)
        }
        Some savedSome = someService.save(some)
        otherService.notifyAll(savedSome)
        otherService.email(savedSome)
        return new ResponseEntity(savedSome, HttpStatus.OK)
    }
```