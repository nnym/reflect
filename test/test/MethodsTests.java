package test;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import mock.AnnotationInterfaceWtihDefault;
import mock.BigInterface;
import net.auoeke.reflect.Methods;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

@SuppressWarnings("AccessStaticViaInstance")
@Testable
public class MethodsTests extends Methods {
	@Test void typeTest() {
		Assert.equal(type(of(Object.class, "toString")), MethodType.methodType(String.class))
			.equal(type(of(String.class, "substring", int.class, int.class)), MethodType.methodType(String.class, List.of(int.class, int.class)))
			.equal(type(of(String.class, "toString")), type(of(Object.class, "toString")));
	}

	@Test void defaultValueTest() {
		Assert.exception(() -> defaultValue(AnnotationInterfaceWtihDefault.class, "nope"))
			.equal(defaultValue(AnnotationInterfaceWtihDefault.class, "value"), AnnotationInterfaceWtihDefault.DEFAULT_VALUE);
	}

	@Test void overridesTest() {
		Assert.truth(overrides(of(String.class, "subSequence"), of(CharSequence.class, "subSequence")))
			.truth(overrides(of(String.class, "toString"), of(CharSequence.class, "toString")))
			.truth(overrides(of(String.class, "toString"), of(Object.class, "toString")))
			.truth(overrides(of(CharSequence.class, "toString"), of(Object.class, "toString")));
	}

	@Test void filterBaseTest() {
		Assert.elementsEquivalent(
			filterBase(Stream.of(of(String.class, "toString"), of(CharSequence.class, "toString"), of(Object.class, "toString"))),
			Stream.of(of(Object.class, "toString"))
		);
	}

	@Test void filterOverridingTest() {
		Assert.elementsEquivalent(
			filterOverriding(Stream.of(of(String.class, "toString"), of(CharSequence.class, "toString"), of(Object.class, "toString"))),
			Stream.of(of(String.class, "toString"))
		);
	}

	@Test void samOverriddenTest() {
		Runnable lambda = () -> {};
		var anonymous = new Runnable() {
			@Override public void run() {}
		};

		var run = of(Runnable.class, "run");

		Assert.exception(() -> overridden(null))
			.elementsEquivalent(overridden(lambda.getClass()), Stream.of(run))
			.elementsEquivalent(overridden(anonymous.getClass()), Stream.of(run));
	}

	@Test void samOverriddenTestGenerics() {
		Function<Integer, String> lambda = Object::toString;
		var anonymous = new Function<Integer, String>() {
			@Override public String apply(Integer integer) {
				return integer.toString();
			}
		};

		var apply = of(Function.class, "apply");

		Assert.elementsEquivalent(overridden(lambda.getClass()), overridden(anonymous.getClass()), Stream.of(apply))
			.equal(sam(lambda.getClass()), sam(anonymous.getClass()), apply);
	}

	@Test void samOverriddenTestAbstractChain() {
		interface R extends Runnable {
			@Override void run();
		}

		R lambda = () -> {};

		Assert.elementsEquivalent(overridden(lambda.getClass()), Stream.of(of(Runnable.class, "run"), of(R.class, "run")))
			.equal(sam(lambda.getClass()), of(Runnable.class, "run"));
	}

	@Test void overriddenBigInterface() {
		Assert.elementsEquivalent(overridden(BigInterface.Impl.class), of(BigInterface.class));
	}

	@Test void samTest() {
		Assert.exception(() -> sam(null))
			.nul(sam(BigInterface.Impl.class))
			.nul(sam(Object.class));
	}

	@Test void copyTest() {
		var method = of(Integer.class, "toString", new Class<?>[0]);
		var copy = copy(method);
		var copyCopy = copy(copy);

		Assert.nul(copy(null))
			.equal(method, copy, copyCopy)
			.equalBy(m -> m.invoke(123), method, copy, copyCopy);
	}
}
