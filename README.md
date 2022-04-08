reflect is a Java library whereby I try to overcome the restrictions on reflection and make it concise.

It reduces the usual verbosity of reflection by providing concise methods for most reflective operations (and more).
**None of these methods (unlike every standard reflective method ever) declares checked exceptions.**

Supported Java versions are 17 and above.

I try to keep this library mostly stable; although occasionally (with prior notice or otherwise) I may break things.

reflect depends on [unsafe](https://github.com/auoeke/unsafe)
and is hosted at https://maven.auoeke.net as `net.auoeke:reflect`.
```groovy
repositories {
    maven {url = "https://maven.auoeke.net"}
}

dependencies {
    implementation("net.auoeke:reflect:5.+")
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
- `Reflect` contains useful state, provides instrumentation, clears reflection filters and does other things.
- `StackFrames` assists in getting stack frames and callers and supports `StackWalker` and the traditional `StackTraceElement[]`.
- `Types` deals with `Class`es in many ways including type conversion.

### Developing
[uncheck for IntelliJ IDEA](https://github.com/auoeke/uncheck#using-the-intellij-plugin) is recommended in order to suppress checked exception warnings.

Relevant tests are in [`ReflectTest`](test/reflect/ReflectTest.java) and the [categorized test classes](test/test).
Other tests are irrelevant but I keep them anyway for now.
