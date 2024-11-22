package splat.parser.elements;

import splat.lexer.Token;

public abstract class Declaration extends ASTElement {
	private String label;

	public Declaration(String label, Token tok) {
		super(tok);
		this.label = label;
	}

	public String getLabel() {
        return label;
    }
}
