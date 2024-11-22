package splat.parser.elements;

import java.util.List;
import splat.lexer.Token;

public class FunctionDecl extends Declaration {

    private List<VariableDecl> parameters;
    private Type returnType;
    private List<VariableDecl> localVars;
    private List<Statement> statements;

    public FunctionDecl(String functionName, Type returnType, List<VariableDecl> parameters, 
                        List<VariableDecl> localVars, List<Statement> statements, Token tok) {
        super(functionName, tok); 
        this.parameters = parameters;
        this.returnType = returnType;
        this.localVars = localVars;
        this.statements = statements;
    }

    public String getFunctionName() {
        return super.getLabel();
    }

    public List<VariableDecl> getParameters() {
        return parameters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<VariableDecl> getLocalVars() {
        return localVars;
    }

    public List<Statement> getStmts() {
        return statements;
    }

}
