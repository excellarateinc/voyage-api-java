## Style guide

This is a modification of the [default Groovy style guide](http://groovy-lang.org/style-guide.html). Unlike the default style guide, we require strict typing for more self-documenting code and better IDE auto-complete.

## Table of Contents
* [Semicolons](#semicolons)
* [Function, Class, and Variable declarations](#function-class-and-variable-declarations)
  * [If your function returns a value, use the return keyword](#if-your-function-returns-a-value-use-the-return-keyword)
  * [Explicitly type function parameters and return value](#explicitly-type-function-parameters-and-return-value)
  * [Explicitly type variables]()

## Semicolons

Omit semicolons in Groovy
> Why? Semicolons are not required in Groovy, and it's more idiomatic to omit them.

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

> Why? Using `def` everywhere is not self documents, it leads to confusion about what the variable is, and breaks IDE auto-completion.

```
// Avoid
def name = "Guillaume"

// Prefer
String name = "Guillaume"
```

#### Avoid redundant `def` when declaring variables

> Why? The def keyword is for implicit typing, nothing else. It is not used to declare a variable of a known type

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

> Why? By default, Groovy considers classes and methods `public`. So you don't have to use the `public` modifier.


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

For package scoped visibility use the `@PackageScope` annotation
```
class Server {
    @PackageScope Cluster cluster
}
```

[](http://groovy-lang.org/style-guide.html#_omitting_parentheses)5\. Omitting parentheses
-----------------------------------------------------------------------------------------

Groovy allows you to omit the parentheses for top-level expressions, like with the `println` command:

```
println "Hello"
method a, b
```

vs:

```
println("Hello")
method(a, b)
```

When a closure is the last parameter of a method call, like when using Groovy's `each{}` iteration mechanism, you can put the closure outside the closing parentheses, and even omit the parentheses:

```
list.each( { println it } )
list.each(){ println it }
list.each  { println it }
```

Always prefer the third form, which is more natural, as an empty pair of parentheses is just useless syntactical noise!

In some cases parentheses are required, such as when making nested method calls or when calling a method without parameters.

```
def foo(n) { n }
def bar() { 1 }

println foo 1 // won't work
def m = bar   // won't work
```

[](http://groovy-lang.org/style-guide.html#_classes_as_first_class_citizens)6\. Classes as first-class citizens
---------------------------------------------------------------------------------------------------------------

The `.class` suffix is not needed in Groovy, a bit like in Java's `instanceof`.

For example:

```
connection.doPost(BASE_URI + "/modify.hqu", params, ResourcesResponse.class)
```

Using GStrings we're going to cover below, and using first class citizens:

```
connection.doPost("${BASE_URI}/modify.hqu", params, ResourcesResponse)
```

[](http://groovy-lang.org/style-guide.html#_getters_and_setters)7\. Getters and Setters

When writing your beans in Groovy, often called POGOs (Plain Old Groovy Objects), you don't have to create the field and getter / setter yourself, but let the Groovy compiler do it for you.

So instead of:

```
class Person {
    private String name
    String getName() { return name }
    void setName(String name) { this.name = name }
}
```

You can simply write:

```
class Person {
    String name
}
```

As you can see, a free standing 'field' without modifier visibility actually makes the Groovy compiler to generate a private field and a getter and setter for you.

When using such POGOs from Java, the getter and setter are indeed there, and can be used as usual, of course.

Although the compiler creates the usual getter/setter logic, if you wish to do anything additional or different in those getters/setters, you're free to still provide them, and the compiler will use your logic, instead of the default generated one.

[](http://groovy-lang.org/style-guide.html#_initializing_beans_with_named_parameters_and_the_default_constructor)8\. Initializing beans with named parameters and the default constructor
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

With a bean like:

```
class Server {
    String name
    Cluster cluster
}
```

Instead of setting each setter in subsequent statements as follows:

```
def server = new Server()
server.name = "Obelix"
server.cluster = aCluster
```

You can use named parameters with the default constructor (first the constructor is called, then the setters are called in the sequence in which they are specified in the map):

```
def server = new Server(name: "Obelix", cluster: aCluster)
```

[](http://groovy-lang.org/style-guide.html#_using_with_for_repeated_operations_on_the_same_bean)9\. Using with() for repeated operations on the same bean
---------------------------------------------------------------------------------------------------------------------------------------------------------

Named-parameters with the default constructor is interesting when creating new instances, but what if you are updating an instance that was given to you, do you have to repeat the 'server' prefix again and again? No, thanks to the with() method that Groovy adds on all objects of any kind:

```
server.name = application.name
server.status = status
server.sessionCount = 3
server.start()
server.stop()
```

vs:

```
server.with {
    name = application.name
    status = status
    sessionCount = 3
    start()
    stop()
}
```

[](http://groovy-lang.org/style-guide.html#_equals_and_code_code)10\. Equals and `==`
-------------------------------------------------------------------------------------

Java's `==` is actually Groovy's `is()` method, and Groovy's `==` is a clever `equals()`!

To compare the references of objects, instead of `==`, you should use `a.is(b)`.

But to do the usual `equals()` comparison, you should prefer Groovy's `==`, as it also takes care of avoiding `NullPointerException`, independently of whether the left or right is `null` or not.

Instead of:

```
status != null && status.equals(ControlConstants.STATUS_COMPLETED)
```

Do:

```
status == ControlConstants.STATUS_COMPLETED
```

[](http://groovy-lang.org/style-guide.html#_gstrings_interpolation_multiline)11\. GStrings (interpolation, multiline)
---------------------------------------------------------------------------------------------------------------------

We often use string and variable concatenation in Java, with many opening `/` closing of double quotes, plus signs, and `\n` characters for newlines. With interpolated strings (called GStrings), such strings look better and are less painful to type:

```
throw new Exception("Unable to convert resource: " + resource)
```

vs:

```
throw new Exception("Unable to convert resource: ${resource}")
```

Inside the curly braces, you can put any kind of expression, not just variables. For simple variables, or `variable.property`, you can even drop the curly braces:

```
throw new Exception("Unable to convert resource: $resource")
```

You can even lazily evaluate those expressions using a closure notation with `${-> resource }`. When the GString will be coerced to a String, it'll evaluate the closure and get the `toString()` representation of the return value.

Example:

```
int i = 3

def s1 = "i's value is: ${i}"
def s2 = "i's value is: ${-> i}"

i++

assert s1 == "i's value is: 3" // eagerly evaluated, takes the value on creation
assert s2 == "i's value is: 4" // lazily evaluated, takes the new value into account
```

When strings and their concatenated expression are long in Java:

```
throw new PluginException("Failed to execute command list-applications:" +
    " The group with name " +
    parameterMap.groupname[0] +
    " is not compatible group of type " +
    SERVER_TYPE_NAME)
```

You can use the `\` continuation character (this is not a multiline string):

```
throw new PluginException("Failed to execute command list-applications:\
The group with name ${parameterMap.groupname[0]}\
is not compatible group of type ${SERVER_TYPE_NAME}")
```

Or using multiline strings with triple quotes:

```
throw new PluginException("""Failed to execute command list-applications:
    The group with name ${parameterMap.groupname[0]}
    is not compatible group of type ${SERVER_TYPE_NAME)}""")
```

You can also strip the indentation appearing on the left side of the multiline strings by calling `.stripIndent()`on that string.

Also note the difference between single quotes and double quotes in Groovy: single quotes always create Java Strings, without interpolation of variables, whereas double quotes either create Java Strings or GStrings when interpolated variables are present.

For multiline strings, you can triple the quotes: i.e. triple double quotes for GStrings and triple single quotes for mere Strings.

If you need to write regular expression patterns, you should use the "slashy" string notation:

```
assert "foooo/baaaaar" ==~ /fo+\/ba+r/
```

The advantage of the "slashy" notation is that you don't need to double escape backslashes, making working with regex a bit simpler.

Last but not least, prefer using single quoted strings when you need string constants, and use double quoted strings when you are explicitly relying on string interpolation.

[](http://groovy-lang.org/style-guide.html#_native_syntax_for_data_structures)12\. Native syntax for data structures
--------------------------------------------------------------------------------------------------------------------

Groovy provides native syntax constructs for data structures like lists, maps, regex, or ranges of values. Make sure to leverage them in your Groovy programs.

Here are some examples of those native constructs:

```
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

[](http://groovy-lang.org/style-guide.html#_the_groovy_development_kit)13\. The Groovy Development Kit
------------------------------------------------------------------------------------------------------

Continuing on the data structures, when you need to iterate over collections, Groovy provides various additional methods, decorating Java's core data structures, like `each{}`, `find{}`, `findAll{}`, `every{}`, `collect{}`, `inject{}`. These methods add a functional flavor to the programming language and help working with complex algorithms more easily. Lots of new methods are applied to various types, through decoration, thanks to the dynamic nature of the language. You can find lots of very useful methods on String, Files, Streams, Collections, and much more:

<http://beta.groovy-lang.org/gdk.html>

[](http://groovy-lang.org/style-guide.html#_the_power_of_switch)14\. The power of switch
----------------------------------------------------------------------------------------

Groovy's `switch` is much more powerful than in C-ish languages which usually only accept primitives and assimilated. Groovy's `switch` accepts pretty much any kind of type.

```
def x = 1.23
def result = ""
switch (x) {
    case "foo": result = "found foo"
    // lets fall through
    case "bar": result += "bar"
    case [4, 5, 6, 'inList']:
        result = "list"
        break
    case 12..30:
        result = "range"
        break
    case Integer:
        result = "integer"
        break
    case Number:
        result = "number"
        break
    case { it > 3 }:
        result = "number > 3"
        break
    default: result = "default"
}
assert result == "number"
```

And more generally, types with an `isCase()` method can also decide whether a value corresponds with a case

[](http://groovy-lang.org/style-guide.html#_import_aliasing)15\. Import aliasing
--------------------------------------------------------------------------------

In Java, when using two classes of the same name but from different packages, like `java.util.List` and `java.awt.List`, you can import one class, but have to use a fully-qualified name for the other.

Also sometimes, in your code, multiple usages of a long class name, can increase verbosity and reduce clarify of the code.

To improve such situations, Groovy features import aliasing:

```
import java.util.List as UtilList
import java.awt.List as AwtList
import javax.swing.WindowConstants as WC

UtilList list1 = [WC.EXIT_ON_CLOSE]
assert list1.size() instanceof Integer
def list2 = new AwtList()
assert list2.size() instanceof java.awt.Dimension
```

You can also use aliasing when importing methods statically:

```
import static java.lang.Math.abs as mabs
assert mabs(-4) == 4
```

[](http://groovy-lang.org/style-guide.html#_groovy_truth)16\. Groovy Truth
--------------------------------------------------------------------------

All objects can be 'coerced' to a boolean value: everything that's `null`, `void`, equal to zero, or empty evaluates to `false`, and if not, evaluates to `true`.

So instead of writing:

```
if (name != null && name.length > 0) {}
```

You can just do:

```
if (name) {}
```

Same thing for collections, etc.

Thus, you can use some shortcuts in things like `while()`, `if()`, the ternary operator, the Elvis operator (see below), etc.

It's even possible to customize the Groovy Truth, by adding an boolean `asBoolean()` method to your classes!

[](http://groovy-lang.org/style-guide.html#_safe_graph_navigation)17\. Safe graph navigation
--------------------------------------------------------------------------------------------

Groovy supports a variant of the `.` operator to safely navigate an object graph.

In Java, when you're interested in a node deep in the graph and need to check for `null`, you often end up writing complex `if`, or nested `if` statements like this:

```
if (order != null) {
    if (order.getCustomer() != null) {
        if (order.getCustomer().getAddress() != null) {
            System.out.println(order.getCustomer().getAddress());
        }
    }
}
```

With `?.` safe dereference operator, you can simplify such code with:

```
println order?.customer?.address
```

Nulls are checked throughout the call chain and no `NullPointerException` will be thrown if any element is `null`, and the resulting value will be null if something's `null`.

[](http://groovy-lang.org/style-guide.html#_assert)18\. Assert
--------------------------------------------------------------

To check your parameters, your return values, and more, you can use the `assert` statement.

Contrary to Java's `assert`, `assert`s don't need to be activated to be working, so `assert`s are always checked.

```
def check(String name) {
    // name non-null and non-empty according to Groovy Truth
    assert name
    // safe navigation + Groovy Truth to check
    assert name?.size() > 3
}
```

You'll also notice the nice output that Groovy's "Power Assert" statement provides, with a graph view of the various values of each sub-expressions being asserted.

[](http://groovy-lang.org/style-guide.html#_elvis_operator_for_default_values)19\. Elvis operator for default values
--------------------------------------------------------------------------------------------------------------------

The Elvis operator is a special ternary operator shortcut which is handy to use for default values.

We often have to write code like:

```
def result = name != null ? name : "Unknown"
```

Thanks to Groovy Truth, the `null` check can be simplified to just 'name'.

And to go even further, since you return 'name' anyway, instead of repeating name twice in this ternary expression, we can somehow remove what's in between the question mark and colon, by using the Elvis operator, so that the above becomes:

```
def result = name ?: "Unknown"
```

[](http://groovy-lang.org/style-guide.html#_catch_any_exception)20\. Catch any exception
----------------------------------------------------------------------------------------

If you don't really care about the type of the exception which is thrown inside your `try` block, you can simply catch any of them and simply omit the type of the caught exception. So instead of catching the exceptions like in:

```
try {
    // ...
} catch (Exception t) {
    // something bad happens
}
```

Then catch anything ('any' or 'all', or whatever makes you think it's anything):

```
try {
    // ...
} catch (any) {
    // something bad happens
}
```

|  | Note that it's catching all Exceptions, not `Throwable`s. If you need to really catch "everything", you'll have to be explicit and say you want to catch `Throwable`s. |

[](http://groovy-lang.org/style-guide.html#_optional_typing_advice)21\. Optional typing advice
----------------------------------------------------------------------------------------------

I'll finish on some words on when and how to use optional typing. Groovy lets you decide whether you use explicit strong typing, or when you use `def`.

I've got a rather simple rule of thumb: whenever the code you're writing is going to be used by others as a public API, you should always favor the use of strong typing, it helps making the contract stronger, avoids possible passed arguments type mistakes, gives better documentation, and also helps the IDE with code completion. Whenever the code is for your use only, like private methods, or when the IDE can easily infer the type, then you're more free to decide when to type or not.