package net.auoeke.reflect;

import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import net.auoeke.result.Result;

import static net.auoeke.dycon.Dycon.*;

/**
 Miscellaneous utilities.

 @since 0.0.0
 */
public class Reflect {
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
	@SuppressWarnings("Java9ReflectionClassVisibility")
	public static Result<Instrumentation> instrument() {
		return ldc(() -> {
			// Try to avoid loading Agent unnecessarily in order to avoid duplication.
			var AgentName = Reflect.class.getPackageName() + ".Agent";
			var AgentPath = AgentName.replace('.', '/') + ".class";
			var systemLoader = ClassLoader.getSystemClassLoader();

			// If Agent was loaded already and this Reflect is defined to a different loader
			// from the one that loaded Agent, then instrumentation will be null here but
			// not in Agent.
			return Result.<Instrumentation>of(() -> Accessor.getReference(systemLoader.loadClass(AgentName), "instrumentation"))
				.filterNotNull()
				.flatOr(() -> Result.ofVoid(() -> Accessor.putReference(Class.forName("openj9.internal.tools.attach.target.AttachHandler"), "allowAttachSelf", "true"))
					.flatOr(() -> Result.ofVoid(() -> Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true"))
						.multiply(() -> Accessor.putBoolean(Class.forName("sun.tools.attach.HotSpotVirtualMachine"), "ALLOW_ATTACH_SELF", true))
					)
					.thenResult(() -> {
						var vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));

						try {
							var manifest = Stream.concat(Stream.ofNullable(Classes.findResource(Reflect.class, JarFile.MANIFEST_NAME)), Classes.resources(Reflect.class.getClassLoader(), JarFile.MANIFEST_NAME))
								.distinct()
								.map(url -> {
									try (var stream = url.openConnection().getInputStream()) {
										return new Manifest(stream);
									}
								})
								.filter(m -> AgentName.equals(m.getMainAttributes().getValue("Agent-Class")))
								.findFirst()
								.orElseThrow(() -> new FileNotFoundException("MANIFEST.MF with \"Agent-Class: %s\"".formatted(AgentName)));

							var agent = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "net.auoeke/reflect")).resolve("agent.jar");

							if (!Files.exists(agent)) {
								try (var jar = new JarOutputStream(Files.newOutputStream(agent), manifest)) {
									jar.putNextEntry(new ZipEntry(AgentPath));
									jar.write(Classes.read(Classes.findResource(Reflect.class, AgentPath).openStream()));
								} catch (Throwable trouble) {
									Files.deleteIfExists(agent);
									throw trouble;
								}
							}

							try {
								vm.loadAgent(agent.toString());
								return Accessor.getReference(systemLoader.loadClass(AgentName), "instrumentation");
							} catch (AgentLoadException exception) {
								throw Exceptions.message(exception, message -> agent + ": " + message);
							}
						} finally {
							vm.detach();
						}
					}));
		});
	}
}
