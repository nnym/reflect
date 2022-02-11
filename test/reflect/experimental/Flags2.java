package reflect.experimental;

public class Flags2 {
    public static int subfield(int field, int start, int end) {
        return range(field, start, end) << start;
    }

    public static int range(int value, int start, int end) {
        return (value & (1 << end) - 1) >> start;
    }
}
