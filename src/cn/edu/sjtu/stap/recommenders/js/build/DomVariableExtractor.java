package cn.edu.sjtu.stap.recommenders.js.build;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.Expression;
import org.eclipse.wst.jsdt.core.dom.FieldAccess;
import org.eclipse.wst.jsdt.core.dom.FunctionInvocation;
import org.eclipse.wst.jsdt.core.dom.SimpleName;

public class DomVariableExtractor extends ASTVisitor {
	private Map<String, Set<String>> properties = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> functions = new HashMap<String, Set<String>>();
	private Map<String, Set<Integer>> variableCalls = new HashMap<String, Set<Integer>>();
	private Set<String> variables = new HashSet<String>();
	
	public DomVariableExtractor(Collection<String> variables) {
		if (variables == null) return;
		this.variables.addAll(variables);
		
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
	
	public boolean visit(FunctionInvocation node) {
		if (variables.size() == 0) return super.visit(node);
		
		Expression expression = node.getExpression();
		if (node == null || node.getName() == null) return super.visit(node);
		
		String name = node.getName().toString();
		String receiver = getToken(expression);
		if (receiver != null) {
			if (variables.contains(receiver)) {
				if (name.equals("call") || name.equals("apply"))
					variableCalls.get(receiver).add(node.arguments().size());
				else 
					functions.get(receiver).add(name);
			}
		}
		else {
			if (variables.contains(name)) 
				variableCalls.get(name).add(node.arguments().size());				
		}
			
		return super.visit(node);	
	}
	
	public boolean visit(FieldAccess node) {
		if (variables.size() == 0) return super.visit(node);
		
		Expression expression = node.getExpression();
		if (node == null || node.getName() == null) return super.visit(node);
		
		String name = node.getName().toString();
		String receiver = getToken(expression);
		if (receiver != null) {
			if (variables.contains(receiver)) {
				properties.get(receiver).add(name);
			}
		}
			
		return super.visit(node);	
	}
	
	private String getToken(Expression expression) {
		String outcome = null;
		
		if (expression != null && 
			expression instanceof SimpleName) 
			outcome = ((SimpleName)expression).getIdentifier();
		
		return outcome;
	}
}
