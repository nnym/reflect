package net.auoeke.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Member;

import static net.auoeke.dycon.Dycon.*;

/**
 @since 1.2.0
 */
public class ConstantPool {
	private final Object constantPool;

	public ConstantPool(Class<?> klass) {
		this.constantPool = JavaLangAccess.getConstantPool(klass);
	}

	public int getSize() {
		return (int) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getSize", int.class)).invoke(this.constantPool);
	}

	public Class<?> getClassAt(int index) {
		return (Class<?>) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getClassAt", Class.class, int.class)).invoke(this.constantPool, index);
	}

	public Class<?> getClassAtIfLoaded(int index) {
		return (Class<?>) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getClassAtIfLoaded", Class.class, int.class)).invoke(this.constantPool, index);
	}

	public Member getMethodAt(int index) {
		return (Member) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getMethodAt", Member.class, int.class)).invoke(this.constantPool, index);
	}

	public Member getMethodAtIfLoaded(int index) {
		return (Member) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getMethodAtIfLoaded", Member.class, int.class)).invoke(this.constantPool, index);
	}

	public Field getFieldAt(int index) {
		return (Field) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getFieldAt", Field.class, int.class)).invoke(this.constantPool, index);
	}

	public Field getFieldAtIfLoaded(int index) {
		return (Field) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getFieldAtIfLoaded", Field.class, int.class)).invoke(this.constantPool, index);
	}

	public String getMemberRefInfoAt(int index) {
		return (String) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getMemberRefInfoAt", String[].class, int.class)).invoke(this.constantPool, index);
	}

	public int getIntAt(int index) {
		return (int) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getIntAt", int.class, int.class)).invoke(this.constantPool, index);
	}

	public long getLongAt(int index) {
		return (long) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getLongAt", long.class, int.class)).invoke(this.constantPool, index);
	}

	public float getFloatAt(int index) {
		return (float) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getFloatAt", float.class, int.class)).invoke(this.constantPool, index);
	}

	public double getDoubleAt(int index) {
		return (double) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getDoubleAt", double.class, int.class)).invoke(this.constantPool, index);
	}

	public String getStringAt(int index) {
		return (String) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getStringAt", String.class, int.class)).invoke(this.constantPool, index);
	}

	public String getUTF8At(int index) {
		return (String) ldc(() -> Invoker.findVirtual(Classes.ConstantPool, "getUTF8At", String.class, int.class)).invoke(this.constantPool, index);
	}
}
