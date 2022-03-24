package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.gudenau.lib.unsafe.Unsafe;

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
    public static final int fieldOffset = (int) Fields.offset(Fields.of(Integer.class, "value"));

    public static final boolean x64 = switch (fieldOffset) {
        case 8 -> false;
        case 12, 16 -> true;
        default -> throw new Error("unsupported field offset %d; report to https://git.auoeke.net/reflect/issues.".formatted(fieldOffset));
    };

    private static final MethodHandle findLoadedClass = Invoker.findVirtual(ClassLoader.class, "findLoadedClass", Class.class, String.class);
    private static final MethodHandle addURL = Invoker.findVirtual(URLClassPath, "addURL", void.class, URL.class);
    private static final MethodHandle getURLs = Invoker.findVirtual(URLClassPath, "getURLs", URL[].class);

    /**
     Change the type of an object. The target type should not be abstract or bigger than the object's type.

     @param object the object whose type to change
     @param <T> the target type
     @return {@code object}
     */
    public static <T> T reinterpret(Object object, T... dummy) {
        return (T) reinterpret(object, Unsafe.allocateInstance(dummy.getClass().getComponentType()));
    }

    /**
     Change the type of an object. The target type should not be abstract or bigger than the object's type.

     @param object the object whose type to change
     @param type the target type
     @param <T> the target type
     @return {@code object}
     */
    public static <T> T reinterpret(Object object, Class<T> type) {
        return reinterpret(object, Unsafe.allocateInstance(type));
    }

    /**
     Change the type of an object. The target type should not be bigger than the object's type.

     @param to the object whose type to change
     @param from an instance of the target type
     @param <T> the target type type
     @return {@code to}
     */
    public static <T> T reinterpret(Object to, T from) {
        Accessor.copyAddress(to, from, classOffset);

        return (T) to;
    }

    /**
     Change the type of an object. The target type should not be bigger than the object's type.

     @param object the object whose type to change
     @param klass the {@linkplain #klass Klass*} of the target type
     @param <T> the target type
     @return {@code object}
     */
    public static <T> T reinterpret(Object object, long klass) {
        Unsafe.putAddress(object, classOffset, klass);

        return (T) object;
    }

    /**
     Get a non-abstract type's {@code Klass*}.

     @param type a type
     @return the type's {@code Klass*}
     */
    public static long klass(Class<?> type) {
        return klass(Unsafe.allocateInstance(type));
    }

    /**
     Get the {@code Klass*} of an object.

     @param object an object
     @return the object's type's {@code Klass*}
     */
    public static long klass(Object object) {
        return Unsafe.getAddress(object, classOffset);
    }

    public static <T> T cast(Object object) {
        return (T) object;
    }

    public static <T> Class<T> findLoadedClass(ClassLoader loader, String klass) {
        return (Class<T>) findLoadedClass.invokeExact(loader, klass);
    }

    public static URL[] urls(ClassLoader classLoader) {
        return urls(classPath(classLoader));
    }

    public static URL[] urls(Object classPath) {
        return (URL[]) getURLs.invoke(classPath);
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
            addURL.invoke(classPath, url);
        }
    }

    public static void addURL(ClassLoader classLoader, URL url) {
        addURL.invoke(classPath(classLoader), url);
    }

    public static void addURL(Object classPath, URL... urls) {
        for (var url : urls) {
            addURL.invoke(classPath, url);
        }
    }

    public static void addURL(Object classPath, URL url) {
        addURL.invoke(classPath, url);
    }

    public static Object classPath(ClassLoader classLoader) {
        return Accessor.getReference(classLoader, classPathField(classLoader.getClass()));
    }

    public static Field classPathField(Class<?> loaderClass) {
        return Fields.all(loaderClass).filter(field -> URLClassPath.isAssignableFrom(field.getType())).findAny().orElse(null);
    }

    /**
     Attempt to load a class by name by a provided class loader and optionally ensure that it is initialized.

     @param loader the class loader whereby to load the class
     @param initialize whether to initialize the class if not yet initialized
     @param name the name of the class
     @param <T> the class
     @return a reference to the class if it was loaded successfully or else {@code null}
     */
    public static <T> Class<T> load(ClassLoader loader, boolean initialize, String name) {
        return Reflect.runNull(() -> (Class<T>) Class.forName(name, initialize, loader));
    }

    /**
     Attempt to load a class by name and ensure that it is initialized.
     The class is loaded by the loader of the caller's class.

     @param name a class name
     @param <T> the class
     @return a reference to the class if it can be loaded or else {@code null}
     */
    public static <T> Class<T> load(String name) {
        return load(StackFrames.caller().getClassLoader(), true, name);
    }

    /**
     Attempt to load and a class by name and optionally ensure that it is initialized.
     The class is loaded by the loader of the caller's class.

     @param initialize whether to initialize the class if uninitialized
     @param name a class name
     @param <T> the class
     @return a reference to the class if it can be loaded or else {@code null}
     */
    public static <T> Class<T> load(boolean initialize, String name) {
        return load(StackFrames.caller().getClassLoader(), initialize, name);
    }

    /**
     Attempt to load and a class by name by a given class loader and ensure that it is initialized.

     @param loader the loader whereby to load the class
     @param <T> the class
     @return a reference to the class if it can be loaded or else {@code null}
     */
    public static <T> Class<T> load(ClassLoader loader, String name) {
        return load(loader, true, name);
    }

    /**
     Initialize a class if it has not been initialized yet.

     @param type a class
     @return the class
     @since 4.6.0
     */
    public static <T> Class<T> initialize(Class<T> type) {
        Unsafe.ensureClassInitialized(type);
        return type;
    }

    /**
     Attempt to locate and read a given type's class file.

     @param loader the class loader that should be used in order to locate the type; {@code null} means the bootstrap class loader
     @param type the name of the type to locate
     @return the class file of the type if located or else {@code null}
     @since 4.6.0
     */
    public static byte[] classFile(ClassLoader loader, String type) {
        type = type.replace('.', '/') + ".class";

        try (var stream = loader == null ? Object.class.getResourceAsStream('/' + type) : loader.getResourceAsStream(type)) {
            return stream == null ? null : stream.readAllBytes();
        }
    }

    /**
     Attempt to locate and read a type's class file.

     @param type the name of the type to locate
     @return the class file of the type if located or else {@code null}
     @since 4.6.0
     */
    public static byte[] classFile(String type) {
        return classFile(StackFrames.caller().getClassLoader(), type);
    }

    /**
     Attempt to locate and read a given type's class file.

     @return the class file of the type if located or else {@code null}
     @since 4.6.0
     */
    public static byte[] classFile(Class<?> type) {
        if (type.isArray() || type.isPrimitive()) {
            throw new IllegalArgumentException("type must be user-defined");
        }

        try (var stream = type.getResourceAsStream('/' + type.getName().replace('.', '/') + ".class")) {
            return stream == null ? null : stream.readAllBytes();
        }
    }

    public static List<Type> genericSupertypes(Class<?> type) {
        var supertypes = new ArrayList<>(Arrays.asList(type.getGenericInterfaces()));
        var superclass = type.getGenericSuperclass();

        if (superclass != null) {
            supertypes.add(superclass);
        }

        return supertypes;
    }

    /**
     Generate a {@link CodeSource} for a {@link URL} with certificates if the URL points to a JAR.

     @return a {@link CodeSource} for {@code url}
     @since 4.8.0
     */
    public static CodeSource codeSource(URL url) {
        return new CodeSource(url, url != null && url.openConnection() instanceof JarURLConnection jar ? jar.getCertificates() : null);
    }

    /**
     Generate a {@link ProtectionDomain} with the provided {@link ClassLoader} and the {@link CodeSource} produced by {@link #codeSource} from the given {@link URL}.

     @return a {@link ProtectionDomain} with a generated {@link CodeSource} and {@code loader}.
     @since 4.8.0
     */
    public static ProtectionDomain protectionDomain(URL url, ClassLoader loader) {
        return new ProtectionDomain(codeSource(url), null, loader, null);
    }

    private static Class<?> tryLoad(String... classes) {
        return Stream.of(classes).map(Classes::load).filter(Objects::nonNull).findFirst().orElse(null);
    }

    static {
        var charArray = new char[0];
        var shortArray = new short[0];
        long offset = 0;

        while (Unsafe.getInt(charArray, offset) == Unsafe.getInt(shortArray, offset)) {
            offset += 4;
        }

        classOffset = offset;
    }
}
