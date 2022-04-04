package field;

import java.util.Arrays;
import java.util.stream.Stream;
import net.auoeke.reflect.Fields;
import org.openjdk.jmh.annotations.Benchmark;
import reflect.misc.TestObject;

public class GetFields {
    @Benchmark public void direct() {
        Arrays.asList(Fields.direct(TestObject.class));
    }

    @Benchmark public void of() {
        Fields.of(TestObject.class).toList();
    }

    @Benchmark public void uncachedStream() {
        Stream.of(Fields.direct(TestObject.class)).toList();
    }

    @Benchmark public void all() {
        Fields.all(TestObject.class).toList();
    }

    @Benchmark public void getDeclaredFields() {
        TestObject.class.getDeclaredFields();
    }
}
