package net.auoeke.reflect;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.auoeke.result.Result;
import net.gudenau.lib.unsafe.Unsafe;

import static net.auoeke.dycon.Dycon.*;

/**
 Utilities that deal mostly with class loading and object type manipulation.

 @since 0.6.0
 */
public class Classes {
	public static final Class<?> ConstantPool = tryLoad("jdk.internal.reflect.ConstantPool", "sun.reflect.ConstantPool");
	public static final Class<?> JavaLangAccess = tryLoad("jdk.internal.access.JavaLangAccess", "jdk.internal.misc.JavaLangAccess", "sun.misc.JavaLangAccess");
	public static final Class<?> Reflection = tryLoad("jdk.internal.reflect.Reflection", "sun.reflect.Reflection");
	public static final Class<?> SharedSecrets = tryLoad("jdk.internal.access.SharedSecrets", "jdk.internal.misc.SharedSecrets", "sun.misc.SharedSecrets");
	public static final Class<?> URLClassPath = tryLoad("jdk.internal.loader.URLClassPath", "sun.misc.URLClassPath");

	public static final Object systemClassPath = classPath(ClassLoader.getSystemClassLoader());
	public static final Pointer klass;
	public static final Pointer firstField = Pointer.of(Fields.instanceOf(Integer.class).findAny().get());

	/**
	 Change the type of an object. The target type should not be abstract or bigger than the object's type.

	 @param object the object whose type to change
	 @param <T> the target type
	 @return {@code object}
	 */
	public static <T> T reinterpret(Object object, T... dummy) {
		return (T) reinterpret(Unsafe.allocateInstance(dummy.getClass().getComponentType()), object);
	}

	/**
	 Change the type of an object. The target type should not be abstract or bigger than the object's type.

	 @param object the object whose type to change
	 @param type the target type
	 @param <T> the target type
	 @return {@code object}
	 */
	public static <T> T reinterpret(Object object, Class<T> type) {
		return reinterpret(Unsafe.allocateInstance(type), object);
	}

	/**
	 Change the type of an object. The target type should not be bigger than the object's type.

	 @param <T> the target type type
	 @param source an instance of the target type
	 @param object the object whose type to change
	 @return {@code to}
	 */
	public static <T> T reinterpret(T source, Object object) {
		klass.copy(source, object);

		return (T) object;
	}

	/**
	 Change the type of an object. The target type should not be bigger than the object's type.

	 @param object the object whose type to change
	 @param klass the {@link #klass Klass*} of the target type
	 @param <T> the target type
	 @return {@code object}
	 */
	public static <T> T reinterpret(Object object, long klass) {
		Classes.klass.put(object, klass);

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
		return klass.getAddress(object);
	}

	/**
	 For when you're dealing with recursive generics and wildcards.
	 */
	public static <T> T cast(Object object) {
		return (T) object;
	}

	public static <T> Class<T> findLoadedClass(ClassLoader loader, String klass) {
		return (Class<T>) ldc(() -> Invoker.findVirtual(ClassLoader.class, "findLoadedClass", Class.class, String.class)).invokeExact(loader, klass);
	}

	public static URL[] urls(ClassLoader classLoader) {
		return urls(classPath(classLoader));
	}

	public static URL[] urls(Object classPath) {
		return (URL[]) ldc(() -> Invoker.findVirtual(URLClassPath, "getURLs", URL[].class)).invoke(classPath);
	}

	public static void addSystemURL(URL... url) {
		addURL(systemClassPath, url);
	}

	public static void addSystemURL(URL url) {
		addURL(systemClassPath, url);
	}

	public static void addURL(ClassLoader classLoader, URL... urls) {
		addURL(classPath(classLoader), urls);
	}

	public static void addURL(ClassLoader classLoader, URL url) {
		addURL(classPath(classLoader), url);
	}

	public static void addURL(Object classPath, URL... urls) {
		for (var url : urls) {
			addURL(classPath, url);
		}
	}

	public static void addURL(Object classPath, URL url) {
		ldc(() -> Invoker.findVirtual(URLClassPath, "addURL", void.class, URL.class)).invoke(classPath, url);
	}

