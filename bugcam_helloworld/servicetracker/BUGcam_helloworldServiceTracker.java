package bugcam_helloworld.servicetracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;

import com.albin.mqtt.ConnConfig;
import com.albin.mqtt.IsidoreyClient;
import com.albin.mqtt.MqttListener;
import com.albin.mqtt.message.QoS;
import com.buglabs.application.AbstractServiceTracker;
import com.buglabs.bug.module.camera.pub.ICameraDevice;
import com.buglabs.bug.module.camera.pub.ICameraModuleControl;

public class BUGcam_helloworldServiceTracker extends AbstractServiceTracker {

	private static IsidoreyClient client;

	private ICameraModuleControl camControl;
	private ICameraDevice camera;

	public BUGcam_helloworldServiceTracker(BundleContext context) {
		super(context);
	}

	public boolean canStart() {
		return super.canStart();
	}

	public void doStart() {
		System.out.println("BUGcam_helloworldServiceTracker: start");
	}

	public void doStop() {
		System.out.println("BUGcam_helloworldServiceTracker: stop");
	}

	/**
	 * This thread is used as a demo. We wait 20 seconds after initServices is
	 * called and then publish data through our API, emulating an MQtt message.
	 * Since the device is subscribed on the published topic, it handles the
	 * logic appropriately by taking a picture.
	 * 
	 */
	class DemoPublishThread extends Thread {
		public void run() {
			try {
				System.out.println("SLEEPING THREAD 20 SECONDS");
				Thread.sleep(1000 * 20);

				ApiInteraction api = new ApiInteraction();

				Map<String, String> keyValueMap = new HashMap<String, String>();
				keyValueMap.put(Constants.controlCamera,
						"VALUE_WHICH_COULD_BE_USED_FOR_NESTED_LOGIC");

				api.publish(Constants.company, Constants.project,
						Constants.tag, Constants.asset, Constants.controlField,
						keyValueMap);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void initServices() {
		System.out.println("INIT SERVICES");

		/**
		 * Start the thread to handle the picture taking
		 */
		Thread thread = new DemoPublishThread();
		thread.start();

		/**
		 * Attempt pub/sub connection
		 */
		pubsub();

		/**
		 * Demo an API call by upserting the time
		 */
		ApiInteraction api = new ApiInteraction();

		Map<String, String> keyValueMap = new HashMap<String, String>();
		Date date = new Date();
		keyValueMap.put("lastInit", String.valueOf(date.getTime()));

		api.publish(Constants.company, Constants.project, Constants.tag,
				Constants.asset, Constants.field, keyValueMap);

		/**
		 * Handle services
		 */
		getServices().add(
				"com.buglabs.bug.module.camera.pub.ICameraModuleControl");
		getServices().add("com.buglabs.bug.module.camera.pub.ICameraDevice");

	}

	/**
	 * Netty issue (?) here that I'm seeing on devices (including Android
	 * mobile), but not in testing -- Caught exception: java.nio.channels.UnresolvedAddressException. Works sporadically --
	 * can I check to see if network services are available for sure?
	 */
	public void pubsub() {
		connect();
	}

	/**
	 * Make an MQtt connection, and subscribe on a topic to see data being
	 * pumped in by device simulations.
	 */
	public void connect() {
		try {
			System.out.println("TRYING CONNECT");

			ConnConfig config = new ConnConfig(Constants.clientId,
					Constants.brokerUrl, 1883, 30, Constants.username,
					Constants.password);

			config.addSubscribedTopic(Constants.subscribedTopic,
					QoS.AT_MOST_ONCE);

			client = new IsidoreyClient(config);
			client.setListener(new EchoListener());

			client.connect();
			client.subscribeToConfigTopics();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Any devices sending data on topics we subscribe (e.g. have permissions
	 * to) will be spit out here.
	 */
	private class EchoListener implements MqttListener {
		public void disconnected() {
			System.err.println("DISCONNECTED");
		}

		public void publishArrived(String topic, byte[] data) {
			String payload = new String(data);
			System.out.println("[" + topic + "]: " + payload);

			if (topic.equals(Constants.controlTopic))
				handleCommands(payload);

		}
	}

	/**
	 * Take a picture if the correct parameter comes through on a given topic.
	 * 
	 * @param payload
	 */
	private void handleCommands(String payload) {
		String[] controlParamsArray = payload.split("~~");
		for (String entry : controlParamsArray) {
			String[] keyValuePair = entry.split("=");
			String key = keyValuePair[0];
			String value = keyValuePair[1];
			if (key.equals(Constants.controlCamera)) {
				System.out.println("CAMERA COMMAND RECEIVED, TAKING PICTURE");
				takePicture();
			}
		}
	}

	/**
	 * Why all the deprecated methods? Save the file and upload the image to
	 * your bucket.
	 */
	private void takePicture() {
		camera = (ICameraDevice) super.getService(ICameraDevice.class);
		camControl = (ICameraModuleControl) super
				.getService(ICameraModuleControl.class);

		try {
			camControl.setFlashBeamIntensity(0);
			camControl.setLEDFlash(true);

			FileOutputStream fos = new FileOutputStream(new File(
					Constants.filePath));
			fos.write(camera.getImage());

			System.out.println("Picture written to file at /tmp/image.jpeg");

			camControl.setLEDFlash(false);

			/**
			 * Upload the file!
			 */
			ApiInteraction api = new ApiInteraction();
			api.uploadFile();
		} catch (IOException e) {
			System.err
					.println("Bugcam_helloworld error: Camera control is not accessible.");
			e.printStackTrace();
		}
	}

}
