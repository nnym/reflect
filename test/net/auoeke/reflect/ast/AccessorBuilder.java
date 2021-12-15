package net.auoeke.reflect.ast;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.SourceOrderer;
import net.auoeke.reflect.ast.base.method.Block;
import net.auoeke.reflect.ast.base.method.expression.Expression;
import net.auoeke.reflect.ast.base.method.expression.Invocation;
import net.auoeke.reflect.ast.base.method.expression.literal.Literal;
import net.auoeke.reflect.ast.base.method.statement.EnhancedFor;
import net.auoeke.reflect.ast.base.method.statement.If;
import net.auoeke.reflect.ast.base.method.statement.Return;
import net.auoeke.reflect.ast.base.type.Type;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.annotation.Testable;

@Testable
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(SourceOrderer.class)
class AccessorBuilder extends TestBuilder {
    static final Class<?>[] fieldTypes = {Field.class, String.class};

    static final Map<Class<?>, String> fieldDiscriminators = map(
        String.class, "fieldName",
        Field.class, "field",
        long.class, "offset"
    );

    static final Map<Class<?>, String> ownerTypes = map(
        Object.class, "objectFieldOffset",
        Class.class, "staticFieldOffset"
    );

    static final String[] suffixes = {"", "Volatile"};

    static final Class<?>[] types = {
        boolean.class,
        byte.class,
        char.class,
        short.class,
        int.class,
        long.class,
        float.class,
        double.class,
        Object.class
    };

    static final Map<Class<?>, Class<?>> typesWithNull = map(
        boolean.class, boolean.class,
        byte.class, byte.class,
        char.class, char.class,
        short.class, short.class,
        int.class, int.class,
        long.class, long.class,
        float.class, float.class,
        double.class, double.class,
        Object.class, Object.class,
        null, Object.class
    );

    public AccessorBuilder() {
        super("net.auoeke.reflect", "Accessor");

        this.public$();
    }

    static <K, V> HashMap<K, V> map(Object... elements) {
        var map = new LinkedHashMap<K, V>();

        assert (elements.length & 1) == 0;

        for (int i = 0; i < elements.length; i += 2) {
            map.put((K) elements[i], (V) elements[i + 1]);
        }

        return map;
    }

    static String name(Class<?> type) {
        if (type == null) return "";
        if (type == Object.class) return "Reference";

        var simpleName = type.getSimpleName();
        return simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
    }

    @Test
    void field() {
        for (var suffix : suffixes) {
            for (var type : typesWithNull.keySet()) {
                var actualType = typesWithNull.get(type);

                for (var access : Access.values()) {
                    this.method(method -> {
                        method.public$().static$().name(access + name(type) + suffix).parameter(Field.class, "field");
                        access.get(() -> method.return$(actualType));
                        access.put(() -> method.parameter(actualType, "value"));

                        method.body(block -> {
                            var offset = new Invocation(Unsafe.class, "staticFieldOffset").argument(block.var("field"));
                            var delcaringClass = block.var("field").invoke("getDeclaringClass");
                            var unsafeMethod = type == null
                                ? new Invocation(access.toString()).argument(delcaringClass, block.var("field").invoke("getType"), offset)
                                : new Invocation(Unsafe.class, method.name()).argument(delcaringClass, offset);

                            access.put(() -> unsafeMethod.argument(block.var("value")));
                            block.return$(unsafeMethod);
                        });
                    });
                }
            }
        }
    }

