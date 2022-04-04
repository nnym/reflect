package types;

import java.util.stream.IntStream;
import net.auoeke.reflect.Types;
import org.openjdk.jmh.annotations.Benchmark;

public class BoxArray {
    static final int[] array = IntStream.range(0, 1000).toArray();

    @Benchmark public void reflectiveBox() {
        Types.convert(array, Types.box(array.getClass().componentType()));
    }

    @Benchmark public void objectBox() {
        Types.box((Object) array);
    }

    @Benchmark public void intBox() {
        Types.box(array);
    }
}
