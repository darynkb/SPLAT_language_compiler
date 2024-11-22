package splat.parser.elements;
import java.util.Map;
import splat.executor.*;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

public class BinaryOpExpression extends Expression {
    private Expression left;
    private String operator;
    private Expression right;

    public BinaryOpExpression(Expression left, String operator, Expression right, Token tok) {
        super(tok);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        Type leftType = left.analyzeAndGetType(funcMap, varAndParamMap);
        Type rightType = right.analyzeAndGetType(funcMap, varAndParamMap);

        if (operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=")) {
            if (!leftType.equals(Type.INTEGER) || !rightType.equals(Type.INTEGER)) {
                throw new SemanticAnalysisException("Relational operators require Integer operands", this);
            }
            return Type.BOOLEAN;
        }

        if (operator.equals("==") || operator.equals("!=")) {
            if (!leftType.equals(rightType)) {
                throw new SemanticAnalysisException("Equality operators require operands of the same type", this);
            }
            if (!leftType.equals(Type.INTEGER) && !leftType.equals(Type.BOOLEAN)) {
                throw new SemanticAnalysisException("Equality operators support only Integer and Boolean types", this);
            }
            return Type.BOOLEAN;
        }

        if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/") || operator.equals("%")) {
            if (!leftType.equals(Type.INTEGER) || !rightType.equals(Type.INTEGER)) {
                throw new SemanticAnalysisException("Arithmetic operators require Integer operands", this);
            }
            return Type.INTEGER;
        }

        if (operator.equals("and") || operator.equals("or")) {
            if (!leftType.equals(Type.BOOLEAN) || !rightType.equals(Type.BOOLEAN)) {
                throw new SemanticAnalysisException("Logical operators require Boolean operands", this);
            }
            return Type.BOOLEAN;
        }

        throw new SemanticAnalysisException("Unsupported operator: " + operator, this);
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ExecutionException {
        Value leftValue = left.evaluate(funcMap, varAndParamMap);
        Value rightValue = right.evaluate(funcMap, varAndParamMap);

        if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/") || operator.equals("%")) {
            if (!(leftValue instanceof IntegerValue) || !(rightValue instanceof IntegerValue)) {
                throw new ExecutionException("Arithmetic operators require Integer operands", getLine(), getColumn());
            }
            int leftInt = ((IntegerValue) leftValue).getValue();
            int rightInt = ((IntegerValue) rightValue).getValue();
            switch (operator) {
                case "+":
                    return new IntegerValue(leftInt + rightInt);
                case "-":
                    return new IntegerValue(leftInt - rightInt);
                case "*":
                    return new IntegerValue(leftInt * rightInt);
                case "/":
                    if (rightInt == 0) {
                        throw new ExecutionException("Division by zero is not possible", getLine(), getColumn());
                    }
                    return new IntegerValue(leftInt / rightInt);
                case "%":
                    return new IntegerValue(leftInt % rightInt);
            }
        }

        if (operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=")) {
            if (!(leftValue instanceof IntegerValue) || !(rightValue instanceof IntegerValue)) {
                throw new ExecutionException("Relational operators require Integer operands", getLine(), getColumn());
            }
            int leftInt = ((IntegerValue) leftValue).getValue();
            int rightInt = ((IntegerValue) rightValue).getValue();
            switch (operator) {
                case ">":
                    return new BooleanValue(leftInt > rightInt);
                case "<":
                    return new BooleanValue(leftInt < rightInt);
                case ">=":
                    return new BooleanValue(leftInt >= rightInt);
                case "<=":
                    return new BooleanValue(leftInt <= rightInt);
            }
        }

        if (operator.equals("==") || operator.equals("!=")) {
            if (!leftValue.getType().equals(rightValue.getType())) {
                throw new ExecutionException("Equality operators require operands of the same type", getLine(), getColumn());
            }
            boolean areEqual = leftValue.toString().equals(rightValue.toString());
            return new BooleanValue(operator.equals("==") ? areEqual : !areEqual);
        }


        if (operator.equals("and") || operator.equals("or")) {
            if (!(leftValue instanceof BooleanValue) || !(rightValue instanceof BooleanValue)) {
                throw new ExecutionException("Logical operators require Boolean operands", getLine(), getColumn());
            }
            boolean leftBool = ((BooleanValue) leftValue).getValue();
            boolean rightBool = ((BooleanValue) rightValue).getValue();
            return new BooleanValue(operator.equals("and") ? (leftBool && rightBool) : (leftBool || rightBool));
        }

        throw new ExecutionException("Unsupported operator: " + operator, getLine(), getColumn());
    }

}
