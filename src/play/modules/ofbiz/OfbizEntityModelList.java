package play.modules.ofbiz;

import java.util.ArrayList;
import java.util.List;

import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

public class OfbizEntityModelList {

	private static final long serialVersionUID = 1L;

	public static <T extends OfbizEntityModel> List<T> createFromGenericValueList(Class<T> clazz, GenericValue gv, String entityName) {
		try {
			List<GenericValue> gvs = gv.getRelatedCache(entityName);
			List<T> list = new ArrayList<T>(gvs.size());
			for (GenericValue genericValue : gvs) {
				list.add(OfbizEntityModel.createFromGenericValue(clazz, genericValue, entityName));
			}
			return list;
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return null;
	}
}
