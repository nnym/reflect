package test;

import mock.EmptyRecord;
import net.auoeke.reflect.Constructors;
import org.junit.jupiter.api.Test;
import reflect.misc.Enumeration;

import java.lang.reflect.Constructor;

@SuppressWarnings("AccessStaticViaInstance")
public class ConstructorsTests extends Constructors {
	@Test void test() {
		Assert.arraysEqual(PrivateCtor.class.getDeclaredConstructors(), PrivateCtor.class.getDeclaredConstructors());
		assert find(PrivateCtor.class, int.class).newInstance(4).test == 4;
		assert construct(PrivateCtor.class, 27).test == 27;
		assert find(0L, Enumeration.class, "", 1, 4D) == null;
		assert find(Enumeration.class, "", 1, 4D) != null;
	}

	@Test void canonicalTest() {
		record R0A() {}
		record R0B() {
			R0B(int p) {this();}
		}
		record R1A(int a) {}
		record R1B(int a) {
			R1B() {this(0);}
		}
		record R2A(int a, String b) {}
		record R2B(int a, String b) {
			R2B(byte b) {this(b, null);}
			R2B() {this(0, null);}
		}

		Assert.equal(find(R0A.class), canonical(R0A.class))
			.equal(find(R0B.class), canonical(R0B.class))
			.equal(find(R1A.class, int.class), canonical(R1A.class))
			.equal(find(R1B.class, int.class), canonical(R1B.class))
			.equal(find(R2A.class, int.class, String.class), canonical(R2A.class))
			.equal(find(R2B.class, int.class, String.class), canonical(R2B.class));
	}

	@Test void instantiateTest() {
		assert Constructors.instantiate(Base.class).o != null;

		var pc = Constructors.instantiate(PrivateCtor.class);
		assert pc.boolea;
		assert pc.byt == 123;
		assert pc.cha == 'i';
		assert pc.shor == 34;
		assert pc.in == 56;
		assert pc.lon == 4;
		assert pc.floa == 1.5F;
		assert pc.doubl == 3.5;
		assert pc.o == null;
	}

	@Test void copyTest() {
		var constructor = find(EmptyRecord.class);
		var copy = copy(constructor);
		var copyCopy = copy(copy);

		Assert.nul(copy(null))
			.equal(constructor, copy, copyCopy)
			.equalBy(Constructor::newInstance, constructor, copy, copyCopy);
	}

	static class Base {
		public final boolean boolea = true;
		public final byte byt = 123;
		public final char cha = 'i';
		public final short shor = 34;
		public final int in = 56;
		public final long lon = 4;
		public final float floa = 1.5F;
		public final double doubl = 3.5;
		public final Object o = new Object();
	}

	static class PrivateCtor extends Base {
		public final int test;

		private PrivateCtor(int test) {
			this.test = test;
		}
	}
}
