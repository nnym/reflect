reflect is a Java library whose purpose is unfettering, condensing and streamlining reflection
by providing for most common reflective operations (and more) powerful methods
that don't obey access limitations or declare checked exceptions (unlike every standard reflective method ever).

See [the actions](https://github.com/nnym/reflect/actions/runs/4322646486) for Java distribution support information.

![](https://img.shields.io/static/v1?label=Java&message=17%2b&color=blue)
[![reflect is available from Central as net.auoeke:reflect.](
https://img.shields.io/maven-central/v/net.auoeke/reflect?label=net.auoeke:reflect%20@%20Central
)](https://central.sonatype.com/search?namespace=net.auoeke&q=reflect)
![](https://img.shields.io/endpoint?url=https://runkit.io/nnym/maven-size/branches/master/reflect)

```groovy
dependencies {
	implementation("net.auoeke:reflect:6.+")
}
```

### A brief summary

- `Accessor` reads, mutates and copies fields.
- `Classes` loads and deals with classes. It can also change object types like `reinterpret_cast` in C++.
- `ClassDefiner` defines classes.
- `ConstantPool` is a proxy for the JDK's internal `ConstantPool` that reads constant pools.
- `Constructors` finds constructors and instantiates classes.
- `EnumConstructor` adds enumeration constants with safety handling.
- `Fields` enumerates class fields directly and without `Field` copying, security checks and filters.
- `Flags` provides utilities for dealing with flags (including particularly Java flags).
- `Invoker` is a static proxy for `IMPL_LOOKUP` (which has unrestricted privileges) and contains other method handle utilities.
- `JavaLangAccess` provides partial access to the JDK's `JavaLangAccess`.
- `Methods` enumerates and finds class methods.
- `Modules` gets and opens modules.
- `Pointer` is a (primarily) field reference that is similar to `Field` but without access restrictions and for use with frequently accessed fields.
- `Reflect` provides `Instrumentation` access.
- `StackFrames` assists in getting stack frames and callers and supports `StackWalker` and the traditional `StackTraceElement[]`.
- `Types` deals with `Class`es in many ways including type conversion.

### Developing

Use [uncheck for IntelliJ IDEA](https://github.com/auoeke/uncheck#using-the-intellij-plugin) in order to suppress checked exception warnings.

Relevant tests are in the [main test classes](test/test); everything else is irrelevant.
