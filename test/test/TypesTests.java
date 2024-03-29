package test;

import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Methods;
import net.auoeke.reflect.Types;
import org.junit.jupiter.api.Test;
import reflect.misc.*;
import sun.misc.Unsafe;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("AccessStaticViaInstance")
class TypesTests extends Types {
	@Test void classesTest() {
		assert classes(C.class, A.class).count() == 2;
		assert classes(Object.class).count() == 1;
	}

	@Test void hierarchyTest() {
		var hierarchy = hierarchy(Interface3.Impl.class).toList();
		var test = List.of(Interface3.Impl.class, Interface3.class, Interface2.class, Interface1.class, Object.class);
		assert test.containsAll(hierarchy) && hierarchy.containsAll(test);
	}

	// Must not depend on the standard library's hierarchy excessively.
	@Test void depthTest() {
		// classes
		Assert.equal(0, depth(null), depth(null, false));
		Assert.equal(1, depth(Object.class), depth(Object.class, false));
		Assert.equal(2, depth(Types.class), depth(Types.class, false));
		Assert.equal(3, depth(TypesTests.class), depth(TypesTests.class, false));

		// interfaces
		assert depth(null, true) == 0;
		Assert.equal(1, depth(Interface1.class), depth(Interface1.class, true));
		Assert.equal(2, depth(Interface2.class), depth(Interface2.class, true));
		Assert.equal(3, depth(Interface3.class), depth(Interface3.class, true));
	}

	@Test void differenceTest() {
		assert difference(Object.class, null) == 1;
		assert difference(null, Object.class) == -1;
		assert difference(TypesTests.class, Integer.class) == Integer.MAX_VALUE;
		Assert.equal(0, difference(null, null), difference(Object.class, Object.class), difference(Interface2.class, Interface2.class));
		assert difference(Interface3.class, Interface1.class) == 2;
		assert difference(Interface3.class, null) == 3;
		assert difference(Interface1.class, Interface2.class) == -1;
		assert difference(Interface1.Impl.class, Interface1.class) == 1;
		assert difference(Interface1.class, Interface1.Impl.class) == -1;
	}

	@Test void sizeTest() {
		Assert.equal(stackSize(Object.class), stackSize(Integer.class), Unsafe.ADDRESS_SIZE)
			.equal(size(Object.class), (int) Classes.firstField.offset)
			.equal(size(void.class), stackSize(void.class), 0)
			.equal(size(boolean.class), stackSize(boolean.class), Byte.BYTES)
			.equal(size(byte.class), stackSize(byte.class), Byte.BYTES)
			.equal(size(short.class), stackSize(short.class), Short.BYTES)
			.equal(size(char.class), stackSize(char.class), Character.BYTES)
			.equal(size(int.class), stackSize(int.class), Integer.BYTES)
			.equal(size(float.class), stackSize(float.class), Float.BYTES)
			.equal(size(long.class), stackSize(long.class), Long.BYTES)
			.equal(size(double.class), stackSize(double.class), Double.BYTES);
	}

	@Test void unbox() {
		assert unbox(Integer.class) == int.class;
		assert unbox(int.class) == int.class;
		assert unbox(Object.class) == null;
		assert unbox(Double.class) == double.class;
		assert unbox(Void.class) == void.class;
		assert unbox(void.class) == void.class;
	}

	@Test void boxTest() {
		assert box(int.class) == Integer.class;
		assert box(Integer.class) == Integer.class;
		assert box(Object.class) == Object.class;
		assert box(double.class) == Double.class;
		assert box(void.class) == Void.class;
		assert box(Void.class) == Void.class;
	}

	@Test void equalsTest() {
		assert similar(int.class, Integer.class);
		assert similar(byte.class, Byte.class);
		assert similar(short.class, Short.class);
		assert similar(Integer.class, int.class);
		assert similar(Double.class, double.class);
		assert !similar(Object.class, Integer.class);
		assert !similar(Object.class, TypesTests.class);
		assert !similar(Object.class, null);
		assert similar(null, null);
		assert similar(Object.class, Object.class);
	}