	public static Object classPath(ClassLoader classLoader) {
		return Accessor.getReference(classLoader, classPathField(classLoader.getClass()));
	}

	public static Field classPathField(Class<?> loaderClass) {
		return Fields.allInstance(loaderClass).filter(field -> URLClassPath.isAssignableFrom(field.getType())).findAny().orElse(null);
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
		return (Class<T>) Result.of(() -> Class.forName(name, initialize, loader)).valueOrNull();
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

	 @param initialize whether to initialize the class if not initialized
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
	 @param name the name of the class to load
	 @param <T> the class
	 @return a reference to the class if it can be loaded or else {@code null}
	 */
	public static <T> Class<T> load(ClassLoader loader, String name) {
		return load(loader, true, name);
	}

	/**
	 Attempt to load a class by name by the loader of this method's caller's caller and ensure that it is initialized.

	 @param name the name of the class to load
	 @param <T> the class
	 @return a reference to the class if it can be loaded or else {@code null}
	 @since 4.11.0
	 */
	public static <T> Class<T> loadWithCaller(String name) {
		return load(StackFrames.caller(2).getClassLoader(), name);
	}

	/**
	 Attempt to load a class by name by the loader of this method's caller's caller and optionally ensure that it is initialized.

	 @param initialize whether to initialize the class if not initialized
	 @param name the name of the class to load
	 @param <T> the class
	 @return a reference to the class if it can be loaded or else {@code null}
	 @since 4.11.0
	 */
	public static <T> Class<T> loadWithCaller(boolean initialize, String name) {
		return load(StackFrames.caller(2).getClassLoader(), initialize, name);
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
		var stream = loader == null ? Object.class.getResourceAsStream('/' + type) : loader.getResourceAsStream(type);
		return stream == null ? null : read(stream);
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
		var stream = checkUserDefined(type).getResourceAsStream('/' + path(type));
		return stream == null ? null : read(stream);
	}

	/**
	 Reads a resource as a {@code byte[]}.
	 This method always {@link InputStream#close closes} {@code resource}.

	 @param resource an {@link InputStream} from a resource
	 @return the contents of {@code resource} as a {@code byte[]}
	 @throws NullPointerException if {@code resource} is {@code null}
	 @since 5.3.0
	 */
	public static byte[] read(InputStream resource) {
		try (var input = resource) {
			return input.readAllBytes();
		}
	}

	/**
	 Converts an {@link Enumeration} into a {@link Stream}.
	 This method's chief intended usage is converting enumerations of {@link URL}s;
	 which some resource-related {@link ClassLoader} methods tend to return.

	 @param enumeration an enumeration
	 @param <T> the type of the elements in {@code enumeration}
	 @return a stream containing the elements in {@code enumeration}
	 @since 5.3.0
	 */
	public static <T> Stream<T> stream(Enumeration<T> enumeration) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(enumeration.asIterator(), Spliterator.NONNULL), false);
	}

	/**
	 Uses the given {@link ClassLoader loader} in order to find resources.
	 If {@code loader == null}, then this method searches the bootstrap class path.

	 @param loader a class loader
	 @param name the name of the resources to fisd
	 @return resources located by {@code loader}
	 @since 5.3.0
	 */
	public static Stream<URL> resources(ClassLoader loader, String name) {
		return loader == null ? stream((Enumeration<URL>) ldc(() -> Invoker.findStatic(Class.forName("jdk.internal.loader.BootLoader"), "findResources", Enumeration.class, String.class)).invokeExact(name)) : loader.resources(name);
	}

	/**
	 Returns the internal name of {@code type}.
	 The internal name of a type is its {@link Class#getName binary name} with all occurrences of {@code '.'} replaced by {@code '/'}.

	 @param type a type
	 @return {@code type}'s internal name
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is primitive or an array type
	 @since 6.1.0
	 */
	public static String internalName(Class<?> type) {
		return checkUserDefined(type).getName().replace('.', '/');
	}

	/**
	 Returns {@code type}'s {@link Class#getName name} without its package.
	 <p>
	 If {@code type} is an array type, then {@code '['} is prepended as by {@link Class#getName}.
	 <p>
	 Unlike {@link Class#getSimpleName}, if {@code type} is nested,
	 this method will retain its enclosing type's name prefix.

	 @param type a type
	 @return {@code type}'s local name
	 @throws NullPointerException if {@code type == null}
	 @since 6.1.0
	 */
	public static String localName(Class<?> type) {
		if (type.isArray()) {
			return '[' + localName(type.componentType());
		}

		var name = type.getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}

	/**
	 Returns the path to {@code type}'s class file relative (without a leading {@code '/'}) to its source root
	 such that {@code type.getClass().getClassLoader().getResource(Classes.path(type))}
	 would conventionally return the location of its class file.

	 @param type a type
	 @return {@code type}'s class file's relative path
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is primitive or an array type
	 @since 6.1.0
	 */
	public static String path(Class<?> type) {
		return internalName(checkUserDefined(type)) + ".class";
	}

	/**
	 Returns the conventional filename of {@code type}'s class file.
	 The filename is computed by appending {@code ".class"} to the longest
	 substring of {@code type}'s {@link Class#getName name} that is not followed by {@code '.'}.

	 @param type a type
	 @return {@code type}'s class file's conventional filename
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is primitive or an array type
	 @since 6.1.0
	 */
	public static String filename(Class<?> type) {
		return localName(checkUserDefined(type)) + ".class";
	}

	/**
	 Uses {@link Class#getResource} in order to attempt to locate a resource at {@code path}
	 relative to the source (for example JAR or directory) of {@code anchor}.
	 If no resource can be located, then this method returns {@code null}.
	 <p>
	 If {@code path} is empty, then this method may not return {@code null}
	 even if the URL does not represent an accessible resource.

	 @param anchor a type to use in order to locate a source
	 @param path a potentially empty path to a resource in the same source as {@code anchor}
	 @return the location of the resource or {@code null} if {@code anchor}'s source cannot be located
	 @throws NullPointerException if {@code anchor == null || path == null}
	 @throws IllegalArgumentException if {@code anchor} is primitive or an array type
	 @since 6.1.0
	 */
	public static URL sourceResource(Class<?> anchor, String path) {
		Objects.requireNonNull(path, "path");

		var anchorLocation = anchor.getResource(filename(anchor));
		if (anchorLocation == null) return null;

		var string = anchorLocation.toString();
		var url = new URL(string.substring(0, string.lastIndexOf(path(anchor))) + path);

		return path.isEmpty() ? url : realURLOrNull(url);
	}

	/**
	 Attempts to locate a resource at {@code path} relative to the source of {@code anchor}.
	 If no resource can be located, then this method returns {@code null}.
	 <p>
	 If {@code path} is empty, then this method may not return {@code null}
	 even if the URL does not represent an accessible resource.
	 <p>
	 This method first considers {@code anchor}'s {@link #location}. If it is not {@code null}, then it appends
	 {@code path} and returns the result. Otherwise, it delegates to {@link #sourceResource}.

	 @param anchor a type whereby to locate the source against which to resolve {@code path}
	 @param path a potentially empty path to a resource
	 @return the location of the resource if it exists or else {@code null}
	 @throws NullPointerException if {@code anchor == null || path == null}
	 @throws IllegalArgumentException if {@code anchor} is primitive or an array type
	 @since 6.1.0
	 */
	public static URL findResource(Class<?> anchor, String path) {
		Objects.requireNonNull(path, "path");

		var source = location(anchor);
		if (source == null) return sourceResource(anchor, path);

		var isJar = source.getPath().endsWith(".jar");
		var url = !isJar && path.isEmpty() ? source
			: new URL(isJar ? "jar:" + source + "!/" + path : source + "/" + path);
		return path.isEmpty() ? url : realURLOrNull(url);
	}

	/**
	 Attempts to locate {@code type}'s source.
	 If it cannot be located, then this method returns {@code null}.
	 <p>
	 If the source is a JAR, then the returned URL will locate the JAR; not an entry in it.
	 <p>
	 This method first considers {@code type}'s {@link #location}. If it is not {@code null}, then it is returned.
	 Otherwise, this method uses {@code type::getResource} in order to locate {@code type} and consequently its source.

	 @param type the type whose source to locate
	 @return the location of {@code type}'s source
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is primitive or an array type
	 @since 6.1.0
	 */
	public static URL findSource(Class<?> type) {
		var location = location(type);
		if (location != null) return location;

		location = type.getResource(filename(type));
		if (location == null) return null;

		var string = location.toString();
		string = string.substring(0, string.lastIndexOf(path(type)));

		return new URL("jar".equals(location.getProtocol()) && string.endsWith("!/") ? string.substring(4, string.length() - 2) : string);
	}

	/**
	 Attempts to locate the root of {@code type}'s source.
	 If it cannot be located, then this method returns {@code null}.
	 <p>
	 If the source is a JAR, then the returned URL's protocol
	 will be {@code "jar"} and it will end with {@code "!/"}.
	 <p>
	 This method first considers {@code type}'s {@link #location}. If it is not {@code null}, then it is returned.
	 Otherwise, this method delegates to {@link #sourceResource} with an empty {@code path}.

	 @param type the type whose source root to locate
	 @return the location of {@code type}'s source root
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is primitive or an array type
	 @since 6.1.0
	 */
	public static URL findRoot(Class<?> type) {
		var location = location(type);
		return location == null ? sourceResource(type, "")
			: location.getPath().endsWith(".jar") ? new URL("jar:" + location + "!/")
			: location;
	}

	/**
	 Attempts to locate a path to the root of {@code type}'s source as acquired from {@link #findRoot}.
	 If it cannot be located, then this method returns {@code null}.
	 <p>
	 If a {@link FileSystem} for the source does not exist, then this method will attempt to make one and
	 should it succeed, the burden of {@link FileSystem#close closing} {@link Path#getFileSystem it}
	 will be on the caller.

	 @param type the type whose source root to locate
	 @return a path to {@code type}'s source root
	 @throws NullPointerException if {@code type == null}
	 @throws IllegalArgumentException if {@code type} is primitive or an array type
	 @throws NoSuchElementException with a {@link Throwable#getCause cause}
	 if an error occurred while converting the source root into a Path
	 @since 6.1.0
	 */
	@SuppressWarnings("resource")
	public static Path findRootPath(Class<?> type) {
		var location = findRoot(type);
		if (location == null) return null;

		return Result.of(location::toURI)
			.flatMap(root -> Result.of(() -> Path.of(root))
				.or(() -> FileSystems.newFileSystem(root, Map.of()).getPath("/")))
			.value();
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
	 Acquire a {@link Class}' {@link ProtectionDomain}'s {@link CodeSource}'s {@link CodeSource#getLocation() location}.

	 @param type a type
	 @return a {@link CodeSource#location} corresponding to the {@code type}; may be {@code null}
	 @since 5.2.0
	 */
	public static URL location(Class<?> type) {
		var domain = type.getProtectionDomain();

		if (domain == null) {
			return null;
		}

		var source = domain.getCodeSource();
		return source == null ? null : source.getLocation();
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

	private static Class<?> checkUserDefined(Class<?> type) {
		if (type.isPrimitive() || type.isArray()) {
			throw new IllegalArgumentException("type must be user-defined");
		}

		return type;
	}

	private static URL realURLOrNull(URL url) {
		return Result.success(url)
			.filter(u -> {
				var connection = u.openConnection();

				if (connection instanceof HttpURLConnection http) {
					http.setRequestMethod("HEAD");
					return http.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST;
				} else {
					connection.getInputStream();
				}

				return true;
			})
			.valueOrNull();
	}

	static {
		var charArray = new char[0];
		var shortArray = new short[0];
		long offset = 0;

		while (Unsafe.getInt(charArray, offset) == Unsafe.getInt(shortArray, offset)) {
			offset += 4;
		}

		klass = Pointer.of(offset, Pointer.ADDRESS);
	}
}
