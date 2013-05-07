package cn.edu.sjtu.stap.recommenders.js.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.core.ast.IFunctionCall;
import org.eclipse.wst.jsdt.core.ast.IFunctionExpression;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.FieldReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.wst.jsdt.ui.text.java.JavaContentAssistInvocationContext;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Main;

import cn.edu.sjtu.stap.recommenders.js.model.JSArgument;
import cn.edu.sjtu.stap.recommenders.js.model.JSFunctionObject;
import cn.edu.sjtu.stap.recommenders.js.model.JSObject;
import cn.edu.sjtu.stap.recommenders.js.model.JSObjectModel;
import cn.edu.sjtu.stap.recommenders.js.model.JSParameter;
import cn.edu.sjtu.stap.recommenders.js.parser.AssignmentExtractor;
import cn.edu.sjtu.stap.recommenders.js.parser.PropertiesExtractor;

public class JSNoTypeCompletionEngine {
	private JSObjectModel model;
	
	
	public JSNoTypeCompletionEngine(JSObjectModel model) {
		this.model = model;
	}
	
	public static Map getCompilerOptions() {
		Map options = new CompilerOptions().getMap();
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
		
		return options;
	}
	
	@SuppressWarnings("restriction")
	public List<ICompletionProposal> getProposals(final int offset, JavaContentAssistInvocationContext context, IProgressMonitor monitor) {
		final List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		CompletionParser parser =
				new CompletionParser(
					new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
						options, 
						new DefaultProblemFactory(Locale.getDefault())));
		
		IJavaScriptUnit unit= context.getCompilationUnit();
		final CompilationResult result = new CompilationResult((ICompilationUnit) unit, 1, 1, 100);
		final CompilationUnitDeclaration parsedUnit = parser.dietParse((ICompilationUnit) unit, result, offset - 1);
		ASTNode node = parsedUnit.concreteStatement();
		
		node.traverse(new org.eclipse.wst.jsdt.core.ast.ASTVisitor() {
			public boolean visit(IFunctionExpression f) {
				return super.visit(f);
			}
			
			public boolean visit(IFunctionCall fc) {
				System.out.println();
				if (fc.getArguments() == null) return super.visit(fc);
				for (int i = 0; i < fc.getArguments().length; ++i) {
					if (fc.getArguments()[i] instanceof CompletionOnSingleNameReference) {
						proposals.addAll(
								getProposalCompleteOnArguments(i, fc, parsedUnit, result, offset)
							);
							return false;
					}
				}
				
				return super.visit(fc);
			}
			
			public boolean visit(IFieldReference fieldReference){
				if (fieldReference instanceof CompletionOnMemberAccess) {
					proposals.addAll(
						getProposalCompleteOnMemberAccess((CompletionOnMemberAccess)fieldReference, parsedUnit, result, offset)
					);
					return false;
				}
				return super.visit(fieldReference);
			}
		});
		
