package net.auoeke.reflect;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 Utilities for getting stack frames and callers.
 */
public class StackFrames {
    public static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     Return a sequential ordered stream of the current thread's stack frames.

     @return a stream of the current thread's stack frames
     */
    public static Stream<StackWalker.StackFrame> frameStream() {
        return walker.walk(frames -> Stream.of(frames.toArray(StackWalker.StackFrame[]::new)).skip(1));
    }

    /**
     Return a list of the current thread's stack frames.

     @return a list of the current thread's stack frames
     */
    public static List<StackWalker.StackFrame> frameList() {
        return walker.walk(frames -> frames.skip(1).toList());
    }

    /**
     @deprecated for {@link #frameList}
     */
    @Deprecated(forRemoval = true)
    public static List<StackWalker.StackFrame> frames() {
        return frameList();
    }

    /**
     Find the first stack frame in the result of a transformation of the given stream of stack frames starting at this method's caller.

     @param transformation the stack frame stream transformation
     @return the first stack frame in the transformed stream
     */
    public static StackWalker.StackFrame first(UnaryOperator<Stream<StackWalker.StackFrame>> transformation) {
        return walker.walk(frames -> transformation.apply(frames.skip(1)).findFirst().get());
    }

    /**
     Find the stack frame at a given depth rooted at this method's caller.

     @param depth the depth of the target stack frame
     @return the stack frame at the given depth
     */
    public static StackWalker.StackFrame frame(int depth) {
        return first(frames -> frames.skip(depth + 1));
    }

    /**
     Find the first stack frame that matches a predicate; starting at this method's caller.

     @param predicate the stack frame predicate
     @return the first matching stack frame
     */
    public static StackWalker.StackFrame frame(Predicate<? super StackWalker.StackFrame> predicate) {
        return first(frames -> frames.skip(1).filter(predicate));
    }

    /**
     Find this method's caller's stack frame.

     @return this method's caller's stack frame
     */
    public static StackWalker.StackFrame frame() {
        return frame(1);
    }

    /**
     Find the first calling class that matches a {@link StackWalker.StackFrame} predicate starting at this method's caller's caller.

     @param predicate a stack frame predicate
     @return the class associated with the first frame that satisfies the predicate
     */
    public static Class<?> caller(Predicate<? super StackWalker.StackFrame> predicate) {
        return first(frames -> frames.skip(2).filter(predicate)).getDeclaringClass();
    }

    /**
     Find the calling class at the specified depth rooted at this method's caller's caller.

     @param depth the depth of the calling class
     @return the calling class
     */
    public static Class<?> caller(int depth) {
        return frame(depth + 2).getDeclaringClass();
    }

    /**
     Find the calling class of the method's caller's caller.

     @return the calling class
     */
    public static Class<?> caller() {
        return caller(1);
    }

    /**
     Return a sequential ordered stream of a thread's stack trace starting at this method's caller.

     @param thread a thread
     @return a stream of the thread's stack trace
     */
    public static Stream<StackTraceElement> traceStream(Thread thread) {
        return Stream.of(thread.getStackTrace()).skip(2);
    }

    /**
     Return an array of a thread's stack trace starting at this method's caller.

     @param thread a thread
     @return an array of the thread's stack trace
     */
    public static StackTraceElement[] trace(Thread thread) {
        var trace = thread.getStackTrace();
        return Arrays.copyOfRange(trace, 2, trace.length);
    }

    /**
     Return an array of the current thread's stack trace starting at this method's caller.

     @return the current thread's stack trace
     */
    public static StackTraceElement[] trace() {
        var trace = Thread.currentThread().getStackTrace();
        return Arrays.copyOfRange(trace, 2, trace.length);
    }

    /**
     Get the stack trace element at a given depth rooted at this method's caller.

     @param depth the target stack trace element's depth
     @return the stack trace element at the given depth
     @deprecated because excessive; {@code trace()[depth]} suffices
     */
    @Deprecated(forRemoval = true)
    public static StackTraceElement traceFrame(int depth) {
        return trace()[1 + depth];
    }

    /**
     Get this method's caller's stack trace element.

     @return this method's caller's stack trace element
     @deprecated because excessive; {@code trace()[0]} suffices
     */
    @Deprecated(forRemoval = true)
    public static StackTraceElement traceFrame() {
        return traceFrame(1);
    }
}
