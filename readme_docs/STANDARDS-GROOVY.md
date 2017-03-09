## Groovy Style guide
Apache Groovy is a dynamic language built on the JVM platform. Groovy is an optionally typed language that can compile 
native Java, Groovy syntax, or a mix of both. Groovy was created as a way to quickly write Java code without much of the
excessive verbosity of Java, and to layer on dynamic language features on top of the statically types Java language. 

Read more about Groovy at [http://groovy-lang.org](http://groovy-lang.org)

This Groovy style guide is a modification of the [default Groovy style guide](http://groovy-lang.org/style-guide.html). Unlike the default style guide, we require strict typing for more self-documenting code and better IDE auto-complete. We've also stripped out basic language information, as that is not related to code style, but just features of the language.

## Table of Contents
* [Semicolons](#semicolons)
* [Function, Class, and Variable declarations](#function-class-and-variable-declarations)
  * [If your function returns a value, use the return keyword](#if-your-function-returns-a-value-use-the-return-keyword)
  * [Explicitly type function parameters and return value](#explicitly-type-function-parameters-and-return-value)
  * [Explicitly type variables](#explicitly-type-variables)
  * [Avoid redundant `def` when declaring variables](#avoid-redundant-def-when-declaring-variables)
  * [Only use visibility modifiers for non-public methods](#only-use-visibility-modifiers-for-non-public-methods)
  * [Use parentheses when calling functions](#use-parentheses-when-calling-functions)
  * [Don't use `.class`](#dont-use--class)
  * [No visibility modifiers for class properties](#no-visibility-modifiers-for-class-properties)
  * [Initializing beans with named parameters using the default constructor](#initializing-beans-with-named-parameters-using-the-default-constructor)
  * [Use `with()` when setting multiple properties on the same bean](#use-with-when-setting-multiple-properties-on-the-same-bean)
* [Strings](#strings)
  * [Use GString for string interpolation](#use-gstring-for-string-interpolation)
  * [Omit curly braces if you're just accessing a variable](#omit-curly-braces-if-youre-just-accessing-a-variable)
  * [Use triple quotes for multi-line strings](#use-triple-quotes-for-multi-line-strings)
  * [Use "slashy" string notation for regular expressions](#use-slashy-string-notation-for-regular-expressions)
  * [Use single quotes for string constants](#use-single-quotes-for-string-constants)
* [Data structures](#data-structures)
  * [Use Groovy's native syntax over Java's](#use-groovys-native-syntax-over-Java's)
* [Truthiness and null checking](#truthiness-and-null-checking)
  * [Use Groovy truthiness to evaluate if something is null, zero, or empty](#use-groovy-truthiness-to-evaluate-if-something-is-null-zero-or-empty)
  * [Use `==` not `.equals()`](#use-not-equals)
  * [Use safe graph navigation](#use-safe-graph-navigation)
  * [Use the Elvis operator for default values](#use-the-elvis-operator-for-default-values)

## Semicolons

Omit semicolons in Groovy
> Why? Semicolons are not required in Groovy, and it's more idiomatic to omit them.

## Function, Class, and Variable declarations

#### If your function returns a value, use the return keyword

> Why? While Groovy does not require the use of the `return`, instead implicitly returning the final line in a function, it can easily lead to confusion as to whether a function modifies an object or returns a new value.
>
> Requiring it makes it easy to see what the function returns, if anything.

```
// Avoid
String toString() {
  "a server"
}

// Prefer
String toString() {
  return "a server"
}

```

```
// Avoid
def props(m1) {
    m2 = m1.findAll { k, v -> v % 2 == 0 }
    m2.c = 3
    m2
}

// Prefer
def props(m1) {
    m2 = m1.findAll { k, v -> v % 2 == 0 }
    m2.c = 3
    return m2
}
```

```
// Avoid
def foo(n) {
    if(n == 1) {
        "Roshan"
    } else {
        "Dawrani"
    }
}

// Prefer
String foo(int n) {
    if(n == 1) {
        return "Roshan"
    } else {
        return "Dawrani"
    }
}
```

#### Explicitly type function parameters and return value

> Why? Omitting types can lead to confusing bugs, misuse of functions, and prevents your IDE from being able to use auto-complete.

```
// Avoid
def foo(n) {
    if(n == 1) {
        "Roshan"
    } else {
        "Dawrani"
    }
}

// Prefer
String foo(int n) {
    if(n == 1) {
        return "Roshan"
    } else {
        return "Dawrani"
    }
}
```

#### Explicitly type variables

> Why? Using `def` everywhere is not self documenting, it leads to confusion about what the variable is, and breaks IDE auto-completion.

```
// Avoid
def name = "Guillaume"

// Prefer
String name = "Guillaume"
```

#### Avoid redundant `def` when declaring variables

> Why? The def keyword is for generic typing, nothing else. It is not used to declare a variable of a known type

```
// Avoid
def String name = "Guillaume"

// Prefer
String name = "Guillaume"
```

```
// Avoid
class MyClass {
  def MyClass() {}
}

// Prefer
class MyClass {
  MyClass() {}
}
```

#### Only use visibility modifiers for non-public methods

> Why? Groovy considers classes and methods `public` by default.

```
// Avoid
public class Server {
    public String toString() { return "a server" }
}

// Prefer
class Server {
    String toString() { "a server" }
}
```

Note: For package scoped visibility use the `@PackageScope` annotation
```
class Server {
    @PackageScope Cluster cluster
}
```

#### Use parentheses when calling functions

> Why? Using parentheses makes it obvious you are calling a function and assists the IDE in showing you the parameters you can pass. It also keeps code consistent, as even in Groovy there are times when parentheses are required.

```
// Avoid
println "Hello"
method a, b

// Prefer
println("Hello")
method(a, b)
```


```
def foo(n) { return n }
int bar() { return 1 }

// These won't work
println foo 1
def m = bar

// These will
println(foo(1))
int m = bar()
```

#### Don't use `.class`

> Why? The `.class` suffix is not needed in Groovy.

```
// Avoid
connection.doPost(BASE_URI + "/modify.hqu", params, ResourcesResponse.class)

// Prefer
connection.doPost("${BASE_URI}/modify.hqu", params, ResourcesResponse)
```

#### No visibility modifiers for class properties

> Why? Unlike methods which are public by default, properties are private by default, but getters and setters are automatically generated by Groovy.

Note: Only declare getters and setters if you wish to perform additional logic when getting or setting.

```
// Avoid
class Person {
    private String name
    String getName() { return name }
    void setName(String name) { this.name = name }
}

// Prefer
class Person {
    String name
}
```


#### Initializing beans with named parameters using the default constructor

Note: If you pass more than 2 or 3 parameters, put each parameter on a new line

```
// Avoid
Server server = new Server(name: "Obelix", cluster: aCluster, size: "Large", host: "Amazon", ipAddress: "127.0.0.1", isEnabled: true)

// Better, but more code and still hard to read
Server server = new Server()
server.name = "Obelix"
server.cluster = aCluster
server.size = "Large"
server.host = "Amazon"
server.ipAddress = "127.0.0.1"
server.isEnabled = true

// Best, less code, easy to read
Server server = new Server(
    name: "Obelix",
    cluster: aCluster,
    size: "Large",
    host: "Amazon",
    ipAddress: "127.0.0.1",
    isEnabled: true
)
```

#### Use with() when setting multiple properties on the same bean

> Why? Using `with` provides the same easy to read syntax as using the default constructor, but can be used to set properties on an existing bean.

```
// Avoid
server.name = application.name
server.status = status
server.sessionCount = 3

// Prefer
server.with {
    name = application.name
    status = status
    sessionCount = 3
}
```

## Strings

#### Use GString for string interpolation

> Why? String interpolation is easier to read, especially when incorporating multiple variables into a string

```
// Avoid
throw new Exception("Unable to convert resource: " + resource)

// Prefer
throw new Exception("Unable to convert resource: $resource")
```

#### Omit curly braces if you're just accessing a variable

> Why? They aren't needed for simple variable accessing, and not having them is easier to read

```
// Avoid
throw new Exception("Unable to convert resource: ${resource}")

// Prefer
throw new Exception("Unable to convert resource: $resource")


int i = 3

// Curly braces required because we're performing an operation
def s1 = "i's value is: ${i + 10}"
```

#### Use triple quotes for multi-line strings

> Why? Using triple strings and placing actual new lines is easier to ready than \n

```
throw new PluginException("""Failed to execute command list-applications:
    The group with name ${parameterMap.groupname[0]}
    is not compatible group of type ${SERVER_TYPE_NAME)}""")
```

Note: You can also strip the indentation appearing on the left side of the multiline strings by calling `.stripIndent()`on that string.

#### Use "slashy" string notation for regular expressions

```
assert "foooo/baaaaar" ==~ /fo+\/ba+r/
```

Note: The advantage of the "slashy" notation is that you don't need to double escape backslashes, making working with regex a bit simpler.

#### Use single quotes for string constants

> Why? Simply a styling preference, keeps code consistent.


## Data structures

#### Use Groovy's native syntax over Java's

> Why? Groovy provides native syntax constructs for data structures like lists, maps, regex, or ranges of values that are simpler, more concise, and easier to read than Java's

```
// Examples below:

def list = [1, 4, 6, 9]

// by default, keys are Strings, no need to quote them
// you can wrap keys with () like [(variableStateAcronym): stateName] to insert a variable or object as a key.
def map = [CA: 'California', MI: 'Michigan']

def range = 10..20
def pattern = ~/fo*/

// equivalent to add()
list << 5

// call contains()
assert 4 in list
assert 5 in list
assert 15 in range

// subscript notation
assert list[1] == 4

// add a new key value pair
map << [WA: 'Washington']
// subscript notation
assert map['CA'] == 'California'
// property notation
assert map.WA == 'Washington'

// matches() strings against patterns
assert 'foo' ==~ pattern
```


## Truthiness and null checking

#### Use Groovy truthiness to evaluate if something is null, zero, or empty

> Why? All objects can be 'coerced' to a boolean value: everything that's `null`, `void`, equal to zero, or empty evaluates to `false`, and if not, evaluates to `true`.

```
// Avoid
if (name != null && name.length > 0) {}

// Prefer
if (name) {}
```

#### Use `==` not `.equals()`

> Why? Groovy's `==` also takes care of avoiding `NullPointerException`, independently of whether the left or right is `null` or not.

```
// Avoid
status != null && status.equals(ControlConstants.STATUS_COMPLETED)

// Prefer
status == ControlConstants.STATUS_COMPLETED
```


#### Use safe graph navigation

> Why? Writing individual null check is harder to read and takes more effort to write

```
// Avoid
if (order != null) {
    if (order.getCustomer() != null) {
        if (order.getCustomer().getAddress() != null) {
            println(order.getCustomer().getAddress());
        }
    }
}

// Prefer
println order?.customer?.address
```

#### Use the Elvis operator for default values

> Why? The Elvis operator is a special ternary operator shortcut, equivalent to overloading JavaScript's `||`, that lets you apply a default value with less code.


```
// Avoid
String result = name != null ? name : 'Unknown'

// Prefer
String result = name ?: 'Unknown'
```
