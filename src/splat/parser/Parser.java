package splat.parser;

import java.util.ArrayList;
import java.util.List;
import splat.lexer.Token;
import splat.parser.elements.*;

public class Parser {

	private List<Token> tokens;
	
	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Compares the next token to an expected value, and throws
	 * an exception if they don't match.  This removes the front-most
	 * (next) token  
	 * 
	 * @param expected value of the next token
	 * @throws ParseException if the actual token doesn't match what 
	 * 			was expected
	 */
	private void checkNext(String expected) throws ParseException {

		Token tok = tokens.remove(0);
		
		if (!tok.getValue().equals(expected)) {
			throw new ParseException("Expected '"+ expected + "', got '" 
					+ tok.getValue()+ "'.", tok);
		}
	}
	
	/**
	 * Returns a boolean indicating whether or not the next token matches
	 * the expected String value.  This does not remove the token from the
	 * token list.
	 * 
	 * @param expected value of the next token
	 * @return true iff the token value matches the expected string
	 */
	private boolean peekNext(String expected) {
		// System.out.println(tokens.get(0).getValue());
		return tokens.get(0).getValue().equals(expected);
	}
	
	/**
	 * Returns a boolean indicating whether or not the token directly after
	 * the front most token matches the expected String value.  This does 
	 * not remove any tokens from the token list.
	 * 
	 * @param expected value of the token directly after the next token
	 * @return true iff the value matches the expected string
	 */
	private boolean peekTwoAhead(String expected) {
		return tokens.get(1).getValue().equals(expected);
	}
	
	
	/*
	 *  <program> ::= program <decls> begin <stmts> end ;
	 */
	public ProgramAST parse() throws ParseException {
		
		try {
			// Needed for 'program' token position info
			Token startTok = tokens.get(0);
			
			checkNext("program");
			
			List<Declaration> decls = parseDecls();
			
			checkNext("begin");
			
			List<Statement> stmts = parseStmts();
			
			checkNext("end");
			checkNext(";");
	
			return new ProgramAST(decls, stmts, startTok);
			
		// This might happen if we do a tokens.get(), and nothing is there!
		} catch (IndexOutOfBoundsException ex) {
			
			throw new ParseException("Unexpectedly reached the end of file.", -1, -1);
		}
	}
	
	/*
	 *  <decls> ::= (  <decl>  )*
	 */
	private List<Declaration> parseDecls() throws ParseException {
		
		List<Declaration> decls = new ArrayList<Declaration>();
		
		while (!peekNext("begin")) {
			Declaration decl = parseDecl();
			decls.add(decl);
		}
		
		return decls;
	}
	
	/*
	 * <decl> ::= <var-decl> | <func-decl>
	 */
	private Declaration parseDecl() throws ParseException {

		if (peekTwoAhead(":")) {
			return parseVarDecl();
		} else if (peekTwoAhead("(")) {
			return parseFuncDecl();
		} else {
			Token tok = tokens.get(0);
			throw new ParseException("Declaration expected", tok);
		}
	}
	
	/*
	 * <func-decl> ::= <label> ( <params> ) : <ret-type> is 
	 * 						<loc-var-decls> begin <stmts> end ;
	 */
	private FunctionDecl parseFuncDecl() throws ParseException {
		Token funcName = tokens.remove(0);
		checkNext("("); 

		List<VariableDecl> params = parseParams();
		checkNext(")");

		checkNext(":");
		Token returnTypeToken = tokens.remove(0);
		Type returnType = getTypeFromToken(returnTypeToken); 
		checkNext("is");

		List<VariableDecl> localVars = parseLocVarDecls();
		checkNext("begin");

		List<Statement> statements = parseStmts();
		checkNext("end");
		checkNext(";");

		return new FunctionDecl(funcName.getValue(), returnType, params, localVars, statements, funcName);
	}

	private Type getTypeFromToken(Token token) throws ParseException {
		switch (token.getValue()) {
			case "Integer": return Type.INTEGER;
			case "Boolean": return Type.BOOLEAN;
			case "String": return Type.STRING;
			case "void": return Type.VOID;
			default: throw new ParseException("Unknown type: " + token.getValue(), token);
		}
	}
	

