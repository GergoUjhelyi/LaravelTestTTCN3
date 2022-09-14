package org.eclipse.titan.LaravelTestTTCN3.testcases;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.titan.LaravelTestTTCN3.user_provided.HTTPmsg__MessageLen_externalfunctions;
import org.eclipse.titan.runtime.core.TitanOctetString;
import org.eclipse.titan.runtime.core.TtcnError;
import org.junit.jupiter.api.Test;

public class HTTPmsgMessageLengthFunctionTest {

	@Test
	public void HTTPMessageLenFunctionNullTest() {
		assertEquals(-1, HTTPmsg__MessageLen_externalfunctions.f__HTTPMessage__len(null).get_int());
	}
	
	@Test
	public void HTTPMessageLenFunctionEmptyTest() {
		assertThrows(TtcnError.class, () -> HTTPmsg__MessageLen_externalfunctions.f__HTTPMessage__len(new TitanOctetString()).get_int());
	}
	//TODO: create a test with valid HTTP message

}
