package test;

import net.auoeke.reflect.Constructors;
import org.junit.jupiter.api.Test;
import reflect.misc.Enumeration;

public class ConstructorsTests extends Constructors {
    @Test void test() {
        Assert.arraysEqual(direct(PrivateCtor.class), PrivateCtor.class.getDeclaredConstructors());
        assert find(PrivateCtor.class, int.class).newInstance(4).test == 4;
        assert construct(PrivateCtor.class, 27).test == 27;
        assert find(0L, Enumeration.class, "", 1, 4D) == null;
        assert find(Enumeration.class, "", 1, 4D) != null;
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
