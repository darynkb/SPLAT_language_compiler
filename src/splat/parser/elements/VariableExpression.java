package splat.parser.elements;

import java.util.Map;
import splat.executor.*;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

public class VariableExpression extends Expression {
    private String variableName;

    public VariableExpression(String variableName, Token tok) {
        super(tok);
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type variableType = varAndParamMap.get(variableName);
        if (variableType == null) {
            throw new SemanticAnalysisException("Undefined variable: " + variableName, getLine(), getColumn());
        }
        return variableType;
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ExecutionException {
        Value variableValue = varAndParamMap.get(variableName);
        if (variableValue == null) {
            throw new ExecutionException("Undefined or uninitialized variable: " + variableName, getLine(), getColumn());
        }

        return variableValue;
    }

}
