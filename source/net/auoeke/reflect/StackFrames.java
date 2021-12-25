package net.auoeke.reflect;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 Utilities for getting stack frames and caller classes.
 */
public class StackFrames {
    public static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     Get the calling class of the method that invoked this method.

     @return the class
     */
    public static Class<?> caller() {
        return caller(1);
    }

    /**
     Get the calling class at the specified depth rooted at the caller of the method wherefrom this method was invoked.

     @param depth the depth of the calling class
     @return the class
     */
    public static Class<?> caller(int depth) {
        return frame(depth + 2).getDeclaringClass();
    }

    /**
     Get the first calling class that matches a {@link StackWalker.StackFrame} predicate starting at this method's caller's preceding frame.

     @param predicate a predicate
     @return the class associated with the first frame that satisfies the predicate
     */
    public static Class<?> caller(Predicate<? super StackWalker.StackFrame> predicate) {
        return walker.walk(frames -> frames.skip(2).filter(predicate).findFirst().get().getDeclaringClass());
    }

    public static StackWalker.StackFrame frame() {
        return frame(1);
    }

    public static StackWalker.StackFrame frame(int depth) {
        return walker.walk(frames -> frames.skip(depth + 1).findFirst().get());
    }

    public static StackWalker.StackFrame frame(Predicate<? super StackWalker.StackFrame> predicate) {
        return walker.walk(frames -> frames.skip(1).filter(predicate).findFirst().get());
    }

    public static List<StackWalker.StackFrame> frames() {
        return walker.walk(frames -> frames.skip(1).toList());
    }

    public static StackTraceElement[] trace() {
        var trace = Thread.currentThread().getStackTrace();
        return Arrays.copyOfRange(trace, 2, trace.length);
    }

    public static StackTraceElement traceFrame(int depth) {
        return trace()[1 + depth];
    }

    public static StackTraceElement traceFrame() {
        return traceFrame(1);
    }
}
