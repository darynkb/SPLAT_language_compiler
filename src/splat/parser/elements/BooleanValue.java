package splat.parser.elements;
import splat.executor.Value;

public class BooleanValue extends Value {
    private boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
