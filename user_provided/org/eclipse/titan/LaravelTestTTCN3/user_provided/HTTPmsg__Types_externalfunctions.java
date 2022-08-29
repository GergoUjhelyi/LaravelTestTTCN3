package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPMessage;
import org.eclipse.titan.runtime.core.TTCN_Buffer;
import org.eclipse.titan.runtime.core.TTCN_Logger;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;
import org.eclipse.titan.runtime.core.TitanBoolean;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanOctetString;

/**
 * 
 * Encoder-decoder Functions independent from sending and receiving.
 * 
 * @author Gergo Ujhelyi
 */
public class HTTPmsg__Types_externalfunctions {

	/*********************************************************
	 * Function: enc__HTTPMessage
	 *
	 * Purpose:
	 *    To encode msg type of HTTPMessage into OCTETSTRING separated from sending functionality
	 *    It is for users using this test port as a protocol module
	 *
	 * References:
	 *   RFC2616
	 *
	 * Precondition:
	 *  msg is filled in properly
	 * Postcondition:
	 *
	 * Detailed Comments:
	 * -
	 *
	 * @param msg - the HTTP Message to be encoded
	 *
	 * @return OCTETSTRING - the encoded message
	 *
	 *********************************************************/
	public static TitanOctetString enc__HTTPMessage(HTTPMessage msg) {
		TTCN_Buffer buf = new TTCN_Buffer();
		buf.clear();
		HTTPmsg__PT.f_HTTP_encodeCommon(msg, buf);
		return new TitanOctetString(buf.get_data());
	}
	/*********************************************************
	 * Function: dec__HTTPMessage
	 *
	 * Purpose:
	 *    To decode msg type of OCTETSTRING into HTTPMessage separated from receiving functionality
	 *    It is for users using this test port as a protocol module
	 *
	 * References:
	 *   RFC2616
	 *
	 * Precondition:
	 *  stream is filled in properly
	 * Postcondition:
	 *  -
	 *  
	 * Detailed Comments:
	 * 	If the full stream is decoded, the return value is zero
	 * 	If nothing is decoded (decoding failed) the return value equals to the original length of the stream
	 * 
	 * @param stream - the message to be decoded
	 * @param msg - reference to the record type of HTTPMessage which will contain the decoded value if the return value less than the length of the original stream
	 * @param socket_debugging - boolean value to provide debug logging messages.
	 * 
	 * @return integer - the length of the remaining data which is not decoded yet.
	 *********************************************************/
	public static TitanInteger dec__HTTPMessage(TitanOctetString stream, HTTPMessage msg, TitanBoolean socket_debugging) {
		TTCN_Logger.log(Severity.DEBUG_TESTPORT, "starting HTTPmsg__Types.dec__HTTPMessage");
		TTCN_Buffer buf_p = new TTCN_Buffer();
		buf_p.put_os(stream);

		int buf_len = buf_p.get_read_len();
		if (buf_len > 0) {
			if (HTTPmsg__PT.f_HTTP_decodeCommon(buf_p, msg, true, socket_debugging.get_value(), null, null)) {
				HTTPmsg__PT.log_debug(socket_debugging.get_value(), "","","dec__HTTPMessage, after decoding:\nbuf_len: %d\nget_len: %d\nget_read_len:%d", buf_len, buf_p.get_len(), buf_p.get_read_len());;
				buf_len = buf_p.get_read_len(); //remaining data length
			} else {
				buf_len = -1;
			}
		} else {
			buf_len = -1;
		}
		buf_p = null;
		return new TitanInteger(buf_len);
	}
}
