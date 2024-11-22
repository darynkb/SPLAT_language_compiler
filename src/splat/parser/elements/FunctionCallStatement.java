package splat.parser.elements;

import splat.semanticanalyzer.SemanticAnalysisException;
import splat.executor.*;
import splat.lexer.Token;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionCallStatement extends Statement {
    private String functionName;
    private List<Expression> arguments;

    public FunctionCallStatement(String functionName, List<Expression> arguments, Token tok) {
        super(tok);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        FunctionDecl functionDecl = funcMap.get(functionName);
        if (functionDecl == null) {
            throw new SemanticAnalysisException("Undefined function: " + functionName, getLine(), getColumn());
        }

        List<VariableDecl> parameters = functionDecl.getParameters();
        if (arguments.size() != parameters.size()) {
            throw new SemanticAnalysisException("Argument count mismatch in call to function: " + functionName, getLine(), getColumn());
        }

        for (int i = 0; i < arguments.size(); i++) {
            Type expectedType = parameters.get(i).getType();
            Type actualType = arguments.get(i).analyzeAndGetType(funcMap, varAndParamMap);
            if (!actualType.equals(expectedType)) {
                throw new SemanticAnalysisException("Type mismatch for argument " + (i + 1) + " in call to function " + functionName +
                        ": expected " + expectedType + " but found " + actualType, getLine(), getColumn());
            }
        }
    }


    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ExecutionException, ReturnFromCall {
        FunctionDecl functionDecl = funcMap.get(functionName);
        if (functionDecl == null) {
            throw new ExecutionException("Undefined function: " + functionName, getLine(), getColumn());
        }

        List<VariableDecl> parameters = functionDecl.getParameters();
        if (arguments.size() != parameters.size()) {
            throw new ExecutionException("Argument count mismatch in call to function: " + functionName, getLine(), getColumn());
        }

        Map<String, Value> localVarMap = new HashMap<>();
        for (int i = 0; i < arguments.size(); i++) {
            Expression argument = arguments.get(i);
            VariableDecl parameter = parameters.get(i);
            Value argumentValue = argument.evaluate(funcMap, varAndParamMap);

            if (!argumentValue.getType().equals(parameter.getType())) {
                throw new ExecutionException("Type mismatch for argument " + (i + 1) + " in call to function " + functionName +
                        ": expected " + parameter.getType() + " but found " + argumentValue.getType(), getLine(), getColumn());
            }

            localVarMap.put(parameter.getLabel(), argumentValue);
        }

        for (VariableDecl localVar : functionDecl.getLocalVars()) {
            localVarMap.put(localVar.getLabel(), null);
        }
        localVarMap.put("returnType", new TypeValue(functionDecl.getReturnType()));

        try {
            for (Statement stmt : functionDecl.getStmts()) {
                stmt.execute(funcMap, localVarMap);
            }
        } catch (ReturnFromCall returnFromCall) {
            Value returnValue = returnFromCall.getReturnVal();
            if (!functionDecl.getReturnType().equals(Type.VOID)) {
                if (returnValue == null || !returnValue.getType().equals(functionDecl.getReturnType())) {
                    throw new ExecutionException("Function " + functionName + " must return a value of type " +
                            functionDecl.getReturnType(), getLine(), getColumn());
                }
            } else if (returnValue != null) {
                throw new ExecutionException("Void function " + functionName + " should not return a value", getLine(), getColumn());
            }
        }
    }

}
