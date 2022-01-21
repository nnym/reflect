reflect is a Java library whereby I try to overcome the restrictions on reflection and make it concise.

It reduces the usual verbosity of reflection by providing concise methods for most reflective operations (and more).
**None of these methods (unlike every standard reflective method ever) declares checked exceptions.**

Supported Java versions are 17 and above.

I try to keep this library mostly stable; although occasionally (with prior notice or otherwise) I may break things.

reflect depends on [my fork](https://git.auoeke.net/unsafe) of [gudenau/java-unsafe](https://github.com/gudenau/java-unsafe).
It is hosted at https://maven.auoeke.net as `net.auoeke:reflect`.
```groovy
repositories {
    maven {url = "https://maven.auoeke.net"}
}

dependencies {
    implementation("net.auoeke:reflect:4.+")
}
```

Documentation is ~~very~~ scarce (500+ lines now) but I have taken to documenting my code recently.

## a brief summary of the classes
- `Accessor` reads, mutates and copies fields.
- `Classes` is a set of methods for working with classes (`defineClass`, `reinterpret` (runtime `reinterpret_cast`)).
- `ConstantPool` is a proxy for the JDK's internal `ConstantPool` that reads constant pools.
- `Constructors` finds constructors and instantiates classes.
- `EnumConstructor` adds enumeration constants with safety handling.
- `Fields` enumerates class fields directly and without slow `Field` copying, security checks and filters.
- `Flags` provides utilities for dealing with flags (including particularly Java flags).
- `Invoker` is a static proxy for `IMPL_LOOKUP` (which has unrestricted privileges) and contains various method handle utilities.
- `JavaLangAccess` provides partial access to the JDK's `JavaLangAccess`.
- `Methods` enumerates and finds class methods.
- `Modules` gets and opening modules.
- `Pointer` is a field reference that is similar to `Field` and for use with frequently accessed fields.
- `Reflect` contains useful state, provides instrumentation, clears reflection filters and does other things.
- `StackFrames` assists in getting stack frames and callers and supports `StackWalker` and the traditional `StackTraceElement[]`.
- `Types` deals with `Class`es in many ways.

Relevant tests are in [`ReflectTest`](test/net/auoeke/reflect/ReflectTest.java) and the [categorized test classes](test/test).

Some tests are irrelevant but I keep them anyway for now.
