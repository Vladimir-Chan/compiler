package front.intermediate.environment;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class Environment {
    private final HashSet<Identifier> globalTable; // 全局符号表，存储全局定义
//    private final HashSet<Variable> functionTable; // 函数中变量符号表
//    private final HashSet<Variable> localTable; // 局部变量符号表，供循环使用
    private final Stack<HashSet<Variable>> variableTable; // 变量表
    private Function currentFunction; // 当前函数

    public Environment(Set<Identifier> typeSet) {
        globalTable = new HashSet<>(typeSet);
        variableTable = new Stack<>();
        currentFunction = null;
    }

    public Function getCurrentFunction() {
        return currentFunction;
    }

    public void setCurrentFunction(Function currentFunction) {
        this.currentFunction = currentFunction;
    }

    public boolean newEnvironment(List<Variable> parameterList) {
        variableTable.push(new HashSet<>(parameterList));
        return variableTable.peek().size() == parameterList.size();
    }

    public void newEnvironment() {
        variableTable.push(new HashSet<>());
    }

    public void goBackEnvironment() {
        variableTable.pop();
    }

    public boolean addDefinition(Identifier identifier) {
        return globalTable.add(identifier);
    }

    public Identifier getDefinition(String identifier) {
        for (Identifier id : globalTable)
            if (id.identifier.equals(identifier))
                return id;
        return null;
    }

    public boolean addVariable(Variable variable) {
        return variableTable.peek().add(variable);
    }

    public Variable getVariable(String identifier) {
        for (int i = variableTable.size() - 1; i >= 0; i--)
            for (Variable var : variableTable.get(i))
                if (var.identifier.equals(identifier))
                    return var;
        return null;
    }

    @Override
    public String toString() {
        return "Environment{\n" +
                "variable" + variableTable.peek() +
                '}';
    }
}
