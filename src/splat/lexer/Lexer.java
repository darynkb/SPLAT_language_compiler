package splat.lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Lexer {
	private File progFile;

	public Lexer(File progFile) {
		this.progFile = progFile;
	}

    private static final Set<String> keywords = new HashSet<>();
    
    static {
        keywords.add("program");
        keywords.add("begin");
        keywords.add("end");
        keywords.add("if");
        keywords.add("then");
        keywords.add("else");
        keywords.add("while");
        keywords.add("do");
        keywords.add("print");
        keywords.add("print_line");
        keywords.add("return");
    }

    private boolean isKeyword(String word) {
        return keywords.contains(word);
    }

    private static final Set<String> operators = new HashSet<>();


	static {
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("/");
        operators.add("%");
		operators.add(":");
        operators.add(">");
        operators.add("<");
		// operators.add("=");
        operators.add(">=");
        operators.add("<=");
        operators.add("==");
		operators.add(":=");
        operators.add("and");
        operators.add("or");
        operators.add("not");
    }


    private boolean isOperator(String op) {
        return operators.contains(op);
    }
    
    private boolean isOperator(char op) {
        return isOperator(String.valueOf(op));
    }

	private String extractOperator(String line, int columnNum) {
		if (columnNum < line.length() - 1) {
			String twoCharOp = line.substring(columnNum, columnNum + 2); 
			if (isOperator(twoCharOp)) {
				return twoCharOp;
			}
		}
		

		char singleCharOp = line.charAt(columnNum);
		// System.out.println(singleCharOp);
		if (isOperator(singleCharOp)) {
			return String.valueOf(singleCharOp);
		}
		return null;
	}

	public List<Token> tokenize() throws LexException {
		List<Token> tokens = new ArrayList<>();
		try {
            BufferedReader reader = new BufferedReader(new FileReader(progFile));
			String line;
			int lineNum = 0;

			while ((line = reader.readLine()) != null) {
				lineNum++;
				int columnNum = 0;

				while (columnNum < line.length()) {
					char currentChar = line.charAt(columnNum);
					if (Character.isWhitespace(currentChar)) {
						columnNum++;
						continue;
					} else if (Character.isDigit(currentChar)) {
						StringBuilder number = new StringBuilder();
						while (columnNum < line.length() && Character.isDigit(line.charAt(columnNum))) {
							number.append(line.charAt(columnNum));
							columnNum++;
						}
						tokens.add(new Token("int-literal", number.toString(), lineNum, columnNum));
					} else if (Character.isLetter(currentChar)) {
						StringBuilder word = new StringBuilder();
						while (columnNum < line.length() && (Character.isLetterOrDigit(line.charAt(columnNum)) || line.charAt(columnNum) == '_')) {
							word.append(line.charAt(columnNum));
							columnNum++;
						}

						String lexeme = word.toString();
						if (lexeme.equals("true") || lexeme.equals("false")) {
							tokens.add(new Token("bool-literal", lexeme, lineNum, columnNum));
						} else if (isKeyword(lexeme)) {
							tokens.add(new Token(lexeme, lexeme, lineNum, columnNum));
						} else if (isOperator(lexeme)) {
							tokens.add(new Token("operator", lexeme, lineNum, columnNum));
						} else {
							tokens.add(new Token("label", lexeme, lineNum, columnNum));
						}
					} else if (currentChar == '"') {
						StringBuilder stringLiteral = new StringBuilder();
						int startColumn = columnNum;  
						columnNum++;  
						

						boolean stringClosed = false;
						while (columnNum < line.length()) {
							char nextChar = line.charAt(columnNum);
							if (nextChar == '"') {
								stringClosed = true;
								columnNum++;  
								break;
							} else {
								stringLiteral.append(nextChar);
								columnNum++;
							}
						}
						
						if (!stringClosed) {
							throw new LexException("String literal wasn't closed properly", lineNum, startColumn);
						}
				
						tokens.add(new Token("string-literal", stringLiteral.toString(), lineNum, startColumn));
					} else if (isOperator(currentChar) || currentChar == '=') {
						String operator = extractOperator(line, columnNum);
						if (operator == null) {
							throw new LexException("Invalid character", lineNum, columnNum);
						}
						tokens.add(new Token("operator", operator, lineNum, columnNum));
						columnNum += operator.length();
					} else if (currentChar == '(' || currentChar == ')' || currentChar == '{' || currentChar == '}' || currentChar == ';' || currentChar == ',' || currentChar == '_') {
						tokens.add(new Token("punctuation", String.valueOf(currentChar), lineNum, columnNum));
						columnNum++;  
					} else {
						throw new LexException("Invalid character", lineNum, columnNum);
					}
				}
			}
			tokens.add(new Token("EOF", "", lineNum + 1, 0));
		} catch (FileNotFoundException e) {
            throw new LexException("File not found: " + progFile.getName(), 0, 0);
        } catch (IOException e) {
            throw new LexException("I/O error: " + e.getMessage(), 0, 0);
        }
		
		return tokens;
	}


}
