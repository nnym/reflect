package types;

import java.util.stream.IntStream;
import net.auoeke.reflect.Types;
import org.openjdk.jmh.annotations.Benchmark;

public class BoxArray {
    static final int[] array = IntStream.range(0, 1000).toArray();

    @Benchmark public Object reflectiveBox() {
        return Types.convert(array, Types.box(array.getClass().componentType()));
    }

    @Benchmark public Object objectBox() {
        return Types.box((Object) array);
    }

    @Benchmark public Object intBox() {
        return Types.box(array);
    }
}
