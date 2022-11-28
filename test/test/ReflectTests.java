package test;

import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.ClassDefiner;
import net.auoeke.reflect.ClassTransformer;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.Reflect;
import net.auoeke.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

@Testable
public class ReflectTests extends Reflect {
	@Test public void instrumentationTest() {
		Result.of(() -> ClassLoader.getSystemClassLoader().loadClass("net.auoeke.reflect.Agent")).and(agent -> Accessor.putReference(agent, "instrumentation", null));

		assert instrument().value().isRedefineClassesSupported() && instrument().value().isRetransformClassesSupported() && instrument().value().isNativeMethodPrefixSupported();

		var string = "toString instrumented";
		instrument().value().addTransformer(ClassTransformer.of((module, loader, name, type, domain, classFile) -> {
			var node = new ClassNode();
			new ClassReader(classFile).accept(node, 0);

			var toString = node.methods.stream().filter(a -> a.name.equals("toString")).findAny().get();
			toString.instructions.clear();
			toString.visitLdcInsn(string);
			toString.visitInsn(Opcodes.ARETURN);

			var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			return writer.toByteArray();
		}).ofType(Object.class).exceptionLogging().singleUse(instrument().value()), true);
		instrument().value().retransformClasses(Object.class);

		Assert.equal(string, new Object().toString());
	}

	@Test public void reflectDefinedByNonSystemLoader() {
		if (Reflect.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
			try (var loader = new URLClassLoader(Stream.of(Reflect.class, ReflectTests.class).map(c -> c.getProtectionDomain().getCodeSource().getLocation()).toArray(URL[]::new))) {
				var clone = new GreedyClassLoader(loader).loadClass(ReflectTests.class.getName());
				clone.getDeclaredMethod("instrumentationTest").invoke(clone.getDeclaredConstructor().newInstance());
			}
		}

		this.instrumentationTest();
	}

	@Test void instrumentationAgentDefinedBySeparateLoader() {
		var agent = Reflect.class.getPackageName().replace('.', '/') + "/Agent";
		var loader = new GreedyClassLoader(new URLClassLoader(Stream.of(Reflect.class, ReflectTests.class).map(c -> c.getProtectionDomain().getCodeSource().getLocation()).toArray(URL[]::new)) {
			@Override public URL findResource(String name) {
				return name.equals(agent + ".class") ? null : super.findResource(name);
			}
		});

		ClassDefiner.make().loader(null).classFile(agent).define();

		var instrumentation = (Instrumentation) Invoker.bind(Methods.firstOf(loader.loadClass(Reflect.class.getName()), "instrument").invoke(null), "value", Object.class).invoke();
		assert instrumentation.isRedefineClassesSupported() && instrumentation.isRetransformClassesSupported() && instrumentation.isNativeMethodPrefixSupported();
	}

	static class GreedyClassLoader extends ClassLoader {
		final MethodHandle findResource;

		public GreedyClassLoader(ClassLoader delegate) {
			this.findResource = Invoker.bind(delegate, "findResource", URL.class, String.class);
		}

		@Override protected Class<?> loadClass(String name, boolean resolve) {
			if (name.startsWith("java.")) {
				return super.loadClass(name, resolve);
			}

			var type = this.findLoadedClass(name);

			if (type == null) {
				var classFile = (URL) this.findResource.invoke(name.replace('.', '/') + ".class");

				if (classFile == null) {
					return super.loadClass(name, resolve);
				}

				var bytes = Classes.read(classFile.openStream());
				type = this.defineClass(null, bytes, 0, bytes.length);
			}

			if (resolve) {
				this.resolveClass(type);
			}

			return type;
		}
	}
}
