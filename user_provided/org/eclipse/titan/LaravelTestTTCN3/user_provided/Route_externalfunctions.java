package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import org.eclipse.titan.LaravelTestTTCN3.generated.Route.Route__Obj__List;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanNull_Type;

public class Route_externalfunctions {

	public static Route__Obj__List f__readRoutesFile(TitanCharString f__filename) {
		if (!f__filename.is_bound() || f__filename.get_value().isEmpty()) {
			//IllegalArgumentException would be good, but for testing an empty list is better
			return new Route__Obj__List(TitanNull_Type.NULL_VALUE);
		}
		// TODO Auto-generated method stub
		return null;
	}

}
