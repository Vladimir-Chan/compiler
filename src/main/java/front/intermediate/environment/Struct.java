package front.intermediate.environment;

import java.util.HashSet;

public class Struct extends Identifier {
    private final HashSet<Variable> members;

    public Struct(String identifier, HashSet<Variable> members) {
        super(identifier);
        this.members = members;
    }

    public Variable getMember(String identifierItem) {
        for (Variable variable : members)
            if (variable.identifier.equals(identifierItem))
                return variable;
        return null;
    }

    public boolean addMember(Variable variable) {
        return members.add(variable);
    }

    @Override
    public String toString() {
        return "Struct{" +
                "members=" + members +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}