		return proposals.size() == 0 ? Collections.EMPTY_LIST : proposals;
	}
	
	private List<ICompletionProposal> getProposalCompleteOnArguments(
			int index,
			IFunctionCall functionCall, 
			CompilationUnitDeclaration parsedUnit, 
			CompilationResult result,
			int offset) {
		final String functionName = new String(functionCall.getSelector()).intern();
		Set<JSObject> resultJSObject = model.getJSObjectByName(functionName);
		
		
		if (resultJSObject == null || resultJSObject.size() == 0) return Collections.EMPTY_LIST;
		
		List<ICompletionProposal> outcome = new LinkedList<ICompletionProposal>();
		for (JSObject jsObj : resultJSObject) {
			if (jsObj instanceof JSFunctionObject) {
				Set<JSParameter> parameters = ((JSFunctionObject) jsObj).getParameters().get(index);
				if (parameters == null) continue;
				
				for (JSParameter jsPara : parameters) {
					String type = jsPara.getJsObject() instanceof JSFunctionObject ? "Function" : "Object";
					String desc = "";
					if (jsPara.getJsObject() instanceof JSFunctionObject) {
						desc = "function (";
						for (int i = 0; i < ((JSFunctionObject)jsPara.getJsObject()).getArgsCount(); ++i)
							if (i == 0)
								desc += "arg" + i;
							else desc += ", arg" + i;
						desc += ")";
					}
					
					if (jsPara instanceof JSArgument) {
						switch(((JSArgument) jsPara).getType()) {
						case JSObject.OBJECT_LITERAL_TYPE:
							type = "Object Literal";
							break;
						case JSObject.FUNCTION_LITERAL_TYPE:
							type = "Function Literal";
							break;
						case JSObject.ARRAY_LITERAL_TYPE:
							type = "Array";
							break;
						case JSObject.BOOLEAN_LITERAL_TYPE:
							type = "Boolean";
							break;
						case JSObject.NUMBER_LITERAL_TYPE:
							type = "Number";
							break;
						case JSObject.STRING_LITERAL_TYPE:
							type = "String";
							break;
						}
//						desc = ((JSArgument) jsPara).getExpression();
//						desc = JSObject.getDesc(jsPara.getJsObject());
//						desc = "<b>" + desc + "</b>";
						desc = "<ul><li><ul><li>asdf</li><ul/></li></ul>";
//						desc = desc.replace("{", "{<p>").replace("}", "</p>}");
					}
					JSNoTypeFieldProposal proposal = new JSNoTypeFieldProposal(jsPara.getName(), "", offset, desc, type);
					outcome.add(proposal);
				}
			}
		}
		System.out.print("aa");
		return outcome;
	}
	
	private List<ICompletionProposal> getProposalCompleteOnMemberAccess(
			CompletionOnMemberAccess member, 
			CompilationUnitDeclaration parsedUnit, 
			CompilationResult result,
			int offset) {
		List<ICompletionProposal> outcome = new ArrayList<ICompletionProposal>();

		outcome.addAll(getProposalForReturn(member, parsedUnit, result, offset));
		outcome.addAll(getProposalForFields(member, parsedUnit, result, offset));
			
		
		return outcome;
	}
	
	private List<ICompletionProposal> getProposalForReturn(
			CompletionOnMemberAccess member, 
			CompilationUnitDeclaration parsedUnit, 
			CompilationResult result,
			int offset) {
		List<ICompletionProposal> outcome = new ArrayList<ICompletionProposal>();
		
		try {
			Set a = model.getJSObjectByProperty("validate");
			for (Object o : a){
				o.toString();
			}
			Context cx = Context.enter();
			Scriptable scope = cx.initStandardObjects(Main.getGlobal());
			Object returnObj = cx.evaluateString(scope, member.getReceiver().toString(), "<complete on return type>", 1, null);
			if (returnObj instanceof ScriptableObject) {
				for (Object s : ((ScriptableObject) returnObj).getAllIds())
					outcome.add(new JSNoTypeFieldProposal(s.toString(), "", offset, s.toString()));
				
				if (returnObj instanceof NativeObject) {
					System.out.print(((Scriptable) returnObj).getPrototype().getIds().length);
					for (Object s : ((Scriptable) returnObj).getPrototype().getIds()) {
						outcome.add(new JSNoTypeFieldProposal(s.toString(), "", offset, s.toString()));
						System.out.println(s);
					}
				}
			}
		} catch(Exception e) {
			
		}
		return outcome;
	}
	
	private List<ICompletionProposal> getProposalForFields(
			CompletionOnMemberAccess member, 
			CompilationUnitDeclaration parsedUnit, 
			CompilationResult result,
			int offset) {
		final String receiver = getReceiverToken(member.getReceiver());;
		if (receiver == null) return Collections.EMPTY_LIST;
		//preFilter
		PropertiesExtractor propertiesExtractor = new PropertiesExtractor(receiver);
		AssignmentExtractor assignmentExtractor = new AssignmentExtractor(receiver);
		ASTNode node = parsedUnit.concreteStatement();
		node.traverse(propertiesExtractor);
		node.traverse(assignmentExtractor);
		Set<String> fields = propertiesExtractor.getProperties();
		Set<JSObject> resultJSObject = model.getJSObjectByProperty(fields);
		if (resultJSObject.size() == 0)
			resultJSObject = model.getJSObjectByName(receiver);
		if (resultJSObject == null || resultJSObject.size() == 0) return Collections.EMPTY_LIST;
		
		List<ICompletionProposal> outcome = new LinkedList<ICompletionProposal>();
		Set<String> results = new HashSet<String>();
		for (JSObject jsObject : resultJSObject) {
			for (Entry<String, JSObject> entry : jsObject.getProperties().entrySet()) {
				results.add(entry.getKey().intern());
			}
		}
		
		for (String s : results)
			outcome.add(new JSNoTypeFieldProposal(s, "", offset, s));
		
		return outcome;
	}
	
	private char[] computeToken(FieldReference field) {
		char[] currentToken = field.token;
		boolean addDot = false;
		if(currentToken != null && currentToken.length == 0)
			addDot = true;
		if(field.receiver != null) {
			if(field.receiver instanceof SingleNameReference) {
				currentToken = CharOperation.concat(((SingleNameReference)field.receiver).token, currentToken, '.');
			} else if(field.receiver instanceof FieldReference) {
				currentToken = CharOperation.concat(computeToken((FieldReference) field.receiver), currentToken, '.');
			}
		}
		if(addDot)
			currentToken = CharOperation.append(currentToken, '.');
		return currentToken;
	}
	
	private String getReceiverToken(IExpression expression) {
		String outcome = null;
		if (expression == null) return null;
		if (expression instanceof IFieldReference) 
			outcome = new String(((IFieldReference) expression).getToken());
//		else if (expression instanceof IFunctionCall)
//			outcome = new String(((IFunctionCall) expression).getSelector());
		else if (expression instanceof SingleNameReference) 
			outcome = new String(((SingleNameReference) expression).getToken());
		
		return outcome;
	}
}
