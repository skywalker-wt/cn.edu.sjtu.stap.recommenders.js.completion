package cn.edu.sjtu.stap.recommenders.js.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.tools.shell.Global;

import cn.edu.sjtu.stap.recommenders.js.model.JSObjectModel;

public class JSObjectSerializer {
	public final static String CONTEXT_PLACE_HOLDER = "CONTEXT_PLACE_HOLDER";
	
	public static void serialize(JSObjectModel model, String filePath) throws IOException {
		File modelFile = new File(filePath);
		if (!modelFile.exists()) {
			if (!modelFile.getParentFile().exists())
				modelFile.getParentFile().mkdirs();
			modelFile.createNewFile();
		}
		
		FileOutputStream modelOutputStream = new FileOutputStream(modelFile);
		JSObjectModelOutputStream jsObjectModelOutputStream = new JSObjectModelOutputStream(modelOutputStream);
		jsObjectModelOutputStream.writeObject(model);
		jsObjectModelOutputStream.close();
	}
	
	public static JSObjectModel deSerialize(String filePath) throws IOException, ClassNotFoundException {
		File modelFile = new File(filePath);
		FileInputStream modelInputStream = new FileInputStream(modelFile);
		
		return deSerialize(modelInputStream);
	}
	
	public static JSObjectModel deSerialize(FileInputStream modelInputStream) throws IOException, ClassNotFoundException {
		JSObjectModel outcome = null;
		
		JSObjectModelInputStream jsObjectModelInputStream = new JSObjectModelInputStream(modelInputStream);
		outcome = (JSObjectModel)jsObjectModelInputStream.readObject();
		outcome.getContext().initStandardObjects(outcome.getGlobal());
		
		jsObjectModelInputStream.close();
		return outcome;
	}
	
//	public static void main(String[] args) {
//		String path1 = "t:\\test33\\model1";
//		String path2 = "t:\\test\\model1";
//		
//		test(path1);
//		test(path2);
//		System.out.println("success");
//	}
//	
//	public static void test(String path) {
//		Global global = new Global();
//		Context context = Context.enter();
//		context.initStandardObjects();
//		global.init(context);
//		
//		JSObjectModel model = new JSObjectModel(global, context);
//		try {
//			JSObjectSerializer.serialize(model, path);
//			model = null;
//			model = JSObjectSerializer.deSerialize(path);
//			assert(model != null);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
}

class JSObjectModelOutputStream extends ObjectOutputStream {
	public JSObjectModelOutputStream(OutputStream out) throws IOException {
		super(out);

		enableReplaceObject(true);
	}

	@Override
    protected Object replaceObject(Object obj) throws IOException
    {
        if (obj instanceof Serializable) {
        	return obj;
        }
        
        if (obj instanceof Context) {
        	return JSObjectSerializer.CONTEXT_PLACE_HOLDER;
        }
        
        return null;
    }
}

class JSObjectModelInputStream extends ObjectInputStream {
	private Context context;

	public JSObjectModelInputStream(InputStream in) throws IOException {
		super(in);
		
		context = Context.enter();
		
		enableResolveObject(true);
	}
	
	@Override
    protected Object resolveObject(Object obj)
        throws IOException
    {
        if (JSObjectSerializer.CONTEXT_PLACE_HOLDER.equals(obj)) {
        	obj = context;
        }
        return obj;
    }
}