	@Test void isWrapperTest() {
		assert isWrapper(Integer.class);
		assert !isWrapper(short.class);
		assert !isWrapper(void.class);
		assert isWrapper(Void.class);
		assert isWrapper(Double.class);
		assert !isWrapper(TypesTests.class);
		assert !isWrapper(Object.class);
		assert !isWrapper(int.class);
	}

	@SuppressWarnings("CloneableClassWithoutClone")
	@Test void isAssignableFromTest() {
		//noinspection unused
		class C<T extends U, U extends C<T, U> & Cloneable> {
			<V extends T> void a(Consumer<? super V[]> consumer, Supplier<? extends V[]> supplier) {}
		}

		class D extends C implements Cloneable {}
		class E extends D {}

		var t = C.class.getTypeParameters()[0];
		var a = Methods.firstOf(C.class, "a").getGenericParameterTypes();
		var sv = ((ParameterizedType) a[0]).getActualTypeArguments()[0];
		var ev = ((ParameterizedType) a[0]).getActualTypeArguments()[0];

		returnPrimitives().forEach(typeA -> {
			returnPrimitives().forEach(typeB -> {
				if (typeA == typeB) {
					Assert.yes(canAssign(typeA, typeB));
				} else {
					Assert.no(canAssign(typeA, typeB));
				}
			});
		});

		Assert.exception(IllegalArgumentException.class, () -> canAssign(Object.class, new Type() {}))
			.yes(canAssign(null, null))
			.no(canAssign(null, Object.class))
			.no(canAssign(Object.class, null))
			.yes(canAssign(Object.class, Types.class))
			.no(canAssign(Types.class, Object.class))
			.yes(canAssign(Number.class, Integer.class))
			.no(canAssign(Integer.class, Number.class))
			.yes(canAssign(Integer.class, Integer.class))
			.no(canAssign(Integer.class, Double.class))
			.yes(canAssign(Object[].class, Number[].class))
			.no(canAssign(Number[].class, Object[].class))
			.no(canAssign(t, C.class))
			.yes(canAssign(t, D.class))
			.no(canAssign(sv, Object.class))
			.no(canAssign(sv, Cloneable[].class))
			.no(canAssign(sv, C[].class))
			.yes(canAssign(sv, D[].class))
			.yes(canAssign(sv, E[].class))
			.yes(canAssign(Object.class, sv))
			.yes(canAssign(Cloneable[].class, sv))
			.yes(canAssign(C[].class, sv))
			.no(canAssign(D[].class, sv))
			.no(canAssign(E[].class, sv))
			.no(canAssign(ev, Object.class))
			.no(canAssign(ev, Cloneable[].class))
			.no(canAssign(ev, C[].class))
			.yes(canAssign(ev, D[].class))
			.yes(canAssign(ev, E[].class))
			.yes(canAssign(Object.class, ev))
			.yes(canAssign(Cloneable[].class, ev))
			.yes(canAssign(C[].class, ev))
			.no(canAssign(D[].class, ev))
			.no(canAssign(E[].class, ev));
	}

