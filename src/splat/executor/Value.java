package splat.executor;
import splat.parser.elements.*;;;


public abstract class Value {
    public abstract Type getType();

    @Override
    public abstract String toString();
}
