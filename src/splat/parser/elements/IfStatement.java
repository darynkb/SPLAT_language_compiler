package splat.parser.elements;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.executor.*;
import splat.lexer.Token;
import java.util.List;
import java.util.Map;

public class IfStatement extends Statement {
    private Expression condition;
    private List<Statement> thenBranch;
    private List<Statement> elseBranch;

    public IfStatement(Expression condition, List<Statement> thenBranch, List<Statement> elseBranch, Token tok) {
        super(tok);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getThenBranch() {
        return thenBranch;
    }

    public List<Statement> getElseBranch() {
        return elseBranch;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type conditionType = condition.analyzeAndGetType(funcMap, varAndParamMap);
        if (!conditionType.equals(Type.BOOLEAN)) {
            throw new SemanticAnalysisException("If statement condition must be of type Boolean", getLine(), getColumn());
        }

        for (Statement stmt : thenBranch) {
            stmt.analyze(funcMap, varAndParamMap);
        }

        if (elseBranch != null) {
            for (Statement stmt : elseBranch) {
                stmt.analyze(funcMap, varAndParamMap);
            }
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ExecutionException, ReturnFromCall {
        Value conditionValue = condition.evaluate(funcMap, varAndParamMap);

        if (!(conditionValue instanceof BooleanValue)) {
            throw new ExecutionException("If statement condition must evaluate to a Boolean value", getLine(), getColumn());
        }


        if (((BooleanValue) conditionValue).getValue()) {
            for (Statement stmt : thenBranch) {
                stmt.execute(funcMap, varAndParamMap);
            }
        } else if (elseBranch != null) {
            for (Statement stmt : elseBranch) {
                stmt.execute(funcMap, varAndParamMap);
            }
        }
    }
}
