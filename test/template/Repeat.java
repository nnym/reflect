package template;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Repeat {
    Class<?>[] types() default {boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class, Object.class};

    Name[] names() default {};

    Name[] methods();

    String[] parameters() default {};
}
