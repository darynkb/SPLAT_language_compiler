package splat.executor;

import java.util.HashMap;
import java.util.Map;

import splat.parser.elements.*;

public class Executor {

	private ProgramAST progAST;
	
	private Map<String, FunctionDecl> funcMap;
	private Map<String, Value> progVarMap;
	
	public Executor(ProgramAST progAST) {
		this.progAST = progAST;
	}

	public void runProgram() throws ExecutionException {

		// This sets the maps that will be needed for executing function 
		// calls and storing the values of the program variables
		setMaps();
		
		try {
			
			// Go through and execute each of the statements
			for (Statement stmt : progAST.getStmts()) {
				stmt.execute(funcMap, progVarMap);
			}
			
		// We should never have to catch this exception here, since the
		// main program body cannot have returns
		} catch (ReturnFromCall ex) {
			System.out.println("Internal error!!! The main program body "
					+ "cannot have a return statement -- this should have "
					+ "been caught during semantic analysis!");
			
			throw new ExecutionException("Internal error -- fix your "
					+ "semantic analyzer!", -1, -1);
		}
	}
	

	private void setMaps() {
		funcMap = new HashMap<>();
		progVarMap = new HashMap<>();
	
		for (Declaration decl : progAST.getDecls()) {
			String label = decl.getLabel();
	
			if (decl instanceof FunctionDecl) {
				funcMap.put(label, (FunctionDecl) decl);
			} else if (decl instanceof VariableDecl) {
				VariableDecl varDecl = (VariableDecl) decl;
	
				if (varDecl.getType().equals(Type.INTEGER)) {
					progVarMap.put(label, new IntegerValue(0));
				} else if (varDecl.getType().equals(Type.BOOLEAN)) {
					progVarMap.put(label, new BooleanValue(false));
				} else if (varDecl.getType().equals(Type.STRING)) {
					progVarMap.put(label, new StringValue(""));
				} else {
					throw new IllegalArgumentException("Variable type couldn't be resolved: " + varDecl.getType());
				}
			}
		}
	}
	


}
