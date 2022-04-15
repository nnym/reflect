package net.auoeke.reflect;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.function.Consumer;

/**
 A functional interface from {@link ClassFileTransformer}.

 @since 4.6.0
 */
@FunctionalInterface
public interface ClassTransformer extends ClassFileTransformer {
    /**
     This method is intended as a "functional cast" for lambda expressions.

     @since 5.1.0
     */
    static ClassTransformer of(ClassTransformer transformer) {
        return transformer;
    }

    @Override byte[] transform(Module module, ClassLoader loader, String name, Class<?> type, ProtectionDomain domain, byte[] classFile);

    /**
     Return a {@link ClassTransformer} that invokes this transformer only if {@code type} matches the {@code type} passed to {@link #transform} and otherwise returns {@code null}.

     @param type the new transformer's target type
     @return a new transformer that runs this transformer only for {@code type} and otherwise returns {@code null}
     @since 5.1.0
     */
    default ClassTransformer ofType(Class<?> type) {
        return (module, loader, name, t, domain, classFile) -> t == type ? this.transform(module, loader, name, t, domain, classFile) : null;
    }

    /**
     Return a {@link ClassTransformer} that invokes this transformer only if {@code type} matches the {@code name} passed to {@link #transform} and otherwise returns {@code null}.

     @param type the name of the new transformer's target types
     @return a new transformer that runs this transformer only for types named {@code type} and otherwise returns {@code null}
     @since 5.1.0
     */
    default ClassTransformer ofType(String type) {
        type = type.replace('.', '/');
        return (module, loader, name, t, domain, classFile) -> name.equals(type) ? this.transform(module, loader, name, t, domain, classFile) : null;
    }

    /**
     Return a {@link ClassTransformer} that wraps this transformer in a {@code try} block and handles exceptions by invoking {@code handler} and rethrowing.

     @param handler an exception handler
     @return a new transformer that catches exceptions thrown by this transformer and handles them by invoking {@code handler} and rethrowing
     @since 5.1.0
     */
    default ClassTransformer exceptionHandling(Consumer<? super Throwable> handler) {
        return (module, loader, name, type, domain, classFile) -> {
            try {
                return this.transform(module, loader, name, type, domain, classFile);
            } catch (Throwable trouble) {
                handler.accept(trouble);
                throw trouble;
            }
        };
    }

    /**
     Return a {@link ClassTransformer} that logs exceptions thrown by this transformer.

     @return a new transformer that logs exceptions thrown by this transformer
     @since 5.1.0
     */
    default ClassTransformer exceptionLogging() {
        return this.exceptionHandling(Throwable::printStackTrace);
    }

    /**
     Return a {@link ClassTransformer} that unregisters itself if the result of {@link #transform} is not {@code null}.

     @param instrumentation the {@link Instrumentation} instance wherefrom to unregister the transformer
     @return a new transformer that unregisters itself if the result of {@link #transform} is not {@code null}
     @since 5.1.0
     */
    default ClassTransformer singleUse(Instrumentation instrumentation) {
        return new ClassTransformer() {
            @Override public byte[] transform(Module module, ClassLoader loader, String name, Class<?> type, ProtectionDomain domain, byte[] classFile) {
                var result = ClassTransformer.this.transform(module, loader, name, type, domain, classFile);

                if (result != null) {
                    instrumentation.removeTransformer(this);
                }

                return result;
            }
        };
    }
}