	private List<VariableDecl> parseParams() throws ParseException {
		List<VariableDecl> params = new ArrayList<>();
		while (!peekNext(")")) {
			params.add(parseVarDecl());
			if (!peekNext(")")) {
				checkNext(",");
			}
		}
	
		return params;
	}

	private List<VariableDecl> parseLocVarDecls() throws ParseException {
		List<VariableDecl> localVars = new ArrayList<>();
		while (!peekNext("begin")) {
			localVars.add(parseVarDecl());
		}
	
		return localVars;
	}

	/*
	 * <var-decl> ::= <label> : <type> ;
	 */
	private VariableDecl parseVarDecl() throws ParseException {
		Token varName = tokens.remove(0);
		checkNext(":");
		Token varTypeToken = tokens.remove(0);
		Type varType = getTypeFromToken(varTypeToken); 
		if (peekNext(";")) {
			checkNext(";");
		}

		return new VariableDecl(varName.getValue(), varType, varName);
	}
	
	/*
	 * <stmts> ::= (  <stmt>  )*
	 */
	private List<Statement> parseStmts() throws ParseException {
		List<Statement> statements = new ArrayList<>();
		while (!peekNext("end") && !peekNext("else") && !peekNext("end if") && !peekNext("end while")) {
			Statement sttm = parseStmt();
			statements.add(sttm);
		}
		
		return statements;
	}

	private Statement parseStmt() throws ParseException {
		Token tok = tokens.get(0);
		String tok_value = tok.getValue();
		String tok_type = tok.getType();
	
		if (tok_value.equals("if")) {
			return parseIfStatement();
		} else if (tok_value.equals("while")) {
			return parseWhileStatement();
		} else if (tok_value.equals("print")) {
			return parsePrintStatement();
		} else if (tok_value.equals("print_line")) {
			return parsePrintLineStatement();
		} else if (tok_value.equals("return")) {
			return parseReturnStatement();
		} else if (tok_type.equals("label")) {
			if (peekTwoAhead("(")) {
				return parseFunctionCallStatement();
			} else {
				return parseAssignmentStatement();
			}
		} else {
			throw new ParseException("Unexpected token in statement: " + tok_value, tok);
		}
	}

	

	private Statement parseIfStatement() throws ParseException {
		Token ifToken = tokens.remove(0);
		Expression condition = parseExpression();
		checkNext("then");
	
		List<Statement> thenBranch = parseStmts();
		List<Statement> elseBranch = new ArrayList<>();
		if (peekNext("else")) {
			tokens.remove(0);
			elseBranch = parseStmts();
		}
	
		checkNext("end");
		checkNext("if");
		checkNext(";");
		return new IfStatement(condition, thenBranch, elseBranch, ifToken);
	}

	private Statement parsePrintLineStatement() throws ParseException {
		Token printLineToken = tokens.remove(0);
		checkNext(";");
		return new PrintLineStatement(printLineToken);
	}
	
	
	

	private Statement parseWhileStatement() throws ParseException {
		Token whileToken = tokens.remove(0);
		Expression condition = parseExpression();
		checkNext("do");

		List<Statement> body = parseStmts();
		checkNext("end");
		checkNext("while");
		checkNext(";");
		return new WhileStatement(condition, body, whileToken);
	}
	

	
	
	private Statement parsePrintStatement() throws ParseException {
		Token printToken = tokens.remove(0);
		Expression expr = parseExpression();
	
		checkNext(";");
		return new PrintStatement(expr, printToken);
	}
	
	
	

	private Statement parseReturnStatement() throws ParseException {
		Token returnToken = tokens.remove(0);
		Expression expr = null;
		if (!peekNext(";")) {
			expr = parseExpression();
		}
	
		checkNext(";");
		return new ReturnStatement(expr, returnToken);
	}
	
	

	private Statement parseAssignmentStatement() throws ParseException {
		Token varName = tokens.remove(0);
		checkNext(":=");
		Expression expr = parseExpression();
		checkNext(";");
		
		return new AssignmentStatement(varName.getValue(), expr, varName);

	}

