package splat.parser.elements;
import splat.semanticanalyzer.SemanticAnalysisException;
import splat.executor.*;
import splat.lexer.Token;
import java.util.Map;

public class UnaryOpExpression extends Expression {
    private String operator;
    private Expression operand;

    public UnaryOpExpression(String operator, Expression operand, Token tok) {
        super(tok);
        this.operator = operator;
        this.operand = operand;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type operandType = operand.analyzeAndGetType(funcMap, varAndParamMap);
        if (operator.equals("-")) {
            if (!operandType.equals(Type.INTEGER)) {
                throw new SemanticAnalysisException("Unary '-' operator requires an integer operand", getLine(), getColumn());
            }
            return Type.INTEGER;
        } else if (operator.equals("not")) {
            if (!operandType.equals(Type.BOOLEAN)) {
                throw new SemanticAnalysisException("Unary 'not' operator requires a boolean operand", getLine(), getColumn());
            }
            return Type.BOOLEAN;
        } else {
            throw new SemanticAnalysisException("Unsupported unary operator: " + operator, getLine(), getColumn());
        }
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ExecutionException {
        Value operandValue = operand.evaluate(funcMap, varAndParamMap);
        switch (operator) {
            case "-":
                if (!(operandValue instanceof IntegerValue)) {
                    throw new ExecutionException("Unary '-' operator requires an integer operand", getLine(), getColumn());
                }
                int intValue = ((IntegerValue) operandValue).getValue();
                return new IntegerValue(-intValue);

            case "not":
                if (!(operandValue instanceof BooleanValue)) {
                    throw new ExecutionException("Unary 'not' operator requires a boolean operand", getLine(), getColumn());
                }
                boolean boolValue = ((BooleanValue) operandValue).getValue();
                return new BooleanValue(!boolValue);

            default:
                throw new ExecutionException("Unsupported unary operator: " + operator, getLine(), getColumn());
        }
    }
}
