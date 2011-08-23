package bugcam_helloworld.servicetracker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.isidorey.api.APIConstants;
import com.isidorey.api.FileAPI;
import com.isidorey.api.PublishAPI;
import com.isidorey.api.exception.APIException;

public class ApiInteraction {

	/**
	 * This call is used to send key/value pairs that relate to a device; it
	 * emulates an MQtt publish through our API, and is also the same one used
	 * to control devices!
	 */
	public void publish(String company, String project, String tag,
			String asset, String field, Map<String, String> keyValueMap) {

		System.out.println("PUBLISHING DATA ON:" + company + "/" + project
				+ "/" + tag + "/" + asset + "/" + field);

		PublishAPI api = new PublishAPI(APIConstants.API_URL,
				Constants.username, Constants.password);
		try {
			api.publish(company, project, tag, asset, field, keyValueMap);
		} catch (APIException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This call is used to upload a file to cloud storage in S3.
	 */
	public void uploadFile() {
		FileAPI api = new FileAPI(APIConstants.API_URL, Constants.username,
				Constants.password);
		try {
			api.uploadFile(Constants.bucket, new File(Constants.filePath));
		} catch (APIException e) {
			e.printStackTrace();
		}
	}

}
