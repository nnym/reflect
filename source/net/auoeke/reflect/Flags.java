package net.auoeke.reflect;

import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 Utilities for working with flags and particularly Java modifiers.
 */
@SuppressWarnings("unused")
public class Flags {
    // @formatter:off
    public static final int PUBLIC       = 1;
    public static final int PRIVATE      = 1 << 1;
    public static final int PROTECTED    = 1 << 2;
    public static final int STATIC       = 1 << 3;
    public static final int FINAL        = 1 << 4;
    public static final int SYNCHRONIZED = 1 << 5;
    public static final int VOLATILE     = 1 << 6;

    /**
     Denotes a method generated as a result of an "overriding" method having a parameter or return type that does not match that of the original method.
     The generated method acts as a bridge from the original method to the "overriding" method by passing the arguments to it and returning its result.
     */
    public static final int BRIDGE       = 1 << 6;
    public static final int TRANSIENT    = 1 << 7;

    /**
     Denotes a method with a varargs parameter.
     */
    public static final int VARARGS      = 1 << 7;
    public static final int NATIVE       = 1 << 8;
    public static final int INTERFACE    = 1 << 9;
    public static final int ABSTRACT     = 1 << 10;

    /**
     strictfp
     */
    public static final int STRICT       = 1 << 11;

    /**
     Compiler-generated.
     */
    public static final int SYNTHETIC    = 1 << 12;

    /**
     Denotes an annotation interface.
     */
    public static final int ANNOTATION   = 1 << 13;

    /**
     Denotes an enum type or an enum constant field.
     */
    public static final int ENUM         = 1 << 14;

    /**
     Declared implicitly according to the specification of the language of the source code; implicit but not synthetic and not an implementation artifact.
     */
    public static final int MANDATED     = 1 << 15;
    // @formatter:on

    public static final int VISIBILITY = PUBLIC | PROTECTED | PRIVATE;

    /**
     @return whether all bits set in {@code target} are set in {@code field}.
     */
    public static boolean all(long field, long target) {
        return (field & target) == target;
    }

    /**
     @return whether {@code member} has all modifiers set in {@code target}.
     */
    public static boolean all(Member member, long target) {
        return all(modifiers(member), target);
    }

    /**
     @return whether {@code type} has all modifiers set in {@code target}.
     */
    public static boolean all(Class<?> type, long target) {
        return all(modifiers(type), target);
    }

    /**
     @return whether any bit set is {@code target} is set in {@code field}.
     */
    public static boolean any(long field, long target) {
        return (field & target) != 0;
    }

    /**
     @return whether {@code member} has any modifier set in {@code target}.
     */
    public static boolean any(Member member, long target) {
        return any(modifiers(member), target);
    }

    /**
     @return whether {@code type} has any modifier set in {@code target}.
     */
    public static boolean any(Class<?> type, long target) {
        return any(modifiers(type), target);
    }

    /**
     @return whether not all bits set in {@code target} are set in {@code field}.
     */
    public static boolean not(long field, long target) {
        return (field & target) != target;
    }

    /**
     @return whether {@code member} does not have all modifiers set in {@code target}.
     */
    public static boolean not(Member member, long target) {
        return not(modifiers(member), target);
    }

    /**
     @return whether {@code type} does not have all modifiers set in {@code target}.
     */
    public static boolean not(Class<?> type, long target) {
        return not(modifiers(type), target);
    }

    /**
     @return whether no bit set is {@code target} is set in {@code field}.
     */
    public static boolean none(long field, long target) {
        return (field & target) == 0;
    }

    /**
     @return whether {@code member} does not have any modifier set in {@code target}.
     */
    public static boolean none(Member member, long target) {
        return none(modifiers(member), target);
    }

    /**
     @return whether {@code type} does not have any modifier set in {@code target}.
     */
    public static boolean none(Class<?> type, long target) {
        return none(modifiers(type), target);
    }

    /**
     @return {@code field} without the bits set in {@code target}.
     */
    public static int remove(int field, int target) {
        return field & ~target;
    }

    /**
     @return {@code field} without the bits set in {@code target}.
     */
    public static long remove(long field, long target) {
        return field & ~target;
    }

    /**
     @return {@code member}'s modifiers without the modifiers set in {@code modifiers}.
     */
    public static int remove(Member member, int modifiers) {
        return remove(modifiers(member), modifiers);
    }

    /**
     @return {@code type}'s modifiers without the modifiers set in {@code modifiers}.
     */
    public static int remove(Class<?> type, int modifiers) {
        return remove(modifiers(type), modifiers);
    }

    /**
     @return {@code modifiers} with visibility set to {@code visibility}.
     */
    public static int visibility(int modifiers, int visibility) {
        return modifiers & ~VISIBILITY | visibility;
    }

    public static boolean isPublic(long modifiers) {
        return all(modifiers, PUBLIC);
    }

    public static boolean isProtected(long modifiers) {
        return all(modifiers, PROTECTED);
    }

    public static boolean isPrivate(long modifiers) {
        return all(modifiers, PRIVATE);
    }

    public static boolean isPackagePrivate(long modifiers) {
        return none(modifiers, VISIBILITY);
    }

