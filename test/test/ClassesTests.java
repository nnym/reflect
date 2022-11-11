package test;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import mock.ClosableInputStream;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Pointer;
import net.auoeke.reflect.Reflect;
import net.auoeke.reflect.Types;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.asm.ClassNode2;
import reflect.util.Logger;
import util.Util;

@SuppressWarnings("AccessStaticViaInstance")
@Testable
public class ClassesTests extends Classes {
	@Test public void reinterpret() {
		Double dubble = 0D;
		var loong = Classes.reinterpret(dubble, Long.class);

		Accessor.putLong(loong, "value", 0xFFFFFFFFFFL);
		assert loong == 0xFFFFFFFFFFL;

		Classes.reinterpret(loong, Double.class);

		var object = (Object) Classes.reinterpret(Unsafe.allocateInstance(Object.class), ClassesTests.class);
		assert object.getClass() == (object = Unsafe.allocateInstance(ClassesTests.class)).getClass();
		assert object.getClass() == Classes.reinterpret(object, ClassesTests.class).getClass();
		assert Classes.reinterpret(new ClassesTests(), Unsafe.allocateInstance(Object.class)).getClass() == ClassesTests.class;
	}

	@Test void classes() {
		var classPath = classPath(ClassesTests.class.getClassLoader());
		var url = Path.of("test").toUri().toURL();
		addURL(classPath, url);
		assert List.of(urls(classPath)).contains(url);
	}

	@Test void definer() {
		class Test {
			static final UUID u = UUID.randomUUID();
		}

		var bytes = classFile(Test.class);
		var buffer = ByteBuffer.wrap(bytes);

		var b = ClassDefiner.make().loader(new ClassLoader() {}).classFile(bytes).unsafe().initialize().define();
		var bb = ClassDefiner.make().loader(new SecureClassLoader() {}).classFile(buffer).unsafe().initialize().define();
		var s = ClassDefiner.make().from(Test.class).secureLoader(new SecureClassLoader() {}, Test.class.getProtectionDomain().getCodeSource()).initialize().define();
		var sb = ClassDefiner.make().secureLoader(new SecureClassLoader() {}, Test.class.getProtectionDomain().getCodeSource()).classFile(buffer.clear()).define();
		var doNotInitialize = ClassDefiner.make().loader(new ClassLoader() {}).classFile("test/DoNotInitialize").define();
		var u = Pointer.of(sb, "u");

		Assert.equal(u.get(), null)
			.equalBy(type -> type.getProtectionDomain().getCodeSource(), s, sb, Test.class)
			.equalBy(Class::getName, Test.class, b, bb, s, sb)
			.distinctBy(Class::getProtectionDomain, s, sb, Test.class)
			.distinct(Test.class, b, bb, s, sb)
			.distinct(Stream.concat(Stream.of(null, Test.u), Stream.of(b, bb, s, initialize(sb)).map(u::get)).toArray())
			.exception("java.* packages are restricted", () -> ClassDefiner.make().loader(new SecureClassLoader() {}).from(UUID.class).unsafe().define());
	}

	@Test void load() {
		var loader = new URLClassLoader(new URL[]{ClassesTests.class.getProtectionDomain().getCodeSource().getLocation()}) {
			@Override protected Class<?> loadClass(String name, boolean resolve) {
				try {
					this.findClass(name);
					return null;
				} catch (ClassNotFoundException bad) {
					return super.loadClass(name, resolve);
				}
			}
		};

		assert loader.loadClass(Object.class.getName()) != null;
		assert loader.loadClass(ClassesTests.class.getName()) == null;

		class Test {
			static Class<?> load(String name) {
				return loadWithCaller(name);
			}
		}

		assert ClassesTests.class == Invoker.findStatic(ClassDefiner.make().loader(loader).classFile(classFile(Test.class)).define(), "load", Class.class, String.class).invoke(ClassesTests.class.getName());
	}

	@Test void streamTest() {
		var enumeration = new Enumeration<URL>() {
			int i = 3;

			@Override public boolean hasMoreElements() {
				return this.i != 0;
			}

			@Override public URL nextElement() {
				return Path.of("file" + this.i--).toUri().toURL();
			}
		};

		Assert.truth(stream(enumeration).count() == 3);
	}

	@Test void resourcesTest() {
		var resources = resources(null, "java/lang/Object.class").toList();
		Assert.equal(1, resources.size());

		var node = new ClassNode2().reader(read(resources.get(0).openStream())).read();
		Assert.equal(node.name, "java/lang/Object");
	}

	@Test void readTest() {
		var path = "/java/lang/Object.class";
		var stream = new ClosableInputStream(Object.class.getResourceAsStream(path));
		var contents = read(stream);
		Assert.truth(stream.closed());

		try (var s = Object.class.getResourceAsStream(path)) {
			Assert.arraysEqual(contents, s.readAllBytes());
		}
	}

