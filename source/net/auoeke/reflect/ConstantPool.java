package net.auoeke.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

import static net.auoeke.reflect.Reflect.runNull;

public class ConstantPool {
    private static final MethodHandle getSize = Invoker.findVirtual(Classes.ConstantPool, "getSize", int.class);
    private static final MethodHandle getClassAt = Invoker.findVirtual(Classes.ConstantPool, "getClassAt", Class.class, int.class);
    private static final MethodHandle getClassAtIfLoaded = Invoker.findVirtual(Classes.ConstantPool, "getClassAtIfLoaded", Class.class, int.class);
    private static final MethodHandle getMethodAt = Invoker.findVirtual(Classes.ConstantPool, "getMethodAt", Member.class, int.class);
    private static final MethodHandle getMethodAtIfLoaded = Invoker.findVirtual(Classes.ConstantPool, "getMethodAtIfLoaded", Member.class, int.class);
    private static final MethodHandle getFieldAt = Invoker.findVirtual(Classes.ConstantPool, "getFieldAt", Field.class, int.class);
    private static final MethodHandle getFieldAtIfLoaded = Invoker.findVirtual(Classes.ConstantPool, "getFieldAtIfLoaded", Field.class, int.class);
    private static final MethodHandle getMemberRefInfoAt = Invoker.findVirtual(Classes.ConstantPool, "getMemberRefInfoAt", String[].class, int.class);
    private static final MethodHandle getIntAt = Invoker.findVirtual(Classes.ConstantPool, "getIntAt", int.class, int.class);
    private static final MethodHandle getLongAt = Invoker.findVirtual(Classes.ConstantPool, "getLongAt", long.class, int.class);
    private static final MethodHandle getFloatAt = Invoker.findVirtual(Classes.ConstantPool, "getFloatAt", float.class, int.class);
    private static final MethodHandle getDoubleAt = Invoker.findVirtual(Classes.ConstantPool, "getDoubleAt", double.class, int.class);
    private static final MethodHandle getStringAt = Invoker.findVirtual(Classes.ConstantPool, "getStringAt", String.class, int.class);
    private static final MethodHandle getUTF8At = Invoker.findVirtual(Classes.ConstantPool, "getUTF8At", String.class, int.class);

    private final Object constantPool;

    public ConstantPool(Class<?> klass) {
        this.constantPool = JavaLangAccess.getConstantPool(klass);
    }

    public Integer getSize() {
        return runNull(() -> (int) getSize.invoke(this.constantPool));
    }

    public Class<?> getClassAt(int index) {
        return runNull(() -> (Class<?>) getClassAt.invoke(this.constantPool, index));
    }

    public Class<?> getClassAtIfLoaded(int index) {
        return runNull(() -> (Class<?>) getClassAtIfLoaded.invoke(this.constantPool, index));
    }

    public Member getMethodAt(int index) {
        return runNull(() -> (Member) getMethodAt.invoke(this.constantPool, index));
    }

    public Member getMethodAtIfLoaded(int index) {
        return runNull(() -> (Member) getMethodAtIfLoaded.invoke(this.constantPool, index));
    }

    public Field getFieldAt(int index) {
        return runNull(() -> (Field) getFieldAt.invoke(this.constantPool, index));
    }

    public Field getFieldAtIfLoaded(int index) {
        return runNull(() -> (Field) getFieldAtIfLoaded.invoke(this.constantPool, index));
    }

    public String getMemberRefInfoAt(int index) {
        return runNull(() -> (String) getMemberRefInfoAt.invoke(this.constantPool, index));
    }

    public Integer getIntAt(int index) {
        return runNull(() -> (int) getIntAt.invoke(this.constantPool, index));
    }

    public Long getLongAt(int index) {
        return runNull(() -> (long) getLongAt.invoke(this.constantPool, index));
    }

    public Float getFloatAt(int index) {
        return runNull(() -> (float) getFloatAt.invoke(this.constantPool, index));
    }

    public Double getDoubleAt(int index) {
        return runNull(() -> (double) getDoubleAt.invoke(this.constantPool, index));
    }

    public String getStringAt(int index) {
        return runNull(() -> (String) getStringAt.invoke(this.constantPool, index));
    }

    public String getUTF8At(int index) {
        return runNull(() -> (String) getUTF8At.invoke(this.constantPool, index));
    }
}
