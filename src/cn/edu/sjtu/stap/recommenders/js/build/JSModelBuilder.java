package cn.edu.sjtu.stap.recommenders.js.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.Main;

import cn.edu.sjtu.stap.recommenders.js.model.JSObjectModel;

public class JSModelBuilder {
	private String[] libFiles;
	private String[] exampleFiles;
	private String outputPath;
	
	public JSModelBuilder(final String[] libFiles, 
						  final String[] exampleFiles, 
						  final String outputPath) {
		this.libFiles = libFiles;
		this.exampleFiles = exampleFiles;
		this.outputPath = outputPath;
	}
	
	public void buildModel() throws IOException {
		Context cx = ContextFactory.getGlobal().enterContext();
		cx.setOptimizationLevel(-1);
		cx.setLanguageVersion(Context.VERSION_1_5);
		Global global = Main.getGlobal();
		if (!global.isInitialized())
			global.init(cx);
		
		for (String libFile : libFiles) 
			Main.processSource(cx, libFile);
		
		JSObjectModel jsObjectModel = new JSObjectModel(global, cx);
		
		for (String exampleFile : exampleFiles) 
			learnFromFile(exampleFile, jsObjectModel);
		
		JSObjectSerializer.serialize(jsObjectModel, outputPath);
		System.out.println("success");
	}
	
	private void learnFromFile(final String filePath, 
							   final JSObjectModel jsObjectModel) throws IOException {
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		File exampleFile = new File(filePath);
		BufferedReader reader = new BufferedReader(new FileReader(exampleFile));
		
		String strLine;
		StringBuilder sb = new StringBuilder();
		while ((strLine = reader.readLine()) != null)
			sb.append(strLine);
		
		parser.setSource(sb.toString().toCharArray());
		JavaScriptUnit result = (JavaScriptUnit) parser.createAST(null);
		result.accept(new DomParameterLearner(jsObjectModel));
		reader.close();
	}
	
	public static void main (String[] args) {
		String[] libFiles = new String[] {
				"W:\\JS\\tools\\rhino1_7R4\\env.js", //Env.js must be the first element
				"W:\\JS\\tools\\rhino1_7R4\\jquery-1.9.1.js",
				"W:\\JS\\tools\\rhino1_7R4\\jquery-validate.js",
				"W:\\JS\\tools\\rhino1_7R4\\jquery.lazyload.js",
				"W:\\runtime-EclipseApplication\\JSTest\\test.js"
		};
		
		String[] exampleFiles = new String[] {
			"W:\\runtime-EclipseApplication\\JSTest\\example.js"	
		};
		
		String outputPath = "W:\\JS\\default.jsmodel";
		
		try {
			new JSModelBuilder(libFiles, exampleFiles, outputPath).buildModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