	@Test void canCastTest() {
		assert !Types.canCast(0L, new Class[]{int.class, Integer.class, Object.class, void.class}, Integer.class, Integer.class, Object.class, void.class);
		assert Types.canCast(0, new Class[]{int.class, Integer.class, Object.class, void.class}, Integer.class, Integer.class, Object.class, Void.class);
		assert Types.canCast(Object.class, Object.class);
		assert Types.canCast(Object.class, this.getClass());
		assert !Types.canCast(this.getClass(), Object.class);
		assert Types.canCast(Integer.class, int.class);
		assert Types.canCast(int.class, Integer.class);
		assert Types.canCast(new Class[]{Double.class, Long.class, long.class, this.getClass(), Object.class}, double.class, Long.class, Long.class, this.getClass(), this.getClass());
		assert Types.canCast(new Class[]{Double.class, Long.class, long.class, this.getClass(), Object.class}, 0D, 0L, 0L, this, this);
		assert !Types.canCast(Double.class, int.class);
		assert !Types.canCast(Double.class, float.class);
		assert !Types.canCast(Double.class, Float.class);
		assert Types.canCast(double.class, float.class);
		assert Types.canCast(long.class, int.class);
		assert Types.canCast(long.class, short.class);
		assert Types.canCast(int.class, char.class);
		assert !Types.canCast(int.class, boolean.class);
		assert Types.canCast(REWRAP, Double.class, Float.class);
		assert Types.canCast(REWRAP, Long.class, Integer.class);
		assert Types.canCast(REWRAP, Integer.class, Short.class);
		assert Types.canCast(REWRAP, Long.class, Byte.class);
		assert !Types.canCast(REWRAP, Integer.class, Long.class);
	}

	@Test void arrayBoxTest() {
		var ints = IntStream.range(0, 10000).toArray();
		assert Arrays.equals(ints, Stream.of(Types.box(ints)).mapToInt(i -> i).toArray());

		var doubles = DoubleStream.iterate(0, d -> d < 100, d -> d + 1).toArray();
		assert Arrays.equals(doubles, Stream.of(Types.box(doubles)).mapToDouble(d -> d).toArray());

		byte[] bytes = {1, 2, 3, 4, 5};
		var box = Types.box(bytes);

		assert box[0] == bytes[0]
			&& box[1] == bytes[1]
			&& box[2] == bytes[2]
			&& box[3] == bytes[3]
			&& box[4] == bytes[4];

		assert Types.box(box) == box;

		var objects = new Object[0];
		assert Types.box(objects) == objects;
	}

	@Test void arrayUnboxTest() {
		Integer[] ints = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		assert Arrays.equals(Stream.of(ints).mapToInt(i -> i).toArray(), Types.unbox(ints));

		var doubles = Stream.iterate(0D, d -> d < 100, d -> d + 1).toArray(Double[]::new);
		assert Arrays.equals(Stream.of(doubles).mapToDouble(d -> d).toArray(), Types.unbox(doubles));

		Byte[] box = {1, 2, 3, 4, 5};
		byte[] bytes = Types.unbox(box);

		assert bytes[0] == box[0]
			&& bytes[1] == box[1]
			&& bytes[2] == box[2]
			&& bytes[3] == box[3]
			&& bytes[4] == box[4];

		assert Types.unbox(bytes) == bytes;
		assert Types.unbox(new Object[0]) == null;
	}

	@Test void arrayCustomUnboxTest() {
		Integer[] integers = {1, 2, 3, 4};
		var ints = new int[]{1, 2, 3, 4};
		var longs = new long[]{1, 2, 3, 4};
		var floats = new float[]{0.1F, 0.1F, 0.2F, 0.3F, 0.5F, 0.8F, 1.3F, 2.1F, 3.4F, 5.5F, 8.9F, 14.4F, 233.123F, 572.6F};
		var doubles = Types.<double[]>convert(floats, double.class);
		assert IntStream.range(0, floats.length).allMatch(index -> floats[index] == doubles[index]);
		assert unbox(ints, int.class) == ints;
		assert Arrays.equals(longs, unbox(ints, long.class));
		assert Arrays.equals(ints, unbox(integers, int.class));
		assert Arrays.equals(longs, unbox(integers, long.class));

		Assert.exception(() -> unbox(integers, Integer.class));
		Assert.exception(() -> unbox(integers, short.class));
		Assert.exception(() -> unbox(integers, boolean.class));
		Assert.exception(() -> unbox(integers, void.class));
	}

	@Test void convert() {
		Integer[] integers = {1, 2, 3, 4};
		int[] ints = {1, 2, 3, 4};
		assert Arrays.equals(integers, convert(ints, Integer.class));
	}
}
