package test;

import net.auoeke.reflect.Constructors;
import org.junit.jupiter.api.Test;
import reflect.misc.Enumeration;

public class ConstructorsTests extends Constructors {
    @Test
    void test() {
        Assert.arraysEqual(direct(PrivateCtor.class), PrivateCtor.class.getDeclaredConstructors());
        assert find(PrivateCtor.class, int.class).newInstance(4).test == 4;
        assert construct(PrivateCtor.class, 27).test == 27;
        assert find(0L, Enumeration.class, "", 1, 4D) == null;
        assert find(Enumeration.class, "", 1, 4D) != null;
    }

    static class PrivateCtor {
        public final int test;

        private PrivateCtor(int test) {
            this.test = test;
        }
    }
}