	private Statement parseFunctionCallStatement() throws ParseException {
		Token functionNameToken = tokens.remove(0);
		checkNext("(");
		List<Expression> arguments = new ArrayList<>();
		while (!peekNext(")")) {
			arguments.add(parseExpression());
			if (!peekNext(")")) {
				checkNext(",");
			}
		}
	
		checkNext(")");
		checkNext(";");
	
		return new FunctionCallStatement(functionNameToken.getValue(), arguments, functionNameToken);
	}
	
	private boolean peekNextType(String expected) {
		return tokens.get(0).getType().equals(expected);
	}
	
	private Expression parseExpression() throws ParseException {
		if (peekNext("(")) {
			tokens.remove(0);

			if ((peekNext("-") && peekNextType("operator")) || (peekNext("not") && peekNextType("operator"))) {
				Token operatorToken = tokens.remove(0);
				String operator = operatorToken.getValue();
	
				Expression operand = parsePrimaryExpression();
				if (!peekNext(")")) {
					throw new ParseException("Expected closing ')' after unary expression", tokens.get(0));
				}
				tokens.remove(0);
	
				return new UnaryOpExpression(operator, operand, operatorToken);
			}

			Expression leftExpr = parseExpression();
			if (!peekNextBinaryOperator()) {
				throw new ParseException("Expected binary operator within parentheses", tokens.get(0));
			}
	
			Token operatorToken = tokens.remove(0);
			String operator = operatorToken.getValue();
			Expression rightExpr = parseExpression();
	
			if (!peekNext(")")) {
				throw new ParseException("Expected closing ')' after binary expression", tokens.get(0));
			}
			tokens.remove(0);
	
			return new BinaryOpExpression(leftExpr, operator, rightExpr, operatorToken);
		} else {
			return parsePrimaryExpression();
		}
	}
	

	private boolean peekNextBinaryOperator() {
		if (tokens.isEmpty()) return false;
		String nextValue = tokens.get(0).getValue();
		return nextValue.equals("+") || nextValue.equals("-") || nextValue.equals("*") || 
			   nextValue.equals("/") || nextValue.equals("%") || nextValue.equals("==") ||
			   nextValue.equals("<") || nextValue.equals(">") || nextValue.equals("<=") || 
			   nextValue.equals(">=") || nextValue.equals("!=") || nextValue.equals("and") ||
			   nextValue.equals("or");
	}
	

	private Expression parsePrimaryExpression() throws ParseException {
		if (tokens.isEmpty()) {
			throw new ParseException("Unexpected end of input", null);
		}
	
		Token token = tokens.remove(0);
		if ((token.getValue().equals("-") && token.getType().equals("operator")) || 
			(token.getValue().equals("not") && token.getType().equals("operator"))) {
			String operator = token.getValue();
			Expression rightExpr = parsePrimaryExpression();
			return new UnaryOpExpression(operator, rightExpr, token);
		} 
		else if (token.getType().equals("int-literal")) {
			return new LiteralExpression(token.getValue(), token);
		} else if (token.getType().equals("bool-literal")) {
			return new LiteralExpression(token.getValue(), token);
		} else if (token.getType().equals("string-literal")) {
			return new LiteralExpression(token.getValue(), token);
		} 
		else if (token.getType().equals("label")) {
			if (peekNext("(")) {
				return parseFunctionCall(token);
			} else {
				return new VariableExpression(token.getValue(), token);
			}
		} 
		else if (token.getValue().equals("(")) {
			Expression groupedExpr = parseExpression();
			return groupedExpr;
		} 
		else {
			throw new ParseException("Unexpected token in expression: " + token.getValue(), token);
		}
	}
	
	

	private Expression parseFunctionCall(Token functionNameToken) throws ParseException {
		checkNext("(");
		List<Expression> arguments = new ArrayList<>();
		while (!peekNext(")")) {
			arguments.add(parseExpression());
			if (!peekNext(")")) {
				checkNext(",");
			}
		}
	
		checkNext(")");
	
		return new FunctionCallExpression(functionNameToken.getValue(), arguments, functionNameToken);
	}
	
}	
