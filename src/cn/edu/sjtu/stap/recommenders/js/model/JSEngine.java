package cn.edu.sjtu.stap.recommenders.js.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

import cn.edu.sjtu.stap.recommenders.js.build.DomParameterLearner;

//import com.sun.script.javascript.RhinoScriptEngine;

public class JSEngine {	
	private static JSEngine m_JSEngine = new JSEngine();
	private JSObjectModel jsObjectModel;
	
	public JSEngine(){	}
	
	public static JSEngine getDefault() {
		return m_JSEngine;
	}
	
	public JSObjectModel getJsObjectModel() {
		return jsObjectModel;
	}

	public void setJsObjectModel(JSObjectModel jsObjectModel) {
		this.jsObjectModel = jsObjectModel;
	}
	
	void evalFile(String[] files) throws IOException {
		Context context = Context.enter();
	    Global global = new Global(context);
	    
	    for (String file : files) {
	    	File f = new File(file);
			BufferedReader reader = new BufferedReader(new FileReader(f));
//			String tmp = "";
//			StringBuilder sb = new StringBuilder();
//			
//			while ((tmp = reader.readLine()) != null)
//					sb.append(tmp);
			
			context.evaluateReader(global, reader, file, 1, null);
	    }
	    
	    jsObjectModel = new JSObjectModel();
		jsObjectModel.init(global, context);
	}

	public void initEngine() {
//		Context cx = ContextFactory.getGlobal().enterContext();
//		Context context = Context.enter();
//	    Global global = new Global(context);
//	    String[] files = new String[]{"W:\\runtime-EclipseApplication\\JSTest\\test.js"};
//		String[] files = new String[]{"W:\\JS\\tools\\rhino1_7R4\\env.js"};
//	    try {
//			evalFile(files);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		Context cx = ContextFactory.getGlobal().enterContext();
		cx.setOptimizationLevel(-1);
		cx.setLanguageVersion(Context.VERSION_1_5);
		Global global = Main.getGlobal();
//		final Global global = new Global(cx);
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//        final PrintStream p = new PrintStream(out);
//        global.setOut(p);
//        global.setErr(p);
//        global.defineFunctionProperties(
//                new String[] { "options" }, JSEngine.class,
//                ScriptableObject.DONTENUM | ScriptableObject.PERMANENT |
//                  ScriptableObject.READONLY);
//		
//		if (!global.isInitialized())
//			global.init(cx);
//		Global global = new Global();
		if (!global.isInitialized())
			global.init(cx);
		
		try {
			Main.processSource(cx, "W:\\JS\\tools\\rhino1_7R4\\env.js");
			Main.processSource(cx, "W:\\JS\\tools\\rhino1_7R4\\test.js");
			Main.processSource(cx, "W:\\JS\\tools\\rhino1_7R4\\jquery-1.9.1.js");
			Main.processSource(cx, "W:\\JS\\tools\\rhino1_7R4\\jquery-validate.js");
			Main.processSource(cx, "W:\\JS\\tools\\rhino1_7R4\\jquery.lazyload.js");
			Main.processSource(cx, "W:\\runtime-EclipseApplication\\JSTest\\test.js");
//			Main.processSource(cx, "Envjs('/W/JS/test/ddd.html')");
//			int i;
//			Context cx1 = Context.enter();
//			Scriptable scope = cx1.initStandardObjects(global);
//			NativeObject result = (NativeObject)cx.evaluateString(scope, "$('p');", "<cmd>", 1, null);
//			Object o1 = result.keySet();
//			Object o2 = result.getPrototype();
			
			jsObjectModel = new JSObjectModel();
			jsObjectModel.init(global, cx);
			Object o = jsObjectModel.getJSObjectByName("fn");
			learnFromFile("W:\\runtime-EclipseApplication\\JSTest\\example.js");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void learnFromFile(String file) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		File f = new File(file);
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String tmp = "";
		StringBuilder sb = new StringBuilder();
		while ((tmp = reader.readLine()) != null)
			sb.append(tmp);
		parser.setSource(sb.toString().toCharArray());
		JavaScriptUnit result = (JavaScriptUnit) parser.createAST(null);
		result.accept(new DomParameterLearner(jsObjectModel));
		reader.close();
	}
}
