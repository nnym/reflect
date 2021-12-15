package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.gudenau.lib.unsafe.Unsafe;

import static net.auoeke.reflect.Reflect.run;
import static net.auoeke.reflect.Reflect.runNull;

@SuppressWarnings("unused")
public class Classes {
    public static final SecureClassLoader systemClassLoader = (SecureClassLoader) ClassLoader.getSystemClassLoader();

    public static final Class<?> ConstantPool = tryLoad("jdk.internal.reflect.ConstantPool", "sun.reflect.ConstantPool");
    public static final Class<?> JavaLangAccess = tryLoad("jdk.internal.access.JavaLangAccess", "jdk.internal.misc.JavaLangAccess", "sun.misc.JavaLangAccess");
    public static final Class<?> Reflection = tryLoad("jdk.internal.reflect.Reflection", "sun.reflect.Reflection");
    public static final Class<?> SharedSecrets = tryLoad("jdk.internal.access.SharedSecrets", "jdk.internal.misc.SharedSecrets", "sun.misc.SharedSecrets");
    public static final Class<?> URLClassPath = tryLoad("jdk.internal.loader.URLClassPath", "sun.misc.URLClassPath");

    public static final Object systemClassPath = classPath(systemClassLoader);

    public static final long classOffset;
    public static final int fieldOffset = (int) Unsafe.objectFieldOffset(Fields.get(Integer.class, "value"));

    public static final boolean x64 = switch (fieldOffset) {
        case 8 -> false;
        case 12, 16 -> true;
        default -> throw new Error("unsupported field offset %d; report to https://git.auoeke.net/reflect/issues.".formatted(fieldOffset));
    };

