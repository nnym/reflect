package net.auoeke.reflect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import com.sun.tools.attach.VirtualMachine;

/**
 Miscellaneous utilities.
 */
public class Reflect {
    /**
     @deprecated Use {@code System.getSecurityManager() == null}.
     */
    @Deprecated(forRemoval = true, since = "4.9.0")
    public static boolean securityDisabled;

    /**
     Attach the current JVM to itself and acquire an {@link Instrumentation} instance that supports all optional operations.
     <p><b>
     Note that this method is not guaranteed to work with all JVM vendors.
     <br>
     Note that this method may stop working in the future; see the comments in {@linkplain sun.tools.attach.HotSpotVirtualMachine#HotSpotVirtualMachine HotSpotVirtualMachine::new}.
     </b>

     @return a {@link Result} containing an {@link Instrumentation} instance if attachment was successful
     @since 4.9.0
     */
    public static Result<Instrumentation> instrument() {
        if (Agent.result == null) {
            var result = new Result<Instrumentation>();

            result.map(() -> {
                result.andSuppress(() -> Accessor.putReference(Class.forName("openj9.internal.tools.attach.target.AttachHandler"), "allowAttachSelf", "true"));
                result.andSuppress(() -> Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true"));
                result.andSuppress(() -> Accessor.putBoolean(Classes.initialize(Class.forName("sun.tools.attach.HotSpotVirtualMachine")), "ALLOW_ATTACH_SELF", true));

                var vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));

                try {
                    var source = Stream.concat(
                            Optional.ofNullable(result.andSuppress(() -> Agent.class.getProtectionDomain().getCodeSource().getLocation())).map(url -> {
                                if (url.openConnection() instanceof JarURLConnection jar) {
                                    var manifest = jar.getManifest();
                                    return manifest == null ? null : Map.entry(jar.getJarFileURL().getPath(), manifest);
                                }

                                var manifest = Path.of(url.toURI()).resolve(JarFile.MANIFEST_NAME);

                                if (!Files.exists(manifest)) {
                                    return null;
                                }

                                try (var input = Files.newInputStream(manifest)) {
                                    return Map.entry("", new Manifest(input));
                                }
                            }).stream(),
                            Agent.class.getClassLoader().resources(JarFile.MANIFEST_NAME).map(url -> {
                                var connection = url.openConnection();

                                if (connection instanceof JarURLConnection jar) {
                                    return Map.entry(jar.getJarFileURL().getPath(), jar.getManifest());
                                }

                                try (var input = connection.getInputStream()) {
                                    return Map.entry("", new Manifest(input));
                                }
                            })
                        )
                        .filter(entry -> entry != null && Agent.class.getName().equals(entry.getValue().getMainAttributes().getValue("Agent-Class")))
                        .findFirst()
                        .orElseThrow(() -> new FileNotFoundException("no MANIFEST.MF with \"Agent-Class: " + Agent.class.getName() + '"'));
                    var sourceString = source.getKey();

                    if (!sourceString.endsWith(".jar")) {
                        var agent = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "net.auoeke/reflect")).resolve("agent.jar");
                        sourceString = agent.toString();

                        try (var jar = new JarOutputStream(Files.newOutputStream(agent))) {
                            jar.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                            source.getValue().write(jar);
                            jar.putNextEntry(new ZipEntry(Agent.class.getName().replace('.', '/') + ".class"));
                            jar.write(Classes.classFile(Agent.class));
                        }
                    }

                    vm.loadAgent(sourceString);

                    if (Agent.class.getClassLoader() == ClassLoader.getSystemClassLoader()) {
                        return Agent.instrumentation;
                    }

                    return Accessor.getReference(ClassLoader.getSystemClassLoader().loadClass(Agent.class.getName()), "instrumentation");
                } finally {
                    vm.detach();
                }
            });

            Agent.result = result;
        }

        return Agent.result;
    }

    /**
     Attach the current JVM to itself and acquire an {@link Instrumentation} instance that supports all optional operations.

     @return an {@link Instrumentation} instance if attachment was successful or else {@code null}
     @see #instrument
     */
    @Deprecated(forRemoval = true, since = "4.9.0")
    public static Instrumentation instrumentation() {
        return instrument().orNull();
    }

    /**
     Disable {@linkplain System#getSecurityManager() the deprecated security manager}.
     <p>
     Using this method is discouraged.
     */
    @SuppressWarnings("removal")
    public static void disableSecurity() {
        if (System.getSecurityManager() != null) {
            Accessor.putReference(System.class, "security", null);
        }
    }

    /**
     Clears the reflection field filter map, preventing {@link Class#getFields} and {@link Class#getDeclaredFields} from filtering,
     as defined in {@linkplain jdk.internal.reflect.Reflection Reflection}.

     @apiNote this method can break code that relies on the aforementioned methods filtering fields.
     */
    public static void clearFieldFilterMap() {
        Accessor.putReferenceVolatile(Classes.Reflection, "fieldFilterMap", new HashMap<>());
    }

    /**
     Clears the reflection method filter map, preventing {@link Class#getMethods} and {@link Class#getDeclaredMethods} from filtering
     as defined in {@linkplain jdk.internal.reflect.Reflection Reflection}.
     <br>

     @apiNote this method can break code (and has broken some part of Gson) that relies on the aforementioned methods filtering methods.
     */
    public static void clearMethodFilterMap() {
        Accessor.putReferenceVolatile(Classes.Reflection, "methodFilterMap", new HashMap<>());
    }

    static boolean tryRun(Runnable runnable) {
        return runNull(() -> {
            runnable.run();
            return true;
        }) != null;
    }

    static <T> T runNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            return null;
        }
    }
}
