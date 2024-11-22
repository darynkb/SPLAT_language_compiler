package splat.semanticanalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import splat.parser.elements.Declaration;
import splat.parser.elements.FunctionDecl;
import splat.parser.elements.IfStatement;
import splat.parser.elements.ProgramAST;
import splat.parser.elements.ReturnStatement;
import splat.parser.elements.Statement;
import splat.parser.elements.Type;
import splat.parser.elements.VariableDecl;

public class SemanticAnalyzer {

	private ProgramAST progAST;
	
	private Map<String, FunctionDecl> funcMap;
	private Map<String, Type> progVarMap;
	
	public SemanticAnalyzer(ProgramAST progAST) {
		this.progAST = progAST;
		this.funcMap = new HashMap<>(); // Initialize funcMap
    	this.progVarMap = new HashMap<>(); // Initialize progVarMap
	}

	public void analyze() throws SemanticAnalysisException {
		
		// Checks to make sure we don't use the same labels more than once
		// for our program functions and variables 
		checkNoDuplicateProgLabels();
		
		// This sets the maps that will be needed later when we need to
		// typecheck variable references and function calls in the 
		// program body
		setProgVarAndFuncMaps();
		
		// Perform semantic analysis on the functions
		for (FunctionDecl funcDecl : funcMap.values()) {	
			analyzeFuncDecl(funcDecl);
		}
		
		// Perform semantic analysis on the program body
		for (Statement stmt : progAST.getStmts()) {
			stmt.analyze(funcMap, progVarMap);
		}
		
	}

	private void analyzeFuncDecl(FunctionDecl funcDecl) throws SemanticAnalysisException {
		checkNoDuplicateFuncLabels(funcDecl);
		
		Map<String, Type> varAndParamMap = getVarAndParamMap(funcDecl);
		for (Statement stmt : funcDecl.getStmts()) {
			stmt.analyze(funcMap, varAndParamMap);
		}

		if (!funcDecl.getReturnType().equals(Type.VOID)) {
			if (!hasReturnStatement(funcDecl.getStmts())) {
				throw new SemanticAnalysisException(
					"Non-void function '" + funcDecl.getLabel() + "' must contain a return statement",
					funcDecl
				);
			}
		}
	}
	
	
	private Map<String, Type> getVarAndParamMap(FunctionDecl funcDecl) throws SemanticAnalysisException {
		Map<String, Type> varAndParamMap = new HashMap<>();
		for (VariableDecl param : funcDecl.getParameters()) {
			String paramLabel = param.getLabel();
			if (varAndParamMap.containsKey(paramLabel)) {
				throw new SemanticAnalysisException("Duplicate parameter name: " + paramLabel, funcDecl);
			}
			varAndParamMap.put(paramLabel, param.getType());
		}

		for (VariableDecl localVar : funcDecl.getLocalVars()) {
			String localVarLabel = localVar.getLabel();
			if (varAndParamMap.containsKey(localVarLabel)) {
				throw new SemanticAnalysisException("Duplicate local variable name: " + localVarLabel, funcDecl);
			}
			varAndParamMap.put(localVarLabel, localVar.getType());
		}

		varAndParamMap.put("returnType", funcDecl.getReturnType());


		return varAndParamMap;
	}


	private void checkNoDuplicateFuncLabels(FunctionDecl funcDecl) throws SemanticAnalysisException {
		Set<String> labels = new HashSet<>();
		for (VariableDecl param : funcDecl.getParameters()) {
			String paramLabel = param.getLabel();
			if (labels.contains(paramLabel)) {
				throw new SemanticAnalysisException("Duplicate parameter name: '" + paramLabel + "' in function " + funcDecl.getLabel(), funcDecl);
			}
			labels.add(paramLabel);
		}
	
		for (VariableDecl localVar : funcDecl.getLocalVars()) {
			String localVarLabel = localVar.getLabel();
			if (labels.contains(localVarLabel)) {
				throw new SemanticAnalysisException("Duplicate local variable name: '" + localVarLabel + "' in function " + funcDecl.getLabel(), funcDecl);
			}
			labels.add(localVarLabel);
		}
	
		for (Map.Entry<String, FunctionDecl> entry : funcMap.entrySet()) {
			FunctionDecl existingFuncDecl = entry.getValue();
			for (VariableDecl param : existingFuncDecl.getParameters()) {
				if (param.getLabel().equals(funcDecl.getLabel())) {
					throw new SemanticAnalysisException("Function name '" + funcDecl.getLabel() + "' conflicts with parameter '" + param.getLabel() + "' in function '" + existingFuncDecl.getLabel() + "'", funcDecl);
				}
			}
	
			for (VariableDecl localVar : existingFuncDecl.getLocalVars()) {
				if (localVar.getLabel().equals(funcDecl.getLabel())) {
					throw new SemanticAnalysisException("Function name '" + funcDecl.getLabel() + "' conflicts with local variable '" + localVar.getLabel() + "' in function '" + existingFuncDecl.getLabel() + "'", funcDecl);
				}
			}
		}
		String funcLabel = funcDecl.getLabel();
		if (labels.contains(funcLabel)) {
			throw new SemanticAnalysisException("Function name conflicts with parameter or local variable: '" + funcLabel + "'", funcDecl);
		}
	}
	
	
	
	private void checkNoDuplicateProgLabels() throws SemanticAnalysisException {
		
		Set<String> labels = new HashSet<String>();
		
 		for (Declaration decl : progAST.getDecls()) {
 			String label = decl.getLabel().toString();
 			
			if (labels.contains(label)) {
				throw new SemanticAnalysisException("Cannot have duplicate label '"
						+ label + "' in program", decl);
			} else {
				labels.add(label);
			}
			
		}
	}
	
	private void setProgVarAndFuncMaps() {
		
		for (Declaration decl : progAST.getDecls()) {

			String label = decl.getLabel().toString();
			
			if (decl instanceof FunctionDecl) {
				FunctionDecl funcDecl = (FunctionDecl)decl;
				funcMap.put(label, funcDecl);
				
			} else if (decl instanceof VariableDecl) {
				VariableDecl varDecl = (VariableDecl)decl;
				progVarMap.put(label, varDecl.getType());
			}
		}
	}

	private boolean hasReturnStatement(List<Statement> statements) {
		for (Statement stmt : statements) {
			if (stmt instanceof ReturnStatement) {
				return true;
			}
			if (stmt instanceof IfStatement) {
				IfStatement ifStmt = (IfStatement) stmt;
				boolean thenHasReturn = hasReturnStatement(ifStmt.getThenBranch());
				boolean elseHasReturn = hasReturnStatement(ifStmt.getElseBranch());
				if (!thenHasReturn || !elseHasReturn) {
					// return false;
					continue;
				} else {
					return true;
				}
			}
		}
		return false;
	}

}
