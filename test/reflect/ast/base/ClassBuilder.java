package reflect.ast.base;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reflect.ast.base.method.expression.literal.Literal;
import reflect.ast.base.method.expression.literal.TypeLiteral;
import reflect.ast.base.type.ConcreteType;

public class ClassBuilder extends MemberBuilder<ClassBuilder> {
	public final String pkg;

	protected final Map<String, TypeLiteral> unqualifiedNames = new HashMap<>();
	protected final Set<String> imports = new HashSet<>();
	protected final List<FieldBuilder> fields = new ArrayList<>();
	protected final List<MethodBuilder> methods = new ArrayList<>();
	protected final List<MemberBuilder<?>> members = new ArrayList<>();
	protected final List<MethodBuilder> clinit = new ArrayList<>();

	public ClassBuilder(String name) {
		var packageEnd = name.lastIndexOf('.');
		this.pkg = packageEnd == -1 ? "" : name.substring(0, packageEnd);
		this.name = name.substring(packageEnd + 1);
	}

	public ClassBuilder(String pkg, String name) {
		this(pkg + '.' + name);
	}

	public ClassBuilder(Class<?> klass) {
		this(klass.getName());
	}

	public ClassBuilder member(MemberBuilder<?> member) {
		this.members.add(member);

		return this;
	}

	public ClassBuilder field(Consumer<FieldBuilder> generator) {
		var field = new FieldBuilder();
		generator.accept(field);

		this.fields.add(field);

		return this;
	}

	public ClassBuilder method(Consumer<MethodBuilder> generator) {
		var method = new MethodBuilder();
		generator.accept(method);

		this.methods.add(method);

		return this;
	}

	public ClassBuilder clinit(Consumer<MethodBuilder> generator) {
		var clinit = new MethodBuilder();
		generator.accept(clinit);

		this.clinit.add(clinit);

		return this;
	}

	public void write(OutputStream output) {
		output.write(this.toString().getBytes());
	}

	public void write(String file) {
		this.write(new FileOutputStream(file));
	}

	protected TypeLiteral imports(ConcreteType type) {
		return this.imports(type.type());
	}

	protected TypeLiteral imports(Class<?> type) {
		if (type.isPrimitive() || type.getPackageName().equals("java.lang")) {
			return Literal.of(type);
		}

		return this.unqualifiedNames.computeIfAbsent(type.getName(), name -> {
			name = name.replace("[", "");

			if (name.charAt(name.length() - 1) == ';') {
				name = name.substring(1, name.length() - 1);
			}

			var nestIndex = name.indexOf('$');
			this.imports.add((nestIndex > 0 ? name.substring(0, nestIndex) : name).replace('$', '.'));

			return Literal.of(type);
		});
	}

	@Override
	public Stream<ConcreteType> referencedTypes() {
		return Stream.concat(this.fields.stream(), this.methods.stream()).flatMap(Node::referencedTypes);
	}

	@Override
	public String toString() {
		var string = new IndentingStringBuilder();

		if (!this.pkg.isEmpty()) {
			string.append("package ").append(this.pkg).append(';').newline().newline();
		}

		string.append(this.referencedTypes().map(this::imports).map(type -> "import " + type + ";\n").collect(Collectors.joining())).newline();
		string.append(this.offsetAccessString()).append("class ").append(this.name).append(" {").descend().newline();

		string.append(this.fields.stream()
			.map(FieldBuilder::toString)
			.collect(Collectors.joining(string.newlineString()))
		);

		string.append(this.methods.stream()
			.map(MethodBuilder::toString)
			.map(method -> method.lines().collect(Collectors.joining(string.newlineString(), "", "\n")))
			.collect(Collectors.joining(string.newlineString()))
		);

		return string.ascend().newline().append('}').toString();
	}
}
