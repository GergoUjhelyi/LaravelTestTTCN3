package org.eclipse.titan.LaravelTestTTCN3.testcases;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPMessage;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPMessage.union_selection_type;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPRequest;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HeaderLine;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HeaderLines;
import org.eclipse.titan.LaravelTestTTCN3.user_provided.HTTPmsg__Types_externalfunctions;
import org.eclipse.titan.runtime.core.TitanBoolean;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanOctetString;
import org.eclipse.titan.runtime.core.TtcnError;
import org.junit.jupiter.api.Test;

public class HTTPmsgTypesExternalFunctionTest {

	@Test
	public void HTTPmsgTypesEncodeNullTest() {
		assertFalse(HTTPmsg__Types_externalfunctions.enc__HTTPMessage(null).is_bound());
	}

	@Test
	public void HTTPmsgTypesEncodeEmptyTest() {
		HTTPMessage emptyMessage = new HTTPMessage();
		assertEquals(2, HTTPmsg__Types_externalfunctions.enc__HTTPMessage(emptyMessage).lengthof().get_int()); // /r/n
	}

	@Test
	public void HTTPmsgTypesEncodeTest() {
		HTTPMessage testMessage = new HTTPMessage(); // same as in the test file
		testMessage.get_field_request().operator_assign(getTestRequest());
		assertEquals(690 + (16*2) + 1, HTTPmsg__Types_externalfunctions.enc__HTTPMessage(testMessage).lengthof().get_int()); // 690 byte data, 16 /r/n sequence, 1 /r/n between headers and body
	}

	@Test
	public void HTTPmsgTypesDecodeNullTest() {
		assertEquals(-1, HTTPmsg__Types_externalfunctions.dec__HTTPMessage(null, null, null).get_int());
	}

	@Test
	public void HTTPmsgTypesDecodeEmptyTest() {
		assertThrows(TtcnError.class, () -> HTTPmsg__Types_externalfunctions.dec__HTTPMessage(new TitanOctetString(), new HTTPMessage(), new TitanBoolean(false)));
	}

	@Test
	public void HTTPmsgTypesDecodeTest() {
		TitanOctetString testMessage = new TitanOctetString("474554202F20485454502F312E310D0A4163636570743A20746578742F68746D6C2C6170706C69636174696F6E2F7868746D6C2B786D6C2C6170706C69636174696F6E2F786D6C3B713D302E392C696D6167652F617669662C696D6167652F776562702C696D6167652F61706E672C2A2F2A3B713D302E382C6170706C69636174696F6E2F7369676E65642D65786368616E67653B763D62333B713D302E390D0A4163636570742D456E636F64696E673A20677A69702C206465666C6174652C2062720D0A4163636570742D4C616E67756167653A2068752C656E3B713D302E390D0A4163636570742D4C616E67756167653A2068752C656E3B713D302E390D0A43616368652D436F6E74726F6C3A206D61782D6167653D300D0A436F6E6E656374696F6E3A206B6565702D616C6976650D0A436F6F6B69653A205048505345535349443D687230356E753536646972693834626C306D3638746864706C650D0A486F73743A207777772E656C74652E68750D0A5365632D46657463682D446573743A20646F63756D656E740D0A5365632D46657463682D4D6F64653A206E617669676174650D0A5365632D46657463682D536974653A206E6F6E650D0A5365632D46657463682D557365723A203F310D0A557365722D4167656E743A204D6F7A696C6C612F352E30202857696E646F7773204E542031302E303B2057696E36343B2078363429204170706C655765624B69742F3533372E333620284B48544D4C2C206C696B65204765636B6F29204368726F6D652F3130352E302E302E30205361666172692F3533372E33360D0A7365632D63682D75613A2022476F6F676C65204368726F6D65223B763D22313035222C20224E6F7429413B4272616E64223B763D2238222C20224368726F6D69756D223B763D22313035220D0A7365632D63682D75612D6D6F62696C653A203F300D0A7365632D63682D75612D706C6174666F726D3A202257696E646F7773220D0A0D0A");
		HTTPMessage actualMessage = new HTTPMessage();
		HTTPmsg__Types_externalfunctions.dec__HTTPMessage(testMessage, actualMessage, new TitanBoolean(false));
		HTTPRequest exceptedRequest = getTestRequest();
		if (!actualMessage.ischosen(union_selection_type.ALT_request)) {
			fail("Message should be an HTTP Request");
		}
		assertTrue(actualMessage.get_field_request().constGet_field_method().operator_equals(exceptedRequest.constGet_field_method()));
		assertTrue(actualMessage.get_field_request().constGet_field_uri().operator_equals(exceptedRequest.constGet_field_uri()));
		assertTrue(actualMessage.get_field_request().constGet_field_version__major().operator_equals(exceptedRequest.constGet_field_version__major()));
		assertTrue(actualMessage.get_field_request().constGet_field_version__minor().operator_equals(exceptedRequest.constGet_field_version__minor()));
		for (int i = 0; i < actualMessage.get_field_request().constGet_field_header().size_of().get_int(); i++) {
			assertTrue(actualMessage.get_field_request().constGet_field_header().constGet_at(i).constGet_field_header__name().operator_equals(exceptedRequest.constGet_field_header().constGet_at(i).constGet_field_header__name()));
			assertTrue(actualMessage.get_field_request().constGet_field_header().constGet_at(i).constGet_field_header__value().operator_equals(exceptedRequest.constGet_field_header().constGet_at(i).constGet_field_header__value()));
		}
		assertTrue(actualMessage.get_field_request().constGet_field_body().operator_equals(exceptedRequest.constGet_field_body()));
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
