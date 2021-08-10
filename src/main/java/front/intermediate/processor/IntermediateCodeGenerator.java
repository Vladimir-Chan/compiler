package front.intermediate.processor;

import front.intermediate.code.AssignmentIntermediateCode;
import front.intermediate.code.IfIntermediateCode;
import front.intermediate.code.IntermediateCode;
import front.intermediate.code.JumpOperationIntermediateCode;
import front.intermediate.environment.*;
import front.lexer.Lexer;
import front.lexer.Token;
import front.parser.item.Production;
import front.parser.item.Symbol;
import front.parser.processor.Parse;
import utils.exceptions.CompileException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class IntermediateCodeGenerator {
    private final Lexer lexer; // 词法分析器
    private final Parse parse; // 语法分析器
    private final ArrayList<IntermediateCode> intermediateCodes;// 中间代码
    private final HashMap<String, Integer> functionMap; // 函数首地址Map
    private final Environment environment; // 环境

    public IntermediateCodeGenerator(Lexer lexer, Parse parse) {
        this.lexer = lexer;
        this.parse = parse;
        intermediateCodes = new ArrayList<>();
        HashSet<Identifier> typeSet = new HashSet<>();
        typeSet.add(new Identifier("void"));
        typeSet.add(new Identifier("char"));
        typeSet.add(new Identifier("int"));
        typeSet.add(new Identifier("double"));
        functionMap = new HashMap<>();
        environment = new Environment(typeSet);
    }

//    public void run() throws IOException, CompileException {
//        int flag = -1;
//        Token token = null;
//        Symbol symbol = null;
//        do {
//            if (flag < 0) {
//                token = lexer.nextToken();
//                if (token == null)
//                    token = new Token(6, "$");
//                symbol = parse.transfer(token);
//            }
//            System.out.println(token);
//            flag = parse.grammaticalAnalysis(symbol);
////            flag = parse.grammaticalAnalysis(token);
//        } while (flag != 0);
//    }

    public void run() throws IOException, CompileException {
        Token dollar = new Token(6, "$");
        Token token = lexer.nextToken();
        if (token == null)
            token = dollar;
        do {
            if (token.getValue().equals("struct")){
                // 规约 struct定义式
                token = getStructDefinition();
            } else if (token.getValue().equals("$")) {
                // 规约完成
                parse.grammaticalAnalysis(parse.transfer(token)); // 规约external_declaration
                parse.grammaticalAnalysis(parse.transfer(token)); // 规约program
                parse.grammaticalAnalysis(parse.transfer(token)); // 规约program'
                break;
            } else {
                // 规约函数
                token = getFunctionDefinition(token);
            }
            if (token == null)
                token = dollar;
        } while (true);
    }

    public void outputCodes(String file) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < intermediateCodes.size(); i++) {
            bufferedWriter.write(i + " " + intermediateCodes.get(i));
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private Token getStructDefinition() throws IOException, CompileException {
        Token dollar = new Token(6, "$");
        parse.grammaticalAnalysis(new Symbol("struct", true)); // 移入动作
        Token token = lexer.nextToken(); // id
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入动作
        String structId = token.getValue();
        if (environment.getDefinition(structId) != null)
            // 已经有这个结构体的定义
            throw new CompileException();
        parse.grammaticalAnalysis(parse.transfer(lexer.nextToken())); // 移入{
        Struct struct = new Struct(structId, new HashSet<>());

        // 规约member_list

        Stack<Token> tokenStack = new Stack<>();
        Identifier type = null;
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        label:
        while (true){
            int flag = parse.grammaticalAnalysis(parse.transfer(token));
            if (flag > 0) {
                String productionLeft = parse.getProduction(flag).getLeft().getValue();
                switch (productionLeft) {
                    // 对于不同的规约产生式分别处理
                    case "program":
                        break label;
                    case "variable_type":
                        type = environment.getDefinition(tokenStack.peek().getValue());
                        if (type == null)
                            if (tokenStack.peek().getValue().equals(structId))
                                type = struct;
                            else {
                                // 结构体没有定义
                                System.out.println(tokenStack);
                                throw new CompileException();
                            }
                        tokenStack.clear();
                        break;
                    case "single_variable":
                        Variable variable = null;
                        if (tokenStack.size() == 1)
                            if (type instanceof Struct && tokenStack.peek().getValue().equals(type.getIdentifier()))
                                // 结构体中包含自身
                                throw new CompileException();
                            else
                                variable = new Variable(tokenStack.peek().getValue(), type, false);
                        else if (tokenStack.size() == 2)
                            variable = new Variable(tokenStack.peek().getValue(), type, true);
                        else if (tokenStack.size() == 4)
                            variable = new Variable(tokenStack.get(0).getValue(), type, true);
                        else {
                            System.out.println(tokenStack);
                            throw new CompileException();
                        }
//                    switch (tokenStack.size()) {
//                        // 栈中为 struct id id 或 struct id id[constant]
//                        case 3, 6 -> variable = new Variable(tokenStack.get(2).getValue(), -1, (Struct) definition, false);
//                        // 栈中为 struct id *id
//                        case 4 -> variable = new Variable(tokenStack.get(3).getValue(), -1, (Struct) definition, true);
//                        default -> {
//                            System.out.println(tokenStack);
//                            System.exit(0);
//                        }
//                    }
                        boolean isContains = !struct.addMember(variable);
                        if (isContains)
                            throw new CompileException();
                        tokenStack.clear();
                        break;
//                    case "variable_declaration":
//                        // 一种类型的变量定义完成
//                        tokenStack.clear();
//                        break;
                }
            } else {
                if (flag != -3 && !token.getValue().equals(","))
                    tokenStack.push(token);
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
            }
        }
        // struct_definition 规约完成
        environment.addDefinition(struct);
        return token;
    }

    private Token getFunctionDefinition(Token token) throws IOException, CompileException {
        // 确定 return type与 is pointer
        Token dollar = new Token(6, "$");
        String value;
        Stack<Token> tokenStack = new Stack<>();
        int flag;
        while (true) {
            flag = parse.grammaticalAnalysis(parse.transfer(token));
            if (flag > 0) {
                value = parse.getProduction(flag).getLeft().getValue();
                if (value.equals("return_type"))
                    break;
            } else {
                tokenStack.push(token);
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
            }
        }
        Identifier returnType;
        boolean pointer;
        if (tokenStack.size() == 3) {
            // 说明是struct
            returnType = environment.getDefinition(tokenStack.get(1).getValue());
            pointer = true;
            if (returnType == null)
                throw new CompileException();
        } else {
            returnType = environment.getDefinition(tokenStack.get(0).getValue());
            pointer = tokenStack.size() == 2;
        }

        //确定id
        String functionName = token.getValue(); // 一定是id
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入id
        token = lexer.nextToken(); // 一定是(
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约function name
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入(

        // 规约parameter_list
        tokenStack.clear();
        ArrayList<Variable> parameters = new ArrayList<>();
        Identifier variableType = null;
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        label:
        while (true) {
            flag = parse.grammaticalAnalysis(parse.transfer(token));
            if (flag > 0) {
                value = parse.getProduction(flag).getLeft().getValue();
                switch (value) {
                    case "variable_type":
                        variableType = environment.getDefinition(tokenStack.peek().getValue());
                        tokenStack.clear();
                        if (variableType == null)
                            // 没有这个定义
                            throw new CompileException();
                        break;
                    case "single_variable":
                        switch (tokenStack.size()) {
                            case 1:
                                parameters.add(new Variable(tokenStack.peek().getValue(), variableType, false));
                                break;
                            case 2:
                                parameters.add(new Variable(tokenStack.peek().getValue(), variableType, true));
                                break;
                            case 4:
                                parameters.add(new Variable(tokenStack.get(0).getValue(), variableType, true));
                                break;
                            default:
                                System.out.println(tokenStack);
                                System.exit(0);
                        }
                        tokenStack.clear();
                        break;
                    case "formal_parameter_list":
                        break label;
                }
            } else {
                tokenStack.push(token);
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
            }
        }
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入)

        // 符号表中加入函数定义
        Function function = new Function(functionName, parameters, returnType, pointer);
        if (!environment.addDefinition(function))
            // 存在相同名
            throw new CompileException();
        // 切换到新环境
        if (!environment.newEnvironment(parameters))
            // 形参存在同名
            throw new CompileException();

        // 设置环境中当前函数
        environment.setCurrentFunction(function);
        functionMap.put(functionName, intermediateCodes.size());
        // 将实参从寄存器中取出
        for (int i = 0; i < parameters.size(); i++)
            intermediateCodes.add(new AssignmentIntermediateCode(null, "$a" + i, null, parameters.get(i).getIdentifier()));

        // 规约函数体，即复合语句，生成中间代码
        if (lexer.nextToken().getValue().equals("{")) {
            token = getCompoundStatement(null, null);
            parse.grammaticalAnalysis(parse.transfer(token)); // 规约函数
            // 返回环境
            environment.goBackEnvironment();
            return token;
        } else
            throw new CompileException();
    }

    private Token getStatement(Token token, ArrayList<Integer> breakIndexes, ArrayList<Integer> continueIndexes) throws IOException, CompileException {
        if (token.getValue().equals("{")) {
            // 切换新环境
            environment.newEnvironment();
            token = getCompoundStatement(breakIndexes, continueIndexes);
            // 返回环境
            environment.goBackEnvironment();
        } else
            token = getSingleStatement(token, breakIndexes, continueIndexes);
        parse.grammaticalAnalysis(parse.transfer(token));; // 规约statement
        return token;
    }

    private Token getSingleStatement(Token token, ArrayList<Integer> breakIndexes, ArrayList<Integer> continueIndexes) throws IOException, CompileException {
        switch (token.getValue()) {
            case "if":
                token = getIfStatement(breakIndexes, continueIndexes);
                break;
            case "while":
                token = getWhileStatement();
                break;
            case "do":
                token =  getDoWhileStatement();
                break;
            case "for":
                token =  getForStatement();
                break;
            case "switch":
                token =  getSwitchStatement(continueIndexes);
                break;
            case "break":
                token = getBreakContinueStatement(breakIndexes, token);
                break;
            case "continue":
                token = getBreakContinueStatement(continueIndexes, token);
                break;
            case "return":
                token = getReturnStatement();
                break;
            case "char":
            case "int":
            case "double":
            case "struct":
                token = getVariableDeclaration(token);
                break;
            default:
                token = getBasicStatement(token);
        }
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约single_statement
        return token;
    }

    private Token getBasicStatement(Token token) throws IOException, CompileException {
        Token dollar = new Token(6, "$");
        if (token.getValue().equals(";")) {
            // 说明是空语句，直接移入并规约
            parse.grammaticalAnalysis(parse.transfer(token)); // 移入;
            token = lexer.nextToken();
            if (token == null)
                token = dollar;
            parse.grammaticalAnalysis(parse.transfer(token)); // 规约basic statement
            return token;
        } else
            // 说明不是空语句
            while (true) {
                Token lastToken = getExpression(token);
                // 显然token 不是;就是,
                parse.grammaticalAnalysis(parse.transfer(lastToken)); // 移入
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
                if (lastToken.getValue().equals(";")) {
                    // 规约basic statement
                    parse.grammaticalAnalysis(parse.transfer(token));
                    return token;
                }
                // 否则为逗号表达式，需要继续规约
            }
    }

    private Token getBreakContinueStatement(ArrayList<Integer> list, Token token) throws IOException, CompileException {
        Token dollar = new Token(6, "$");
        if (list != null) {
            // 存下break语句编号
            list.add(intermediateCodes.size());
            intermediateCodes.add(new JumpOperationIntermediateCode());
            while (parse.grammaticalAnalysis(parse.transfer(token)) < 0) {
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
            }
            // 规约后退出循环
            return token;
        } else
            throw new CompileException();
    }

    private Token getReturnStatement() throws IOException, CompileException {
        parse.grammaticalAnalysis(new Symbol("return", true)); // 移入 return
        Token dollar = new Token(6, "$");
        Token token = lexer.nextToken();
        if (token == null)
            token = dollar;
        Stack<Variable> stack = new Stack<>();
        token = getArithmeticExpression(token, stack, "return_statement");
        if (stack.isEmpty())
            // 说明没有规约算术表达式，即返回值是void
            if (environment.getCurrentFunction().checkReturnParameterType())
                intermediateCodes.add(new JumpOperationIntermediateCode("$ra"));
            else
                throw new CompileException();
        else
            if (environment.getCurrentFunction().checkReturnParameterType(stack.peek())) {
                // 将结果存储到$v中
                intermediateCodes.add(new AssignmentIntermediateCode(null, stack.peek().getIdentifier(), null, "$v"));
                intermediateCodes.add(new JumpOperationIntermediateCode("$ra"));
            } else
                throw new CompileException();
        return token;
    }

    private Token getExpression(Token token) throws IOException, CompileException {
        ArrayList<IntermediateCode> codes = new ArrayList<>();
        ArrayList<Integer> raIndexes = new ArrayList<>();
        token = getExpression(token, codes, raIndexes);
        int size = intermediateCodes.size();
        for (int index : raIndexes)
            codes.set(index, new AssignmentIntermediateCode(null, String.valueOf(size + index + 2), null, "$ra"));
        intermediateCodes.addAll(codes);
        return token;
    }

    private Token getExpression(Token token, ArrayList<IntermediateCode> codes, ArrayList<Integer> ras) throws IOException, CompileException {
        // constant类型与variable类型map
        HashMap<Integer, Identifier> stringIdentifierHashMap = new HashMap<>();
        stringIdentifierHashMap.put(4, environment.getDefinition("char"));
        stringIdentifierHashMap.put(2, environment.getDefinition("int"));
        stringIdentifierHashMap.put(3, environment.getDefinition("double"));
        // 输入结束符
        Token dollar = new Token(6, "$");
        // 输入符号栈
        Stack<Token> tokenStack = new Stack<>();
        // 规约值
        Variable variable = null, constant = null, value = null;
        // 寄存器编号
        int index = 0;
        // 处理算术表达式
        Stack<Variable> stack = new Stack<>();
        Variable result;
        // 处理函数调用语句
        ArrayList<Variable> parameters = new ArrayList<>();
        Function function = null;
        // 处理赋值语句
        Stack<Variable> rsStack = new Stack<>();
        // 处理a[i]
        String lastVariable = null;
        while (true) {
            int flag = parse.grammaticalAnalysis(parse.transfer(token));
            if (flag < 0) {
                tokenStack.push(token);
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
                else if (token.getValue().equals("["))
                    lastVariable = tokenStack.peek().getValue();
            } else {
                Production production = parse.getProduction(flag);
                switch (production.getLeft().getValue()) {
                    case "function_name":
                        // 规约函数名，查找符号表得到函数
                        function = (Function) environment.getDefinition(tokenStack.peek().getValue());
                        if (function == null)
                            throw new CompileException();
                        tokenStack.clear();
                        break;
                    case "constant":
                        constant = new Variable(tokenStack.peek().getValue(), stringIdentifierHashMap.get(tokenStack.peek().getTag()), false);
                        tokenStack.clear();
                        break;
                    case "variable":
                        if (production.getRight().size() == 4)
                            variable = getVariable(lastVariable, stack.pop());
                        else
                            variable = getVariable(tokenStack, production);
                        tokenStack.clear();
                        break;
                    case "value":
                        if (production.getRight().get(0).getValue().equals("variable"))
                            value = variable;
                        else
                            value = constant;
                        break;
                    case "real_parameter_list":
                        // 规约到了函数参数，将参数加入list
                        parameters.add(value);
                        break;
                    case "function_call_expression":
                        assert function != null;
                        if (function.checkParameterType(parameters)) {
                            // 给的参数类型符合
                            for (int i = 0; i < parameters.size(); i++)
                                // 将第i个参数放入寄存器ai
                                codes.add(new AssignmentIntermediateCode(null, parameters.get(i).getIdentifier(), null, "$a" + i));
                            // 存储下$ra寄存器的值
                            // 栈指针+1
                            codes.add(new AssignmentIntermediateCode("-", "$sp", "1", "$sp"));
                            // 存储
                            codes.add(new AssignmentIntermediateCode(null, "$ra", null, "$sp[0]"));
                            // 将下下条指令地址放入ra寄存器
                            // codes.size()是下一条指令编号，即指令：存入ra寄存器的编号，调用函数是code.size()+1，随后从栈中恢复$ra
                            // 存储这条指令编号，以在并入中间代码后修改
                            ras.add(codes.size());
                            codes.add(new AssignmentIntermediateCode(null, null, null, "$ra"));
                            // 调用函数
                            codes.add(new JumpOperationIntermediateCode(String.valueOf(functionMap.get(function.getIdentifier()))));
                            // 恢复$ra
                            codes.add(new AssignmentIntermediateCode(null, "$sp[0]", null, "$ra"));
                            // 恢复$sp
                            codes.add(new AssignmentIntermediateCode("+", "$sp", "1", "$sp"));
                        } else
                            throw new CompileException();
                        break;
                    case "arithmetic_atomic_expression":
                        if (production.getRight().size() == 3)
                            // 如果是括号语句，将寄存器作为新的value
                            value = stack.pop();
                        // value加入栈
                        stack.push(value);
                        break;
                    case "ad_operator":
                    case "md_operator":
                        stack.push(new Variable(tokenStack.peek().getValue(), null, false));
                        tokenStack.clear();
                        break;
                    case "md_expression":
                    case "arithmetic_expression":
                        if (production.getRight().size() == 1)
                            // 产生式为单独一项
                            result = stack.pop();
                        else {
                            // 验证乘号两端变量是否是指针类型，并且是否同类型
                            Variable right = stack.pop();
                            String operator = stack.pop().getIdentifier();
                            Variable left = stack.pop();
                            if (!left.isPointer() && !right.isPointer() && left.isSameType(right)) {
                                result = new Variable("$t" + index, left.getType(), false); // 放入寄存器
                                index ++;
                                // 加入中间代码
                                codes.add(new AssignmentIntermediateCode(operator, left.getIdentifier(), right.getIdentifier(), result.getIdentifier()));
                            } else
                                throw new CompileException();
                        }
                        // result进栈
                        stack.push(result);
                        break;
                    case "rs":
                        rsStack.push(variable);
                        break;
                    case "assignment_expression":
                        switch (production.getRight().get(2).getValue()) {
                            case "function_call_expression":
                                // 检测返回值类型
                                assert function != null;
                                if (function.checkReturnParameterType(rsStack.peek()))
                                    // 类型相同
                                    codes.add(new AssignmentIntermediateCode(null, "$v", null, rsStack.pop().getIdentifier()));
                                else
                                    throw new CompileException();
                                break;
                            case "arithmetic_expression":
                                // 检测返回值类型
                                if (rsStack.peek().isSameType(stack.peek()))
                                    if (stack.peek().getIdentifier().contains("$t")) {
                                        ((AssignmentIntermediateCode) codes.get(codes.size() - 1)).setRs(rsStack.pop().getIdentifier());
                                        stack.pop();
                                    } else
                                        codes.add(new AssignmentIntermediateCode(null, stack.pop().getIdentifier(), null, rsStack.pop().getIdentifier()));
                                else
                                    throw new CompileException();
                                break;
                            case "&":
                                // 检测返回值类型
                                if (rsStack.peek().isPointer() && !stack.peek().isPointer())
                                    codes.add(new AssignmentIntermediateCode("&", stack.pop().getIdentifier(), null, rsStack.pop().getIdentifier()));
                                else
                                    throw new CompileException();
                                break;
                        }
                        break;
                    case "expression":
                        return token;
                }
            }
        }
    }

    private Token getCompoundStatement(ArrayList<Integer> breakIndexes, ArrayList<Integer> continueIndexes) throws IOException, CompileException {
        parse.grammaticalAnalysis(new Symbol("{", true)); // 移入{
        Token token = lexer.nextToken();
        Token dollar = new Token(6, "$");
        if (token == null)
            token = dollar;
        while (!token.getValue().equals("}")) {
            // 不等于}时规约statement_list
            token = getStatement(token, breakIndexes, continueIndexes);
            parse.grammaticalAnalysis(parse.transfer(token)); // 规约statement_list
        }
        // 等于} 说明 statement_list规约完成
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入}
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约compound_statment
        return token;
    }

    private Token getSwitchStatement(ArrayList<Integer> continueList) throws CompileException, IOException {
        Token dollar = new Token(6, "$");
        Token token = new Token(1, "switch");

        // 处理算术表达式
        Stack<Variable> stack = new Stack<>();
        token = getArithmeticExpression(token, stack, "switch_front");
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入{
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        // 规约case
        // constant类型与variable类型map
        HashMap<Integer, Identifier> stringIdentifierHashMap = new HashMap<>();
        stringIdentifierHashMap.put(4, environment.getDefinition("char"));
        stringIdentifierHashMap.put(2, environment.getDefinition("int"));
        stringIdentifierHashMap.put(3, environment.getDefinition("double"));
        // 存储if语句
        ArrayList<IfIntermediateCode> ifIntermediateCodes = new ArrayList<>();
        HashMap<Integer, Integer> caseBodyMap = new HashMap<>(); // 存储if语句与对应case_body的关系
        ArrayList<Integer> breakList = new ArrayList<>();
        // 记录下一条语句编号
        int bodyIndex = intermediateCodes.size();
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约case_statement -> \epsilon
        if (token.getValue().equals("case"))
            do {
                int a = parse.grammaticalAnalysis(parse.transfer(token)); // 移入case
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
                parse.grammaticalAnalysis(parse.transfer(token)); // 移入constant
                Token lastToken = token;
                token = lexer.nextToken(); // :
                if (token == null)
                    token = dollar;
                parse.grammaticalAnalysis(parse.transfer(token)); // 规约constant
                Variable constant = new Variable(lastToken.getValue(), stringIdentifierHashMap.get(lastToken.getTag()), false); // constant
                if (constant.isSameType(stack.peek()))
                    ifIntermediateCodes.add(new IfIntermediateCode("==", stack.peek().getIdentifier(), constant.getIdentifier()));
                else
                    throw new CompileException();
                parse.grammaticalAnalysis(parse.transfer(token)); // 移入:
                // 规约case_body
                // 存储if与case_body对应关系
                caseBodyMap.put(ifIntermediateCodes.size() - 1, intermediateCodes.size());
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
                while (!token.getValue().equals("case") && !token.getValue().equals("default") && !token.getValue().equals("}")) {
                    token = getStatement(token, breakList, continueList);
                    parse.grammaticalAnalysis(parse.transfer(token));  // 规约statement_list
                }
                parse.grammaticalAnalysis(parse.transfer(token)); // 规约case_body
                parse.grammaticalAnalysis(parse.transfer(token)); // 规约case_statement
            } while (token.getValue().equals("case"));
        else
            parse.grammaticalAnalysis(parse.transfer(token)); // 规约case_statement
        // 规约default
        int defaultIndex = -1;
        if (token.getValue().equals("default")) {
            defaultIndex = intermediateCodes.size();
            parse.grammaticalAnalysis(parse.transfer(token)); // 移入default
            parse.grammaticalAnalysis(parse.transfer(lexer.nextToken())); // 移入:
            while (!token.getValue().equals("}"))
                token = getStatement(token, breakList, continueList);
        }
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约default_statement
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入}
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约switch_statement
        if (defaultIndex != -1)
            if (defaultIndex == intermediateCodes.size())
                throw new CompileException(); // 说明default:后没有语句
            else
                intermediateCodes.add(bodyIndex, new JumpOperationIntermediateCode(String.valueOf(defaultIndex + ifIntermediateCodes.size())));
        else if (caseBodyMap.get(ifIntermediateCodes.size() - 1) == intermediateCodes.size())
                throw new CompileException(); // 说明case: 后没有语句
        else {
            int outIndex = intermediateCodes.size() + 1;
            intermediateCodes.add(bodyIndex, new JumpOperationIntermediateCode(String.valueOf(outIndex + ifIntermediateCodes.size())));
        }
        // 回填if语句以及标号
        for (int i = 0; i < ifIntermediateCodes.size(); i++)
            ifIntermediateCodes.get(i).setGotoIndex(caseBodyMap.get(i) + ifIntermediateCodes.size());
        intermediateCodes.addAll(bodyIndex, ifIntermediateCodes);
        // 回填break语句
        for (int breakIndex : breakList)
            intermediateCodes.set(breakIndex + ifIntermediateCodes.size() + 1, new JumpOperationIntermediateCode(String.valueOf(intermediateCodes.size())));
        return token;
    }

    private Token getForStatement() throws IOException, CompileException {
        ArrayList<Integer> breakList = new ArrayList<>();
        ArrayList<Integer> continueList = new ArrayList<>();
        parse.grammaticalAnalysis(new Symbol("for", true)); // 移入for
        Token dollar = new Token(6, "$");
        Token token = lexer.nextToken();
        if (token == null)
            token = dollar;
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入(
        // 规约for front
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        while (!token.getValue().equals(";")) {
            if (token.getValue().equals(",")) {
                parse.grammaticalAnalysis(parse.transfer(token)); // 移入,
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
            }
            token = getExpression(token);
        }
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约for_front
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入;
        // 规约 for middle
        int loopStartIndex = intermediateCodes.size(); // 记录循环开始编号
        Stack<ArrayList<int[]>> logicalStack = new Stack<>();
        token = getControlStream(logicalStack, "for_middle");
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入;
        // 规约 for back
        ArrayList<IntermediateCode> codes = new ArrayList<>();
        ArrayList<Integer> raIndexes = new ArrayList<>();
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        while (!token.getValue().equals(")")) {
            if (token.getValue().equals(",")) {
                parse.grammaticalAnalysis(parse.transfer(token)); // 移入,
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
            }
            token = getExpression(token, codes, raIndexes);
        }
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约for_back
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入)
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        int ifStartIndex = intermediateCodes.size(); // 记录循环体开始编号
        token = getStatement(token, breakList, continueList); // 规约循环体语句
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约for

        // 加入for back的中间代码
        int size = intermediateCodes.size();
        for (int index : raIndexes)
            codes.set(index, new AssignmentIntermediateCode(null, String.valueOf(size + index + 2), null, "$ra"));
        intermediateCodes.addAll(codes);

        // 加入跳转语句
        intermediateCodes.add(new JumpOperationIntermediateCode(String.valueOf(loopStartIndex)));

        // 回填if中间代码
        int loopEndIndex = intermediateCodes.size();
        backFillIf(logicalStack, ifStartIndex, loopEndIndex);
        // 回填break与continue语句
        for (int i : breakList)
            intermediateCodes.set(i, new JumpOperationIntermediateCode(String.valueOf(loopEndIndex)));
        for (int i : continueList)
            intermediateCodes.set(i, new JumpOperationIntermediateCode(String.valueOf(loopStartIndex)));
        return token;
    }

    private Token getDoWhileStatement() throws IOException, CompileException {
        ArrayList<Integer> breakList = new ArrayList<>();
        ArrayList<Integer> continueList = new ArrayList<>();
        parse.grammaticalAnalysis(new Symbol("do", true)); // 移入do
        Token dollar = new Token(6, "$");
        Token token = lexer.nextToken();
        if (token == null)
            token = dollar;
        // 记录下循环体首标号
        int loopIndex = intermediateCodes.size();
        token = getStatement(token, breakList, continueList);
        // token一定是while
        assert Objects.requireNonNull(token).getValue().equals("while");
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入while
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        assert token.getValue().equals("(");
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入(
        // 规约logical expression
        Stack<ArrayList<int[]>> logicalStack = new Stack<>();
        int ifStartIndex = intermediateCodes.size(); // 记录下if判断开始的编号
        token = getControlStream(logicalStack, "right_bracket");
        int outIndex = intermediateCodes.size(); // 记录下出循环的编号
        // 回填if中间代码
        // 此时栈中应该只有一个if中间代码集合
        if (logicalStack.size() != 1) {
            System.out.println(logicalStack);
            System.exit(0);
        } else {
            for (int[] ints : logicalStack.pop()) {
                IfIntermediateCode ifIntermediateCode = (IfIntermediateCode) intermediateCodes.get(ints[0]);
                if (ints[1] == -2)
//                    ifIntermediateCode.setGotoIndex(loopIndex);
                    if (ints[2] == -3 && ints[0] + 1 == outIndex || ints[2] == ints[0] + 1)
                        ifIntermediateCode.setGotoIndex(loopIndex);
                    else
                        System.out.println(ints[0] + ", " + ints[1] + ", " + ints[2]);
                else if (ints[1] == -3)
                    if (ints[2] == -2) {
                        ifIntermediateCode.setPositive(false);
                        ifIntermediateCode.setGotoIndex(loopIndex);
//                    } else
//                        ifIntermediateCode.setGotoIndex(outIndex);
                    } else if (ints[2] == ints[0] + 1)
                        ifIntermediateCode.setGotoIndex(outIndex);
                    else
                        System.out.println(ints[0] + ", " + ints[1] + ", " + ints[2]);
                else if (ints[1] == ints[0] + 1) {
                    ifIntermediateCode.setPositive(false);
                    if (ints[2] == -2)
                        ifIntermediateCode.setGotoIndex(loopIndex);
                    else if (ints[2] == -3)
                        ifIntermediateCode.setGotoIndex(outIndex);
                    else
                        ifIntermediateCode.setGotoIndex(ints[2]);
//                } else
//                    ifIntermediateCode.setGotoIndex(ints[1]);
                } else if (ints[2] == ints[0] + 1)
                    ifIntermediateCode.setGotoIndex(ints[1]);
                else
                    System.out.println(ints[0] + ", " + ints[1] + ", " + ints[2]);
            }
        }
        // 回填break与continue
        for (int i : breakList)
            intermediateCodes.set(i, new JumpOperationIntermediateCode(String.valueOf(outIndex)));
        for (int i : continueList)
            intermediateCodes.set(i, new JumpOperationIntermediateCode(String.valueOf(ifStartIndex)));

        // 规约do while
        assert token.getValue().equals(";");
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入;
        token = lexer.nextToken();
        if (token == null)
            token = dollar;
        parse.grammaticalAnalysis(parse.transfer(token)); // 规约do_while
        return token;
    }

    private Token getWhileStatement() throws IOException, CompileException {
        ArrayList<Integer> breakList = new ArrayList<>();
        ArrayList<Integer> continueList = new ArrayList<>();
        parse.grammaticalAnalysis(new Symbol("while", true)); // 移入while
        Token dollar = new Token(6, "$");
        Token token = lexer.nextToken();
        if (token == null)
            token = dollar;
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入(
        int ifStartIndex = intermediateCodes.size(); // 记录下循环开始时的编号

        // 规约logical expression
        Stack<ArrayList<int[]>> logicalStack = new Stack<>();
        token = getControlStream(logicalStack, "right_bracket");
        if (token == null)
            token = dollar;
        // 记录当前即if
        int ifIndex = intermediateCodes.size();
        // 规约statement
        token = getStatement(token, breakList, continueList);
        // 加入跳转到循环开始的语句
        intermediateCodes.add(new JumpOperationIntermediateCode(String.valueOf(ifStartIndex)));
        // 规约while语句
        parse.grammaticalAnalysis(parse.transfer(token));
        int loopEndIndex = intermediateCodes.size(); // 循环结束编号
        // 回填if 语句
        backFillIf(logicalStack, ifIndex, loopEndIndex);

        // 回填break与continue语句
        for (int i : breakList)
            intermediateCodes.set(i, new JumpOperationIntermediateCode(String.valueOf(loopEndIndex)));
        for (int i : continueList)
            intermediateCodes.set(i, new JumpOperationIntermediateCode(String.valueOf(ifStartIndex)));
        return token;
    }

    private Token getIfStatement(ArrayList<Integer> breakList, ArrayList<Integer> continueList) throws CompileException, IOException {
        Stack<ArrayList<int[]>> logicalStack = new Stack<>();
        parse.grammaticalAnalysis(new Symbol("if", true));; // 移入if
        Token dollar = new Token(6, "$");
        Token token = lexer.nextToken();
        if (token == null)
            token = dollar;
        parse.grammaticalAnalysis(parse.transfer(token)); // 移入(
        token = getControlStream(logicalStack, "right_bracket"); // 规约条件语句
        if (token == null)
            token = dollar;
        // 已经规约了右括号
        int ifIndex = intermediateCodes.size(); // 记录下if语句体的第一条中间代码编号
        token = getStatement(token, breakList, continueList);
        if (token == null)
            token = dollar;
        int outIndex = intermediateCodes.size(); // 记录下if语句之后的第一条中间代码编号
        int jumpIndex = -1;
        if (token.getValue().equals("else")) {
            // 说明还有else语句
            // 在if体最后加上跳转语句跳过else块
            jumpIndex = intermediateCodes.size(); // 存下此时的编号
            intermediateCodes.add(new JumpOperationIntermediateCode());
            outIndex = intermediateCodes.size(); // 更新编号

            parse.grammaticalAnalysis(parse.transfer(token)); // 移入else
            token = lexer.nextToken();
            if (token == null)
                token = dollar;
            // 规约语句
            token = getStatement(token, breakList, continueList);
        }
        // 规约if语句
        parse.grammaticalAnalysis(parse.transfer(token));
        // 回填if
        backFillIf(logicalStack, ifIndex, outIndex);
        // 如果有else，回填if体最后的跳转语句编号
        if (jumpIndex != -1)
            // 设置为else体的下一条语句
            ((JumpOperationIntermediateCode)intermediateCodes.get(jumpIndex)).setAddress(String.valueOf(intermediateCodes.size()));
        return token;
    }

    private Token getControlStream(Stack<ArrayList<int[]>> logicalStack, String terminator) throws IOException, CompileException {
        /*
           规约控制流
         */

        // constant类型与variable类型map
        HashMap<Integer, Identifier> stringIdentifierHashMap = new HashMap<>();
        stringIdentifierHashMap.put(4, environment.getDefinition("char"));
        stringIdentifierHashMap.put(2, environment.getDefinition("int"));
        stringIdentifierHashMap.put(3, environment.getDefinition("double"));

        /*
          由于logical expression由relational expression构成，而relational expression由assignment expression构成
          所以规约logical expression的过程中也会规约assignment expression
         */

        // 规约logical expression
        Token token = lexer.nextToken();
        Token dollar = new Token(6, "$");
        // 规约arithmetic expression
        Variable constant = null, variable = null, value = null, result, s0 = null, s1 = null;
        String relationalOperator = null;
        int index = 0; // 寄存器编号
        Stack<Token> tokenStack = new Stack<>();
        Stack<Variable> stack = new Stack<>(); // 存储变量用以算术表达式
        String lastVariable = null; // 处理 a[i]这样的表达式

        if (token == null)
            token = dollar;

        while (true) {
            int flag = parse.grammaticalAnalysis(parse.transfer(token));
            if (flag < 0) {
                tokenStack.push(token);
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
                else if (token.getValue().equals("["))
                    lastVariable = tokenStack.peek().getValue();
            } else {
                Production production = parse.getProduction(flag);
                if (production.getLeft().getValue().equals(terminator))
                    return token;
                else
                    switch (production.getLeft().getValue()) {
                        case "constant":
                            constant = new Variable(tokenStack.peek().getValue(), stringIdentifierHashMap.get(tokenStack.peek().getTag()), false);
                            tokenStack.clear();
                            break;
                        case "variable":
                            if (production.getRight().size() == 4)
                                variable = getVariable(lastVariable, stack.pop());
                            else
                                variable = getVariable(tokenStack, production);
                            tokenStack.clear();
                            break;
                        case "value":
                            if (production.getRight().get(0).getValue().equals("variable"))
                                value = variable;
                            else
                                value = constant;
                            break;
                        case "arithmetic_atomic_expression":
                            if (production.getRight().get(0).getValue().equals("( arithmetic_expression )"))
                                // 如果是括号语句，将寄存器作为新的value
                                value = stack.pop();
                            // value加入栈
                            stack.push(value);
                            break;
                        case "ad_operator":
                        case "md_operator":
                            stack.push(new Variable(tokenStack.peek().getValue(), null, false));
                            tokenStack.clear();
                            break;
                        case "md_expression":
                        case "arithmetic_expression":
                            if (production.getRight().size() == 1)
                                // 产生式为单独一项
                                result = stack.pop();
                            else {
                                // 验证乘号两端变量是否是指针类型，并且是否同类型
                                Variable right = stack.pop();
                                String operator = stack.pop().getIdentifier();
                                Variable left = stack.pop();
                                if (!left.isPointer() && !right.isPointer() && left.isSameType(right)) {
                                    result = new Variable("$t" + index, left.getType(), false); // 放入寄存器
                                    index++;
                                    // 加入中间代码
                                    intermediateCodes.add(new AssignmentIntermediateCode(operator, left.getIdentifier(), right.getIdentifier(), result.getIdentifier()));
                                } else
                                    throw new CompileException();
                            }
                            // result进栈
                            stack.push(result);
                            break;
                        case "relational_operator":
                            // 规约到这，说明左边的算术表达式规约完成
                            relationalOperator = tokenStack.pop().getValue(); // 存储关系运算符
                            tokenStack.clear();
                            // 将算术表达式结果存进s0寄存器
                            s0 = new Variable("$s0", stack.peek().getType(), stack.peek().isPointer());
                            intermediateCodes.add(new AssignmentIntermediateCode(null, stack.peek().getIdentifier(), null, "$s0"));
                            // 清空栈用于右边的规约
                            index = 0;
                            stack.clear();
                            break;
                        case "relational_expression":
                            // 规约到这，说明关系表达式规约完成
                            // 将算术表达式结果存进s1寄存器
                            s1 = new Variable("$s1", stack.peek().getType(), stack.peek().isPointer());
                            intermediateCodes.add(new AssignmentIntermediateCode(null, stack.peek().getIdentifier(), null, "$s1"));
                            // 清空栈用于以后可能的新关系表达式
                            index = 0;
                            stack.clear();
                            // 检查两端类型
                            assert s0 != null;
                            if (!s0.isSameType(s1))
                                // 类型不同报错
                                throw new CompileException();
                            break;
                        case "logical_atomic_expression":
                            switch (production.getRight().size()) {
                                case 1:
                                    // 右端为relational expression
                                    assert s0 != null;
                                    assert s1 != null;
                                    // 加入if语句信息
                                    ArrayList<int[]> information = new ArrayList<>();
                                    information.add(new int[]{intermediateCodes.size(), -2, -3});
                                    logicalStack.add(information);
                                    // 加入if语句
                                    intermediateCodes.add(new IfIntermediateCode(relationalOperator, s0.getIdentifier(), s1.getIdentifier()));
                                    break;
                                case 4:
                                    // 右端为 ! ( relation expression )
                                    // 处理最上方的语句
                                    for (int[] ints : logicalStack.peek())
                                        for (int i = 1; i < 3; i++)
                                            if (ints[i] == -2)
                                                ints[i] = -3;
                                            else if (ints[i] == -3)
                                                ints[i] = -2;
                                    break;
                            }
                            break;
                        case "and_expression":
                            if (production.getRight().size() == 3) {
                                ArrayList<int[]> rights = logicalStack.pop();
                                ArrayList<int[]> lefts = logicalStack.pop();
                                for (int[] ints : lefts)
                                    for (int i = 1; i < 3; i++)
                                        if (ints[i] == -2)
                                            ints[i] = rights.get(0)[0];
                                lefts.addAll(rights);
                                logicalStack.push(lefts);
                            }
                            break;
                        case "logical_expression":
                            if (production.getRight().size() == 3) {
                                ArrayList<int[]> rights = logicalStack.pop();
                                ArrayList<int[]> lefts = logicalStack.pop();
                                for (int[] ints : lefts)
                                    for (int i = 1; i < 3; i++)
                                        if (ints[i] == -3)
                                            ints[i] = rights.get(0)[0];
                                lefts.addAll(rights);
                                logicalStack.push(lefts);
                            }
                            break;
                }
            }
        }
    }

    private void backFillIf(Stack<ArrayList<int[]>> logicalStack, int ifIndex, int outIndex) {
        // 回填if中间代码
        // 此时栈中应该只有一个if中间代码集合
        if (logicalStack.size() != 1) {
            System.out.println(logicalStack);
            System.exit(0);
        } else {
            for (int[] ints : logicalStack.pop()) {
                IfIntermediateCode ifIntermediateCode = (IfIntermediateCode) intermediateCodes.get(ints[0]);
                if (ints[1] == -2)
                    if (ints[2] == -3) {
                        ifIntermediateCode.setGotoIndex(outIndex);
                        ifIntermediateCode.setPositive(false);
//                    } else
//                        ifIntermediateCode.setGotoIndex(ifIndex);
                    } else if (ints[2] == ints[0] + 1)
                        ifIntermediateCode.setGotoIndex(ifIndex);
                    else
                        System.out.println(ints[0] + ", " + ints[1] + ", " + ints[2]);
//                else if (ints[1] == -3)
//                    ifIntermediateCode.setGotoIndex(outIndex);
                else if (ints[1] == -3)
                    if (ints[2] == -2 && ints[0] + 1 == ifIndex || ints[2] == ints[0] + 1)
                        ifIntermediateCode.setGotoIndex(outIndex);
                    else
                        System.out.println(ints[0] + ", " + ints[1] + ", " + ints[2]);
                else if (ints[1] == ints[0] + 1) {
                    ifIntermediateCode.setPositive(false);
                    if (ints[2] == -2)
                        ifIntermediateCode.setGotoIndex(ifIndex);
                    else if (ints[2] == -3)
                        ifIntermediateCode.setGotoIndex(outIndex);
                    else
                        ifIntermediateCode.setGotoIndex(ints[2]);
//                } else
//                    ifIntermediateCode.setGotoIndex(ints[1]);
                } else
                    if (ints[2] == ints[0] + 1)
                        ifIntermediateCode.setGotoIndex(ints[1]);
                    else
                        System.out.println(ints[0] + ", " + ints[1] + ", " + ints[2]);
            }
        }
    }

    private Variable getVariable(Stack<Token> tokenStack, Production production) throws CompileException {
        Variable variable = null;
        switch (production.getRight().size()) {
            case 1:
                variable = environment.getVariable(tokenStack.peek().getValue());
                if (variable == null)
                    throw new CompileException();
                break;
            case 2:
                Variable temp = environment.getVariable(tokenStack.peek().getValue());
                if (temp == null || !temp.isPointer())
                    throw new CompileException();
                variable = new Variable("*" + temp.getIdentifier(), temp.getType(), false);
                break;
            case 3:
                Variable structVariable = environment.getVariable(tokenStack.get(tokenStack.size() - 3).getValue());
                if (structVariable == null || !(structVariable.getType() instanceof Struct)) {
                    System.out.println(tokenStack);
                    throw new CompileException();
                }
                Variable member = ((Struct) structVariable.getType()).getMember(tokenStack.peek().getValue());
                if (member == null)
                    throw new CompileException();
                if (production.getRight().get(1).getValue().equals("."))
                    // id.id形式
                    if (structVariable.isPointer())
                        throw new CompileException();
                    else
                        variable = new Variable(structVariable.getIdentifier() + "." + member.getIdentifier(), member.getType(), member.isPointer());
                else
                if (structVariable.isPointer())
                    variable = new Variable(structVariable.getIdentifier() + "->" + member.getIdentifier(), member.getType(), member.isPointer());
                else
                    // id->id形式
                    throw new CompileException();
                break;
        }
        return variable;
    }

    private Variable getVariable(String array, Variable offset) throws CompileException {
        Variable arrayVariable = environment.getVariable(array);
        if (arrayVariable == null || !arrayVariable.isPointer()) {
            System.out.println(array);
            System.out.println(arrayVariable);
            System.out.println(environment);
            throw new CompileException();
        }
        else
            return new Variable(array + "[" + offset.getIdentifier() + "]" , arrayVariable.getType(), false);
    }

    private Token getVariableDeclaration(Token token) throws IOException, CompileException {
        Stack<Token> tokenStack = new Stack<>();
        Token dollar = new Token(6, "$");
        Identifier type = null;
        while (true){
            int flag = parse.grammaticalAnalysis(parse.transfer(token));
            if (flag > 0) {
                Production production = parse.getProduction(flag);
                switch (production.getLeft().getValue()) {
                    // 对于不同的规约产生式分别处理
                    case "variable_type":
                        type = environment.getDefinition(tokenStack.peek().getValue());
                        if (type == null) {
                                // 类型没有定义
                                System.out.println(tokenStack);
                                throw new CompileException();
                        }
                        tokenStack.clear();
                        break;
                    case "single_variable":
                        Variable variable;
                        switch (production.getRight().size()) {
                            case 1:
                                variable = new Variable(tokenStack.peek().getValue(), type, false);
                                break;
                            case 2:
                                variable = new Variable(tokenStack.peek().getValue(), type, true);
                                break;
                            case 4:
                                variable = new Variable(tokenStack.get(tokenStack.size() - 4).getValue(), type, true);
                                break;
                            default:
                                System.out.println(tokenStack);
                                throw new CompileException();
                        }
//                    switch (tokenStack.size()) {
//                        // 栈中为 struct id id 或 struct id id[constant]
//                        case 3, 6 -> variable = new Variable(tokenStack.get(2).getValue(), -1, (Struct) definition, false);
//                        // 栈中为 struct id *id
//                        case 4 -> variable = new Variable(tokenStack.get(3).getValue(), -1, (Struct) definition, true);
//                        default -> {
//                            System.out.println(tokenStack);
//                            System.exit(0);
//                        }
//                    }
                        tokenStack.clear();
                        environment.addVariable(variable);
                        break;
                    case "variable_declaration":
                        // 变量定义完成
                        return token;
                }
            } else {
                tokenStack.push(token);
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
            }
        }
    }

    private Token getArithmeticExpression(Token token, Stack<Variable> stack, String terminator) throws IOException, CompileException {
        // constant类型与variable类型map
        HashMap<Integer, Identifier> stringIdentifierHashMap = new HashMap<>();
        stringIdentifierHashMap.put(4, environment.getDefinition("char"));
        stringIdentifierHashMap.put(2, environment.getDefinition("int"));
        stringIdentifierHashMap.put(3, environment.getDefinition("double"));
        ArrayList<Integer> backFill = new ArrayList<>(); // 需要回填语句的标号
        int index = 0;
        Token dollar = new Token(6, "$");
        Stack<Token> tokenStack = new Stack<>();
        Variable constant = null, variable = null, value = null, result;
        String lastVariable = null;
        if (token == null)
            token = dollar;
        while (true) {
            int flag = parse.grammaticalAnalysis(parse.transfer(token));
            if (flag < 0) {
                tokenStack.push(token);
                token = lexer.nextToken();
                if (token == null)
                    token = dollar;
                else if (token.getValue().equals("["))
                    lastVariable = tokenStack.peek().getValue();
            } else {
                Production production = parse.getProduction(flag);
                if (!"arithmetic_expression".equals(terminator) && production.getLeft().getValue().equals(terminator))
                    return token;
                else
                    switch (production.getLeft().getValue()) {
                    case "constant":
                        constant = new Variable(tokenStack.peek().getValue(), stringIdentifierHashMap.get(tokenStack.peek().getTag()), false);
                        tokenStack.clear();
                        break;
                    case "variable":
                        if (production.getRight().size() == 4)
                            variable = getVariable(lastVariable, stack.pop());
                        else
                            variable = getVariable(tokenStack, production);
                        tokenStack.clear();
                        break;
                    case "value":
                        if (production.getRight().get(0).getValue().equals("variable"))
                            value = variable;
                        else
                            value = constant;
                        break;
                    case "arithmetic_atomic_expression":
                        if (production.getRight().get(0).getValue().equals("( arithmetic_expression )"))
                            // 如果是括号语句，将寄存器作为新的value
                            value = stack.pop();
                        // value加入栈
                        stack.push(value);
                        break;
                    case "ad_operator":
                    case "md_operator":
                        stack.push(new Variable(tokenStack.peek().getValue(), null, false));
                        tokenStack.clear();
                        break;
                    case "md_expression":
                    case "arithmetic_expression":
                        if (production.getRight().size() == 1)
                            // 产生式为单独一项
                            result = stack.pop();
                        else {
                            // 验证乘号两端变量是否是指针类型，并且是否同类型
                            Variable right = stack.pop();
                            String operator = stack.pop().getIdentifier();
                            Variable left = stack.pop();
                            if (!left.isPointer() && !right.isPointer() && left.isSameType(right)) {
                                result = new Variable("$t" + index, left.getType(), false); // 放入寄存器
                                index ++;
                                // 加入中间代码
                                intermediateCodes.add(new AssignmentIntermediateCode(operator, left.getIdentifier(), right.getIdentifier(), result.getIdentifier()));
                            } else
                                throw new CompileException();
                        }
                        // result进栈
                        stack.push(result);
                        if ("arithmetic_expression".equals(terminator))
                            return token;
                }
            }
        }
    }
}
