package play.modules.ofbiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;

import play.Logger;

import com.sun.xml.internal.ws.util.StringUtils;

public class OfbizQuery {

	private EntityFindOptions options = new EntityFindOptions();
	private boolean useCache = true;
	private Class clazz;
	private List<EntityCondition> conditions = new ArrayList<EntityCondition>();

	public <T extends OfbizEntityModel> OfbizQuery(Class<T> clazz, String query, Object ... fields) {
		this.clazz = clazz;

		query = query.replaceFirst("^by", "");

		String[] fieldNames = query.split("And");
		for (int i = 0 ; i < fieldNames.length; i++) {
			conditions.add(EntityCondition.makeCondition(StringUtils.decapitalize(fieldNames[i]), EntityOperator.EQUALS, fields[i]));
		}

		Logger.info("Searching for %s with query %s", clazz.getSimpleName(), query);
	}

	public <T extends OfbizEntityModel> OfbizQuery(Class<T> clazz, Map<String, Object> fieldMap) {
		this.clazz = clazz;

		for (Entry<String, Object> entry : fieldMap.entrySet()) {
			conditions.add(EntityCondition.makeCondition(StringUtils.decapitalize(entry.getKey()), EntityOperator.EQUALS, entry.getValue()));
		}
		
		Logger.info("Searching for %s with values %s", clazz.getSimpleName(), fieldMap.toString());
	}

	public <T extends OfbizEntityModel> OfbizQuery(Class<T> clazz, EntityCondition cond) {
		this.clazz = clazz;
		conditions.add(EntityCondition.makeCondition(cond));
	}


	public OfbizQuery limit(int limit) {
		options.setMaxRows(limit);
		return this;
	}
	
	public OfbizQuery noCache() {
		useCache = false;
		return this;
	}
	
	public <T extends OfbizEntityModel> T first() {
		List<T> results = fetch();
		if (results.size() > 0) {
			return results.get(0);
		}
		return null;
	}

	public <T extends OfbizEntityModel> List<T> fetch() {
		List<T> result = new ArrayList<T>();
		try {
			List<GenericValue> values = OfbizPlugin.delegator.findList(clazz.getSimpleName(), 
					EntityCondition.makeCondition(conditions), null, null, options, useCache);
			
			Logger.info("Query for %s with condition %s yielded %s results", clazz.getSimpleName(), conditions, values.size());
			
			for (GenericValue gv : values) {
				T ofbizEntityModel = (T) OfbizEntityModel.createFromGenericValue(clazz, gv);
				result.add(ofbizEntityModel);
			}
		} catch (GenericEntityException e) {
			Logger.error(e, "Problem fetching entites");
		}
		return result;
	}

}
