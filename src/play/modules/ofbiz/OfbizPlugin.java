package play.modules.ofbiz;

import org.ofbiz.base.component.ComponentConfig;
import org.ofbiz.base.component.ComponentException;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.entity.GenericDelegator;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;

public class OfbizPlugin extends PlayPlugin {

	private OfbizEnhancer enhancer = new OfbizEnhancer();
	public static Delegator delegator;

	@Override
	public void onApplicationStart() {
		try {
			ComponentConfig.getComponentConfig("play", "conf/ofbiz/");
		} catch (ComponentException e) {
			Logger.error(e, "Error loading play component");
		}
		delegator = DelegatorFactory.getDelegator(null);
		Logger.info("OfBiz Entity engine plugin loaded");
	}

	@Override
	public void enhance(ApplicationClass applicationClass) throws Exception {
		// Dont enhance if run via "play ofbiz:create" commandline...
		if (!"entityWriter".equals(Play.id)) {
			enhancer.enhanceThisClass(applicationClass);
		}
	}
}
