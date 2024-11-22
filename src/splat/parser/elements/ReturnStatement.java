package splat.parser.elements;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.executor.*;
import splat.lexer.Token;
import java.util.Map;

public class ReturnStatement extends Statement {
    private Expression returnValue;

    public ReturnStatement(Expression returnValue, Token tok) {
        super(tok);
        this.returnValue = returnValue;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    @Override
    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type expectedReturnType = varAndParamMap.get("returnType");

        if (expectedReturnType == null) {
            throw new SemanticAnalysisException("Expected return type not specified in function context", getLine(), getColumn());
        }
    
        if (returnValue == null) {
            if (!expectedReturnType.equals(Type.VOID)) {
                throw new SemanticAnalysisException("Non-void function must return a value", getLine(), getColumn());
            }
        } else {
            Type actualReturnType = returnValue.analyzeAndGetType(funcMap, varAndParamMap);
            if (!actualReturnType.equals(expectedReturnType)) {
                throw new SemanticAnalysisException(
                    "Return type mismatch: expected " + expectedReturnType + " but found " + actualReturnType,
                    getLine(), getColumn()
                );
            }
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        Value returnTypeValue = varAndParamMap.get("returnType");

        if (returnTypeValue == null || !(returnTypeValue instanceof TypeValue)) {
            throw new ExecutionException("Expected return type not specified in function context", getLine(), getColumn());
        }

        Type expectedReturnType = ((TypeValue) returnTypeValue).getType();
        if (returnValue == null) {
            if (!expectedReturnType.equals(Type.VOID)) {
                throw new ExecutionException("Non-void function must return a value", getLine(), getColumn());
            }
            throw new ReturnFromCall(null);
        }

        Value evaluatedReturnValue = returnValue.evaluate(funcMap, varAndParamMap);

        if (!evaluatedReturnValue.getType().equals(expectedReturnType)) {
            throw new ExecutionException(
                "Return type mismatch: expected " + expectedReturnType + " but found " + evaluatedReturnValue.getType(),
                getLine(), getColumn()
            );
        }
        throw new ReturnFromCall(evaluatedReturnValue);
    }

}