    @Test
    void objectStringAndObjectField() {
        for (var suffix : suffixes) {
            for (var type : typesWithNull.keySet()) {
                var actualType = typesWithNull.get(type);

                for (var objectType : ownerTypes.keySet()) {
                    for (var fieldType : fieldTypes) {
                        if (objectType != Class.class || fieldType != Field.class) {
                            for (var access : Access.values()) {
                                this.method(method -> {
                                    method.public$().static$().name(access + name(type) + suffix);

                                    access.get(() -> {
                                        if (type == Object.class) {
                                            method.returnAny();
                                        } else {
                                            method.return$(actualType);
                                        }
                                    });

                                    var objectName = objectType == Class.class ? "type" : "object";
                                    method.parameter(objectType == Class.class ? Type.wildcard(Class.class) : Type.of(objectType), objectName)
                                        .parameter(fieldType, "fieldName");

                                    access.put(() -> method.parameter(actualType, "value"));

                                    method.body(block -> {
                                        var field = fieldType == Field.class ? block.var("fieldName") : new Invocation(Fields.class, "field").argument(
                                            block.var(objectName),
                                            block.var("fieldName")
                                        );

                                        if (type == null && fieldType == String.class) {
                                            field = block.var(Field.class, "field", field).newline().var("field");
                                        }

                                        var offset = new Invocation(Unsafe.class, objectType == Class.class ? "staticFieldOffset" : "objectFieldOffset").argument(field);
                                        var unsafeMethod = type == null
                                            ? new Invocation(access.toString()).argument(block.var(objectName), field.invoke("getType"), offset)
                                            : new Invocation(Unsafe.class, method.name()).argument(block.var(objectName), offset);

                                        access.put(() -> unsafeMethod.argument(block.var("value")));
                                        block.return$(unsafeMethod);
                                    });
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void objectClassStringAndObjectClassField() {
        for (var suffix : suffixes) {
            for (var type : typesWithNull.keySet()) {
                var actualType = typesWithNull.get(type);

                for (var fieldType : fieldTypes) {
                    for (var access : Access.values()) {
                        this.method(method -> {
                            method.public$().static$();

                            access.get(() -> {
                                if (type == Object.class) {
                                    method.returnAny();
                                } else {
                                    method.return$(actualType);
                                }
                            });

                            method.name(access + name(type) + suffix)
                                .parameter(Object.class, "object")
                                .parameter(Type.wildcard(Class.class), "type")
                                .parameter(fieldType, "fieldName");

                            access.put(() -> method.parameter(actualType, "value"));

                            method.body(block -> {
                                var field = fieldType == Field.class ? block.var("fieldName") : new Invocation(Fields.class, "field").argument(
                                    block.var("type"),
                                    block.var("fieldName")
                                );

                                if (type == null && fieldType == String.class) {
                                    block.var(Field.class, "field", field).newline();
                                    field = block.var("field");
                                }

                                var offset = new Invocation(Unsafe.class, "objectFieldOffset").argument(field);
                                var unsafeMethod = type == null
                                    ? new Invocation(access.toString()).argument(block.var("object"), field.invoke("getType"), offset)
                                    : new Invocation(Unsafe.class, method.name()).argument(block.var("object"), offset);

                                access.put(() -> unsafeMethod.argument(block.var("value")));
                                block.return$(unsafeMethod);
                            });
                        });
                    }
                }
            }
        }
    }

    @Test
    void getPut() {
        for (var suffix : suffixes) {
            for (var access : Access.values()) {
                this.method(method -> {
                    method.public$().static$().name(access.toString() + suffix).parameter(Object.class, "object").parameter(Type.wildcard(Class.class), "type").parameter(long.class, "offset");
                    access.get(() -> method.return$(Object.class));
                    access.put(() -> method.parameter(Object.class, "value"));

                    method.body(block -> {
                        var unsafeMethod = new Invocation(Unsafe.class, "%sObject%s".formatted(access, suffix)).argument(block.var("object"), block.var("offset"));

                        block.statement(If.chain(Stream.of(types).filter(type -> type != Object.class).map(type -> {
                            var invocation = unsafeMethod.copy().name(access + name(type) + suffix);
                            access.put(() -> invocation.argument(block.var("value").cast(type)));

                            return new If().identical(block.var("type"), Literal.of(type)).then(access.put() ? invocation.statement() : new Return(invocation));
                        })));

                        access.get(() -> block.newline().return$(unsafeMethod));
                        access.put(() -> unsafeMethod.argument(block.var("value")).statement());
                    });
                });
            }
        }
    }

    @Test
    void copy() {
        for (var suffix : suffixes) {
            for (var type : typesWithNull.keySet()) {
                for (var ownerType : ownerTypes.keySet()) {
                    for (var discriminator : fieldDiscriminators.keySet()) {
                        if (type != null || discriminator != long.class) {
                            var methodName = name(type) + suffix;

                            this.method(method -> {
                                method.public$().static$().name("copy" + methodName);

                                if (ownerType == Object.class) {
                                    method.typeParameter("T").parameter("T", "to").parameter("T", "from");
                                } else {
                                    var classWildcard = Type.wildcard(Class.class);
                                    method.parameter(classWildcard, "to").parameter(classWildcard, "from");
                                }

                                method.parameter(discriminator, fieldDiscriminators.get(discriminator)).body(block -> {
                                    if (discriminator != long.class) {
                                        var offsetMethod = new Invocation(Unsafe.class, ownerType == Class.class ? "staticFieldOffset" : "objectFieldOffset");

                                        block.var(variable -> {
                                            variable.type(long.class).name("offset");

                                            if (discriminator == String.class) {
                                                Expression field = new Invocation(Fields.class, "field").argument(
                                                    block.var("to"),
                                                    block.var("fieldName")
                                                );

                                                if (type == null) {
                                                    block.var(Field.class, "field", field);
                                                    field = block.var("field");
                                                }

                                                variable.initialize(offsetMethod.copy().argument(field));
                                            } else {
                                                variable.initialize(offsetMethod.argument(block.var("field")));
                                            }
                                        }).newline();
                                    }

                                    var unsafeMethod = type == null ? new Invocation("put" + suffix).argument(
                                        block.var("to"),
                                        block.var("field").invoke("getType"),
                                        block.var("offset"),
                                        new Invocation("get" + suffix).argument(
                                            block.var("from"),
                                            block.var("field").invoke("getType"),
                                            block.var("offset")
                                        )
                                    ) : new Invocation(Unsafe.class, "put" + methodName).argument(
                                        block.var("to"),
                                        block.var("offset"),
                                        new Invocation(Unsafe.class, "get" + methodName).argument(
                                            block.var("from"),
                                            block.var("offset")
                                        )
                                    );

                                    block.statement(unsafeMethod);
                                });
                            });
                        }
                    }
                }
            }
        }
    }

    @Test
    void cone() {
        this.method(method -> method.public$().static$()
            .name("clone")
            .typeParameter("T")
            .return$("T")
            .parameter("T", "object")
            .body(block -> block.if$(if$ -> if$.null$(block.var("object")).then(new Block().return$(Literal.null$)))
                .newline()
                .var(method.typeArgument("T"), "clone", new Invocation(Unsafe.class, "allocateInstance").argument(block.var("object").invoke("getClass")).cast(method.typeArgument("T")))
                .newline()
                .statement(new EnhancedFor()
                    .var(Field.class, "field")
                    .iterable(new Invocation(Fields.class, "allInstance").argument(block.var("object")))
                    .action(field -> Block.wrap(new Invocation(Accessor.class, "copy").argument(block.var("clone"), block.var("object"), field))))
                .newline()
                .return$(block.var("clone"))
            )
        );
    }

    @Test
    void offset() {
        for (var methodType : new String[]{"static", "object"}) {
            this.method(method -> method.public$().static$().return$(long.class).name(methodType + "FieldOffset")
                .parameter(Type.wildcard(Class.class), "type")
                .parameter(String.class, "name")
                .body(block -> block.return$(new Invocation(Unsafe.class, method.name()).argument(new Invocation(Fields.class, "field").argument(
                    block.var("type"),
                    block.var("name")
                ))))
            );
        }
    }

    @Override
    @AfterEach
    protected void tearDown() {
        super.tearDown();
    }

    @Test
    void a() {
        // this.method(method -> method.nameTemplate("put{type}", (Class<?> type) -> ));
    }
}
