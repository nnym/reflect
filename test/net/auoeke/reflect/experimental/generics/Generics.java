package net.auoeke.reflect.experimental.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.gudenau.lib.unsafe.Unsafe;

public class Generics {
    private static final int TEST = 0;

    public static List<Type> genericSupertypes(Class<?> klass) {
        Type genericSuperclass = klass.getGenericSuperclass();
        List<Type> genericSupertypes = new ArrayList<>(Arrays.asList(klass.getGenericInterfaces()));

        if (genericSuperclass != null) {
            genericSupertypes.add(genericSuperclass);
        }

        return genericSupertypes;
    }

    public static List<TypeArgument> typeArguments(Class<?> klass) {
        var loader = klass.getClassLoader();
        var types = new ArrayList<TypeArgument>();

        try {
            for (Type genericSupertype : genericSupertypes(klass)) {
                var supertype = Class.forName(genericSupertype.getTypeName().replaceFirst("<.*>", ""), false, loader);

                if (genericSupertype instanceof ParameterizedType parameterizedType) {
                    var typeParameters = supertype.getTypeParameters();
                    var typeArguments = parameterizedType.getActualTypeArguments();

                    assert typeParameters.length == typeArguments.length;

                    for (int i = 0; i < typeParameters.length; i++) {
                        var parameter = typeParameters[i];
                        var argument = typeArguments[i];

                        types.add(new TypeArgument(supertype, argument instanceof Class classArgument ? classArgument : Class.forName(argument.getTypeName(), false, loader), parameter.getName(), i));
                    }
                }

                types.addAll(typeArguments(supertype));
            }
        } catch (Throwable throwable) {
            throw Unsafe.throwException(throwable);
        }

        return types;
    }
}
