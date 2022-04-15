package net.auoeke.reflect;

import java.io.FileNotFoundException;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
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

            result.map(() -> {
                result.andSuppress(() -> Accessor.putReference(Class.forName("openj9.internal.tools.attach.target.AttachHandler"), "allowAttachSelf", "true"));
                result.andSuppress(() -> Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true"));
                result.andSuppress(() -> Accessor.putBoolean(Class.forName("sun.tools.attach.HotSpotVirtualMachine"), "ALLOW_ATTACH_SELF", true));

                var agentClass = Class.forName(Reflect.class.getPackageName() + ".Agent");
                var vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));

                try {
                    var source = Stream.concat(
                            Optional.ofNullable(result.andSuppress(() -> agentClass.getProtectionDomain().getCodeSource().getLocation())).map(url -> {
                                if (url.openConnection() instanceof JarURLConnection jar) {
                                    var manifest = jar.getManifest();
                                    return manifest == null ? null : Map.entry(path(jar), manifest);
                                }

                                var manifest = Path.of(url.toURI()).resolve(JarFile.MANIFEST_NAME);

                                if (!Files.exists(manifest)) {
                                    return null;
                                }

                                try (var input = Files.newInputStream(manifest)) {
                                    return Map.entry("", new Manifest(input));
                                }
                            }).stream(),
                            agentClass.getClassLoader().resources(JarFile.MANIFEST_NAME).map(url -> {
                                var connection = url.openConnection();

                                if (connection instanceof JarURLConnection jar) {
                                    return Map.entry(path(jar), jar.getManifest());
                                }

                                try (var input = connection.getInputStream()) {
                                    return Map.entry("", new Manifest(input));
                                }
                            })
                        )
                        .filter(entry -> entry != null && agentClass.getName().equals(entry.getValue().getMainAttributes().getValue("Agent-Class")))
                        .findFirst()
                        .orElseThrow(() -> new FileNotFoundException("no MANIFEST.MF with \"Agent-Class: %s\"".formatted(agentClass.getName())));
                    var sourceString = source.getKey();

                    if (!sourceString.endsWith(".jar")) {
                        var agent = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "net.auoeke/reflect")).resolve("agent.jar");
                        sourceString = agent.toString();

                        try (var jar = new JarOutputStream(Files.newOutputStream(agent))) {
                            jar.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                            source.getValue().write(jar);
                            jar.putNextEntry(new ZipEntry(agentClass.getName().replace('.', '/') + ".class"));
                            jar.write(Classes.classFile(agentClass));
                        }
                    }

                    try {
                        vm.loadAgent(sourceString);
                    } catch (AgentLoadException exception) {
                        throw Exceptions.message(exception, message -> sourceString + ": " + message);
                    }

                    return agentClass.getClassLoader() != ClassLoader.getSystemClassLoader() ? Accessor.getReference(ClassLoader.getSystemClassLoader().loadClass(agentClass.getName()), "instrumentation")
                        : agentClass.getClassLoader() == Reflect.class.getClassLoader() ? Agent.instrumentation
                        : Accessor.getReference(agentClass, "instrumentation");
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

    private static String path(JarURLConnection jar) {
        // Must not use URL::getPath because it prepends '/' on Windows.
        return Path.of(jar.getJarFileURL().toURI()).toString();
    }
}
