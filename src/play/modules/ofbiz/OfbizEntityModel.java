package play.modules.ofbiz;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

import play.Logger;

public class OfbizEntityModel {

	protected GenericValue gv;
	
	public OfbizEntityModel(String entityName) {
		gv = OfbizPlugin.delegator.makeValue(entityName);
	}

	public static OfbizQuery find(String query, Object ... fields) {
		throw new RuntimeException("Not implemented. Wrong code generation run?");
	}

	public static <T extends OfbizEntityModel> T createFromGenericValue(Class<T> clazz, GenericValue gv) {
		try {
			T instance = clazz.newInstance();
			instance.gv = gv;
			return instance;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T extends OfbizEntityModel> T createFromGenericValue(Class<T> clazz, GenericValue gv, String entityName) {
		try {
			return (T) OfbizEntityModel.createFromGenericValue(clazz, gv.getRelatedOneCache(entityName));
		} catch (GenericEntityException e) {
			Logger.error(e, "Problem getting related cache entity (%s) %s", clazz.getSimpleName(), entityName);
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T extends OfbizEntityModel> List<T> createFromGenericValueList(Class<T> clazz, GenericValue entity, String relationName) {
		List<T> results = new ArrayList<T>();
		try {
			List<GenericValue> gvs = entity.getRelated(relationName);
			for (GenericValue gv : gvs) {
				results.add(createFromGenericValue(clazz, gv));
			}
		} catch (GenericEntityException e) {
			Logger.error(e, "Problem getting related cache entity (List<%s>) %s", clazz.getSimpleName(), relationName);
		}
		return results;
	}
	
}
