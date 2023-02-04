package net.auoeke.reflect;

import java.lang.instrument.Instrumentation;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.jar.JarFile;
import net.auoeke.result.Result;

import static net.auoeke.dycon.Dycon.*;

/**
 Miscellaneous utilities.

 @since 0.0.0
 */
public class Reflect {
	/**
	 Lazily generates and loads an agent in $TMPDIR/net.auoeke/reflect/
	 and returns an {@link Instrumentation} that supports all optional operations.

	 @return a {@link Result.Success} containing an {@link Instrumentation} if the agent was loaded successfully
	 @since 4.9.0
	 */
	@SuppressWarnings("Java9ReflectionClassVisibility")
	public static Result<Instrumentation> instrument() {
		return ldc(() -> {
			// Try to avoid loading Agent unnecessarily in order to avoid duplication.
			var AgentName = Reflect.class.getPackageName() + ".Agent";
			var AgentPath = AgentName.replace('.', '/') + ".class";
			var systemLoader = ClassLoader.getSystemClassLoader();
			var Agent = Classes.load(systemLoader, AgentName);

			// If Agent was loaded already and this Reflect is defined to a different loader
			// from the one that loaded Agent, then instrumentation will be null here but
			// not in Agent.
			return Result.<Instrumentation>of(() -> Accessor.getReference(Agent, "instrumentation"))
				.filterNotNull()
				.or(() -> {
					var manifest = """
						Premain-Class: %s
						Agent-Class: %1$s
						Launcher-Agent-Class: %1$s
						Can-Redefine-Classes: true
						Can-Retransform-Classes: true
						Can-Set-Native-Method-Prefix: true
						""".formatted(AgentName).getBytes();

					var filename = "agent-%s.jar".formatted(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(manifest)));
					var agent = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "net.auoeke/reflect")).resolve(filename);

					if (!Files.exists(agent)) {
						try (var jar = FileSystems.newFileSystem(agent, Map.of("create", "true"))) {
							var manifestPath = jar.getPath(JarFile.MANIFEST_NAME);
							var classPath = jar.getPath(AgentPath);
							Files.createDirectories(manifestPath.getParent());
							Files.createDirectories(classPath.getParent());
							Files.write(manifestPath, manifest);
							Files.write(classPath, Classes.read(Classes.findResource(Reflect.class, AgentPath).openStream()));
						} catch (Throwable trouble) {
							Files.deleteIfExists(agent);
							throw trouble;
						}
					}

					try {
						if (Agent == null) {
							Classes.addSystemURL(agent.toUri().toURL());
						}

						Invoker.findStatic(Class.forName("sun.instrument.InstrumentationImpl"), "loadAgent", void.class, String.class).invoke(agent.toString());
						return Accessor.getReference(systemLoader.loadClass(AgentName), "instrumentation");
					} catch (Throwable trouble) {
						throw Exceptions.message(trouble, message -> agent + ": " + message);
					}
				});
		});
	}
}
