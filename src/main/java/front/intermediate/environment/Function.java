package front.intermediate.environment;


import java.util.List;

public class Function extends Identifier {
    private final List<Variable> parameters;
    private final Identifier returnType;
    private final boolean pointer;

    public Function(String identifier, List<Variable> parameters, Identifier returnType, boolean pointer) {
        super(identifier);
        this.parameters = parameters;
        this.returnType = returnType;
        this.pointer = pointer;
    }

    public boolean checkParameterType(List<Variable> parameters) {
        if (parameters.size() != this.parameters.size())
            return false;
        for (int i = 0; i < parameters.size(); i++)
            if (!parameters.get(i).isSameType(this.parameters.get(i)))
                return false;
        return true;
    }

    public boolean checkReturnParameterType(Variable parameter) {
        return parameter.getType().equals(returnType) && parameter.isPointer() == pointer;
    }

    public boolean checkReturnParameterType() {
        return returnType.identifier.equals("void");
    }
}
