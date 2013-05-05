package cn.edu.sjtu.stap.recommenders.js.action;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import cn.edu.sjtu.stap.recommenders.js.model.JSEngine;
import cn.edu.sjtu.stap.recommenders.js.model.JSObject;
import cn.edu.sjtu.stap.recommenders.js.model.JSObjectModel;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class BuildJSHierarchyAction extends AbstractHandler {
	JSEngine engine;
	/**
	 * The constructor.
	 */
	public BuildJSHierarchyAction() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//		MessageDialog.openInformation(
//				window.getShell(),
//				"Js",
//				"BuildJSHierarchy");
		
//		IJavaScriptProject  p = JavaScriptHeadlessUtil.getJavaScriptProjectFromWorkspace("JQueryTest");
//		try {
//			for (IResource r : p.getProject().members()) {
//				if (r instanceof IFile && ((IFile)r).getName().endsWith(".js")) {
//					
//					final CompilationUnit c = 
//							(CompilationUnit) JavaScriptCore.create((IFile)r);
//					System.out.print(c.getSource());
////					JSEngine.getDefault().buildHierachy(c.getSource());
//				}
//			}
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
		if (engine == null)
			engine = JSEngine.getDefault();
		else engine.initEngine();
		
		
//		Set<String> tSet = new HashSet<String>();
//		tSet.add("init");
//		tSet.add("jquery");
//		test(tSet, model);
//		
//		tSet.clear();
//		tSet.add("validateExtend");
//		tSet.add("validateSetup");
//		test(tSet, model);
		
		return null;
	}
	
	void test(Set<String> tSet, JSObjectModel model) {
		System.out.println();
		Set<JSObject> test = model.getJSObjectByProperty(tSet);
		for (JSObject obj : test) {
			for (String s : obj.getProperties().keySet()) {
				System.out.print(s + ",");
			}
			System.out.println();
		}
	}
}
