package cn.edu.sjtu.stap.recommenders.js.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.core.ast.IFunctionCall;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;

public class VariableExtractor extends ASTVisitor {
	private Map<String, Set<String>> properties = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> functions = new HashMap<String, Set<String>>();
	private Map<String, Set<Integer>> variableCalls = new HashMap<String, Set<Integer>>();
	private Set<String> variables = new HashSet<String>();

	
	public VariableExtractor(Set<String> variables) {
		this.variables = variables;
		
		if (variables == null) return;
		for (String v : variables) {
			properties.put(v, new HashSet<String>());
			functions.put(v, new HashSet<String>());
			variableCalls.put(v, new HashSet<Integer>());
		}
	}

	public Map<String, Set<Integer>> getVariableCalls() {
		return variableCalls;
	}

	public Map<String, Set<String>> getProperties() {
		return properties;
	}

	public Map<String, Set<String>> getFunctions() {
		return functions;
	}

	public Set<String> getVariables() {
		return variables;
	}
	
	public boolean visit(IFunctionCall function) {
		String receiver = new String(getReceiverToken(function.getReceiver()));
		String token = new String(function.getSelector());
		if (variables.contains(token)) {
			variableCalls.get(token).add(function.getArguments().length);
		}
			
		if (function.getReceiver() != null 
				&& getReceiverToken(function.getReceiver()) != null
				&& variables.contains(receiver)) {
			if (token.equals("apply")) 
				variableCalls.get(receiver).add(function.getArguments().length);
			else if (token .length() > 0)
				functions.get(receiver).add(token);
		}
		
		return super.visit(function);
	}

	public boolean visit(IFieldReference fieldReference){
		String receiver = new String(getReceiverToken(fieldReference.getReceiver()));
		if (fieldReference.getReceiver() != null 
				&& getReceiverToken(fieldReference.getReceiver()) != null
				&& variables.contains(receiver) ) {
			String token = new String(fieldReference.getToken());
			if (token .length() > 0)
				properties.get(receiver).add(token);
		}
		
		return super.visit(fieldReference);
	}
	
	private String getReceiverToken(IExpression expression) {
		String outcome = null;
		if (expression == null) return null;
		if (expression instanceof IFieldReference) 
			outcome = new String(((IFieldReference) expression).getToken());
		else if (expression instanceof SingleNameReference) 
			outcome = new String(((SingleNameReference) expression).getToken());
		
		return outcome;
	}
}
