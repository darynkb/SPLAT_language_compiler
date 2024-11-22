package splat.parser.elements;

import splat.executor.Value;

public class TypeValue extends Value {
    private final Type type;

    public TypeValue(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TypeValue{" + "type=" + type + '}';
    }
}
