package reflect.ast;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import net.auoeke.reflect.Flags;
import net.auoeke.reflect.Invoker;
import org.junit.jupiter.api.AfterEach;
import reflect.ast.base.ClassBuilder;
import reflect.ast.base.method.expression.Expression;
import reflect.ast.base.method.expression.Invocation;
import reflect.ast.base.method.expression.literal.Literal;

public abstract class TestBuilder extends ClassBuilder {
	public TestBuilder(Class<?> klass) {
		super(klass);
	}

	public TestBuilder(String name) {
		super(name);
	}

	public TestBuilder(String pakage, String name) {
		super(pakage, name);
	}

	@AfterEach
	protected void tearDown() {
		System.out.println(this);
	}

	protected ClassBuilder methodHandle(Expression klass, Method method) {
		var name = Flags.isStatic(method) ? "findStatic" : Flags.isPrivate(method) ? "findSpecial" : "findVirtual";
		var initializer = new Invocation(Invoker.class, name).argument(
			klass,
			Literal.of(method.getName()),
			Literal.of(method.getReturnType())
		);

		Stream.of(method.getParameterTypes()).map(Literal::of).forEach(initializer::argument);
		this.field(field -> field.type(MethodHandle.class).name(method.getName()).initialize(initializer));

		return this;
	}
}
