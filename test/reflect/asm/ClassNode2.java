package reflect.asm;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.auoeke.reflect.Classes;
import net.gudenau.lib.unsafe.Unsafe;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class ClassNode2 extends ClassNode implements BitField, Opcodes {
	static final ClassLoader loader = ClassNode2.class.getClassLoader();

	private ClassReader reader;

	public ClassNode2() {
		super(ASM9);
	}

	public ClassNode2(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this();

		this.visit(version, access, name, signature, superName, interfaces);
	}

	public ClassNode2(int access, String name, String signature, String superName, String[] interfaces) {
		this(V16, access, name, signature, superName, interfaces);
	}

	public ClassNode2(int access, String name, String superName) {
		this(access, name, null, superName, null);
	}

	public ClassNode2(int access, String name) {
		this(access, name, "java/lang/Object");
	}

	public ClassNode2 reader(Path clas) {
		return this.reader(Files.newInputStream(clas));
	}

	public ClassNode2 reader(String clas) {
		this.reader = new ClassReader(clas);

		return this;
	}

	public ClassNode2 reader(ZipFile entry, ZipEntry clas) {
		return this.reader(entry.getInputStream(clas));
	}

	public ClassNode2 reader(InputStream clas) {
		this.reader = new ClassReader(clas);

		return this;
	}

	public ClassNode2 reader(byte[] clas) {
		this.reader = new ClassReader(clas);

		return this;
	}

	public ClassNode2 read(int flags) {
		this.reader.accept(this, flags);

		return this;
	}

	public ClassNode2 read() {
		return this.read(0);
	}

	@Override
	public int flags() {
		return this.access;
	}

	@Override
	public FieldNode visitField(int access, String name, String descriptor, String signature, Object value) {
		return (FieldNode) super.visitField(access, name, descriptor, signature, value);
	}

	public FieldNode visitField(int access, String name, String descriptor) {
		return this.visitField(access, name, descriptor, null, null);
	}

	@Override
	public MethodNode2 visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		var method = new MethodNode2(this, access, name, descriptor, signature, exceptions);
		this.methods.add(method);

		return method;
	}

	public MethodNode2 visitMethod(int access, String name, String descriptor) {
		return this.visitMethod(access, name, descriptor, null, null);
	}

	public MethodNode2 clinit() {
		return this.visitMethod(ACC_STATIC, "<clinit>", "()V");
	}

	public void defaultConstructor() {
		var init = this.visitMethod(0, "<init>", "()V", null, null);
		init.visitVarInsn(ALOAD, 0);
		init.visitMethodInsn(INVOKESPECIAL, this.superName, "<init>", "()V", false);
		init.visitInsn(RETURN);
	}

	public <T> Class<T> define() {
		var bytecode = this.bytecode();
		return Unsafe.defineClass(null, bytecode, 0, bytecode.length, loader, ClassNode2.class.getProtectionDomain());
	}

	public <T> Class<T> init() {
		return Classes.initialize(this.define());
	}

	public byte[] bytecode() {
		var writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		this.accept(writer);

		return writer.toByteArray();
	}
}
