package field;

import net.auoeke.reflect.Fields;
import org.openjdk.jmh.annotations.Benchmark;

public class GetField {
    @Benchmark public void getDeclaredField() {
        Integer.class.getDeclaredField("value");
    }

    @Benchmark public void of() {
        Fields.of(Integer.class, "value");
    }
}
