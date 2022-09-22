package org.eclipse.titan.LaravelTestTTCN3.testcases;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPMessage;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPRequest;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HeaderLine;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HeaderLines;
import org.eclipse.titan.LaravelTestTTCN3.user_provided.HTTPmsg__Types_externalfunctions;
import org.eclipse.titan.runtime.core.TitanCharString;
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
		HTTPMessage testMessage = new HTTPMessage(); // same as in the test file
		testMessage.get_field_request().operator_assign(getTestRequest());
		assertEquals(690 + (16*2) + 1, HTTPmsg__Types_externalfunctions.enc__HTTPMessage(testMessage).lengthof().get_int()); // 690 byte data, 16 /r/n sequence, 1 /r/n between headers and body
	}

	@Test
	public void testDec__HTTPMessage() {
		//fail("Not yet implemented");
	}

	private HTTPRequest getTestRequest() {
		HTTPRequest testRequest = new HTTPRequest();
		testRequest.get_field_method().operator_assign("GET");
		testRequest.get_field_uri().operator_assign("/");
		testRequest.get_field_version__major().operator_assign(1);
		testRequest.get_field_version__minor().operator_assign(1);
		HeaderLines hdrLines = new HeaderLines();
		HeaderLine[] hdrLinesArray = { new HeaderLine(new TitanCharString("Accept"), new TitanCharString("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")),
				new HeaderLine(new TitanCharString("Accept-Encoding"), new TitanCharString("gzip, deflate, br")),
				new HeaderLine(new TitanCharString("Accept-Language"), new TitanCharString("hu,en;q=0.9")),
				new HeaderLine(new TitanCharString("Accept-Language"), new TitanCharString("hu,en;q=0.9")),
				new HeaderLine(new TitanCharString("Cache-Control"), new TitanCharString("max-age=0")),
				new HeaderLine(new TitanCharString("Connection"), new TitanCharString("keep-alive")),
				new HeaderLine(new TitanCharString("Cookie"), new TitanCharString("PHPSESSID=hr05nu56diri84bl0m68thdple")),
				new HeaderLine(new TitanCharString("Host"), new TitanCharString("www.elte.hu")),
				new HeaderLine(new TitanCharString("Sec-Fetch-Dest"), new TitanCharString("document")),
				new HeaderLine(new TitanCharString("Sec-Fetch-Mode"), new TitanCharString("navigate")),
				new HeaderLine(new TitanCharString("Sec-Fetch-Site"), new TitanCharString("none")),
				new HeaderLine(new TitanCharString("Sec-Fetch-User"), new TitanCharString("?1")),
				new HeaderLine(new TitanCharString("User-Agent"), new TitanCharString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")),
				new HeaderLine(new TitanCharString("sec-ch-ua"), new TitanCharString("\"Google Chrome\";v=\"105\", \"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"105\"")),
				new HeaderLine(new TitanCharString("sec-ch-ua-mobile"), new TitanCharString("?0")),
				new HeaderLine(new TitanCharString("sec-ch-ua-platform"), new TitanCharString("\"Windows\""))
		};
		for (int i = 0; i < hdrLinesArray.length; i++) {
			hdrLines.get_at(i).operator_assign(hdrLinesArray[i]);
		}
		testRequest.get_field_header().operator_assign(hdrLines);
		testRequest.get_field_body().operator_assign("");
		return testRequest;
	}
}
