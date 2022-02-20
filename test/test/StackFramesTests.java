package test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.auoeke.reflect.StackFrames;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.util.Box;

@SuppressWarnings({"removal", "AccessStaticViaInstance"})
@Testable
public class StackFramesTests extends StackFrames {
    @Test
    void framesTest() {
        var list = frameList(); var stream = frameStream().toList();
        Assert.equal(List::size, list, stream);

        for (var index = 0; index < list.size(); index++) {
            var l = list.get(index);
            var s = stream.get(index);
            Assert.equal(StackWalker.StackFrame::getDeclaringClass, l, s)
                .equal(StackWalker.StackFrame::getClassName, l, s)
                .equal(StackWalker.StackFrame::getMethodName, l, s)
                .equal(StackWalker.StackFrame::getDescriptor, l, s)
                .equal(StackWalker.StackFrame::getFileName, l, s)
                .equal(StackWalker.StackFrame::getLineNumber, l, s)
                .equal(StackWalker.StackFrame::getMethodType, l, s)
                .equal(StackWalker.StackFrame::isNativeMethod, l, s)
                .equal(StackWalker.StackFrame::toStackTraceElement, l, s);
        }
    }

    @Test
    void frameTest() {
        assert frame().getDeclaringClass() == this.getClass()
            && frame().getMethodName().equals("frameTest");
    }

    @SuppressWarnings("RedundantExplicitVariableType")
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
        Assert.equal(trace()[0].getClassName(), this.getClass().getName())
            .equal(traceFrame().getMethodName(), "traceTest")
            .equal(traceFrame(0).getMethodName(), "traceTest")
            .arraysEqual(trace(), trace(Thread.currentThread()), traceStream(Thread.currentThread()).toArray());

        var box = new Box<StackTraceElement[]>();
        var thread = new Thread("traceTest") {
            @Override public void run() {
                synchronized (this) {
                    this.notify();
                    this.wait(); box.set(Thread.currentThread().getStackTrace()).set(trace -> Arrays.copyOfRange(trace, 1, trace.length));
                    this.notify();
                }
            }
        };

        synchronized (thread) {
            thread.start();
            thread.wait();

            var trace = trace(thread);
            assert trace[0].getMethodName().equals("wait");
            trace = traceStream(thread).dropWhile(e -> e.getMethodName().equals("wait")).toArray(StackTraceElement[]::new);
            Assert.arraysEqual(trace(thread), traceStream(thread).toArray());
            thread.notify();
            thread.wait();

            Assert.arraysEqual(trace, box.value);
            Assert.arraysEqual(trace(thread), traceStream(thread).toArray());
        }
    }
}
