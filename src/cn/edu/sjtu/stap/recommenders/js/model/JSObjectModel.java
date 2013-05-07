package cn.edu.sjtu.stap.recommenders.js.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

public class JSObjectModel {
	public static final int BUILD_LEVEL = 10;
	
//	private Map<String, JSObject> m_JSObjects 
//				= new TreeMap<String, Set<JSObject>>();
	private Map<String, Set<JSObject>> m_propertyToObject
				= new HashMap<String, Set<JSObject>>();
	private Map<String, Set<JSObject>> m_nameToObject
				= new HashMap<String, Set<JSObject>>();

	private void init(Map<Object, JSObject> map, ScriptableObject jsObject, Context context){
		Object[] keys = jsObject.getAllIds();
		
		for (Object key : keys) {
			try {
				Object property = jsObject.get(key);
				if (property == null) continue;
				if (map.get(property) != null) continue;
				JSObject newObject = JSObject.createJSObject(property, context);					
				map.put(property, newObject);
				
				if (property instanceof ScriptableObject) {
					init(map, (ScriptableObject)property, context);
//					Scriptable s;
//					NativeObject d;
					if (property instanceof NativeObject 
							&& ((ScriptableObject)property).getParentScope() instanceof ScriptableObject) {
						init(map, (ScriptableObject)((ScriptableObject)property).getParentScope(), context);
					}
				}
			} catch (Exception e){
				System.out.println();
			}
		}
	}
	
	public void init(ScriptableObject global, Context context) {
		Map<Object, JSObject> jsObjects = new HashMap<Object, JSObject>();
		jsObjects.put(global, JSObject.createJSObject(global, context));
		
		init(jsObjects, global, context);
		
		for (Entry<Object, JSObject> entry : jsObjects.entrySet()) {
			if (entry.getKey() instanceof ScriptableObject) {
				ScriptableObject property = (ScriptableObject)entry.getKey();
				Set keys = new HashSet();
				Collections.addAll(keys, property.getAllIds());
				
				if (entry.getKey() instanceof NativeObject 
						&& ((NativeObject)entry.getKey()).getPrototype() instanceof ScriptableObject) {
					Collections.addAll(keys, ((NativeObject)entry.getKey()).getPrototype().getIds());
				}
				
				for (Object key : keys) {
//					if (key.equals("jQuery")) 
//						System.out.println();
					try {
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
		
		System.out.print(m_propertyToObject.get("getWindow"));
	}
	
	public Set<JSObject> getJSObjectByName(String property) {
		return m_nameToObject.get(property);
	}
	
	public Set<JSObject> getJSObjectByProperty(String name) {
		return m_propertyToObject.get(name);
	}
	
	public Set<JSObject> getJSObjectByProperty(Set<String> properties) {
		Set<JSObject> tmp = null;
		Set<JSObject> outcome = new HashSet<JSObject>();
		
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
