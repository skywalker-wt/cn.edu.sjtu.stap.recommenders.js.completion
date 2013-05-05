package cn.edu.sjtu.stap.recommenders.js.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.wst.jsdt.core.dom.ArrayInitializer;
import org.eclipse.wst.jsdt.core.dom.BooleanLiteral;
import org.eclipse.wst.jsdt.core.dom.Expression;
import org.eclipse.wst.jsdt.core.dom.FunctionExpression;
import org.eclipse.wst.jsdt.core.dom.NumberLiteral;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteral;
import org.eclipse.wst.jsdt.core.dom.ObjectLiteralField;
import org.eclipse.wst.jsdt.core.dom.StringLiteral;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

public class JSObject {
	public final static int NULL_TYPE = 0;
	public final static int FUNCTION_TYPE = 1;
	public final static int OBJECT_LITERAL_TYPE = 2;
	public final static int STRING_LITERAL_TYPE = 3;
	public final static int BOOLEAN_LITERAL_TYPE = 4;
	public final static int NUMBER_LITERAL_TYPE = 5;
	public final static int FUNCTION_LITERAL_TYPE = 6;
	public final static int ARRAY_LITERAL_TYPE = 7;
	
	public final static int COMPARE_LEVEL = 2;
	
	public static JSObject createJSObject(Object object, Context context) {
		if (object instanceof BaseFunction) {
			JSFunctionObject functionObj = new JSFunctionObject(object);
			functionObj.init(context);
			return functionObj;
		}
		
		JSObject outcome = new JSObject(object);
		if (object instanceof String)
			outcome.setObjectType(STRING_LITERAL_TYPE);
		else if (object instanceof Integer || object instanceof Double || object instanceof Float)
			outcome.setObjectType(NUMBER_LITERAL_TYPE);
		else if (object instanceof Boolean)
			outcome.setObjectType(BOOLEAN_LITERAL_TYPE);
		
		return outcome;
	}
	
	public static JSObject createJSObject(Object object) {
		if (object instanceof BaseFunction) {
			JSFunctionObject functionObj = new JSFunctionObject(object);
			return functionObj;
		}
		
		JSObject outcome = new JSObject(object);
		if (object instanceof String)
			outcome.setObjectType(STRING_LITERAL_TYPE);
		else if (object instanceof Number)
			outcome.setObjectType(NUMBER_LITERAL_TYPE);
		else if (object instanceof Boolean)
			outcome.setObjectType(BOOLEAN_LITERAL_TYPE);
		
		return outcome;
	}
	
	public static JSObject createJSObject(int type) {
		if (type == FUNCTION_TYPE)
			return new JSFunctionObject(null);
		
		JSObject outcome = new JSObject(null);
		outcome.setObjectType(type);
		return outcome;
	}
	
	public static JSObject createJSObject(Expression e) {

		if (e instanceof ObjectLiteral) {
			JSObject outcome = JSObject.createJSObject(OBJECT_LITERAL_TYPE);
			for (Object field : ((ObjectLiteral) e).fields())
				outcome.addProperty(((ObjectLiteralField)field).getFieldName().toString(), createJSObject(((ObjectLiteralField)field).getInitializer()));
			return outcome;
		}
		if (e instanceof StringLiteral)
			return createJSObject(e.toString());
		if (e instanceof BooleanLiteral)
			return createJSObject(Boolean.parseBoolean(e.toString()));
		if (e instanceof NumberLiteral) {
			JSObject outcome = JSObject.createJSObject(e.toString());
			outcome.setObjectType(NUMBER_LITERAL_TYPE);
			return outcome;
		}
		if (e instanceof FunctionExpression) {
			JSObject outcome = JSObject.createJSObject(FUNCTION_LITERAL_TYPE);
			outcome.setValue(e.toString());
			return outcome;
		}
		if (e instanceof ArrayInitializer) {
			JSObject outcome = JSObject.createJSObject(ARRAY_LITERAL_TYPE);
			outcome.setValue(e.toString());
			return outcome;
		}
		
		return createJSObject(NULL_TYPE);
	}
	
	public static String getDesc(JSObject jsObj) {
		if (jsObj == null || jsObj.getProperties().size() == 0) return "{ }";
		
		StringBuilder outcome = new StringBuilder("{");
		
		boolean isFirst = true;
		for (Entry<String, JSObject> entry : jsObj.getProperties().entrySet()) {
			if (!isFirst) {
				outcome.append(",");
			}
			isFirst = true;
			outcome.append(entry.getKey() + ":");
			outcome.append(getDesc(entry.getValue()));
		}
		
		outcome.append("}");
		return outcome.toString();
	}
	
	private Map<String, JSObject> properties = new HashMap<String, JSObject>();
	private Object value;
	private JSObject parent;
	private int objectType = NULL_TYPE;
	
	protected JSObject(Object value) {
		this.value = value;
	}
	
	protected JSObject(Object value, int objectType) {
		this.value = value;
		this.objectType = objectType;
	}
	
	public void addProperty(String name, JSObject property) {
		properties.put(name, property);
	}
	
	public boolean containsProperty(String property) {
		return properties.containsKey(property);
	}
	
	public JSObject getProperty(String property) {
		return properties.get(property);
	}

	public Map<String, JSObject> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, JSObject> properties) {
		this.properties = properties;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public JSObject getParent() {
		return parent;
	}

	public void setParent(JSObject parent) {
		this.parent = parent;
	}

	public ScriptableObject getJsObj() {
		if (value == null) return null;
		if (value instanceof ScriptableObject) return (ScriptableObject)value;
		return null;
	}

	public void setJsObj(ScriptableObject jsObj) {
		this.value = jsObj;
	}
	
	public int getObjectType() {
		return objectType;
	}


	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}


	@Override
	public int hashCode() {
		int hashCode = 0;
		for (String key : properties.keySet())
			hashCode ^= key.hashCode();
		return hashCode;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof JSObject)) return false;
		return equals(this, (JSObject)o, COMPARE_LEVEL);
	}
	
	
	protected boolean equals(JSObject o1, JSObject o2, int level) {
		if (o1 == null && o2 == null) return true;
		if (o1 == null || o2 == null) return false;
		
		if (level < 0) return true;
		
		boolean outcome = true;
		
		outcome &= o1.getProperties().size() == o2.getProperties().size();
		Iterator<String> it = 
				o1.getProperties().keySet().iterator();
		
		String tmp;
		while (outcome && it.hasNext()) {
			tmp = it.next();
			outcome &= containsProperty(tmp);
			if (outcome) {
				outcome &= equals(
							o1.getProperty(tmp), 
							o2.getProperty(tmp), 
							level - 1);
			}
		}
		
		return outcome;
	}
}
