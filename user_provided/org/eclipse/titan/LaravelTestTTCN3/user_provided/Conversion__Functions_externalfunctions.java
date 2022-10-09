package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.File;
import java.io.IOException;
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
public class Conversion__Functions_externalfunctions {
	
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
		// TODO Auto-generated method stub
		return null;
	}
}
