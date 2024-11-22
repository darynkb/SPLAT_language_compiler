package splat.parser.elements;

import java.util.Map;
import splat.executor.*;
import splat.lexer.Token;

public class LiteralExpression extends Expression {
    private String value;
    private Type type;

    public LiteralExpression(String value, Token tok) {
        super(tok);
        this.value = value;
        this.type = inferTypeFromToken(tok);
    }

    private Type inferTypeFromToken(Token tok) {
        String tokenType = tok.getType();
        switch (tokenType) {
            case "int-literal":
                return Type.INTEGER;
            case "bool-literal":
                return Type.BOOLEAN;
            case "string-literal":
                return Type.STRING;
            default:
                throw new IllegalArgumentException("Unknown literal type: " + tokenType);
        }
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) {
        return type;
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) 
            throws ExecutionException {
        if (type.equals(Type.INTEGER)) {
            return new IntegerValue(Integer.parseInt(value));
        } else if (type.equals(Type.BOOLEAN)) {
            return new BooleanValue(Boolean.parseBoolean(value));
        } else if (type.equals(Type.STRING)) {
            return new StringValue(value);
        } else {
            throw new ExecutionException("Unsupported literal type: " + type, getLine(), getColumn());
        }
    }

}
