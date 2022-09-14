package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types;
import org.eclipse.titan.runtime.core.TTCN_Buffer;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanOctetString;

/**
 * Utility class for HTTP testport related functions.
 * 
 * @author Gergo Ujhelyi
 *
 */
public class HTTPmsg__MessageLen_externalfunctions {

	/**
	 * Decode HTTP message and calculate its length.
	 * 
	 * @param stream the HTTP message in octet format
	 * @return the length of the message or -1 if any error occurred
	 */
	public static TitanInteger f__HTTPMessage__len(final TitanOctetString stream) {
		if (stream == null) {
			//Don't cause error.
			return new TitanInteger(-1);
		}
		HTTPmsg__Types.HTTPMessage msg = new HTTPmsg__Types.HTTPMessage();
		TTCN_Buffer buf_p = new TTCN_Buffer();
		buf_p.put_os(stream);
		
		int buf_len = buf_p.get_read_len();
		if (buf_len > 0) {
			if (HTTPmsg__PT.f_HTTP_decodeCommon(buf_p, msg, true, false, null, null)) {
				buf_len -= buf_p.get_read_len();
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
