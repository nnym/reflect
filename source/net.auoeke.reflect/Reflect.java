package net.auoeke.reflect;

import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import net.auoeke.result.Result;

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

	 @return a {@link Result.Success} containing an {@link Instrumentation} instance if attachment was successful
	 @since 4.9.0
	 */
	public static Result<Instrumentation> instrument() {
		if (instrumentation == null) {
			record Candidate(String path, Manifest manifest) {}

			// Try to avoid loading Agent unnecessarily in order to avoid duplication.
			var AgentName = Reflect.class.getPackageName() + ".Agent";
			var AgentPath = AgentName.replace('.', '/') + ".class";
			var systemLoader = ClassLoader.getSystemClassLoader();

			// If Agent was loaded already and this Reflect is defined to a different loader
			// from the one that loaded Agent, then instrumentation will be null here but
			// not in Agent.
			instrumentation = Result.<Instrumentation>of(() -> Accessor.getReference(systemLoader.loadClass(AgentName), "instrumentation"))
				.filterNotNull()
				.flatOr(() -> Result.ofVoid(() -> Accessor.putReference(Class.forName("openj9.internal.tools.attach.target.AttachHandler"), "allowAttachSelf", "true"))
					.flatOr(() -> Result.ofVoid(() -> Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true"))
						.multiply(() -> Accessor.putBoolean(Class.forName("sun.tools.attach.HotSpotVirtualMachine"), "ALLOW_ATTACH_SELF", true))
					)
					.thenResult(() -> {
						var vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));

						try {
							var jarManifestURL = Classes.findResource(Reflect.class, JarFile.MANIFEST_NAME);
							var reflectLoader = Reflect.class.getClassLoader();
							var urls = Stream.of(
								Optional.ofNullable(jarManifestURL).stream(),
								Classes.resources(reflectLoader, JarFile.MANIFEST_NAME)
							).flatMap(Function.identity());

							var manifest = urls.distinct()
								.map(url -> {
									var connection = url.openConnection();

									if (connection instanceof JarURLConnection jar) {
										var jarURL = jar.getJarFileURL();

										return new Candidate(
											// Loading the original JAR as an agent would add it to the system class loader's
											// class path which would be problematic unless it is in the class path already.
											reflectLoader != systemLoader && Objects.equals(url, jarManifestURL) ? null
												: "file".equals(jarURL.getProtocol()) ? Path.of(jarURL.toURI()).toString()
												: jarURL.getPath(),
											jar.getManifest()
										);
									}

									try (var stream = connection.getInputStream()) {
										return new Candidate(null, new Manifest(stream));
									}
								})
								.filter(entry -> AgentName.equals(entry.manifest.getMainAttributes().getValue("Agent-Class")))
								.min((a, b) -> a.path == null ? 1 : -1)
								.orElseThrow(() -> new FileNotFoundException("MANIFEST.MF with \"Agent-Class: %s\"".formatted(AgentName)));

							var agentString = manifest.path;

							if (agentString == null) {
								var agent = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "net.auoeke/reflect")).resolve("agent.jar");
								agentString = agent.toString();

								if (!Files.exists(agent)) {
									try (var jar = new JarOutputStream(Files.newOutputStream(agent), manifest.manifest)) {
										jar.putNextEntry(new ZipEntry(AgentPath));
										jar.write(Classes.read(Classes.findResource(Reflect.class, AgentPath).openStream()));
									} catch (Throwable trouble) {
										Files.deleteIfExists(agent);
										throw trouble;
									}
								}
							}

							try {
								vm.loadAgent(agentString);
								return Accessor.getReference(systemLoader.loadClass(AgentName), "instrumentation");
							} catch (AgentLoadException exception) {
								throw Exceptions.message(exception, message -> agentString + ": " + message);
							}
						} finally {
							vm.detach();
						}
					}));
		}

		return instrumentation;
	}
}
