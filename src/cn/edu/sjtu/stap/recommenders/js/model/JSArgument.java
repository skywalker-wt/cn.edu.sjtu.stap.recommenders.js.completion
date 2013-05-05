package cn.edu.sjtu.stap.recommenders.js.model;

import org.eclipse.wst.jsdt.core.ast.IExpression;

public class JSArgument extends JSParameter{
	private String expression;
	private int type;

	public JSArgument(JSObject jsObject) {
		super(jsObject);
	}
	
	public JSArgument() {	}
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
//	public String getDesc() {
//		String outcome = null;
//		
//		if (getJsObject() == null) return "{}";
//		
//	} 
}
