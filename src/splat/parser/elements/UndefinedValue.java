package splat.parser.elements;
import splat.executor.*;;

public class UndefinedValue extends Value {
    private Type type;

    public UndefinedValue(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "UndefinedValue{" +
               "type=" + type +
               '}';
    }
}
