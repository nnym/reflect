package user11681.reflect.generator.base.method;

import user11681.reflect.generator.base.type.Type;

public interface Expression {
     default Invocation invoke() {
          return new Invocation().owner(this);
     }

     default Invocation invoke(String method) {
         return this.invoke().name(method);
     }

     default Expression cast(Type type) {
          return new CastExpression(this, type);
     }

     default Expression cast(Class<?> type) {
          return this.cast(new ConcreteType(type));
     }
}
