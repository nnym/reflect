package stackframes;

import net.auoeke.reflect.StackFrames;
import org.openjdk.jmh.annotations.Benchmark;

public class Caller {
    static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    @Benchmark public Object getCallerClass() {
        return walker.getCallerClass();
    }

    @Benchmark public Object caller() {
        return StackFrames.caller();
    }
}
