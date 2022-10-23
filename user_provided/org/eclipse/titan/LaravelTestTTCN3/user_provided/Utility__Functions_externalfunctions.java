package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.titan.LaravelTestTTCN3.generated.Route.Route__Obj__List;
import org.eclipse.titan.LaravelTestTTCN3.generated.Route.Route__obj;
import org.eclipse.titan.runtime.core.PreGenRecordOf.PREGEN__RECORD__OF__CHARSTRING;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanNull_Type;
import org.eclipse.titan.runtime.core.TtcnError;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class for conversion functions. Conversion between JSON route file data and JAVA/TTCN-3 objects.
 * 
 * @author Gergo Ujhelyi
 *
 */
public class Utility__Functions_externalfunctions {

	/**
	 * Static function to read file with the Laravel routes in JSON format and convert it to TTCN-3 type.
	 * 
	 * @param f__filename - the file name what the function read
	 * @return list with the routes or an empty list
	 * 
	 */
	public static Route__Obj__List ef__readRoutesFile(TitanCharString f__filename) {
		if (!f__filename.is_bound() || f__filename.get_value().isEmpty()) {
			//IllegalArgumentException would be good, but for testing an empty list is better
			return new Route__Obj__List(TitanNull_Type.NULL_VALUE);
		}
		String fileContent = null;
		//Read file with Java 11 features
		try {
			fileContent = Files.readString(Path.of(System.getProperty("user.dir") + File.separator + f__filename.get_value().toString()));
		} catch (IOException e) {
			TtcnError.TtcnWarning("Error in reading routes file: " + e.getMessage() + ". Return an empty record of!");
			return new Route__Obj__List(TitanNull_Type.NULL_VALUE);
		}
		if (fileContent == null || fileContent.isEmpty()) {
			TtcnError.TtcnWarning("Empty or null given from file content. Return an empty record of!");
			return new Route__Obj__List(TitanNull_Type.NULL_VALUE);
		}
		JSONArray routesArray = new JSONArray(fileContent);
		if (routesArray.isEmpty()) {
			TtcnError.TtcnWarning("Empty array given from JSONArray. Return an empty record of!");
			return new Route__Obj__List(TitanNull_Type.NULL_VALUE);
		}
		//Need initialized list inside of the Route__Obj__List object
		Route__Obj__List list = new Route__Obj__List(TitanNull_Type.NULL_VALUE);
		for (int i = 0; i < routesArray.length(); i++) {
			JSONObject JSONRouteObject = routesArray.getJSONObject(i);
			Route__obj ttcnRouteObject = new Route__obj();

			//Assignment
			if (!JSONRouteObject.isNull("domain")) {
				ttcnRouteObject.get_field_domain().operator_assign(JSONRouteObject.getString("domain"));
			}
			if (!JSONRouteObject.isNull("method")) {
				ttcnRouteObject.get_field_method().operator_assign(JSONRouteObject.getString("method"));
			}
			if (!JSONRouteObject.isNull("uri")) {

				String uri = JSONRouteObject.getString("uri");
				if (!uri.equals("/")) {
					ttcnRouteObject.get_field_uri().operator_assign('/' + JSONRouteObject.getString("uri"));
				} else {
					ttcnRouteObject.get_field_uri().operator_assign(JSONRouteObject.getString("uri"));
				}
			}
			if (!JSONRouteObject.isNull("name")) {
				ttcnRouteObject.get_field_name().operator_assign(JSONRouteObject.getString("name"));
			}
			if (!JSONRouteObject.isNull("action")) {
				ttcnRouteObject.get_field_action__str().operator_assign(JSONRouteObject.getString("action"));
			}
			if (!JSONRouteObject.isNull("middleware")) {
				ttcnRouteObject.get_field_action__str().operator_assign(JSONRouteObject.getString("action"));
				ttcnRouteObject.get_field_middleware().operator_assign(TitanNull_Type.NULL_VALUE);
				for (int j = 0; j < JSONRouteObject.getJSONArray("middleware").length(); j++) {
					ttcnRouteObject.get_field_middleware().get_at(j).operator_assign(JSONRouteObject.getJSONArray("middleware").getString(j));
				}
			}
			list.get_at(i).operator_assign(ttcnRouteObject);
		}
		return list;
	}

	/**
	 * Split the parameter string at '|' characters.
	 * 
	 * @param methodNames the string to split
	 * @return a list of strings in TTCN-3/Titan format
	 */
	public static PREGEN__RECORD__OF__CHARSTRING ef__splitMethod(final TitanCharString methodNames) {
		if (methodNames == null) {
			throw new TtcnError(new IllegalArgumentException("methodNames can't be null!"));
		}
		methodNames.must_bound("methodNames must be a value!");
		final String methodNameString = methodNames.get_value().toString();
		final String splitOperator = "|";
		String[] splittedMethods = methodNameString.split(splitOperator);
		PREGEN__RECORD__OF__CHARSTRING returnValue = new PREGEN__RECORD__OF__CHARSTRING(TitanNull_Type.NULL_VALUE);
		for (int i = 0; i < splittedMethods.length; i++) {
			returnValue.get_at(i).operator_assign(splittedMethods[i]);
		}
		return returnValue;
	}

	/**
	 * 
	 * @param httpserverhost the server host name
	 * @param httpserverport the server port number
	 * @return the csrf token in String format
	 */
	public static String ef__getCSRFToken(TitanCharString httpserverhost, TitanInteger httpserverport) {
		try {
			URL serverURL = new URL("http://" + httpserverhost.get_value().toString() + ':' + httpserverport.get_int() + "/token");
			HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "application/json");
			//5 seconds timeout
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			connection.disconnect();
			return content.toString();
		} catch (IOException e) {
			TtcnError.TtcnWarning("Error in ef__getCSRFToken: " + e.getMessage());
			return "";
		}
	}

	public static String ef__getJSONFormatPOSTRequestBody() {
		JSONObject testJSONObject = new JSONObject();
		testJSONObject.put("plugin_id", "TestID");
		testJSONObject.put("plugin_version_qualifier", "bbbb");
		testJSONObject.put("plugin_version", "22.10.23");
		testJSONObject.put("os_version", "Plain Java");
		testJSONObject.put("os_arch", "jvm");
		testJSONObject.put("eclipse_version", "1.2.3");
		testJSONObject.put("eclipse_version_qualifier", "Java with Json lib");
		testJSONObject.put("user_id", "java_user");
		testJSONObject.put("java_version", "18");
		testJSONObject.put("os_name", "jvm os");
		testJSONObject.put("info", "External function test 1.0");
		return testJSONObject.toString(1);
	}
}
