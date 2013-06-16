package cn.edu.sjtu.stap.recommenders.js.build;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.Expression;
import org.eclipse.wst.jsdt.core.dom.FunctionExpression;
import org.eclipse.wst.jsdt.core.dom.FunctionInvocation;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteral;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteralField;

import cn.edu.sjtu.stap.recommenders.js.model.JSArgument;
import cn.edu.sjtu.stap.recommenders.js.model.JSFunctionObject;
import cn.edu.sjtu.stap.recommenders.js.model.JSObject;
import cn.edu.sjtu.stap.recommenders.js.model.JSObjectModel;
import cn.edu.sjtu.stap.recommenders.js.model.JSParameter;

public class DomParameterLearner extends ASTVisitor{
	private final JSObjectModel model;
	
	public DomParameterLearner(final JSObjectModel model) {
		this.model = model;
	}
	
	public boolean visit(FunctionInvocation node) {
		if (node.arguments().size() == 0) return super.visit(node);
		
		final String functionName = new String(node.getName().toString()).intern();
		Set<JSObject> functions = model.getJSObjectByName(functionName);
		if (functions == null || functions.size() == 0) return super.visit(node);
		
		for (JSObject jsObj : functions) {
			if (jsObj instanceof JSFunctionObject) {
				for (int i = 0; i < node.arguments().size(); ++i) {
					Set<JSParameter> parameters = ((JSFunctionObject) jsObj).getParameters().get(i);
					if (parameters == null) {
						parameters = new HashSet<JSParameter>();
						((JSFunctionObject) jsObj).getParameters().put(1,  parameters);
					}
					
					JSObject argumentObj = JSObject.createJSObject((Expression)node.arguments().get(i));
					JSArgument argument = new JSArgument(argumentObj);
					argument.setIndex(i);
					argument.setExpression(node.arguments().get(i).toString());
					argument.setType(argumentObj.getObjectType());
					String name = "arg" + i;
					
					switch (argumentObj.getObjectType()) {
					case JSObject.ARRAY_LITERAL_TYPE:
						name = "[ ]";
						break;
					case JSObject.BOOLEAN_LITERAL_TYPE:
						name = "true | false";
						break;
					case JSObject.NUMBER_LITERAL_TYPE:
						name = "number";
						break;
					case JSObject.FUNCTION_LITERAL_TYPE:
						name = "function()";
						break;
					case JSObject.STRING_LITERAL_TYPE:
						name = "str";
						break;
					case JSObject.OBJECT_LITERAL_TYPE:
						name = "{ }";
						break;
					}
					
					argument.setName(name);
					parameters.add(argument);
				}
			}
		}
		
		
		return super.visit(node);
	}
}
