package bugcam_helloworld;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;

import bugcam_helloworld.servicetracker.BUGcam_helloworldServiceTracker;

import com.buglabs.util.ServiceFilterGenerator;

public class Activator implements BundleActivator {

	private BUGcam_helloworldServiceTracker stc;
	private ServiceTracker tracker;

	public void start(BundleContext context) throws Exception {
		System.out.println("START");

		stc = new BUGcam_helloworldServiceTracker(context);
		Filter f = context.createFilter(ServiceFilterGenerator
				.generateServiceFilter(stc.getServices()));
		tracker = new ServiceTracker(context, f, stc);
		tracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println("STOP");

		stc.stop();
		tracker.close();
	}
}
