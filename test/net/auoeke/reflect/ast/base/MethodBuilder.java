package net.auoeke.reflect.ast.base;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.jshell.spi.ExecutionControl;
import net.auoeke.reflect.Invoker;
import net.auoeke.reflect.ast.base.method.Block;
import net.auoeke.reflect.ast.base.method.MethodBlock;
import net.auoeke.reflect.ast.base.method.Variable;
import net.auoeke.reflect.ast.base.type.BoundType;
import net.auoeke.reflect.ast.base.type.ConcreteType;
import net.auoeke.reflect.ast.base.type.Type;
import net.auoeke.reflect.ast.base.type.TypeParameter;
import net.auoeke.reflect.util.Util;
import net.gudenau.lib.unsafe.Unsafe;

public class MethodBuilder extends MemberBuilder<MethodBuilder> {
    protected final Map<String, TypeParameter> typeParameters = new LinkedHashMap<>();
    protected final Map<String, Variable> parameters = new LinkedHashMap<>();

    protected Type returnType = ConcreteType.voidType;
    protected Block body;

    @Override
    public Stream<MethodBuilder> instantiate() {
        return null;
    }

    public MethodBuilder typeParameter(String name, BoundType boundType, Type bound) {
        if (this.typeParameters.put(name, new TypeParameter(name, boundType, bound)) != null) {
            throw new IllegalStateException("type parameter \"%s\" already exists".formatted(name));
        }

        return this;
    }

    public MethodBuilder typeParameter(String name, Type bound) {
        return this.typeParameter(name, BoundType.EXTENDS, bound);
    }

    public MethodBuilder typeParameter(String name) {
        return this.typeParameter(name, null, null);
    }

    public MethodBuilder typeParameter(BoundType boundType, Type bound) {
        return this.typeParameter(this.nextTypeParameter(), boundType, bound);
    }

    public MethodBuilder typeParameter(Type bound) {
        return this.typeParameter(this.nextTypeParameter(), BoundType.EXTENDS, bound);
    }

    public MethodBuilder typeParameter() {
        return this.typeParameter(this.nextTypeParameter(), null, null);
    }

    public TypeParameter typeArgument(String name) {
        return this.typeParameters.get(name);
    }

    public MethodBuilder return$(Type type) {
        this.returnType = Objects.requireNonNull(type);

        return this;
    }

    public MethodBuilder return$(Class<?> type) {
        return this.return$(new ConcreteType(type));
    }

    public MethodBuilder return$(String type) {
        return this.return$(Objects.requireNonNull(this.typeArgument(type), "type parameter \"%s\" does not exist".formatted(type)));
    }

    public MethodBuilder returnAny() {
        var name = this.nextTypeParameter();
        return this.typeParameter(name).return$(name);
    }

    public MethodBuilder parameter(Class<?> type, String name) {
        return this.parameter(new ConcreteType(type), name);
    }

    public MethodBuilder parameter(String type, String name) {
        return this.parameter(this.typeArgument(type), name);
    }

    public MethodBuilder parameter(Type type, String name) {
        if (this.parameters.put(name, new Variable(type, name)) != null && name != null) {
            throw new IllegalStateException("parameter \"%s\" already exists".formatted(name));
        }

        return this;
    }

    public MethodBuilder parameter(Class<?>... types) {
        for (var type : types) {
            this.parameter(type, null);
        }

        return this;
    }

    public MethodBuilder inherit(Method method) {
        for (var parameter : method.getParameters()) {
            this.parameter(parameter.getType(), parameter.getName());
        }

        return this.return$(method.getReturnType()).access(method.getModifiers());
    }

    public MethodBuilder inherit(MethodHandle handle) {
        return this.inherit(Invoker.method(handle));
    }

    public MethodBuilder inherit(MethodType type) {
        return this.return$(type.returnType()).parameter(type.parameterArray());
    }

    public MethodBuilder body(Consumer<Block> generator) {
        generator.accept(this.body == null ? this.body = new MethodBlock(this) : this.body);

        return this;
    }

    public Type return$() {
        return this.returnType;
    }

    public Variable parameter(String name) {
        return this.parameters.get(name);
    }

    @Override
    public Stream<ConcreteType> referencedTypes() {
        return Util.join(this.typeParameters.values().stream(), this.parameters.values().stream(), Stream.of(this.returnType, this.body)).flatMap(Node::referencedTypes);
    }

    @Override
    public String toString() {
        return Util.buildString(string -> {
            string.append(Modifier.toString(this.access)).append(' ');

            if (!this.typeParameters.isEmpty()) {
                string.append(this.typeParameters.values().stream().map(TypeParameter::declaration).collect(Collectors.joining(", ", "<", "> ")));
            }

            string.append(this.returnType)
                .append(' ').append(this.name)
                .append(this.parameters.values().stream().map(Variable::declaration).collect(Collectors.joining(", ", "(", ") ")))
                .append(this.body.toString());
        });
    }

    private String nextTypeParameter() {
        var typeParameters = this.typeParameters.keySet();

        if (typeParameters.isEmpty()) {
            return "A";
        }

        for (var typeParameter : typeParameters) {
            var character = typeParameter.charAt(0);
            var lowercase = Character.isLowerCase(character);
            var offset = lowercase ? 96 : 64;
            var next = Character.toString((character - offset) % 26 + offset + 1);

            if (!this.typeParameters.containsKey(next)) {
                return next;
            }
        }

        throw Unsafe.throwException(new ExecutionControl.NotImplementedException(""));
    }
}
