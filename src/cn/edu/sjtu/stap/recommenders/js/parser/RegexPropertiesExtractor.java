package cn.edu.sjtu.stap.recommenders.js.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;

public class RegexPropertiesExtractor extends ASTVisitor {
	private Map<String, Set<String>> properties = new HashMap<String, Set<String>>();
	private String regex;
	
	public RegexPropertiesExtractor(String regex) {
		this.regex = regex;
	}
	
//	public boolean visit(IFieldReference fieldReference){
//		if (fieldReference.getReceiver() != null 
//				&& getReceiverToken(fieldReference.getReceiver()) != null
//				&& new String(getReceiverToken(fieldReference.getReceiver())).equals(variable) ) {
//			String token = new String(fieldReference.getToken());
//			if (token .length() > 0)
//				properties.add(token);
//		}
//		
//		return super.visit(fieldReference);
//	}
	
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
