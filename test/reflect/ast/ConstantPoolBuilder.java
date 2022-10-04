package reflect.ast;

import net.auoeke.reflect.Classes;
import net.auoeke.reflect.Flags;
import net.auoeke.reflect.Methods;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.commons.annotation.Testable;
import reflect.ast.base.method.expression.GetField;

@Disabled
@Testable
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConstantPoolBuilder extends TestBuilder {
	private ConstantPoolBuilder() {
		super("net.auoeke.reflect.ConstantPool");

		this.public$();
	}

	@Override
	@AfterEach
	protected void tearDown() {
		super.tearDown();
	}

	@Test
	public void generate() {
		Methods.of(Classes.ConstantPool).forEach(method -> {
			var returnType = method.getReturnType();

			if (returnType.getModule().isExported(returnType.getPackageName()) && !Flags.isNative(method)) {
				this.methodHandle(new GetField().of(Classes.class).name("ConstantPool"), method);
			}
		});
	}
}
