package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import org.eclipse.titan.runtime.core.TitanBoolean;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TtcnError;

public class Init__Functions_externalfunctions {

	//Checks the input server directory
	public static TitanBoolean ef__valid__path(TitanCharString serverDirectory) {
		if (serverDirectory == null || !serverDirectory.is_bound()) {
			throw new TtcnError(new IllegalArgumentException("Server directory must be bound!"));
		}
		try {
			final Path path = Path.of(serverDirectory.get_value().toString());
		} catch (InvalidPathException e) {
			return new TitanBoolean(false);
		}
		return new TitanBoolean(true);
	}

	public static void ef__create__route__file(TitanCharString serverDirectory, TitanCharString routeFile) {
		// TODO Auto-generated method stub
	}

}
