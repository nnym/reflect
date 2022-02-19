package test;

import java.util.function.Supplier;
import net.auoeke.reflect.StackFrames;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@Testable
public class StackFramesTests extends StackFrames {
    @Test
    void callerTest() {
        Supplier<Class<?>> lambda = StackFrames::caller;

        Assert.equal(
            caller(),
            caller(0),
            caller(frame -> true),
            lambda.get(),
            frameList().get(1).getDeclaringClass()
        );
    }

    @Test
    void traceTest() {
        Assert.equal(trace()[0].getClassName(), this.getClass().getName());
        Assert.equal(traceFrame().getMethodName(), "traceTest");
        Assert.equal(traceFrame(0).getMethodName(), "traceTest");
    }
}
