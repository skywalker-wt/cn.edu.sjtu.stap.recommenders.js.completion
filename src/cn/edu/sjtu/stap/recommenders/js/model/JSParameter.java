package cn.edu.sjtu.stap.recommenders.js.model;

import java.io.Serializable;
import java.util.Map;

public class JSParameter  implements Serializable{
	private JSObject jsObject;
	private int index;
	private String name;
	
	public JSParameter(JSObject jsObject) {
		this.jsObject = jsObject;
	}
	
	public JSParameter() {
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, JSObject> getProperties() {
		return jsObject.getProperties();
	}

	public JSObject getJsObject() {
		return jsObject;
	}
}
