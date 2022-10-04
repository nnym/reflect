package other;

import java.lang.reflect.Field;
import net.auoeke.reflect.Accessor;
import net.auoeke.reflect.Fields;
import net.auoeke.reflect.Flags;
import net.auoeke.reflect.Pointer;
import org.openjdk.jmh.annotations.Benchmark;

public class SetField {
	static final Integer integer = Integer.MAX_VALUE / 2;
	static final Pointer valuePointer = Pointer.of(integer, "value");
	static final Field valueField = Fields.of(integer, "value");
	static int nextValue;

	@Benchmark public Integer pointer() {
		valuePointer.put(nextValue++);

		return integer;
	}

	@Benchmark public Integer field() {
		valueField.set(integer, nextValue++);

		return integer;
	}

	@Benchmark public Integer accessor() {
		Accessor.putInt(integer, "value", nextValue++);

		return integer;
	}

	@Benchmark public Integer accessorGeneric() {
		Accessor.put(integer, "value", nextValue++);

		return integer;
	}

	static {
		Fields.override.put(valueField, true);
		Fields.modifiers.put(valueField, Flags.remove(valueField, Flags.FINAL));
	}
}
