package org.eclipse.titan.LaravelTestTTCN3.testcases;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPMessage;
import org.eclipse.titan.LaravelTestTTCN3.user_provided.HTTPmsg__Types_externalfunctions;
import org.eclipse.titan.runtime.core.TitanOctetString;
import org.junit.jupiter.api.Test;

public class HTTPmsgTypesExternalFunctionTest {

	@Test
	public void HTTPmsgTypesEncodeNullTest() {
		assertFalse(HTTPmsg__Types_externalfunctions.enc__HTTPMessage(null).is_bound());
	}
	
	@Test
	public void HTTPmsgTypesEncodeEmptyTest() {
		HTTPMessage emptyMessage = new HTTPMessage();
	    assertEquals(2, HTTPmsg__Types_externalfunctions.enc__HTTPMessage(emptyMessage).lengthof().get_int()); // \r\n
	}
	
	@Test
	public void HTTPmsgTypesEncodeTest() {
		HTTPMessage testMessage = new HTTPMessage(); // same as in the 
	}
	
	

	@Test
	void testDec__HTTPMessage() {
		//fail("Not yet implemented");
	}

}
