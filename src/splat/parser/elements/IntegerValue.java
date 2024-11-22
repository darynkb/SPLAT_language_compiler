package splat.parser.elements;
import splat.executor.*;;

public class IntegerValue extends Value {
    private int value;

    public IntegerValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
