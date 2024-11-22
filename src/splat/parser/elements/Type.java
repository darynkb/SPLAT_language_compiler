package splat.parser.elements;

public class Type {
    private String name;

    public static final Type INTEGER = new Type("Integer");
    public static final Type BOOLEAN = new Type("Boolean");
    public static final Type STRING = new Type("String");
    public static final Type VOID = new Type("void");

    private Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Type type = (Type) obj;
        return name.equals(type.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