    public static boolean isStatic(long modifiers) {
        return all(modifiers, STATIC);
    }

    public static boolean isInstance(long modifiers) {
        return !isStatic(modifiers);
    }

    public static boolean isFinal(long modifiers) {
        return all(modifiers, FINAL);
    }

    public static boolean isSynchronized(long modifiers) {
        return all(modifiers, SYNCHRONIZED);
    }

    public static boolean isVolatile(long modifiers) {
        return all(modifiers, VOLATILE);
    }

    /**
     @return whether {@code modifiers} denote a bridge method.
     */
    public static boolean isBridge(long modifiers) {
        return all(modifiers, BRIDGE);
    }

    public static boolean isTransient(long modifiers) {
        return all(modifiers, TRANSIENT);
    }

    public static boolean isVarargs(long modifiers) {
        return all(modifiers, VARARGS);
    }

    public static boolean isNative(long modifiers) {
        return all(modifiers, NATIVE);
    }

    public static boolean isAbstract(long type) {
        return all(type, ABSTRACT);
    }

    public static boolean isStrict(long modifiers) {
        return all(modifiers, STRICT);
    }

    public static boolean isSynthetic(long modifiers) {
        return all(modifiers, SYNTHETIC);
    }

    public static boolean isAnnotation(long modifiers) {
        return all(modifiers, ANNOTATION);
    }

    public static boolean isEnum(long modifiers) {
        return all(modifiers, ENUM);
    }

    /**
     @return whether {@code modifiers} denotes an export that is implicitly declared in the source of a module declaration.
     */
    public static boolean isMandated(long modifiers) {
        return all(modifiers, MANDATED);
    }

    public static boolean isPublic(Class<?> type) {
        return all(type, PUBLIC);
    }

    public static boolean isPublic(Member member) {
        return all(member, PUBLIC);
    }

    public static boolean isProtected(Class<?> type) {
        return all(type, PROTECTED);
    }

    public static boolean isProtected(Member member) {
        return all(member, PROTECTED);
    }

    public static boolean isPrivate(Class<?> type) {
        return all(type, PRIVATE);
    }

    public static boolean isPrivate(Member member) {
        return all(member, PRIVATE);
    }

    public static boolean isPackagePrivate(Class<?> type) {
        return none(type, VISIBILITY);
    }

    public static boolean isPackagePrivate(Member member) {
        return none(member, VISIBILITY);
    }

    public static boolean isStatic(Class<?> type) {
        return all(type, STATIC);
    }

    public static boolean isStatic(Member member) {
        return all(member, STATIC);
    }

    public static boolean isInstance(Class<?> type) {
        return !isStatic(type);
    }

    public static boolean isInstance(Member member) {
        return !isStatic(member);
    }

    public static boolean isFinal(Class<?> type) {
        return all(type, FINAL);
    }

    public static boolean isFinal(Member member) {
        return all(member, FINAL);
    }

    public static boolean isSynchronized(Member member) {
        return all(member, SYNCHRONIZED);
    }

    public static boolean isVolatile(Field field) {
        return all(field, VOLATILE);
    }

    public static boolean isBridge(Method method) {
        return all(method, BRIDGE);
    }

    public static boolean isTransient(Field field) {
        return all(field, TRANSIENT);
    }

    public static boolean isVarargs(Executable executable) {
        return all(executable, VARARGS);
    }

    public static boolean isNative(Method method) {
        return all(method, NATIVE);
    }

    public static boolean isAbstract(Class<?> type) {
        return all(type, ABSTRACT);
    }

    public static boolean isAbstract(Method method) {
        return all(method, ABSTRACT);
    }

    public static boolean isStrict(Class<?> type) {
        return all(type, STRICT);
    }

    public static boolean isStrict(Method method) {
        return all(method, STRICT);
    }

    public static boolean isSynthetic(Member member) {
        return all(member, SYNTHETIC);
    }

    public static boolean isSynthetic(Class<?> type) {
        return all(type, SYNTHETIC);
    }

    public static boolean isSynthetic(Module module) {
        return is(module, ModuleDescriptor.Modifier.SYNTHETIC);
    }

    public static boolean isAnnotation(Class<?> type) {
        return all(type, ANNOTATION);
    }

    public static boolean isEnum(Class<?> type) {
        return all(type, ENUM);
    }

    public static boolean isEnum(Field field) {
        return all(field, ENUM);
    }

    public static boolean isMandated(Module module) {
        return is(module, ModuleDescriptor.Modifier.MANDATED);
    }

    public static String string(long modifiers) {
        return Modifier.toString((int) modifiers);
    }

    public static String string(Class<?> type) {
        return string(type.getModifiers());
    }

    public static String string(Member member) {
        return string(member.getModifiers());
    }

    private static boolean is(Module module, ModuleDescriptor.Modifier modifier) {
        return module.getDescriptor().modifiers().contains(modifier);
    }

    private static int modifiers(Class<?> type) {
        return type == null ? 0 : type.getModifiers();
    }

    private static int modifiers(Member member) {
        return member == null ? 0 : member.getModifiers();
    }
}
