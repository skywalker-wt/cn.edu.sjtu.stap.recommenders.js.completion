package cn.edu.sjtu.stap.recommenders.js.model;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSObjectGenerator {
	public static final String VAR = "var ";
	public static final String ASSIGN = "=";
	public static final String GENERATE_OBJECT = "new Object();";
	
	public static void declearObject(final JSEngine engine, final Scriptable scope, final String variableName) {
		
		engine.execute(VAR + variableName + ASSIGN + GENERATE_OBJECT, scope);
	}
}
