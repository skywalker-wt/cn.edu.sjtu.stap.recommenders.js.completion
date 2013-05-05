package cn.edu.sjtu.stap.recommenders.js.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;

import cn.edu.sjtu.stap.recommenders.js.build.DomParameterExtractor;
import cn.edu.sjtu.stap.recommenders.js.build.DomVariableExtractor;

public class JSFunctionObject extends JSObject {
	private final static Pattern p = Pattern.compile("function\\s*\\(");
	
	protected JSFunctionObject(Object object) {
		super(object, FUNCTION_TYPE);
	}
	
	protected JSFunctionObject(Object value, int argsCount) {
		this(value);
		
		this.argsCount = argsCount;
	}
	
	private Map<Integer, Set<JSParameter>> parameters = new HashMap<Integer, Set<JSParameter>>();
	private Set<JSObject> returns = new HashSet<JSObject>();
	private String name;
	private int argsCount = -1;
	
	public void init(Context context) {
		Object object = this.getValue();
		if (object != null && object instanceof BaseFunction) {
			String body = context.decompileFunctionBody((BaseFunction)object, 0);
			String function = context.decompileFunction((BaseFunction)object, 0);

			if (!body.startsWith("[")) {
				try {
					Matcher m = p.matcher(function);
					if (m.find()) {
						function = function.replace(m.group(), "function parse(");
					}
					
					ASTParser parser =ASTParser.newParser(AST.JLS3);  
					parser.setSource(function.toCharArray());
					JavaScriptUnit result = (JavaScriptUnit) parser.createAST(null);
					
					DomParameterExtractor parameterExtractor = new DomParameterExtractor();
					result.accept(parameterExtractor);
					DomVariableExtractor variableExtractor = new DomVariableExtractor(parameterExtractor.getParameters());
					result.accept(variableExtractor);
					
					this.setArgsCount(parameterExtractor.getParameters().size());
					
					int index = 0;
					for (String para : parameterExtractor.getParameters()){
						Set<JSParameter> parameters = this.getParameters().get(index);
						if (parameters == null) {
							parameters = new HashSet<JSParameter>();
							this.getParameters().put(index, parameters);
						}
						
						Set<String> properties = variableExtractor.getProperties().get(para);
						Set<String> functions = variableExtractor.getFunctions().get(para);
						Set<Integer> calls = variableExtractor.getVariableCalls().get(para);
						
						JSObject paraJsObj = JSObject.createJSObject(JSObject.NULL_TYPE);
						for (String property : properties) 
							paraJsObj.addProperty(property, JSObject.createJSObject(JSObject.NULL_TYPE));
						for (String f : functions)
							paraJsObj.addProperty(f, JSObject.createJSObject(JSObject.FUNCTION_TYPE));
						
						JSParameter parameter = new JSParameter(paraJsObj);
						parameter.setIndex(index);
						parameter.setName(para);
						parameters.add(parameter);
						
						for (Integer i : calls) {
							paraJsObj = JSObject.createJSObject(JSObject.FUNCTION_TYPE);
							((JSFunctionObject)paraJsObj).setArgsCount(i);
							parameter = new JSParameter(paraJsObj);
							parameter.setIndex(index);
							parameter.setName(para);
							parameters.add(parameter);
						}
						
						++index;
					}
				} catch (IllegalArgumentException e) {
					System.out.print("");
				}
			}
		}
	}
	
	public int getArgsCount() {
		return argsCount;
	}

	public void setArgsCount(int argsCount) {
		this.argsCount = argsCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Integer, Set<JSParameter>> getParameters() {
		return parameters;
	}

	public void setParameters(Map<Integer, Set<JSParameter>> parameters) {
		this.parameters = parameters;
	}

	public Set<JSObject> getReturns() {
		return returns;
	}
	
	public void setReturns(Set<JSObject> returns) {
		this.returns = returns;
	}
}
