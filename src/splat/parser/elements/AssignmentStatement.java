package splat.parser.elements;
import splat.semanticanalyzer.SemanticAnalysisException;
import java.util.Map;
import splat.executor.*;
import splat.lexer.Token;


public class AssignmentStatement extends Statement {
    private String variableName;
    private Expression expression;

    public AssignmentStatement(String variableName, Expression expression, Token tok) {
        super(tok);
        this.variableName = variableName;
        this.expression = expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type varType = varAndParamMap.get(variableName);
        if (varType == null) {
            throw new SemanticAnalysisException("Variable " + variableName + " not declared", getLine(), getColumn());
        }

        Type exprType = expression.analyzeAndGetType(funcMap, varAndParamMap);
        if (!exprType.equals(varType)) {
            throw new SemanticAnalysisException("Type mismatch: " + variableName + " is " + varType + " but got " + exprType, getLine(), getColumn());
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ExecutionException, ReturnFromCall {
        if (!varAndParamMap.containsKey(variableName)) {
            throw new ExecutionException("Variable " + variableName + " not declared", getLine(), getColumn());
        }

        Value evaluatedValue = expression.evaluate(funcMap, varAndParamMap);

        Value currentValue = varAndParamMap.get(variableName);
        if (currentValue != null && !evaluatedValue.getType().equals(currentValue.getType())) {
            throw new ExecutionException(
                "Type mismatch: Variable " + variableName + " is of type " + currentValue.getType() +
                " but tried to assign " + evaluatedValue.getType(), getLine(), getColumn()
            );
        }

        varAndParamMap.put(variableName, evaluatedValue);
    }
}
