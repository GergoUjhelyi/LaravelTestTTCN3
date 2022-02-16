package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.File;
import java.io.IOException;
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

	//Create route file
	public static void ef__create__route__file(TitanCharString serverDirectory, TitanCharString routeFile) {
		if (serverDirectory == null || !serverDirectory.is_bound()) {
			throw new TtcnError(new IllegalArgumentException("Server directory must be bound!"));
		}
		if (routeFile == null || !routeFile.is_bound()) {
			throw new TtcnError(new IllegalArgumentException("Route file must be bound!"));
		}
		
		final boolean isAbsolute = Path.of(serverDirectory.get_value().toString()).isAbsolute();
		final String directory = isAbsolute ? serverDirectory.get_value().toString() : System.getProperty("user.dir") + serverDirectory.get_value().toString();
		final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		
		ProcessBuilder builder = new ProcessBuilder();
		if (isWindows) {
			builder.command("cmd.exe", "/c", String.format("php artisan route:list --json > %s/%s", System.getProperty("user.dir"), routeFile));
		} else {
			builder.command("sh", "-c", String.format("php artisan route:list --json > %s", routeFile));
		}
		builder.directory(new File(directory));
		try {
			Process process = builder.start();
		} catch (IOException e) {
			throw new TtcnError(e);
		}
	}
}
