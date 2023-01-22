package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.nio.channels.SelectableChannel;
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
	}
	
	protected boolean parameter_set(final String parameter_name, final String parameter_value) {
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
