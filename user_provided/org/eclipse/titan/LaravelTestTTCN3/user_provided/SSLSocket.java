package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 
 * @author Gergo Ujhelyi
 *
 */
public class SSLSocket extends Abstract_Socket {

	// Parameter Constant variables instead of a function call
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
	// Java specific parameter name
	protected static final String JAVA_KEYSTORE_PASSWORD_NAME = "java_keystore_password";
	protected static final String JAVA_KEYSTORE_TYPE_NAME = "java_keystore_type";
	protected static final String JAVA_KEYSTORE_ALGORITHM_NAME = "java_keystore_algorithm";
	protected static final String JAVA_TRUSTSTORE_PASSWORD_NAME = "java_truststore_password";
	protected static final String JAVA_TRUSTSTORE_TYPE_NAME = "java_truststore_type";
	protected static final String JAVA_TRUSTSTORE_ALGORITHM_NAME = "java_truststore_algorithm";
	protected static final String JAVA_CERT_LOCATION_NAME = "java_cert_files_location";
	protected static final String JAVA_KEYSTORE_FILE = "java_keystore_file";
	protected static final String JAVA_TRUSTSTORE_FILE = "java_truststore_file";

	private static final String KEYSTORE_DEFAULT_PASSWORD = "password";
	private static final String DEFAULT_CERT_FILES_LOCATION = System.getProperty("user.dir") + File.separator + "resources" + File.separator;

	private static final String DEFAULT_KEYSTORE_FILE_NAME = "mykeystore." + KeyStore.getDefaultType();
	private static final String DEFAULT_TRUSTSTORE_FILE_NAME = "mytruststore." + KeyStore.getDefaultType();

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
	private String keystore_file;

	private String truststore_password;
	private String truststore_type;
	private String truststore_algorithm;
	private String truststore_file;

	private KeyStore ks_keys; // KeyStore
	private KeyStore ts_keys; // TrustStore

	private ByteBuffer my_app_data;
	private ByteBuffer my_net_data;
	private ByteBuffer peer_app_data;
	private ByteBuffer peer_net_data;

	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

	public SSLSocket() {
		super();
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
		super(tp_type, tp_name);
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
	}

