package cn.edu.sjtu.stap.recommenders.js.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

public class JSObjectModel implements Serializable{
	public static final int BUILD_LEVEL = 10;
	
	private Map<String, Set<JSObject>> m_propertyToObject;
	private Map<String, Set<JSObject>> m_nameToObject;
	
	private Global global;
	private Context context;
	
	public JSObjectModel() {
		m_nameToObject = new TreeMap<String, Set<JSObject>>();
		m_propertyToObject = new TreeMap<String, Set<JSObject>>();
	}
	
	public JSObjectModel(Global global, Context context) {
		this();
		
		init(global, context);
		
		this.global = global;
		this.context = context;
	}

	public void init(Global global, Context context) {
		Map<Object, JSObject> jsObjects = new HashMap<Object, JSObject>();
		jsObjects.put(global, JSObject.createJSObject(global, context));
		
		init(jsObjects, global, context);
		for (Entry<Object, JSObject> entry : jsObjects.entrySet()) {
			if (entry.getKey() instanceof ScriptableObject) {
				ScriptableObject property = (ScriptableObject)entry.getKey();
				Set keys = new HashSet();
				Collections.addAll(keys, property.getAllIds());
				
				if (entry.getKey() instanceof IdScriptableObject 
						&& ((IdScriptableObject)entry.getKey()).getPrototype() instanceof ScriptableObject) {
					Scriptable prototype = ((IdScriptableObject)entry.getKey()).getPrototype();
					Collections.addAll(keys, prototype.getIds());
				}
				
				for (Object key : keys) {
					try {
						if (key.equals("validate"))
							System.out.print("");
						Object pKey = property.get(key);
						JSObject pObject = jsObjects.get(pKey);
						if (pObject == null) continue;
						pObject.setParent(entry.getValue());
						entry.getValue().addProperty(key.toString(), pObject);
						Set<JSObject> objectSet = m_nameToObject.get(key.toString());
						if (objectSet == null) {
							objectSet = new HashSet<JSObject>();
							m_nameToObject.put(key.toString(), objectSet);
						}
						objectSet.add(pObject);
					} catch (EcmaError e){
						JSObject pObject = JSObject.createJSObject(JSObject.NULL_TYPE);
						pObject.setParent(entry.getValue());
						entry.getValue().addProperty(key.toString(), pObject);
					} finally {
						Set<JSObject> objectSet = m_propertyToObject.get(key.toString());
						if (objectSet == null) {
							objectSet = new HashSet<JSObject>();
							m_propertyToObject.put(key.toString(), objectSet);
						}
						if (entry.getValue() != null)
							objectSet.add(entry.getValue());
					}
				}
				
				
			}
		}
	}
	
	private void init(Map<Object, JSObject> map, ScriptableObject jsObject, Context context){
		int i;
		Object[] keys = jsObject.getAllIds();
		
		for (Object key : keys) {
			try {
				if (key.equals("validate"))
					System.out.print("");
				Object property = jsObject.get(key);
				if (property == null) continue;
				if (map.get(property) != null) continue;
				JSObject newObject = JSObject.createJSObject(property, context);					
				map.put(property, newObject);
				
				if (property instanceof ScriptableObject) {
					init(map, (ScriptableObject)property, context);
					if (property instanceof NativeObject 
							&& ((ScriptableObject)property).getParentScope() instanceof ScriptableObject) {
						init(map, (ScriptableObject)((ScriptableObject)property).getParentScope(), context);
					}
				}
			} catch (EcmaError e){
				e.printStackTrace();
			}
		}
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Set<JSObject> getJSObjectByName(String property) {
		return m_nameToObject.get(property);
	}
	
	public Set<JSObject> getJSObjectByProperty(String name) {
		return m_propertyToObject.get(name);
	}
	
	public Map<String, Set<JSObject>> getM_propertyToObject() {
		return m_propertyToObject;
	}

	public void setM_propertyToObject(Map<String, Set<JSObject>> m_propertyToObject) {
		this.m_propertyToObject = m_propertyToObject;
	}

	public Map<String, Set<JSObject>> getM_nameToObject() {
		return m_nameToObject;
	}

	public void setM_nameToObject(Map<String, Set<JSObject>> m_nameToObject) {
		this.m_nameToObject = m_nameToObject;
	}

	public Global getGlobal() {
		return global;
	}

	public void setGlobal(Global global) {
		this.global = global;
	}

	public Set<JSObject> getJSObjectByProperty(final Set<String> properties) {
		return getJSObjectByPropertyAndMethod(properties, new HashSet<String>());
	}
	
	public Set<JSObject> getJSObjectByPropertyAndMethod(final Set<String> properties, 
														final Set<String> methods) {
		return getJSObject(properties, methods, null);
	}
	
	public Set<JSObject> getJSObject(final Set<String> properties, 
									 final Set<String> methods,
									 final String name) {
		// TODO: must replace the implementation here.
		Set<JSObject> tmp = null;
		Set<JSObject> outcome = new HashSet<JSObject>();
		
		properties.addAll(methods);
		for (String p : properties) {
			if (tmp == null || tmp.size() > m_propertyToObject.get(p).size())
				tmp = m_propertyToObject.get(p);
		}
		
		if (tmp == null) return outcome;
		for (JSObject jsObject : tmp) {
			if (jsObject.getProperties().keySet().containsAll(properties))
				outcome.add(jsObject);
		}
			
		return outcome;
	}
}