    private static final MethodHandle findLoadedClass = Invoker.findVirtual(ClassLoader.class, "findLoadedClass", Class.class, String.class);
    private static final MethodHandle URLClassPath$addURL = Invoker.findVirtual(URLClassPath, "addURL", void.class, URL.class);
    private static final MethodHandle URLClassPath$getURLs = Invoker.findVirtual(URLClassPath, "getURLs", URL[].class);
    private static final MethodHandle defineClass0 = Invoker.findVirtual(ClassLoader.class, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
    private static final MethodHandle defineClass1 = Invoker.findVirtual(ClassLoader.class, "defineClass", Class.class, String.class, ByteBuffer.class, ProtectionDomain.class);
    private static final MethodHandle defineClass2 = Invoker.findVirtual(SecureClassLoader.class, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, CodeSource.class);
    private static final MethodHandle defineClass3 = Invoker.findVirtual(SecureClassLoader.class, "defineClass", Class.class, String.class, ByteBuffer.class, CodeSource.class);

    /**
     Change the class of <b>{@code object}</b> to that represented by <b>{@code T}</b> such that <b>{@code to.getClass() == T}</b>.

     @param object the object whose class pointer to change.
     @param dummy  a dummy varargs parameter for reifying <b>{@code T}</b>.
     @param <T>    the desired new type.
     @return <b>{@code object}</b>.
     */
    @SafeVarargs
    public static <T> T reinterpret(Object object, T... dummy) {
        return (T) reinterpret(object, Unsafe.allocateInstance(dummy.getClass().getComponentType()));
    }

    /**
     Change the class of <b>{@code object}</b> to the class represented by <b>{@code klass}</b> such that <b>{@code object.getClass().getName().equals(klass)}</b>.

     @param object the object whose class pointer to change.
     @param klass  the name of class to set as <b>{@code object}</b>'s class.
     @param <T>    the desired new type.
     @return <b>{@code object}</b>.
     */
    public static <T> T reinterpret(Object object, String klass) {
        return reinterpret(object, (T) Unsafe.allocateInstance(load(Reflect.defaultClassLoader, false, klass)));
    }

    /**
     Change the class of <b>{@code object}</b> to <b>{@code klass}</b> such that <b>{@code object.getClass() == klass}</b>.

     @param object the object whose class pointer to change.
     @param klass  the class to set as <b>{@code object}</b>'s class.
     @param <T>    the desired new type.
     @return <b>{@code object}</b>.
     */
    public static <T> T reinterpret(Object object, Class<T> klass) {
        return reinterpret(object, Unsafe.allocateInstance(klass));
    }

    /**
     Change the class of <b>{@code to}</b> to that of <b>{@code from}</b> such that <b>{@code to.getClass() == from.getClass()}</b>.

     @param to   the object whose class pointer to change.
     @param from the object from which to get the class pointer.
     @param <T>  the desired new type.
     @return <b>{@code to}</b>.
     */
    public static <T> T reinterpret(Object to, T from) {
        Accessor.copyAddress(to, from, classOffset);

        return (T) to;
    }

    /**
     Change the class of <b>{@code object}</b> to the class represented by <b>{@code from}</b>.

     @param object       the object whose class pointer to change.
     @param classPointer the class pointer.
     @param <T>          a convenience type parameter for casting.
     @return <b>{@code to}</b>.
     */
    public static <T> T reinterpret(Object object, long classPointer) {
        Unsafe.putAddress(object, classOffset, classPointer);

        return (T) object;
    }

    /**
     Get the <b>{@code Klass*}</b> from a {@link Class}.<br>
     <b>{@code klass}</b> must not be abstract.

     @param clas a class.
     @return the Klass*.
     */
    public static long klass(Class<?> clas) {
        return klass(Unsafe.allocateInstance(clas));
    }

    /**
     Get the <b>{@code Klass*}</b> of an object.

     @param object the object.
     @return its <b>{@code Klass*}</b>.
     */
    public static long klass(Object object) {
        return Unsafe.getAddress(object, classOffset);
    }

    public static <T> T cast(Object object) {
        return (T) object;
    }

    public static <T> Class<T> findLoadedClass(ClassLoader loader, String klass) {
        return run(() -> (Class<T>) findLoadedClass.invokeExact(loader, klass));
    }

    public static URL[] urls(ClassLoader classLoader) {
        return urls(classPath(classLoader));
    }

    public static URL[] urls(Object classPath) {
        return run(() -> (URL[]) URLClassPath$getURLs.invoke(classPath));
    }

    public static void addSystemURL(URL... url) {
        addURL(systemClassPath, url);
    }

    public static void addSystemURL(URL url) {
        addURL(systemClassPath, url);
    }

    public static void addURL(ClassLoader classLoader, URL... urls) {
        var classPath = classPath(classLoader);

        for (var url : urls) {
            run(() -> URLClassPath$addURL.invoke(classPath, url));
        }
    }

    public static void addURL(ClassLoader classLoader, URL url) {
        run(() -> URLClassPath$addURL.invoke(classPath(classLoader), url));
    }

    public static void addURL(Object classPath, URL... urls) {
        for (var url : urls) {
            run(() -> URLClassPath$addURL.invoke(classPath, url));
        }
    }

    public static void addURL(Object classPath, URL url) {
        run(() -> URLClassPath$addURL.invoke(classPath, url));
    }

    public static Object classPath(ClassLoader classLoader) {
        return Accessor.getReference(classLoader, classPathField(classLoader.getClass()));
    }

    public static Field classPathField(Class<?> loaderClass) {
        return Fields.all(loaderClass).filter(field -> URLClassPath.isAssignableFrom(field.getType())).findAny().orElse(null);
    }

    public static void load(String... classes) {
        load(Reflect.defaultClassLoader, true, classes);
    }

    public static void load(boolean initialize, String... classes) {
        load(Reflect.defaultClassLoader, initialize, classes);
    }

    public static void load(ClassLoader loader, String... classes) {
        load(loader, true, classes);
    }

    public static void load(ClassLoader loader, boolean initialize, String... classes) {
        for (var klass : classes) {
            load(loader, initialize, klass);
        }
    }

    public static <T> Class<T> load(String name) {
        return load(Reflect.defaultClassLoader, true, name);
    }

    public static <T> Class<T> load(boolean initialize, String name) {
        return load(Reflect.defaultClassLoader, initialize, name);
    }

    public static <T> Class<T> load(ClassLoader loader, String name) {
        return load(loader, true, name);
    }

    public static <T> Class<T> load(ClassLoader loader, boolean initialize, String name) {
        return runNull(() -> (Class<T>) Class.forName(name, initialize, loader));
    }

    public static <T> Class<T> defineClass(ClassLoader classLoader, String name, byte[] bytecode, int offset, int length, ProtectionDomain protectionDomain) {
        return run(() -> (Class<T>) defineClass0.invokeExact(classLoader, name, bytecode, offset, length, protectionDomain));
    }

    public static <T> Class<T> defineClass(ClassLoader classLoader, String name, ByteBuffer bytecode, ProtectionDomain protectionDomain) {
        return run(() -> (Class<T>) defineClass1.invokeExact(classLoader, name, bytecode, protectionDomain));
    }

    public static <T> Class<T> defineClass(SecureClassLoader classLoader, String name, byte[] bytecode, int offset, int length, CodeSource codeSource) {
        return run(() -> (Class<T>) defineClass2.invokeExact(classLoader, name, bytecode, offset, length, codeSource));
    }

    public static <T> Class<T> defineClass(SecureClassLoader classLoader, String name, ByteBuffer bytecode, CodeSource codeSource) {
        return run(() -> (Class<T>) defineClass3.invokeExact(classLoader, name, bytecode, codeSource));
    }

    public static <T> Class<T> defineClass(ClassLoader classLoader, byte[] bytecode, int offset, int length) {
        return defineClass(classLoader, null, bytecode, offset, length, null);
    }

    public static <T> Class<T> defineClass(ClassLoader classLoader, String name, byte[] bytecode) {
        return defineClass(classLoader, name, bytecode, 0, bytecode.length);
    }

    public static <T> Class<T> defineClass(ClassLoader classLoader, String name, byte[] bytecode, int offset, int length) {
        return defineClass(classLoader, name, bytecode, offset, length, null);
    }

    public static <T> Class<T> defineClass(ClassLoader classLoader, String name, byte[] bytecode, ProtectionDomain protectionDomain) {
        return defineClass(classLoader, name, bytecode, 0, bytecode.length, protectionDomain);
    }

    public static <T> Class<T> defineClass(SecureClassLoader classLoader, String name, byte[] bytecode, CodeSource codeSource) {
        return defineClass(classLoader, name, bytecode, 0, bytecode.length, codeSource);
    }

    @SuppressWarnings("ConstantConditions")
    public static <T> Class<T> crossDefineClass(ClassLoader resourceLoader, ClassLoader classLoader, String name, ProtectionDomain protectionDomain) {
        return run(() -> defineClass(classLoader, name, Files.readAllBytes(Path.of(resourceLoader.getResource(name.replace('.', '/') + ".class").toURI())), protectionDomain));
    }

    public static <T> Class<T> crossDefineClass(ClassLoader resourceLoader, ClassLoader classLoader, String name) {
        return crossDefineClass(resourceLoader, classLoader, name, null);
    }

    @SuppressWarnings("ConstantConditions")
    public static <T> Class<T> defineBootstrapClass(ClassLoader resourceLoader, String name) {
        var url = resourceLoader.getResource(name.replace('.', '/') + ".class");
        var bytecode = run(() -> Files.readAllBytes(Path.of(url.toURI())));

        return Unsafe.defineClass(name, bytecode, 0, bytecode.length, null, new ProtectionDomain(new CodeSource(url, (CodeSigner[]) null), null, null, null));
    }

    @SuppressWarnings("ConstantConditions")
    public static <T> Class<T> defineSystemClass(ClassLoader resourceLoader, String name) {
        var url = resourceLoader.getResource(name.replace('.', '/') + ".class");

        return defineClass(systemClassLoader, name, run(() -> Files.readAllBytes(Path.of(url.toURI()))), new CodeSource(url, (CodeSigner[]) null));
    }

    public static List<Class<?>> supertypes(Class<?> type) {
        var supertypes = new ArrayList<>(Arrays.asList(type.getInterfaces()));
        type = type.getSuperclass();

        if (type != null) {
            supertypes.add(type);
        }

        return supertypes;
    }

    public static List<Type> genericSupertypes(Class<?> type) {
        var supertypes = new ArrayList<>(Arrays.asList(type.getGenericInterfaces()));
        var superclass = type.getGenericSuperclass();

        if (superclass != null) {
            supertypes.add(superclass);
        }

        return supertypes;
    }

    private static Class<?> tryLoad(String... classes) {
        return Stream.of(classes).map(Classes::load).filter(Objects::nonNull).findAny().orElse(null);
    }

    static {
        var byteArray = new byte[0];
        var shortArray = new short[0];

        long offset = 0;

        while (Unsafe.getInt(byteArray, offset) == Unsafe.getInt(shortArray, offset)) {
            offset += 4;
        }

        classOffset = offset;
    }
}
