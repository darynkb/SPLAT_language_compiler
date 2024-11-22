package splat.parser.elements;

import java.util.List;
import java.util.Map;
import splat.executor.*;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

public class WhileStatement extends Statement {
    private Expression condition;
    private List<Statement> body;

    public WhileStatement(Expression condition, List<Statement> body, Token tok) {
        super(tok);
        this.condition = condition;
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type conditionType = condition.analyzeAndGetType(funcMap, varAndParamMap);
        if (!conditionType.equals(Type.BOOLEAN)) {
            throw new SemanticAnalysisException("While loop condition must be of type Boolean", getLine(), getColumn());
        }

        for (Statement stmt : body) {
            stmt.analyze(funcMap, varAndParamMap);
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        Value conditionValue = condition.evaluate(funcMap, varAndParamMap);

        if (!(conditionValue instanceof BooleanValue)) {
            throw new ExecutionException("While loop condition must evaluate to a Boolean value", getLine(), getColumn());
        }

        while (((BooleanValue) conditionValue).getValue()) {
            for (Statement stmt : body) {
                stmt.execute(funcMap, varAndParamMap);
            }

            conditionValue = condition.evaluate(funcMap, varAndParamMap);
            if (!(conditionValue instanceof BooleanValue)) {
                throw new ExecutionException("While loop condition must evaluate to a Boolean value", getLine(), getColumn());
            }
        }
    }

}
