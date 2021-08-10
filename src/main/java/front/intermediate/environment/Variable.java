package front.intermediate.environment;

public class Variable extends Identifier {
    private final Identifier type;
    private final boolean pointer;

    public Variable(String identifier, Identifier type, boolean pointer) {
        super(identifier);
        this.type = type;
        this.pointer = pointer;
    }

    public Identifier getType() {
        return type;
    }

    public boolean isPointer() {
        return pointer;
    }

    public boolean isSameType(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Variable that = (Variable) o;
        return pointer == that.pointer && type.equals(that.type);
    }

    @Override
    public String toString() {
        return "Variable{" +
                "type=" + type.identifier +
                ", pointer=" + pointer +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}
