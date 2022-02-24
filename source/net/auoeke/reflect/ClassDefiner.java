package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.net.JarURLConnection;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.Objects;
import net.gudenau.lib.unsafe.Unsafe;

/**
 A class definition builder.
 The only required parameter in all cases is {@linkplain #classFile(byte[]) a class file}
 but most likely in most cases {@linkplain #loader(ClassLoader) a class loader} will have to be specified.

 @param <T> the type of the class to define
 @since 4.6.0
 */
public class ClassDefiner<T> {
    private static final MethodHandle defineClass = Invoker.findVirtual(ClassLoader.class, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
    private static final MethodHandle bufferDefineClass = Invoker.findVirtual(ClassLoader.class, "defineClass", Class.class, String.class, ByteBuffer.class, ProtectionDomain.class);
    private static final MethodHandle secureDefineClass = Invoker.findVirtual(SecureClassLoader.class, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, CodeSource.class);
    private static final MethodHandle secureBufferDefineClass = Invoker.findVirtual(SecureClassLoader.class, "defineClass", Class.class, String.class, ByteBuffer.class, CodeSource.class);

    private String name;
    private ClassLoader loader;
    private boolean secure;
    private Class<?> type;
    private Object classFile;
    private int offset;
    private int length;
    private ProtectionDomain domain;
    private CodeSource source;
    private boolean unsafe;
    private boolean initialize;

    /**
     Construct a class definer with a wildcard type argument.

     @return a new class definer
     */
    public static ClassDefiner<?> make() {
        return new ClassDefiner<>();
    }

    /**
     Set an expected name for the class for security checks; completely optional and may be {@code null}.

     @param name the expected name for the class
     @return {@code this}
     */
    public ClassDefiner<T> name(String name) {
        this.name = name.replace('/', '.');

        return this;
    }

    /**
     Set the loader that should define the class; {@code null} (default) means the bootstrap class loader.

     @param loader the defining loader of the class
     @return {@code this}
     */
    public ClassDefiner<T> loader(ClassLoader loader) {
        this.loader = loader;
        this.secure = false;

        return this;
    }

    /**
     Set the {@linkplain SecureClassLoader secure loader} that should define the class by one of {@linkplain SecureClassLoader#defineClass the CodeSource-accepting methods}.

     @param loader the defining loader of the class; must not be {@code null}
     @return {@code this}
     */
    public ClassDefiner<T> secureLoader(SecureClassLoader loader) {
        this.loader = Objects.requireNonNull(loader);
        this.secure = true;

        return this;
    }

    /**
     Set the class file of the class to be defined.

     @param classFile an array that contains the class file
     @param offset   the offset of the class file in {@code classFile}
     @param length   the length of the class file in {@code classFile}
     @return {@code this}
     */
    public ClassDefiner<T> classFile(byte[] classFile, int offset, int length) {
        this.classFile = classFile;
        this.offset = offset;
        this.length = length;

        return this;
    }

    /**
     Set the class file of the class to be defined.

     @param classFile the class file as a byte array
     @return {@code this}
     */
    public ClassDefiner<T> classFile(byte[] classFile) {
        return this.classFile(classFile, 0, classFile.length);
    }

    /**
     Set the class file of the class to be defined.

     @param classFile the class file buffer
     @return {@code this}
     */
    public ClassDefiner<T> classFile(ByteBuffer classFile) {
        this.classFile = classFile;

        return this;
    }

    /**
     Set the {@link CodeSource} of the protection domain of the class to be defined.

     @param source the source of the protection domain of the class to be defined
     @return {@code this}
     */
    public ClassDefiner<T> source(CodeSource source) {
        this.source = source;

        return this;
    }

    /**
     Set the protection domain of the class to be defined.

     @param domain the protection domain of the class to be defined
     @return {@code this}
     */
    public ClassDefiner<T> protectionDomain(ProtectionDomain domain) {
        this.domain = domain;

        return this;
    }

    /**
     Use {@linkplain jdk.internal.misc.Unsafe#defineClass Unsafe} in order to define the class.

     @return {@code this}
     */
    public ClassDefiner<T> unsafe() {
        this.unsafe = true;

        return this;
    }

    /**
     Initialize the class immediately after its definition.

     @return {@code this}
     */
    public ClassDefiner<T> initialize() {
        this.initialize = true;

        return this;
    }

    /**
     Inherit class file and protection domain from an existing class.

     @return {@code this}
     */
    public ClassDefiner<T> from(Class<?> type) {
        this.type = type;

        return this;
    }

    /**
     Define the class.

     @return the class
     */
    public Class<T> define() {
        var array = this.classFile() instanceof byte[];
        var type = this.isUnsafe() ? this.unsafeDefine() : this.secure ? this.secureDefine(array) : this.define(array);

        return (Class<T>) (this.initialize ? Classes.initialize(type) : type);
    }

    private ClassLoader loader() {
        if (this.loader == null) {
            throw new IllegalStateException("loader is null");
        }

        return this.loader;
    }

    private Object classFile() {
        if (this.classFile == null) {
            if (this.type == null) {
                throw new IllegalStateException("bytecode is null");
            }

            this.classFile(Classes.classFile(this.type));
        }

        return this.classFile;
    }

    private CodeSource source() {
        if (this.source == null && this.domain != null) {
            this.source = this.domain.getCodeSource();
        }

        return this.source;
    }

    private ProtectionDomain domain() {
        if (this.domain == null) {
            if (this.source != null) {
                var url = this.source.getLocation();
                this.domain = new ProtectionDomain(new CodeSource(url, url != null && url.openConnection() instanceof JarURLConnection jar ? jar.getCertificates() : null), null, this.loader, null);
            } else if (this.type != null) {
                this.domain = this.type.getProtectionDomain();
            }
        }

        return this.domain;
    }

    private boolean isUnsafe() {
        return this.unsafe || this.loader == null;
    }

    private Class<?> unsafeDefine() {
        var bytecode = this.classFile();
        byte[] array;
        int length;

        if (bytecode instanceof ByteBuffer buffer) {
            buffer.get(array = new byte[buffer.remaining()]);
            length = array.length;
        } else {
            array = (byte[]) bytecode;
            length = this.length;
        }

        return Unsafe.defineClass(this.name, array, this.offset, length, this.loader, this.domain());
    }

    private Class<?> secureDefine(boolean array) {
        return (Class<?>) (array
            ? secureDefineClass.invoke(this.loader(), this.name, this.classFile(), this.offset, this.length, this.source())
            : secureBufferDefineClass.invoke(this.loader(), this.name, this.classFile(), this.source())
        );
    }

    private Class<?> define(boolean array) {
        return (Class<?>) (array
            ? defineClass.invoke(this.loader(), this.name, this.classFile(), this.offset, this.length, this.domain())
            : bufferDefineClass.invoke(this.loader(), this.name, this.classFile(), this.domain())
        );
    }
}
