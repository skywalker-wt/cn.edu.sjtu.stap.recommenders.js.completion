package cn.edu.sjtu.stap.recommenders.js.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import cn.edu.sjtu.stap.recommenders.js.build.JSObjectSerializer;

public class JSEngine {	
	public static String DEFAULT_HTML_PATH = "http://www.jquery.com/";
	public static final String DEFAULT_ENGINE = "DEFAULT_ENGINE";
	public static final String DEFAULT_SCOPE = "DEFAULT_SCOPE";
	public static final String GLOBAL_SCOPE = "GLOBAL_SCOPE";
	public static final String TMP_SCOPE = "TMP_SCOPE";
	public static final String SUB_SCOPE = "SUB_SCOPE";

	private static Map<String, JSEngine> engines = new TreeMap<String, JSEngine>();
	
	public static JSEngine getJSEngine(String name) {
		if (name == null) name = DEFAULT_ENGINE;
		
		JSEngine outcome = engines.get(name);
		if (outcome == null) {
			outcome = new JSEngine();
			engines.put(name, outcome);
		}
		
		return outcome;
	}
	
	private JSObjectModel jsObjectModel;
	private Map<String, Scriptable> scriptables = new HashMap<String, Scriptable>();
	
	private JSEngine(){	}
	
	public JSObjectModel getJsObjectModel() {
		return jsObjectModel;
	}

	public void setJsObjectModel(JSObjectModel jsObjectModel) {
		this.jsObjectModel = jsObjectModel;
	}
	
	public void initEngine() throws ClassNotFoundException, IOException {
		String defaultModelPath = "W:\\JS\\default.jsmodel";
		initEngine(new File(defaultModelPath));
	}
	
	public void initEngine(final File modelFile) throws ClassNotFoundException, IOException {
		FileInputStream modelInputStream = new FileInputStream(modelFile);
		initEngine(modelInputStream);
	}
	
	public void initEngine(final FileInputStream modelFileInputStream) throws ClassNotFoundException, IOException {
		this.jsObjectModel = JSObjectSerializer.deSerialize(modelFileInputStream);
		scriptables.put(GLOBAL_SCOPE, jsObjectModel.getGlobal());
	}
	
	public void bindRelatedHtmlFile(String htmlPath){
		if (htmlPath == null)
			htmlPath = DEFAULT_HTML_PATH;
		
		if (!htmlPath.equals(getCurrentURL())) {
			executeInGlobal("Envjs('" + htmlPath + "')", 1);
		}
	}
	
	public String getCurrentURL() {
		Object result = evaluateScript("window.location.href");
		
		if (result != null && result instanceof String)
			return (String)result;
		
		return null;
	}
	
	public Scriptable execute(final String script, final int lineNumber) {
		return execute(script, lineNumber, DEFAULT_SCOPE);
	}
	
	public Scriptable executeInGlobal(final String script, final int lineNumber) {
		return execute(script, lineNumber, GLOBAL_SCOPE);
	}
	
	public Scriptable execute(final String script, final int lineNumber, final String scopeName) {
		Scriptable executeScope = scriptables.get(scopeName);
		if (executeScope == null) {
			executeScope = new ScriptableObject() {
				@Override
				public String getClassName() {
					return scopeName;
				}
			};
			executeScope.setParentScope(jsObjectModel.getGlobal());
			scriptables.put(scopeName, executeScope);
		}
		
		jsObjectModel.getContext().evaluateString(executeScope, script, null, lineNumber, null);
		return executeScope;
	}
	
	public Scriptable execute(final String script, final int lineNumber, final Scriptable parentScope) {
		Scriptable executeScope = new ScriptableObject() {
				@Override
				public String getClassName() {
					return SUB_SCOPE + parentScope.getClassName();
				}
			};
		executeScope.setParentScope(parentScope);
		
		jsObjectModel.getContext().evaluateString(executeScope, script, null, lineNumber, null);
		return executeScope;
	}
	
	public Scriptable execute(final String script, final Scriptable scope) {
		jsObjectModel.getContext().evaluateString(scope, script, null, 1, null);
		return scope;
	}
	
	public Object evaluateScript(final String script) {
		return evaluateScript(script, jsObjectModel.getGlobal(), TMP_SCOPE);
	}
	
	public Object evaluateScript(final String script, final Scriptable parentScope) {
		return evaluateScript(script, parentScope, TMP_SCOPE);
	}
	
	public Object evaluateScript(final String script, final Scriptable parentScope, final String scopeName) {
		Scriptable executeScope = new ScriptableObject() {
			@Override
			public String getClassName() {
				return scopeName;
			}
		};
		executeScope.setParentScope(parentScope);
		
		return jsObjectModel.getContext().evaluateString(executeScope, script, null, 1, null);
	}
}
