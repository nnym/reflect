package reflect.other;

import net.auoeke.reflect.Fields;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import reflect.asm.ClassNode2;
import reflect.util.Logger;

@Disabled
@Testable
public class BytecodeTest implements Opcodes {
	@Test void nonVirtual() {
		var object = Type.getInternalName(Object.class);
		var a = Type.getInternalName(A.class);
		var b = Type.getInternalName(B.class);
		var c = Type.getInternalName(BytecodeTest.class) + "$C";
		var invoker = Type.getInternalName(BytecodeTest.class) + "$Invoker";

		var node = new ClassNode2(V1_1, 0, c, null, b, null);
		node.defaultConstructor();

		MethodNode print = node.visitMethod(ACC_STATIC, "print", "(L" + c + ";)V", null, null);
		print.visitVarInsn(ALOAD, 0);
		print.visitMethodInsn(INVOKESPECIAL, a, "print", "()V", false);
		print.visitInsn(RETURN);

		node.define();

		C.print(new C());
	}

	@Test void finalFieldMayHaveBeenAssigned() {
		//		class A {final int a;
		//			A() {this.a = 1;}
		//			A(int b) {this(); this.a = b;}}
		var klass = new ClassNode2(ACC_PUBLIC, "A");
		klass.visitField(ACC_FINAL, "a", "I");

		var ctr0 = klass.visitMethod(0, "<init>", "()V");
		ctr0.visitVarInsn(ALOAD, 0);
		ctr0.method(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		ctr0.visitVarInsn(ALOAD, 0);
		ctr0.visitInsn(ICONST_1);
		ctr0.visitFieldInsn(PUTFIELD, "A", "a", "I");
		ctr0.visitInsn(RETURN);

		var ctr1 = klass.visitMethod(ACC_PUBLIC, "<init>", "(I)V");
		ctr1.visitVarInsn(ALOAD, 0);
		ctr1.visitMethodInsn(INVOKESPECIAL, "A", "<init>", "()V", false);
		ctr1.visitVarInsn(ALOAD, 0);
		ctr1.visitVarInsn(ILOAD, 1);
		ctr1.visitFieldInsn(PUTFIELD, "A", "a", "I");
		ctr1.visitInsn(RETURN);

		Class<?> A = klass.define();
		A.getDeclaredConstructor(int.class).newInstance(2);
	}

	@Test void defaultMethod() {
		var node = new ClassNode2(ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE, "defaultMethod");

		MethodNode defaultMethod = node.visitMethod(ACC_PUBLIC, "defaultMethod", "()V");
		defaultMethod.visitInsn(RETURN);

		node.define();
	}

	@Test void interfaceField() {
		var node = new ClassNode2(ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT, "interfaceField");
		node.visitField(ACC_PUBLIC | ACC_ABSTRACT | ACC_STATIC | ACC_FINAL, "field", "I");
		node.define().getFields();
	}

	// Allowed in initializers.
	@Test void finalReassignment() {
		var node = new ClassNode2(ACC_PUBLIC, "finalReassignment");
		var field = node.visitField(ACC_STATIC | ACC_FINAL, "final", "I");
		var clinit = node.clinit();

		clinit.visitInsn(ICONST_4);
		clinit.field(PUTSTATIC, field.name);
		clinit.printI(() -> clinit.field(GETSTATIC, field.name));

		clinit.visitInsn(ICONST_M1);
		clinit.field(PUTSTATIC, field.name);
		clinit.printI(() -> clinit.field(GETSTATIC, field.name));

		clinit.visitInsn(RETURN);

		node.init();
	}

	// Not allowed.
	@Test void sameNameFields() {
		var node = new ClassNode2(0, "sameNameFields");
		node.visitField(ACC_STATIC, "field", "I", null, null);
		node.visitField(0, "field", "I", null, null);

		Fields.of(node.init()).forEach(Logger::log);
	}

	static class A {
		void print() {
			System.out.println("A");
		}
	}

	static class B extends A {
		@Override
		void print() {
			System.out.println("B");
		}
	}

	static class C extends B {
		static native void print(C c);
	}
}
