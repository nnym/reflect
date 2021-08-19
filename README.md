reflect is a Java reflection library that tries to overcome the restrictions on reflection
and make it concise while achieving higher performance by utilizing `Unsafe`.

Although reflect can disable the security manager, doing so is discouraged because [the security manager is deprecated for removal](https://openjdk.java.net/jeps/411).

It reduces the usual verbosity of reflection by providing concise methods for most reflective operations (and more).
**None of these methods declares thrown exceptions.**

Supported Java versions are 16 and above.

I try to keep this library mostly stable, although occasionally (with prior notice) I may break things.

reflect is hosted on Artifactory. It depends on [my fork](https://git.auoeke.net/unsafe) of [gudenau/java-unsafe](https://github.com/gudenau/java-unsafe).
```groovy
repositories {
    maven {url = "https://auoeke.jfrog.io/artifactory/maven"}
}

dependencies {
    /* Use "latest.integration" for the latest version or pick from
    https://auoeke.jfrog.io/artifactory/maven/net/gudenau/lib/unsafe and
    https://auoeke.jfrog.io/artifactory/maven/net/auoeke/reflect. */
    api("net.gudenau.lib:unsafe:latest.integration")
    api("net.auoeke:reflect:latest.integration")
}
```

## a brief summary of classes
- `Accessor`: a collection of methods for reading and mutating fields
- `Classes`: a set of methods for working with classes (`defineClass`, change class of object)
- `ConstantPool`: a proxy for the JDK's internal `ConstantPool` that reads constant pools
- `Constructors`: a collection of utilities for finding and invoking constructors
- `EnumConstructor`: a class that constructs enum constants and adds them to the `values` array and a few other places;
  it can be used through static methods or directly as an object (more performant)
- `Fields`: a set of utilities for enumerating class fields directly: without slow `Field` copying, security checks or filters
- `Invoker`: a static proxy for `IMPL_LOOKUP`; which should be able to access everything;
  its methods do not declare thrown exceptions
- `JavaLangAccess`: a partial proxy for `JavaLangAccess`
- `Methods`: a collection of utilities for listing class methods and dealing with them
- `Pointer`: a field reference that is similar to `Field` and does not force exceptions to be handled;
  intended for use with frequently accessed fields
- `Reflect`: a container for useful state and a method that disables the security manager
- `StackFrames`: a set of utility methods that assist in getting stack frames and callers
  with support for `StackWalker` and the traditional `StackTraceElement[]`
- `Types`: a collection of methods for dealing with `Class`es; mainly conversion between primitive and wrapper types and array types

In addition to tests, the `test` subproject contains various experiments that I perform
to test the limits of reflection and decide if something should be added to reflect.
Some of them are irrelevant but I keep them anyway.
