package field;

import java.util.Arrays;
import java.util.stream.Stream;
import net.auoeke.reflect.Fields;
import org.openjdk.jmh.annotations.Benchmark;
import reflect.misc.TestObject;

public class GetFields {
    @Benchmark public Object direct() {
        return Arrays.asList(Fields.direct(TestObject.class));
    }

    @Benchmark public Object of() {
        return Fields.of(TestObject.class).toList();
    }

    @Benchmark public Object uncachedStream() {
        return Stream.of(Fields.direct(TestObject.class)).toList();
    }

    @Benchmark public Object all() {
        return Fields.all(TestObject.class).toList();
    }

    @Benchmark public Object getDeclaredFields() {
        return TestObject.class.getDeclaredFields();
    }
}
