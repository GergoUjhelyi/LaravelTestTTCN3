package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

/**
 * 
 * @author Gergo Ujhelyi
 *
 */
public class SSLSocket extends Abstract_Socket {

	//Parameter Constant variables instead of a function call
	protected static final String SSL_USE_SSL_NAME = "ssl_use_ssl";
	protected static final String SSL_USE_SESSION_RESUMPTION_NAME = "ssl_use_session_resumption";
	protected static final String SSL_PRIVATE_KEY_FILE_NAME = "ssl_private_key_file";
	protected static final String SSL_TRUSTEDCALIST_FILE_NAME = "ssl_trustedCAlist_file";
	protected static final String SSL_CERTIFICATE_FILE_NAME = "ssl_certificate_chain_file";
	protected static final String SSL_PASSWORD_NAME = "ssl_private_key_password";
	protected static final String SSL_CIPHER_LIST_NAME = "ssl_allowed_ciphers_list";
	protected static final String SSL_VERIFYCERTIFICATE_NAME = "ssl_verify_certificate";
	protected static final String SSL_DISABLE_SSLV2 = "ssl_disable_SSLv2";
	protected static final String SSL_DISABLE_SSLV3 = "ssl_disable_SSLv3";
	protected static final String SSL_DISABLE_TLSV1 = "ssl_disable_TLSv1";
	protected static final String SSL_DISABLE_TLSV1_1 = "ssl_disable_TLSv1_1";
	protected static final String SSL_DISABLE_TLSV1_2 = "ssl_disable_TLSv1_2";
	protected static final String SSL_DISABLE_TLSV1_3 = "ssl_disable_TLSv1_3";
	// TODO: add Java specific parameter name
	protected static final String JAVA_KEYSTORE_PASSWORD_NAME = "java_keystore_password";
	protected static final String JAVA_KEYSTORE_TYPE_NAME = "java_keystore_type";
	protected static final String JAVA_KEYSTORE_ALGORITHM_NAME = "java_keystore_algorithm";
	protected static final String JAVA_TRUSTSTORE_PASSWORD_NAME = "java_truststore_password";
	protected static final String JAVA_TRUSTSTORE_TYPE_NAME = "java_truststore_type";
	protected static final String JAVA_TRUSTSTORE_ALGORITHM_NAME = "java_truststore_algorithm";
	protected static final String JAVA_CERT_LOCATION_NAME = "java_cert_files_location";

	private static final String KEYSTORE_DEFAULT_PASSWORD = "password";
	private static final String DEFAULT_CERT_FILES_LOCATION = System.getProperty("user.dir") + File.separator + "resources" + File.separator;

	private boolean ssl_verify_certificate;     // verify other part's certificate or not
	private boolean ssl_use_ssl;                // whether to use SSL
	private boolean ssl_initialized;            // whether SSL already initialized or not
	private boolean ssl_use_session_resumption; // use SSL sessions or not

	// Supported protocols flags - SSLv2 not supported by Java
	private boolean SSLv3;
	private boolean TLSv1;
	private boolean TLSv1_1;
	private boolean TLSv1_2;
	private boolean TLSv1_3;

	private String ssl_key_file;              // private key file
	private String ssl_certificate_file;      // own certificate file
	private String ssl_trustedCAlist_file;    // trusted CA list file
	private String ssl_cipher_list;           // ssl_cipher list restriction to apply
	private String ssl_password;              // password to decode the private key

	private SSLSession ssl_session;
	private SSLContext ssl_ctx;
	private SSLEngine ssl_engine;

	private String cert_location;
	private String keystore_password;
	/**
	 * Possible Java KeyStore types, for more details check the testport documentation. 
	 * KeyStore types: 
	 * 	- JKS
	 *  - JCEKS
	 *  - PKCS12
	 *  - PKCS11
	 *  - DKS
	 *  - Windows-MY
	 *  - BKS
	 */
	private String keystore_type;
	private String keystore_algorithm;

	private String truststore_password;
	private String truststore_type;
	private String truststore_algorithm;

	private KeyStore ks_keys; // KeyStore
	private KeyStore ts_keys; // TrustStore
	
	private ByteBuffer my_app_data;
    private ByteBuffer my_net_data;
    private ByteBuffer peer_app_data;
    private ByteBuffer peer_net_data;

	public SSLSocket() {
		ssl_use_ssl = false;
		ssl_initialized = false;
		ssl_key_file = null;
		ssl_certificate_file = null;
		ssl_trustedCAlist_file = null;
		ssl_cipher_list = null;
		ssl_verify_certificate = false;
		ssl_use_session_resumption = true;
		ssl_session = null;
		ssl_password = null;
		test_port_type = null;
		test_port_name = null;
		ssl_ctx = null;
		ssl_engine = null;
		SSLv3 = true;
		TLSv1 = true;
		TLSv1_1 = true;
		TLSv1_2 = true;
		TLSv1_3 = true;
	}

	public SSLSocket(final String tp_type, final String tp_name) {
		ssl_use_ssl = false;
		ssl_initialized = false;
		ssl_key_file = null;
		ssl_certificate_file = null;
		ssl_trustedCAlist_file = null;
		ssl_cipher_list = null;
		ssl_verify_certificate = false;
		ssl_use_session_resumption = true;
		ssl_session = null;
		ssl_password = null;
		test_port_type = tp_type;
		test_port_name = tp_name;
		ssl_ctx = null;
		ssl_engine = null;
		SSLv3 = true;
		TLSv1 = true;
		TLSv1_1 = true;
		TLSv1_2 = true;
		TLSv1_3 = true;
		//Java specific
	}

