package splat.lexer;

public class Token {
    private String type;   
    private String lexeme; 
    private int line;     
    private int column;    


    public Token(String type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }


    public String getValue() {
        return getLexeme();
    }


    public String getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("Token[type=%s, lexeme=%s, line=%d, column=%d]", type, lexeme, line, column);
    }
}

