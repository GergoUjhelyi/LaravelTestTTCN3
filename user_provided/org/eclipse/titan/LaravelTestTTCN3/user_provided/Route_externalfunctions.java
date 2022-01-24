package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.titan.LaravelTestTTCN3.generated.Route.Route__Obj__List;
import org.eclipse.titan.LaravelTestTTCN3.generated.Route.Route__obj;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanNull_Type;
import org.eclipse.titan.runtime.core.TtcnError;

import org.json.JSONArray;
import org.json.JSONObject;

public class Route_externalfunctions {

	public static Route__Obj__List f__readRoutesFile(TitanCharString f__filename) {
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

}