	protected boolean parameter_set(final String parameter_name, final String parameter_value) {
		if (parameter_name == null || parameter_value == null) {
			log_error("Parameter name and parameter value should not be null");
		}
		log_debug("entering SSL_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);

		if (parameter_name.equals(SSL_USE_SSL_NAME)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				ssl_use_ssl = true;
			} else if (parameter_value.toLowerCase().equals("no")) {
				ssl_use_ssl = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_USE_SSL_NAME);
			}
		} else if (parameter_name.equals(SSL_USE_SESSION_RESUMPTION_NAME)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				ssl_use_session_resumption = true;
			} else if (parameter_value.toLowerCase().equals("no")) {
				ssl_use_session_resumption = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_USE_SESSION_RESUMPTION_NAME);
			}
		} else if (parameter_name.equals(SSL_PRIVATE_KEY_FILE_NAME)) {
			ssl_key_file = parameter_value;
		} else if (parameter_name.equals(SSL_TRUSTEDCALIST_FILE_NAME)) {
			ssl_trustedCAlist_file = parameter_value;
		} else if (parameter_name.equals(SSL_CERTIFICATE_FILE_NAME)) {
			ssl_certificate_file = parameter_value;
		} else if (parameter_name.equals(SSL_CIPHER_LIST_NAME)) {
			ssl_cipher_list = parameter_value;
		} else if (parameter_name.equals(SSL_PASSWORD_NAME)) {
			ssl_password = parameter_value;
		} else if (parameter_name.equals(SSL_VERIFYCERTIFICATE_NAME)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				ssl_verify_certificate = true;
			} else if (parameter_value.toLowerCase().equals("no")) {
				ssl_verify_certificate = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_VERIFYCERTIFICATE_NAME);
			}
		} else if (parameter_name.equals(SSL_DISABLE_SSLV2)) {
			log_warning("SSLv2 is not supported by Java - parameter value : %s is not used!", parameter_value);
		} else if (parameter_name.equals(SSL_DISABLE_SSLV3)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				SSLv3 = false;
			} else if (parameter_value.toLowerCase().equals("no")) {
				SSLv3 = true;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_DISABLE_SSLV3);
			}
		} else if (parameter_name.equals(SSL_DISABLE_TLSV1)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				TLSv1 = false;
			} else if (parameter_value.toLowerCase().equals("no")) {
				TLSv1 = true;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_DISABLE_TLSV1);
			}
		} else if (parameter_name.equals(SSL_DISABLE_TLSV1_1)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				TLSv1_1 = false;
			} else if (parameter_value.toLowerCase().equals("no")) {
				TLSv1_1 = true;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_DISABLE_TLSV1_1);
			}
		} else if (parameter_name.equals(SSL_DISABLE_TLSV1_2)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				TLSv1_2 = false;
			} else if (parameter_value.toLowerCase().equals("no")) {
				TLSv1_2 = true;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_DISABLE_TLSV1_2);
			}
		} else if (parameter_name.equals(SSL_DISABLE_TLSV1_3)) {
			if (parameter_value.toLowerCase().equals("yes")) {
				TLSv1_3 = false;
			} else if (parameter_value.toLowerCase().equals("no")) {
				TLSv1_3 = true;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SSL_DISABLE_TLSV1_3);
			}
		} else if (parameter_name.equals(JAVA_KEYSTORE_PASSWORD_NAME)) {
			keystore_password = parameter_value;
		} else if (parameter_name.equals(JAVA_KEYSTORE_TYPE_NAME)) {
			keystore_type = parameter_value;
		} else if (parameter_name.equals(JAVA_TRUSTSTORE_ALGORITHM_NAME)) {
			keystore_algorithm = parameter_value;
		} else if (parameter_name.equals(JAVA_TRUSTSTORE_PASSWORD_NAME)) {
			truststore_password = parameter_value;
		} else if (parameter_name.equals(JAVA_TRUSTSTORE_TYPE_NAME)) {
			truststore_type = parameter_value;
		} else if (parameter_name.equals(JAVA_TRUSTSTORE_ALGORITHM_NAME)) {
			truststore_algorithm = parameter_value;
		} else if (parameter_name.equals(JAVA_CERT_LOCATION_NAME)) {
			cert_location = parameter_value;
		} else {
			log_debug("leaving SSL_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);
			return super.parameter_set(parameter_name, parameter_value);
		}
		log_debug("leaving SSL_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);
		return true;
	}

	@Override
	protected void listen_port_opened(int port_number) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void client_connection_opened(int client_id) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void message_incoming(byte[] message_buffer, int length, int client_id) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void Add_Fd_Read_Handler(SelectableChannel fd) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void Add_Fd_Write_Handler(SelectableChannel fd) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void Remove_Fd_Read_Handler(SelectableChannel fd) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void Remove_Fd_Write_Handler(SelectableChannel fd) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void Remove_Fd_All_Handlers(SelectableChannel fd) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void Handler_Uninstall() {
		// TODO Auto-generated method stub

	}

	private String[] getProtocolsString() {
		List<String> protocols = new ArrayList<>();
		if (SSLv3) {
			protocols.add("SSLv3");
		}
		if (TLSv1) {
			protocols.add("TLSv1");
		}
		if (TLSv1_1) {
			protocols.add("TLSv1.1");
		}
		if (TLSv1_2) {
			protocols.add("TLSv1.2");
		}
		if (TLSv1_3) {
			protocols.add("TLSv1.3");
		}
		return (String[])protocols.toArray();
	}
}
