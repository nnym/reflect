package net.auoeke.reflect;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 A functional interface from {@link ClassFileTransformer}.

 @since 4.6.0
 */
@FunctionalInterface
public interface ClassTransformer extends ClassFileTransformer {
    @Override byte[] transform(Module module, ClassLoader loader, String name, Class<?> type, ProtectionDomain domain, byte[] classFile);
}