	@Override
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
		} else if (parameter_name.equals(JAVA_KEYSTORE_FILE)) {
			keystore_file = parameter_value;
		} else if (parameter_name.equals(JAVA_TRUSTSTORE_FILE)) {
			truststore_file = parameter_value;
		} else {
			log_debug("leaving SSL_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);
			return super.parameter_set(parameter_name, parameter_value);
		}
		log_debug("leaving SSL_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);
		return true;
	}

	/**
	 *  Called after a TCP connection is established (client side or server accepted a connection).
	 *  It will create a new SSL conenction on the top of the TCP connection.
	 */
	@Override
	protected boolean add_user_data(SelectableChannel id) {
		log_debug("entering SSL_Socket.add_user_data()");
		if (!ssl_use_ssl) {
			log_debug("leaving SSL_Socket.add_user_data()");
			return super.add_user_data(id);
		}

		ssl_init_SSL();

		log_debug("Create a new SSL object");

		if (ssl_ctx == null) {
			log_error("Creation of SSL object failed");
		}

		set_user_data(id, ssl_engine);
		log_debug("New client added with key '%s'", id.toString());
		log_debug("Binding SSL to the socket");


		return false;
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

	/**
	 * Initialize SSL libraries and create the SSL context.
	 * <ol>
	 * <li> Init SSL context</li>
	 * <li> Init Java KeyStore</li>
	 * <li> Load Keys to KeyStore</li>
	 * <li> Init Java TrustStore(KeyStore)</li>
	 * </ol>
	 */
	private void ssl_init_SSL() {
		if (ssl_initialized) {
			log_debug("SSL already initialized, no action needed");
			return;
		}
		log_debug("Init SSL started");

		//Init SSL context
		if (!TLSv1_3) {
			try {
				ssl_ctx = SSLContext.getDefault();
			} catch (NoSuchAlgorithmException e) {
				log_error("SSL context creation failed: %s", e.getMessage());
			}
		} else {
			try {
				ssl_ctx = SSLContext.getInstance("TLSv1.3");
			} catch (NoSuchAlgorithmException e) {
				log_error("SSL context creation failed: %s", e.getMessage());
			}
		}

		// Init Java KeyStore
		try {
			ks_keys = KeyStore.getInstance((keystore_type == null || keystore_type.isEmpty()) ? KeyStore.getDefaultType() : keystore_type );
		} catch (KeyStoreException e) {
			log_error("Error during initializing key store: %s", e.getMessage());
		}
		File keyFile = new File((keystore_file == null || keystore_file.isEmpty()) ? DEFAULT_CERT_FILES_LOCATION + DEFAULT_KEYSTORE_FILE_NAME : keystore_file);
		if (!keyFile.exists()) {
			//Create keyfile to default location with default name
			try {
				ks_keys.load(null, (keystore_password == null || keystore_password.isEmpty()) ? KEYSTORE_DEFAULT_PASSWORD.toCharArray() : keystore_password.toCharArray());
			} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
				log_error("Error during initializing key store: %s", e.getMessage());
			}
			FileOutputStream keyStoreOutputStream = null;
			try {
				keyStoreOutputStream = new FileOutputStream(keyFile, true);
			} catch (FileNotFoundException e) {
				log_error("Error during initializing key store: %s", e.getMessage());
			}
			try {
				ks_keys.store(keyStoreOutputStream, (keystore_password == null || keystore_password.isEmpty()) ? KEYSTORE_DEFAULT_PASSWORD.toCharArray() : keystore_password.toCharArray());
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
				log_error("Error during initializing key store: %s", e.getMessage());
			}
		} else {
			try {
				ks_keys.load(new FileInputStream(keyFile), (keystore_password == null || keystore_password.isEmpty()) ? KEYSTORE_DEFAULT_PASSWORD.toCharArray() : keystore_password.toCharArray());
			} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
				log_error("Error during initializing key store: %s", e.getMessage());
			}
		}

		// Init Java TrustStore
		try {
			ts_keys = KeyStore.getInstance((truststore_type == null || truststore_type.isEmpty()) ? KeyStore.getDefaultType() : truststore_type );
		} catch (KeyStoreException e) {
			log_error("Error during initializing trust key store: %s", e.getMessage());
		}
		keyFile = new File((truststore_file == null || truststore_file.isEmpty()) ? DEFAULT_CERT_FILES_LOCATION + DEFAULT_TRUSTSTORE_FILE_NAME : truststore_file);
		if (!keyFile.exists()) {
			//Create keyfile to default location with default name
			try {
				ts_keys.load(null, (truststore_password == null || truststore_password.isEmpty()) ? KEYSTORE_DEFAULT_PASSWORD.toCharArray() : truststore_password.toCharArray());
			} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
				log_error("Error during initializing trust key store: %s", e.getMessage());
			}
			FileOutputStream keyStoreOutputStream = null;
			try {
				keyStoreOutputStream = new FileOutputStream(keyFile, true);
			} catch (FileNotFoundException e) {
				log_error("Error during initializing trust key store: %s", e.getMessage());
			}
			try {
				ts_keys.store(keyStoreOutputStream, (truststore_password == null || truststore_password.isEmpty()) ? KEYSTORE_DEFAULT_PASSWORD.toCharArray() : truststore_password.toCharArray());
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
				log_error("Error during initializing trust key store: %s", e.getMessage());
			}
		} else {
			try {
				ts_keys.load(new FileInputStream(keyFile), (truststore_password == null || truststore_password.isEmpty()) ? KEYSTORE_DEFAULT_PASSWORD.toCharArray() : truststore_password.toCharArray());
			} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
				log_error("Error during initializing trust key store: %s", e.getMessage());
			}
		}

		//Init Java KeyManagerFactory
		KeyManagerFactory kmf = null;
		try {
			kmf =  KeyManagerFactory.getInstance((keystore_type == null || keystore_type.isEmpty()) ? KeyManagerFactory.getDefaultAlgorithm() : keystore_type);
		} catch (NoSuchAlgorithmException e) {
			log_error("Error during initializing key store manager: %s", e.getMessage());
		}
		try {
			kmf.init(ks_keys, (keystore_password == null || keystore_password.isEmpty()) ? KEYSTORE_DEFAULT_PASSWORD.toCharArray() : keystore_password.toCharArray());
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			log_error("Error during initializing key store manager: %s", e.getMessage());
		}

		//Init Java TrustManagerFactory
		TrustManagerFactory tmf = null;
		// If TrustStore is empty load default certificates
		log_debug("Check for consistency between private and public keys");
		try {
			if (ts_keys.size() == 0) {
				TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				defaultTmf.init((KeyStore) null);
				for (TrustManager trustManager : defaultTmf.getTrustManagers()) {
					if (trustManager instanceof X509TrustManager) {
						for (X509Certificate acceptedIssuer : ((X509TrustManager) trustManager).getAcceptedIssuers()) {
							ts_keys.setCertificateEntry(acceptedIssuer.getSubjectX500Principal().getName(), acceptedIssuer);
						}
					}
				}
			}
			tmf = TrustManagerFactory.getInstance((truststore_type == null || truststore_type.isEmpty()) ? TrustManagerFactory.getDefaultAlgorithm() : truststore_type);
			tmf.init(ts_keys);
		} catch (KeyStoreException | NoSuchAlgorithmException e) {
			log_error("Error during initializing trust manager: %s", e.getMessage());
		}
		try {
			ssl_ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		} catch (KeyManagementException e) {
			log_error("SSL context creation failed: %s", e.getMessage());
		}
		// Create SSLEngine
		ssl_engine = ssl_ctx.createSSLEngine();
		ssl_engine.setUseClientMode(!get_server_mode());
		ssl_session = ssl_engine.getSession();

		// Allocate buffers
		my_app_data = ByteBuffer.allocate(ssl_session.getApplicationBufferSize() + 30);
		my_net_data = ByteBuffer.allocate(ssl_session.getPacketBufferSize() + 30);
		peer_app_data = ByteBuffer.allocate(ssl_session.getApplicationBufferSize() + 30);
		peer_net_data = ByteBuffer.allocate(ssl_session.getPacketBufferSize() + 30);
	}


	private void ssl_handshake(final SocketChannel socketChannel) {
		log_debug("Start SSL Handshake");
		SSLEngineResult result = null;
		SSLEngineResult.HandshakeStatus handshakeStatus = null;
		final int applicationBufferSize = ssl_session.getApplicationBufferSize();

		ByteBuffer myAppData = ByteBuffer.allocate(applicationBufferSize);
		ByteBuffer peerAppData = ByteBuffer.allocate(applicationBufferSize);

		try {
			ssl_engine.beginHandshake();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		handshakeStatus = ssl_engine.getHandshakeStatus();

		while (handshakeStatus != HandshakeStatus.FINISHED && handshakeStatus != HandshakeStatus.NOT_HANDSHAKING) {
			switch (handshakeStatus) {
			case NEED_UNWRAP:
				try {
					if (socketChannel.read(peer_net_data) < 0) {
						// TODO close connection properly
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				peer_net_data.flip();
				try {
					result = ssl_engine.unwrap(peer_net_data, peerAppData);
				} catch (SSLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				peer_net_data.compact();
				handshakeStatus = result.getHandshakeStatus();
				// Check status
				switch (result.getStatus()) {
				case OK:
					// Nothing done
					break;
				case BUFFER_UNDERFLOW:
					if (ssl_engine.getSession().getPacketBufferSize() > peer_net_data.capacity()) {
						peer_net_data = ByteBuffer.allocate(ssl_session.getPacketBufferSize());
					} else {
						peer_net_data.clear();
					}
					break;
				case BUFFER_OVERFLOW:
					if (ssl_engine.getSession().getApplicationBufferSize() > peerAppData.capacity()) {
						peerAppData = ByteBuffer.allocate(ssl_engine.getSession().getApplicationBufferSize());
					} else {
						peerAppData.clear();
					}
					break;
				case CLOSED:
					if (!ssl_engine.isOutboundDone()) {
						ssl_engine.closeOutbound();
						handshakeStatus = ssl_engine.getHandshakeStatus();
					}
					break;
				default:
					throw new IllegalStateException("Invalid SSLEngineResult status");
				}
				break;
			case NEED_WRAP:
				my_net_data.clear();
				try {
					result = ssl_engine.wrap(myAppData, my_net_data);
				} catch (SSLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handshakeStatus = result.getHandshakeStatus();
				// Check status
				switch (result.getStatus()) {
				case OK:
					my_net_data.flip();
					while (my_net_data.hasRemaining()) {
						try {
							socketChannel.write(my_net_data);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				case BUFFER_OVERFLOW:
					// TODO implement
					break;
				case BUFFER_UNDERFLOW:
					// TODO implement
					break;
				case CLOSED:
					// TODO implement
					break;
				default:
					break;
				}
				break;
			case NEED_TASK:
				if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
					Runnable runnable;
					while ((runnable = ssl_engine.getDelegatedTask()) != null) {
						singleThreadExecutor.execute(runnable);
					}
					handshakeStatus = ssl_engine.getHandshakeStatus();
				}
				break;
			default:
				break;
			}
		}
		log_debug("SSL Handshake finished");

	}

	private ByteBuffer enlargeBuffer(final ByteBuffer buffer, int newLength) {
		if (newLength > buffer.capacity()) {
			ByteBuffer tempBuffer = ByteBuffer.allocate(newLength);
			buffer.flip();
			tempBuffer.put(buffer);
			return tempBuffer;
		}
		return buffer;
	}
}
