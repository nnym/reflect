package reflect.ast.base.method.statement;

import java.util.stream.Stream;
import reflect.ast.base.Node;

public interface Statement extends Node {
	default Stream<String> lines() {
		return this.toString().lines();
	}
}
