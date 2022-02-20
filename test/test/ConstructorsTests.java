package test;

import net.auoeke.reflect.Constructors;
import org.junit.jupiter.api.Test;
import reflect.misc.Enumeration;

public class ConstructorsTests {
    @Test
    void constructor() {
        Assert.arraysEqual(Constructors.direct(PrivateCtor.class), PrivateCtor.class.getDeclaredConstructors());
        assert Constructors.find(PrivateCtor.class, int.class).newInstance(4).test == 4;
        assert Constructors.construct(PrivateCtor.class, 27).test == 27;
        assert Constructors.find(0L, Enumeration.class, "", 1, 4D) == null;
        assert Constructors.find(Enumeration.class, "", 1, 4D) != null;
    }

    static class PrivateCtor {
        public final int test;

        private PrivateCtor(int test) {
            this.test = test;
        }
    }
}
