package field;

import net.auoeke.reflect.Fields;
import org.openjdk.jmh.annotations.Benchmark;

public class GetField {
    @Benchmark public Object getDeclaredField() {
        return Integer.class.getDeclaredField("value");
    }

    @Benchmark public Object of() {
        return Fields.of(Integer.class, "value");
    }
}
