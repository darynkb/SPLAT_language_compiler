package splat.parser.elements;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.executor.Value;
import splat.lexer.Token;
import splat.executor.*;
import java.util.Map;

public class PrintStatement extends Statement {
    private Expression expression;

    public PrintStatement(Expression expression, Token tok) {
        super(tok);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type expressionType = expression.analyzeAndGetType(funcMap, varAndParamMap);
        if (!expressionType.equals(Type.INTEGER) &&
            !expressionType.equals(Type.BOOLEAN) &&
            !expressionType.equals(Type.STRING)) {
            throw new SemanticAnalysisException(
                "Print statement cannot print expression of type " + expressionType,
                getLine(), getColumn()
            );
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ReturnFromCall, ExecutionException {
        Value evaluatedValue = expression.evaluate(funcMap, varAndParamMap);

        if (evaluatedValue == null) {
            throw new ExecutionException("Cannot print a null value", getLine(), getColumn());
        }
        System.out.print(evaluatedValue.toString());
    }

}
