package reflect.experimental;

import java.lang.instrument.Instrumentation;
import net.auoeke.reflect.Classes;
import net.auoeke.reflect.ClassTransformer;

public class Classes2 extends Classes {
    public static byte[] stealBytecode(Instrumentation instrumentation, Class<?> type) {
        var box = new byte[1][];

        if (instrumentation != null) {
            ClassTransformer bytecodeStealer = (module, loader, name, t, domain, classFile) -> t == type ? box[0] = classFile : classFile;
            instrumentation.addTransformer(bytecodeStealer, true);
            instrumentation.retransformClasses(type);
            instrumentation.removeTransformer(bytecodeStealer);
        }

        return box[0];
    }
}
