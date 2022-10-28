package net.auoeke.reflect;

import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;

/**
 Miscellaneous utilities.

 @since 0.0.0
 */
public class Reflect {
	private static Result<Instrumentation> instrumentation;

	/**
	 Attach the current JVM to itself and acquire an {@link Instrumentation} instance that supports all optional operations.
	 <p><b>
	 Note that this method is not guaranteed to work with all JVM vendors.
	 <br>
	 Note that this method may stop working in the future; see the comments in {@link sun.tools.attach.HotSpotVirtualMachine#HotSpotVirtualMachine HotSpotVirtualMachine::new}.
	 </b>

	 @return a {@link Result} containing an {@link Instrumentation} instance if attachment was successful
	 @since 4.9.0
	 */
	public static Result<Instrumentation> instrument() {
		if (instrumentation == null) {
			var result = new Result<Instrumentation>();

			record Candidate(String path, Manifest manifest) {}

			result.map(() -> {
				result.andSuppress(() -> Accessor.putReference(Class.forName("openj9.internal.tools.attach.target.AttachHandler"), "allowAttachSelf", "true"));
				result.andSuppress(() -> Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true"));
				result.andSuppress(() -> Accessor.putBoolean(Class.forName("sun.tools.attach.HotSpotVirtualMachine"), "ALLOW_ATTACH_SELF", true));

				var vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));

				try {
					// Do not reference Agent directly because it might belong to a different loader.
					var agentClass = Class.forName(Reflect.class.getPackageName() + ".Agent");

					// @formatter:off
					var manifests = Stream.of(
						manifest(agentClass),
						manifest(Reflect.class),
						Classes.resources(agentClass.getClassLoader(), JarFile.MANIFEST_NAME),
						Classes.resources(Reflect.class.getClassLoader(), JarFile.MANIFEST_NAME)
					).flatMap(Function.identity())
					 .distinct()
					 .map(url -> {
						 var connection = url.openConnection();

						 if (connection instanceof JarURLConnection jar) {
							 url = jar.getJarFileURL();
							 return new Candidate("file".equals(url.getProtocol()) ? Path.of(url.toURI()).toString() : url.getPath(), jar.getManifest());
						 }

						 try (var stream = connection.getInputStream()) {
							 return new Candidate(null, new Manifest(stream));
						 }
					 })
					 .filter(entry -> agentClass.getName().equals(entry.manifest.getMainAttributes().getValue("Agent-Class")))
					 .toList();
					// @formatter:on

					if (manifests.isEmpty()) {
						throw new FileNotFoundException("MANIFEST.MF with \"Agent-Class: %s\"".formatted(agentClass.getName()));
					}

					var entry = manifests.stream().filter(e -> e.path != null).findFirst().orElse(manifests.get(0));
					var agentString = entry.path;

					if (agentString == null) {
						var agent = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "net.auoeke/reflect")).resolve("agent.jar");
						agentString = agent.toString();

						if (!Files.exists(agent)) {
							try (var jar = new JarOutputStream(Files.newOutputStream(agent))) {
								jar.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
								entry.manifest.write(jar);
								jar.putNextEntry(new ZipEntry(agentClass.getName().replace('.', '/') + ".class"));

								// If agentClass is from tmpdir, then its source is being overwritten and incomplete.
								// Thus, attempting to read it now will throw an EOFException.
								jar.write(Classes.read(new URL(Reflect.class.getResource("Reflect.class").toString().replaceFirst("Reflect(?=\\.class$)", "Agent")).openStream()));
							} catch (Throwable trouble) {
								Files.deleteIfExists(agent);

								throw trouble;
							}
						}
					}

					try {
						vm.loadAgent(agentString);
					} catch (AgentLoadException exception) {
						throw Exceptions.message(exception, message -> agentString + ": " + message);
					}

					return Accessor.getReference(ClassLoader.getSystemClassLoader().loadClass(agentClass.getName()), "instrumentation");
				} finally {
					vm.detach();
				}
			});

			instrumentation = result;
		}

		return instrumentation;
	}

	static <T> T runNull(Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Throwable throwable) {
			return null;
		}
	}

	private static Stream<URL> manifest(Class<?> type) {
		return Optional.ofNullable(Classes.location(type)).map(url -> {
			var string = url.toString();

			if (string.endsWith(".jar")) {
				return new URL("jar:" + string + "!/" + JarFile.MANIFEST_NAME);
			}

			var path = Path.of(url.toURI()).resolve(JarFile.MANIFEST_NAME);
			return Files.exists(path) ? path.toUri().toURL() : null;
		}).stream();
	}
}
