package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.Objects;
import net.gudenau.lib.unsafe.Unsafe;

/**
 A class definition builder.
 The only required parameter in all cases is {@link #classFile(byte[]) a class file}
 but most likely in most cases {@link #loader(ClassLoader) a class loader} will have to be specified.

 @param <T> the type of the class to define
 @since 4.6.0 */
public class ClassDefiner<T> {
    private static final MethodHandle defineClass = Invoker.findVirtual(ClassLoader.class, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
    private static final MethodHandle bufferDefineClass = Invoker.findVirtual(ClassLoader.class, "defineClass", Class.class, String.class, ByteBuffer.class, ProtectionDomain.class);
    private static final MethodHandle secureDefineClass = Invoker.findVirtual(SecureClassLoader.class, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, CodeSource.class);
    private static final MethodHandle secureBufferDefineClass = Invoker.findVirtual(SecureClassLoader.class, "defineClass", Class.class, String.class, ByteBuffer.class, CodeSource.class);

    private String name;
    private ClassLoader loader;
    private boolean secure;
    private Object classFile;
    private int offset;
    private int length;
    private ProtectionDomain domain;
    private CodeSource source;
    private boolean unsafe;
    private boolean initialize;

    /**
     Construct a class definer and set its loader to the caller's class' loader.
     */
    public ClassDefiner() {
        this.loader = StackFrames.caller().getClassLoader();
    }

    /**
     Construct a class definer with a wildcard type argument.

     @return a new class definer
     */
    public static ClassDefiner<?> make() {
        return new ClassDefiner().loader(StackFrames.caller().getClassLoader());
    }

    /**
     Set an expected name for the class for validation; completely optional and may be {@code null}.

     @param name the expected name for the class
     @return {@code this}
     */
    public ClassDefiner<T> name(String name) {
        this.name = name.replace('/', '.');

        return this;
    }

    /**
     Set the loader that should define the class; {@code null} means the bootstrap class loader.
     The default is the loader of the class of the caller of this definer's constructor or factory method {@link #make()}.
     <p>
     {@code loader == null =>} {@link #unsafe()}

     @param loader the defining loader of the class
     @return {@code this}
     */
    public ClassDefiner<T> loader(ClassLoader loader) {
        this.loader = loader;
        this.secure = false;

        if (loader == null) {
            this.unsafe = true;
        }

        return this;
    }

    /**
     Set the {@link SecureClassLoader} that should define the class by one of {@link SecureClassLoader#defineClass the CodeSource-accepting methods}
     and the {@link CodeSource} of the {@link ProtectionDomain} of the class.

     @param loader the defining loader of the class; must not be {@code null}
     @param source the code source of the class' protection domain
     @return {@code this}
     @throws NullPointerException if {@code loader == null}
     @since 4.8.0
     */
    public ClassDefiner<T> secureLoader(SecureClassLoader loader, CodeSource source) {
        this.loader = Objects.requireNonNull(loader);
        this.secure = true;
        this.source = source;

        return this;
    }

    /**
     Set the class file of the class to be defined.

     @param classFile an array that contains the class file
     @param offset the offset of the class file in {@code classFile}
     @param length the length of the class file in {@code classFile}
     @return {@code this}
     @throws IllegalArgumentException if {@code offset < 0 || offset + length > classFile.length}
     @throws NullPointerException if {@code classFile == null}
     */
    public ClassDefiner<T> classFile(byte[] classFile, int offset, int length) {
        if (offset < 0) throw new IllegalArgumentException("offset == " + offset);
        if (offset + length > classFile.length) throw new IllegalArgumentException("offset + length > classFile.length");

        this.classFile = Objects.requireNonNull(classFile);
        this.offset = offset;
        this.length = length;

        return this;
    }

    /**
     Set the class file of the class to be defined.

     @param classFile the class file as a byte array
     @return {@code this}
     @throws NullPointerException if {@code classFile == null}
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
     Copy the class file of a type.

     @param resourceLoader the class loader whereby to locate the type
     @param type the binary or internal name of the type
     @return {@code this}
     @since 4.8.0
     */
    public ClassDefiner<T> classFile(ClassLoader resourceLoader, String type) {
        this.classFile(Classes.classFile(resourceLoader, type));

        return this;
    }

    /**
     Copy the class file of a type by the loader of this method's caller's class.

     @param type the binary or internal name of the type
     @return {@code this}
     @since 4.8.0
     */
    public ClassDefiner<T> classFile(String type) {
        this.classFile(Classes.classFile(StackFrames.caller().getClassLoader(), type));

        return this;
    }

    /**
     Generate a {@link ProtectionDomain} with a given {@link CodeSource}.

     @param source the source of the protection domain of the class to be defined
     @return {@code this}
     */
    public ClassDefiner<T> source(CodeSource source) {
        this.secure = false;

        if (source != null) {
            this.domain = new ProtectionDomain(source, null, this.loader, null);
        }

        return this;
    }

    /**
     Set the protection domain of the class to be defined.

     @param domain the protection domain of the class to be defined
     @return {@code this}
     */
    public ClassDefiner<T> protectionDomain(ProtectionDomain domain) {
        this.domain = domain;
        this.secure = false;

        return this;
    }

    /**
     Use {@link jdk.internal.misc.Unsafe#defineClass Unsafe::defineClass} in order to define the class.

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
     Inherit class file and protection domain from an existing type.

     @return {@code this}
     @throws NullPointerException if {@code type == null}
     */
    public ClassDefiner<T> from(Class<?> type) {
        this.protectionDomain(Objects.requireNonNull(type).getProtectionDomain()).classFile(Classes.classFile(type));

        return this;
    }

    /**
     Define the class and initialize it if {@link #initialize()} was invoked.

     @return the class
     */
    public Class<T> define() {
        var array = this.classFile instanceof byte[];
        var type = this.unsafe ? this.unsafeDefine() : this.secure ? this.secureDefine(array) : this.define(array);

        return (Class<T>) (this.initialize ? Classes.initialize(type) : type);
    }

    private ClassLoader loader() {
        if (this.loader == null) {
            throw new IllegalStateException("loader is null");
        }

        return this.loader;
    }

    private Class<?> unsafeDefine() {
        var bytecode = this.classFile;
        byte[] array;
        int length;

        if (bytecode instanceof ByteBuffer buffer) {
            buffer.get(array = new byte[buffer.remaining()]);
            length = array.length;
        } else {
            array = (byte[]) bytecode;
            length = this.length;
        }

        return Unsafe.defineClass(this.name, array, this.offset, length, this.loader, this.domain);
    }

    private Class<?> secureDefine(boolean array) {
        return (Class<?>) (array
            ? secureDefineClass.invoke(this.loader(), this.name, this.classFile, this.offset, this.length, this.source)
            : secureBufferDefineClass.invoke(this.loader(), this.name, this.classFile, this.source)
        );
    }

    private Class<?> define(boolean array) {
        return (Class<?>) (array
            ? defineClass.invoke(this.loader(), this.name, this.classFile, this.offset, this.length, this.domain)
            : bufferDefineClass.invoke(this.loader(), this.name, this.classFile, this.domain)
        );
    }
}
