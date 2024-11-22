package splat.parser.elements;

import splat.lexer.Token;

public class VariableDecl extends Declaration {
	private String name;
	private Type type;
	
	public VariableDecl(String label, Type type, Token tok) {
		super(label, tok); 
		this.name = tok.getValue();
        this.type = type;

	}

	public String getName() {
        return name;
    }
	
	public String toString() {
		return "VariableDecl{name='" + name + "', type='" + type + "'}";
	}

	public Type getType() {
        return type;
    }
}
