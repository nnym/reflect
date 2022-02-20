package net.auoeke.reflect;

import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import net.gudenau.lib.unsafe.Unsafe;

/**
 Miscellaneous utilities.
 */
public class Reflect {
    public static boolean securityDisabled;

    /**
     The default class loader for operations that require a class loader.
     */
    public static ClassLoader defaultClassLoader = Reflect.class.getClassLoader();

    /**
     Attach the current JVM to itself and acquire an {@link Instrumentation} instance that supports all optional operations.
     <b><p>
     Note that this method is not guaranteed to work with all JVM vendors.
     <br>
     Note that this method may stop working in the future; see the comments in {@linkplain sun.tools.attach.HotSpotVirtualMachine#HotSpotVirtualMachine HotSpotVirtualMachine::new}.
     </b></p>

     @return an {@link Instrumentation} instance if attachment was successful or {@code null} otherwise
     */
    public static Instrumentation instrumentation() {
        if (Agent.instrumentation == null) tryRun(() -> {
            // Attempt both methods.
            tryRun(() -> Accessor.putReference(Class.forName("openj9.internal.tools.attach.target.AttachHandler"), "allowAttachSelf", "true"));
            tryRun(() -> Accessor.<Map<String, String>>getReference(Class.forName("jdk.internal.misc.VM"), "savedProps").put("jdk.attach.allowAttachSelf", "true"));
            tryRun(() -> {
                var HotSpotVirtualMachine = Class.forName("sun.tools.attach.HotSpotVirtualMachine");
                Unsafe.ensureClassInitialized(HotSpotVirtualMachine);
                Accessor.putBoolean(HotSpotVirtualMachine, "ALLOW_ATTACH_SELF", true);
            });

            var vm = VirtualMachine.attach(String.valueOf(ProcessHandle.current().pid()));

            try {
                var source = Agent.class.getProtectionDomain().getCodeSource().getLocation().getPath();

                if (!source.endsWith(".jar")) {
                    var agent = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "net.auoeke/reflect")).resolve("agent.jar");
                    source = agent.toString();

                    if (Files.exists(agent)) try {
                        vm.loadAgent(source);
                        return;
                    } catch (AgentLoadException exception) {}

                    var jar = new JarOutputStream(Files.newOutputStream(agent));
                    jar.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
                    jar.write(Agent.class.getClassLoader().resources(JarFile.MANIFEST_NAME)
                        .filter(manifest -> Agent.class.getName().equals(new Manifest(manifest.openStream()).getMainAttributes().getValue("Agent-Class")))
                        .findAny().get().openStream().readAllBytes()
                    );
                    jar.putNextEntry(new ZipEntry(Agent.class.getName().replace('.', '/') + ".class"));
                    jar.write(Agent.class.getResourceAsStream("Agent.class").readAllBytes());
                    jar.close();
                }

                vm.loadAgent(source);
            } finally {
                vm.detach();
            }
        });

        return Agent.instrumentation;
    }

    /**
     Disable {@linkplain System#getSecurityManager() the deprecated security manager}.
     <p>
     Using this method is discouraged.
     */
    @SuppressWarnings("removal")
    public static void disableSecurity() {
        if (!securityDisabled) {
            Accessor.putReference(System.class, "security", null);
            securityDisabled = true;
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

     @apiNote This method can break (and has broken some part of Gson) code that relies on the aforementioned methods filtering methods.
     */
    public static void clearMethodFilterMap() {
        Accessor.putReferenceVolatile(Classes.Reflection, "methodFilterMap", new HashMap<>());
    }

    static boolean tryRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            return false;
        }

        return true;
    }

    static <T> T runNull(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            return null;
        }
    }
}
