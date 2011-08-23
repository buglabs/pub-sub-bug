package bugcam_helloworld.servicetracker;

public class Constants {

	/**
	 * Credentials
	 */
	public static String username = "";
	public static String password = "";
	public static String clientId = username + "/bug";

	/**
	 * Cloud storage
	 */
	public static String bucket = username;
	public static String filePath = "/tmp/image.jpg";

	/**
	 * Device specific (would normally be MAC/serial related)
	 */
	public static String company = username;
	public static String project = "P-000728";
	public static String tag = "bug";
	public static String asset = "labs";
	public static String field = "hub";
	public static String controlField = "ctrl";
	
	/**
	 * Control
	 */
	public static String controlCamera = "CAMERA";

	/**
	 * MQtt
	 */
	public static String brokerUrl = "broker.isidorey.net";
	public static String subscribedTopic = company + "/" + project + "/#";
	public static String controlTopic = company + "/" + project + "/" + tag
			+ "/" + asset + "/" + controlField;

}
