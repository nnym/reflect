package reflect.other;

import java.util.ArrayList;
import net.gudenau.lib.unsafe.Unsafe;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;
import reflect.util.Logger;

@SuppressWarnings("ALL")
@Disabled
@Testable
public class SpeedTest {
	static int iterations = 10000;
	static Runnable runnable0;
	static Runnable runnable1;
	static boolean boolea;

	static long mean(String label, Runnable test) {
		long time = System.nanoTime();

		for (int i = 0; i < iterations; i++) {
			test.run();
		}

		long duration = Math.round((double) (System.nanoTime() - time) / iterations);

		if (label == null) {
			Logger.log(duration);
		} else {
			Logger.log("%s: %s", label, duration);
		}

		return duration;
	}

	static long time(String label, Runnable test) {
		long time = System.nanoTime();

		test.run();

		time = System.nanoTime() - time;

		if (label == null) {
			Logger.log(time);
		} else {
			Logger.log("%s: %s", label, time);
		}

		return time;
	}

	@Test void instantiation() {
		mean("constructor", () -> new ArrayList<>());
		mean("Unsafe", () -> Unsafe.allocateInstance(ArrayList.class));
	}

	@SuppressWarnings("Convert2Lambda")
	@Test void lambda() {
		time("anonymous class", () -> runnable0 = new Runnable() {
			@Override public void run() {}
		});

		time("lambda", () -> runnable1 = () -> {});
	}

	@Test void cast() {
		mean("checkcast", () -> {SpeedTest test = null;});
		mean("Class#cast", () -> {var test = SpeedTest.class.cast(null);});
	}

	@Test void stringChars() {
		var string = "a".repeat(10000);

		Logger.log("array");
		mean("toCharArray", string::toCharArray);
		mean("codePoints", () -> string.codePoints().toArray());
		mean("getBytes", string::getBytes);

		Logger.log("\niteration");
		mean("toCharArary", () -> {
			for (char character : string.toCharArray()) {}
		});
		mean("codePoints", () -> string.codePoints().forEach(value -> {}));
		mean("getBytes", () -> {
			for (byte bite : string.getBytes()) {}
		});
	}

	@Test void typeCheck() {
		enum Type {
			INT,
			DOUBLE
		}
		record BoxRecord(Type type) {}
		BoxClass b = new IntBox();
		var r = new BoxRecord(Type.INT);

		mean("enum", () -> {boolea = r.type() == Type.DOUBLE;});
		mean("instanceof", () -> {boolea = b instanceof DoubleBox;});
	}

	abstract sealed class BoxClass permits IntBox, DoubleBox {}

	final class IntBox extends BoxClass {
		int i;
	}

	final class DoubleBox extends BoxClass {
		double d;
	}
}
