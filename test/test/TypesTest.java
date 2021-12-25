package test;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.auoeke.reflect.Types;
import net.auoeke.reflect.misc.A;
import net.auoeke.reflect.misc.C;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

class TypesTest extends Types {
    @Test
    void classesTest() {
        assert classes(C.class, A.class).count() == 2;
        assert classes(Object.class).count() == 1;
    }

    @Test
    void sizeTest() {
        assert size(boolean.class) == Byte.SIZE;
        assert size(byte.class) == Byte.SIZE;
        assert size(short.class) == Short.SIZE;
        assert size(char.class) == Character.SIZE;
        assert size(int.class) == Integer.SIZE;
        assert size(float.class) == Float.SIZE;
        assert size(long.class) == Long.SIZE;
        assert size(double.class) == Double.SIZE;
        assert size(Object.class) == Unsafe.ADDRESS_SIZE * Byte.SIZE;
    }

    @Test
    void unbox() {
        assert unbox(Integer.class) == int.class;
        assert unbox(int.class) == int.class;
        assert unbox(Object.class) == null;
        assert unbox(Double.class) == double.class;
        assert unbox(Void.class) == void.class;
        assert unbox(void.class) == void.class;
    }

    @Test
    void boxTest() {
        assert box(int.class) == Integer.class;
        assert box(Integer.class) == Integer.class;
        assert box(Object.class) == Object.class;
        assert box(double.class) == Double.class;
        assert box(void.class) == Void.class;
        assert box(Void.class) == Void.class;
    }

    @Test
    void equalsTest() {
        assert equals(int.class, Integer.class);
        assert equals(byte.class, Byte.class);
        assert equals(short.class, Short.class);
        assert equals(Integer.class, int.class);
        assert equals(Double.class, double.class);
        assert !equals(Object.class, Integer.class);
        assert !equals(Object.class, TypesTest.class);
        assert !equals(Object.class, null);
        assert equals(null, null);
        assert equals(Object.class, Object.class);
    }

    @Test
    void isWrapperTest() {
        assert isWrapper(Integer.class);
        assert !isWrapper(short.class);
        assert !isWrapper(void.class);
        assert isWrapper(Void.class);
        assert isWrapper(Double.class);
        assert !isWrapper(TypesTest.class);
        assert !isWrapper(Object.class);
        assert !isWrapper(int.class);
    }

    @Test
    void canCastTest() {
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
    }

    @Test
    void boxTest1() {
        var ints = IntStream.range(0, 10000).toArray();
        assert Arrays.equals(ints, Stream.of(Types.box(ints)).mapToInt(i -> (int) i).toArray());

        var doubles = DoubleStream.iterate(0, d -> d < 100, d -> d + 1).toArray();
        assert Arrays.equals(doubles, Stream.of(Types.box(doubles)).mapToDouble(d -> (double) d).toArray());

        byte[] bytes = {1, 2, 3, 4, 5};
        var box = Types.<Byte>box(bytes);

        assert box[0] == bytes[0]
               && box[1] == bytes[1]
               && box[2] == bytes[2]
               && box[3] == bytes[3]
               && box[4] == bytes[4];

        assert Types.box(box) == box;

        var objects = new Object[0];
        assert Types.box(objects) == objects;
    }

    @Test
    void unboxTest2() {
        var ints = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).toArray(Integer[]::new);
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
}