	@Test void internalNameTest() {
		this.arrayException(Classes::internalName);

		Assert.entriesEqualBy(Classes::internalName, Map.of(
			Unsafe.class, "net/gudenau/lib/unsafe/Unsafe",
			Util.TopLevel, "TopLevel",
			Object.class, "java/lang/Object",
			MethodHandles.Lookup.class, "java/lang/invoke/MethodHandles$Lookup",
			HttpClient.Builder.class, "java/net/http/HttpClient$Builder"
		));
	}

	@Test void localNameTest() {
		Types.returnPrimitives().forEach(type -> Assert.equal(type.getName(), localName(type)));

		Assert.entriesEqualBy(Classes::localName, Map.of(
			Unsafe.class, "Unsafe",
			Util.TopLevel, "TopLevel",
			Object.class, "Object",
			MethodHandles.Lookup.class, "MethodHandles$Lookup",
			HttpClient.Builder.class, "HttpClient$Builder",
			int[].class, "[int",
			Unsafe[].class, "[Unsafe",
			Object[].class, "[Object"
		));
	}

	@Test void pathTest() {
		this.arrayException(Classes::path);

		Assert.entriesEqualBy(Classes::path, Map.of(
			Unsafe.class, "net/gudenau/lib/unsafe/Unsafe.class",
			Util.TopLevel, "TopLevel.class",
			Object.class, "java/lang/Object.class",
			MethodHandles.Lookup.class, "java/lang/invoke/MethodHandles$Lookup.class",
			HttpClient.Builder.class, "java/net/http/HttpClient$Builder.class"
		));
	}

	@Test void filenameTest() {
		this.arrayException(Classes::filename);

		Assert.entriesEqualBy(Classes::filename, Map.of(
			Unsafe.class, "Unsafe.class",
			Util.TopLevel, "TopLevel.class",
			Object.class, "Object.class",
			MethodHandles.Lookup.class, "MethodHandles$Lookup.class",
			HttpClient.Builder.class, "HttpClient$Builder.class"
		));
	}

	@Test void sourceResourceTest() {
		Assert.notNull(read(sourceResource(Reflect.class, path(Reflect.class)).openStream()))
			.notNull(sourceResource(Reflect.class, ""))
			.notNull(sourceResource(Unsafe.class, JarFile.MANIFEST_NAME))
			.notNull(sourceResource(Util.TopLevel, path(ClassesTests.class)))
			.notNull(sourceResource(Object.class, path(Integer.class)))
			.nul(sourceResource(Reflect.class, internalName(Reflect.class)));
	}

	@Test void findResourceTest() {
		Assert.notNull(read(findResource(Reflect.class, path(Reflect.class)).openStream()))
			.notNull(findResource(Reflect.class, ""))
			.notNull(findResource(Unsafe.class, JarFile.MANIFEST_NAME))
			.notNull(findResource(Util.TopLevel, path(ClassesTests.class)))
			.notNull(findResource(Object.class, path(Integer.class)))
			.nul(findResource(Reflect.class, internalName(Reflect.class)));
	}

	@Test void findSourceTest() {
		Assert.notNull(findSource(Reflect.class))
			.notNull(findSource(Object.class));
	}

	@Test void findRootTest() {
		Assert.notNull(findRoot(Reflect.class))
			.notNull(findRoot(Object.class));
	}

	@Test void findRootPathTest() {
		Assert.notNull(findRootPath(Reflect.class))
			.notNull(findRootPath(Object.class))
			.equal("/", findRootPath(Unsafe.class).toString());
	}

	@Test void miscResourceTest() {
		Assert.equal(sourceResource(Reflect.class, ""), findResource(Reflect.class, ""), findRoot(Reflect.class));
		Assert.equalBy(Objects::isNull, sourceResource(Reflect.class, JarFile.MANIFEST_NAME), findResource(Reflect.class, JarFile.MANIFEST_NAME));
	}

	private void arrayException(Consumer<Class<?>> test) {
		var types = Stream.concat(
			Types.stackPrimitives(),
			Stream.of(Object.class, MethodHandles.Lookup.class, HttpClient.Builder.class)
		).map(Class::arrayType).toList();

		Consumer<Class<?>> assertion = type -> Assert.exception(() -> test.accept(type));
		Types.returnPrimitives().forEach(assertion);
		types.forEach(assertion);
		types.stream().map(Class::arrayType).forEach(assertion);
	}
}

class DoNotInitialize {
	static {
		if (true) { // Thankfully the compiler is stupid.
			throw new UnsupportedOperationException("initialization");
		}
	}
}
