package org.eclipse.titan.LaravelTestTTCN3.testcases;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.titan.LaravelTestTTCN3.user_provided.HTTPmsg__MessageLen_externalfunctions;
import org.eclipse.titan.runtime.core.TitanOctetString;
import org.eclipse.titan.runtime.core.TtcnError;
import org.junit.jupiter.api.Test;

public class HTTPmsgMessageLengthFunctionTest {
	
	protected static final String TEST_RESOURCES_DIR = System.getProperty("user.dir") + File.separatorChar + "test_resources" + File.separatorChar;

	@Test
	public void HTTPMessageLenFunctionNullTest() {
		assertEquals(-1, HTTPmsg__MessageLen_externalfunctions.f__HTTPMessage__len(null).get_int());
	}
	
	@Test
	public void HTTPMessageLenFunctionEmptyTest() {
		assertThrows(TtcnError.class, () -> HTTPmsg__MessageLen_externalfunctions.f__HTTPMessage__len(new TitanOctetString()).get_int());
	}

	/**
	 * Function test for f__HTTPMessage__len function. Test steps:
	 * 
	 * <ol type="1">
	 * 
	 * <li> Read HTTP request from the resource file. </li>
	 * <li> Convert the input <code>String</code> to <code>TitanOctetString</code>.
	 * <li> Call the function and check the result.</li>
	 * </ol>
	 */
	@Test
	public void HTTPMessageLenFunctionValidRequestTest() {
		Path path = Paths.get(TEST_RESOURCES_DIR + "test_http_request.txt");
		try {
			String readedRequest = Files.readString(path);
			TitanOctetString testRequestInOctet = new TitanOctetString(readedRequest.getBytes());
			assertEquals(690, HTTPmsg__MessageLen_externalfunctions.f__HTTPMessage__len(testRequestInOctet).get_int());
		} catch (IOException e) {
			fail("IOException occured: " + e.getMessage());
		}
	}
	
	@Test
	public void HTTPMessageLenFunctionValidRequestTestwithLowerCase() {
		Path path = Paths.get(TEST_RESOURCES_DIR + "test_http_request.txt");
		try {
			String readedRequest = Files.readString(path).toLowerCase();
			TitanOctetString testRequestInOctet = new TitanOctetString(readedRequest.getBytes());
			assertEquals(690, HTTPmsg__MessageLen_externalfunctions.f__HTTPMessage__len(testRequestInOctet).get_int());
		} catch (IOException e) {
			fail("IOException occured: " + e.getMessage());
		}
	}
	
	@Test
	public void HTTPMessageLenFunctionValidRequestTestwithUpperCase() {
		Path path = Paths.get(TEST_RESOURCES_DIR + "test_http_request.txt");
		try {
			String readedRequest = Files.readString(path).toUpperCase();
			TitanOctetString testRequestInOctet = new TitanOctetString(readedRequest.getBytes());
			assertEquals(690, HTTPmsg__MessageLen_externalfunctions.f__HTTPMessage__len(testRequestInOctet).get_int());
		} catch (IOException e) {
			fail("IOException occured: " + e.getMessage());
		}
	}
}
