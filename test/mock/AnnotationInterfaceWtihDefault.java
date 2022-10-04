package mock;

public @interface AnnotationInterfaceWtihDefault {
	String DEFAULT_VALUE = "default value";

	String value() default DEFAULT_VALUE;
}
