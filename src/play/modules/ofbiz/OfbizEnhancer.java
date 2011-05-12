package play.modules.ofbiz;

import java.lang.reflect.Method;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;

import org.apache.commons.lang.StringUtils;

import antlr.collections.List;

import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

public class OfbizEnhancer extends Enhancer {

	@Override
	public void enhanceThisClass(ApplicationClass applicationClass)
	throws Exception {
		CtClass ctClass = makeClass(applicationClass);
		
		if (!ctClass.subtypeOf(classPool.get("play.modules.ofbiz.OfbizEntityModel"))) {
			return;
		}

		for (CtField ctField : ctClass.getFields()) {
			String name = ctField.getName();
			if ("gv".equals(name)) {
				continue;
			}
			
			if (name.startsWith("related")) {
				createRelatedEntityGetterAndSetter(ctClass, ctField);
			} else {
				createFieldGetterAndSetter(ctClass, ctField);
			}
		}

//		Logger.info("Enhanced ofbiz entity %s", applicationClass.name);
		applicationClass.enhancedByteCode = ctClass.toBytecode();
		ctClass.defrost();
	}

    private void createRelatedEntityGetterAndSetter(CtClass ctClass, CtField ctField) throws Exception {
		CtClass type = ctField.getType();
		String name = ctField.getName();
		String returnTypeName = type.getName();
		String capitalizedName = StringUtils.capitalize(name);

		String getterName = "get" + capitalizedName;
        String getter = "";
        if (List.class.isAssignableFrom(type.getClass())) {
        	getter = String.format("public java.util.List %s() { return play.modules.ofbiz.OfbizEntityModel.createFromGenericValueList(%s.class, gv, \"%s\"); }", returnTypeName, getterName, returnTypeName, name.replaceFirst("^related", ""));
        } else {
        	getter = String.format("public %s %s() { return (%s) play.modules.ofbiz.OfbizEntityModel.createFromGenericValue(%s.class, gv, \"%s\"); }", returnTypeName, getterName, returnTypeName, returnTypeName, name.replaceFirst("^related", ""));
        }
        if (ctClass.getName().equals("Product")) {
        	Logger.info("Setting getter for related ctField %s: %s", returnTypeName, getter);
        }
		addMethod(ctClass, getterName, getter);

	}

	private void createFieldGetterAndSetter(CtClass ctClass, CtField ctField) throws Exception {
		CtClass type = ctField.getType();
		String name = ctField.getName();
		String returnTypeName = type.getName();
		String capitalizedName = StringUtils.capitalize(name);
		
		// Adopt the getter to use the generic value
		String getterName = "get" + capitalizedName;
		String getter = String.format("public %s %s() { return (%s) gv.get(\"%s\"); }", returnTypeName, getterName, returnTypeName, name);
		addMethod(ctClass, getterName, getter);

		// Adopt the setter
		String setterName = "set" + capitalizedName;
		String setter = String.format("public void %s(%s value) { return gv.set(\"%s\", value); }", setterName, returnTypeName, name);
		addMethod(ctClass, setterName, setter);
	}

	private boolean isSynthetic(CtMethod method) {
		return (method.getMethodInfo().getAccessFlags() & AccessFlag.SYNTHETIC) != 0;
	}
	
	private void addMethod(CtClass ctClass, String methodName, String methodDefinition) throws Exception {
//		if (ctClass.getName().equals("models.Product")) {
//			Logger.info("Got definition for methodName %s: %s", methodName, methodDefinition);
//		}
		try {
			// Only replace if method was added by the properties enhancer
			CtMethod ctMethod = CtMethod.make(methodDefinition, ctClass);
			CtMethod oldCtMethod = findMethod(ctClass, methodName);
			if (oldCtMethod != null) {
				if (isSynthetic(oldCtMethod)) {
					oldCtMethod.setModifiers(oldCtMethod.getModifiers() | AccessFlag.SYNTHETIC);
					ctClass.removeMethod(oldCtMethod);
					ctClass.addMethod(ctMethod);
				} else {
					Logger.info("Method %s is set in class %s, not overwriting", methodName, ctClass.getName());
				}
			} else {
				Logger.error("Could not find method %s in class %s", methodName, ctClass.getName());
			}
			
		} catch (NullPointerException e) {
			Logger.error(e, "Problem with method %s() in class %s", ctClass.getName(), methodName);
		}
	}

	private CtMethod findMethod(CtClass ctClass, String methodName) {
		for (CtMethod method : ctClass.getMethods()) {
			if (method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}

}
