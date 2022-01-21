/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.runtime.core.TTCN_Buffer;
import org.eclipse.titan.runtime.core.TTCN_Logger;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;
import org.eclipse.titan.runtime.core.TtcnError;

/**
 * @author Gergo Ujhelyi
 */
public abstract class Abstract_Socket {

	protected enum TCP_STATES { CLOSED, LISTEN, ESTABLISHED, CLOSE_WAIT, FIN_WAIT };

	protected enum READING_STATES { STATE_DONT_RECEIVE, STATE_WAIT_FOR_RECEIVE_CALLBACK, STATE_BLOCK_FOR_SENDING, STATE_DONT_CLOSE, STATE_NORMAL };

	private static final int AS_TCP_CHUNCK_SIZE = 4096;

	private static final int NI_MAXHOST = 1024;

	private static final int NI_MAXSERV = 32;

	private boolean halt_on_connection_reset_set;
	private boolean halt_on_connection_reset;
	private boolean client_TCP_reconnect;
	private int TCP_reconnect_attempts;
	private int TCP_reconnect_delay;
	private boolean server_mode;
	private boolean use_connection_ASPs;
	private boolean handle_half_close;
	private boolean socket_debugging;
	private boolean nagling;
	private boolean use_non_blocking_socket;
	private boolean ttcn_buffer_usercontrol;
	private String local_host_name;
	private int local_port_number;
	private String remote_host_name;
	private int remote_port_number;
	private int server_backlog;
	private ServerSocketChannel listen_fd;

	protected String test_port_type;
	protected String test_port_name;

	private List<as_client_struct> peer_list_root;

	/********************************
	 **  Abstract_Socket
	 **  abstract base type for TCP socket handling
	 *********************************/
	public Abstract_Socket() {
		server_mode = false;
		socket_debugging = true;
		nagling = false;
		use_non_blocking_socket = false;
		halt_on_connection_reset = true;
		halt_on_connection_reset_set = false;
		client_TCP_reconnect = false;
		TCP_reconnect_attempts = 5;
		TCP_reconnect_delay = 1;
		server_backlog = 1;
		local_host_name = null;
		local_port_number = 0;
		remote_host_name = null;
		remote_port_number = 0;
		test_port_type = null;
		test_port_name = null;
		ttcn_buffer_usercontrol = false;
		use_connection_ASPs = false;
		handle_half_close = false;
		peer_list_root = null;
	}

	public Abstract_Socket(String test_port_type, String test_port_name) {
		this.test_port_type = test_port_type;
		this.test_port_name = test_port_name;
		server_mode = false;
		socket_debugging = false;
		nagling = false;
		use_non_blocking_socket = false;
		halt_on_connection_reset = true;
		halt_on_connection_reset_set = false;
		client_TCP_reconnect = false;
		TCP_reconnect_attempts = 5;
		TCP_reconnect_delay = 1;
		server_backlog = 1;
		local_host_name = null;
		local_port_number = 0;
		remote_host_name = null;
		remote_port_number = 0;
		ttcn_buffer_usercontrol = false;
		use_connection_ASPs = false;
		handle_half_close = false;
		peer_list_root = null;
	}

	////////////////////////////////////////////////////////////////////////
	/////    Default log functions
	////////////////////////////////////////////////////////////////////////
	protected void log_debug(final String fmt, final Object... args) {
		if (socket_debugging) {
			TTCN_Logger.begin_event(Severity.DEBUG_TESTPORT);
			if (test_port_type != null && test_port_name != null) {
				TTCN_Logger.log_event("%s test port (%s): ", test_port_type, test_port_name);
			} else {
				TTCN_Logger.log_event_str("Abstract socket: ");
			}
			TTCN_Logger.log_event_va_list(fmt, args);
			TTCN_Logger.end_event();
		}
	}

	protected void log_warning(final String fmt, final Object... args) {
		TTCN_Logger.begin_event(Severity.WARNING_UNQUALIFIED);
		if (test_port_type != null && test_port_name != null) {
			TTCN_Logger.log_event("%s test port (%s): warning: ", test_port_type, test_port_name);
		} else {
			TTCN_Logger.log_event_str("Abstract socket: warning: ");
		}
		TTCN_Logger.log_event_va_list(fmt, args);
		TTCN_Logger.end_event();
	}

	protected void log_error(final String fmt, final Object... args) {
		String error_str = String.format(fmt, args);
		try {
			if (test_port_type != null && test_port_name != null) {
				throw new TtcnError(String.format("%s test port (%s): %s", test_port_type, test_port_name, error_str));
			} else {
				throw new TtcnError(String.format("Abstract socket: %s", error_str));
			}
		} catch (TtcnError e) {
			error_str = null;
			throw e;
		}
	}

	protected void log_hex(final String prompt, final byte[] msg, int length) {
		if (socket_debugging) {
			TTCN_Logger.begin_event(Severity.DEBUG_TESTPORT);
			if (test_port_type != null && test_port_name != null) {
				TTCN_Logger.log_event("%s test port (%s): ", test_port_type, test_port_name);
			} else {
				TTCN_Logger.log_event_str("Abstract socket: ");
			}
			if (prompt != null) {
				TTCN_Logger.log_event_str(prompt);
			}
			TTCN_Logger.log_event("Size: %d, Msg:", length);
			for (int i = 0; i < length; i++) {
				TTCN_Logger.log_event(" %02x", msg[i]);
			}
			TTCN_Logger.end_event();
		}
	}

	// Shall be called from set_parameter()
	protected boolean parameter_set(final String parameter_name, final String parameter_value) {
		log_debug("entering Abstract_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);

		if (parameter_name.equals(socket_debugging_name())) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				socket_debugging = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				socket_debugging = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, socket_debugging_name());
			}
		} else if (parameter_name.equals(server_mode_name())) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				server_mode = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				server_mode = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, server_mode_name());
			}
		} else if (parameter_name.equals(use_connection_ASPs_name())) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				use_connection_ASPs = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				use_connection_ASPs = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, use_connection_ASPs_name());
			}
		} else if (parameter_name.equals(halt_on_connection_reset_name())) {
			halt_on_connection_reset_set = true;
			if (parameter_value.equalsIgnoreCase("yes")) {
				halt_on_connection_reset = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				halt_on_connection_reset = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, halt_on_connection_reset_name());
			}
		} else if (parameter_name.equals(client_TCP_reconnect_name())) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				client_TCP_reconnect = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				client_TCP_reconnect = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, client_TCP_reconnect_name());
			}
		} else if (parameter_name.equals(TCP_reconnect_attempts_name())) {
			try {
				TCP_reconnect_attempts = Integer.valueOf(parameter_value);
			} catch (NumberFormatException e) {
				log_error("Invalid input as TCP_reconnect_attempts counter given: %s", parameter_value);
			}
			if (TCP_reconnect_attempts <= 0) {
				log_error("TCP_reconnect_attempts must be greater than 0, %d is given", TCP_reconnect_attempts);
			}
		} else if (parameter_name.equals(TCP_reconnect_delay_name())) {
			try {
				TCP_reconnect_delay = Integer.valueOf(parameter_value);
			} catch (NumberFormatException e) {
				log_error("Invalid input as TCP_reconnect_delay given: %s", parameter_value);
			}
			if (TCP_reconnect_delay < 0) {
				log_error("TCP_reconnect_delay must not be less than 0, %d is given", TCP_reconnect_delay);
			}
		} else if (parameter_name.equals(remote_address_name())) {
			remote_host_name = parameter_value;
		} else if (parameter_name.equals(local_address_name())) {
			local_host_name = parameter_value;
		} else if (parameter_name.equals(remote_port_name())) {
			try {
				int a = Integer.valueOf(parameter_value);
				if (a > 65535 || a < 0) {
					log_error("Port number must be between 0 and 65535, %d is given", a);
				} else {
					remote_port_number = a;
				}
			} catch (NumberFormatException e) {
				log_error("Invalid input as port number given: %s", parameter_value);
			}
		} else if (parameter_name.equals(local_port_name())) {
			try {
				int a = Integer.valueOf(parameter_value);
				if (a > 65535 || a < 0) {
					log_error("Port number must be between 0 and 65535, %d is given", a);
				} else {
					local_port_number = a;
				}
			} catch (NumberFormatException e) {
				log_error("Invalid input as port number given: %s", parameter_value);
			}
		} else if (parameter_name.equals(nagling_name())) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				nagling = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				nagling = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, nagling_name());
			}
		} else if (parameter_name.equals(use_non_blocking_socket_name())) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				use_non_blocking_socket = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				use_non_blocking_socket = false;
			}
		} else if (parameter_name.equals(server_backlog_name())) {
			try {
				server_backlog = Integer.valueOf(parameter_value);
			} catch (NumberFormatException e) {
				log_error("Invalid input as server backlog given: %s", parameter_value);
			}
		} else {
			log_debug("leaving Abstract_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);
			return false;
		}
		log_debug("leaving Abstract_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);
		return true;
	}

	// Shall be called from Handle_Event()
	protected void Handle_Socket_Event(final SelectableChannel fd, final boolean is_readable, final boolean is_writable, final boolean is_error) {
		log_debug("entering Abstract_Socket::Handle_Socket_Event(): fd: %s%s%s%s", fd.toString(), is_readable ? " readable" : "", is_writable ? " writable" : "", is_error ? " error" : "");

		if (!fd.equals(listen_fd) && (is_readable || is_writable) && get_peer(fd, false) != null &&  get_peer(fd, false).reading_state != READING_STATES.STATE_DONT_RECEIVE) { /* on server the connection requests are handled after the user messages */
			log_debug("receiving data");

			int messageLength = receive_message_on_fd(fd);
			if (messageLength == 0) { // peer disconnected
				as_client_struct client_data = get_peer(fd, false);
				log_debug("Abstract_Socket.Handle_Socket_Event(). Client %s closed connection.", fd.toString());
				switch (client_data.reading_state) {
				case STATE_BLOCK_FOR_SENDING:
					log_debug("Abstract_Socket.Handle_Socket_Event(): state is STATE_BLOCK_FOR_SENDING, don't close connection.");
					Remove_Fd_Read_Handler(fd);
					client_data.reading_state = READING_STATES.STATE_DONT_CLOSE;
					log_debug("Abstract_Socket.Handle_Socket_Event(): setting socket state to STATE_DONT_CLOSE");
					break;
				case STATE_DONT_CLOSE:
					log_debug("Abstract_Socket::Handle_Socket_Event(): state is STATE_DONT_CLOSE, don't close connection.");
					break;
				default:
					if((client_data.tcp_state == TCP_STATES.CLOSE_WAIT) || (client_data.tcp_state == TCP_STATES.FIN_WAIT)) {
						remove_client(fd);
						peer_disconnected(fd);
					} else {
						client_data.tcp_state = TCP_STATES.CLOSE_WAIT;
						Remove_Fd_Read_Handler(fd);
						peer_half_closed(fd);
					}
				}
			} else if (messageLength > 0) {
				as_client_struct client_data = get_peer(fd, false);
				if (socket_debugging) {
					InetSocketAddress clientAddr = client_data.clientAddr;
					log_debug("Message received from address %s:%d", clientAddr.getAddress().toString(), clientAddr.getPort());
				}
				log_hex("Message received, buffer content: ", client_data.fd_buff.get_data(), client_data.fd_buff.get_len());
				handle_message(fd);
			}
		}

		if (fd.equals(listen_fd) && is_readable) {
			// new connection request arrived
			log_debug("waiting for accept");
			// receiving new connection on the TCP server
			SocketChannel newclient_fd = null; 
			try {
				newclient_fd = listen_fd.accept();
			} catch (IOException e) {
				log_error("Cannot accept connection at port");
			}

			as_client_struct client_data = peer_list_add_peer(newclient_fd);
			Add_Fd_Read_Handler(newclient_fd); // Done here - as in case of error: remove_client expects the handler as added
			log_debug("Abstract_Socket.Handle_Socket_Event(). Handler set to other fd %s", newclient_fd);
			client_data.fd_buff = new TTCN_Buffer();
			client_data.clientAddr = (InetSocketAddress) newclient_fd.socket().getLocalSocketAddress();
			client_data.tcp_state = TCP_STATES.ESTABLISHED;
			client_data.reading_state = READING_STATES.STATE_NORMAL;

			if (add_user_data(newclient_fd)) {
				String hname = "";
				int clientPort = 0;

				hname = newclient_fd.socket().getInetAddress().toString();
				clientPort = newclient_fd.socket().getLocalPort();
				log_debug("Client %s connected from address %s/%d", newclient_fd.toString(), hname, clientPort);
				peer_connected(peer_list_root.indexOf(client_data), hname, clientPort);
				log_debug("Handle_Socket_Event updated with client %s ", newclient_fd.toString());

				try {
					newclient_fd.configureBlocking(use_non_blocking_socket);
				} catch (IOException e) {
					log_error("Set blocking mode failed.");
				}
			} else {
				remove_client(newclient_fd);
				peer_disconnected(newclient_fd);
			}
		}

		log_debug("leaving Abstract_Socket.Handle_Socket_Event()");
	}

	protected int receive_message_on_fd(SelectableChannel fd) {
		as_client_struct client_data = get_peer(fd, false);
		TTCN_Buffer recv_tb = client_data.fd_buff;
		ByteBuffer buf = ByteBuffer.allocate(AS_TCP_CHUNCK_SIZE);
		buf.clear();
		int messageLength = 0;
		try {
			messageLength = ((SocketChannel) fd).read(buf);
		} catch (IOException e) {
			log_warning("Error when reading the received TCP PDU: %s", e.getMessage());
			return 0;
		}

		if (messageLength == -1) {
			log_warning("The channel has reached end-of-stream");
			return 0;
		}

		if (messageLength == 0) {
			return messageLength;
		}

		buf.rewind();
		recv_tb.rewind();
		byte received[] = new byte[messageLength];
		buf.get(received, 0, messageLength);
		recv_tb.put_s(received);

		return messageLength;
	}

	protected int send_message_on_fd(final SelectableChannel client_id, final byte[] send_par) {
		get_peer(client_id, false);
		try {
			return ((SocketChannel) client_id).write(ByteBuffer.wrap(send_par));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	protected PacketHeaderDescr Get_Header_Descriptor() {
		return null;
	}

	/**
	 * Called after a peer is connectedd
	 * 
	 * Do nothing in here. Override in your testport implementation.
	 * 
	 * @param client_id - int
	 * @param host - String
	 * @param port - int
	 */
	protected void peer_connected(final int client_id, final String host, final int port) {

	}

	private void handle_message(final SelectableChannel client_id) {
		final PacketHeaderDescr head_descr = Get_Header_Descriptor();
		as_client_struct client_data = get_peer(client_id, false);
		TTCN_Buffer recv_tb = client_data.fd_buff;

		if (head_descr == null) {
			message_incoming(recv_tb.get_data(), recv_tb.get_len(), peer_list_root.indexOf(client_data));
			if (!ttcn_buffer_usercontrol) {
				recv_tb.clear();
			}
		} else {
			recv_tb.rewind();
			long valid_header_length = head_descr.Get_Valid_Header_Length();
			while (recv_tb.get_len() > 0) {
				if ((long)recv_tb.get_len() < valid_header_length) {
					// this is a message without a valid header
					// recv_tb->handle_fragment();
					return;
				}
				long message_length = head_descr.Get_Message_Length(recv_tb.get_data());
				if (message_length < valid_header_length) {
					// this is a message with a malformed length
					log_error("Malformed message: invalid length: %l. The length should be at least %l.", message_length, valid_header_length);
				}
				if ((long)recv_tb.get_len() < message_length) {
					// this is a fragmented message with a valid header
					// recv_tb->handle_fragment();
					return;
				}
				message_incoming(recv_tb.get_data(), recv_tb.get_len(), peer_list_root.indexOf(client_data));
				if (!ttcn_buffer_usercontrol) {
					recv_tb.set_pos((int)message_length);
					recv_tb.cut();
				}
			}
		}
		log_debug("leaving Abstract_Socket.handle_message()");
	}

	// Shall be called from user_map()
	protected void map_user() {
		log_debug("entering Abstract_Socket.map_user()");
		if (!use_connection_ASPs) {
			// If halt_on_connection_reset is not set explicitly
			// set it to the default value: true on clients, false on servers
			if (!halt_on_connection_reset_set) {
				if (local_port_number != 0) {
					halt_on_connection_reset = false;
				} else {
					halt_on_connection_reset = true;
				}
			}
		}

		all_mandatory_configparameters_present();

		String remotePort = String.format("%d", remote_port_number);
		String localPort = String.format("%d", local_port_number);

		if (!use_connection_ASPs) {
			if (server_mode) {
				open_listen_port(local_host_name, localPort);
			} else {
				open_client_connection(remote_host_name, remotePort, local_host_name, localPort);
			}
		}

		log_debug("leaving Abstract_Socket.map_user()");
	}

	protected int open_listen_port(final String localHostname, final String localServicename) {
		log_debug("Local address: %s/%s", (localHostname != null) ? localHostname : "UNSPEC", (localServicename != null) ? localServicename : "UNSPEC");
		/* Set up a socket to listen for connections. */
		InetAddress[] new_local_addrs;
		if (localHostname == null && localServicename != null) {
			try {
				new_local_addrs = InetAddress.getAllByName(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				log_warning("InetAddress.getAllByName : %s", e.getMessage());
				return -1;
			}
		} else if (localHostname != null && localServicename == null) {
			try {
				new_local_addrs = InetAddress.getAllByName(localHostname);
			} catch (UnknownHostException e) {
				log_warning("InetAddress.getAllByName : %s", e.getMessage());
				return -1;
			}
		} else if (localHostname != null && localServicename != null) {
			try {
				new_local_addrs = InetAddress.getAllByName(localHostname);
			} catch (UnknownHostException e) {
				log_warning("InetAddress.getAllByName : %s", e.getMessage());
				return -1;
			}
		} else {
			return -1;
		}

		if (socket_debugging) {
			/* count the returned addresses: */
			log_debug("Number of local addresses: %d\n", new_local_addrs.length);
		}

		for (int i = 0; i < new_local_addrs.length; i++) {
			try {
				listen_fd = ServerSocketChannel.open();
				log_debug("Waiting for connection via: %s\n", ((new_local_addrs[i] instanceof Inet4Address) ? "IPv4" : ((new_local_addrs[i] instanceof Inet6Address) ? "IPv6" : "unknown")));
			} catch (IOException e) {
				if (use_connection_ASPs) {
					listen_port_opened(-1);
					log_warning("Cannot open ServerSocketChannel when trying to open the listen port: %s", e.getMessage());
					return -1;
				} else {
					log_error("Setsockopt failed");
				}
			}

			/* Tell the system to allow local addresses to be reused. */
			try {
				listen_fd.socket().setReuseAddress(true);
			} catch (SocketException e) {
				try {
					listen_fd.close();
				} catch (IOException e1) {
					log_error("Setsockopt failed");
					listen_fd = null;
				}
				listen_fd = null;
				if (use_connection_ASPs) {
					log_warning("setReuseAddress failed when trying to open the listen port: %s", e.getMessage());
					listen_port_opened(-1);
					return -1;
				} else {
					log_error("Setsockopt failed");
				}
			}

			if (!nagling) {
				try {
					listen_fd.configureBlocking(false);
				} catch (IOException e) {
					log_error("Setsockopt failed: %s", e.getMessage());
				}
			}

			log_debug("Bind to port...");
			try {
				if (localServicename != null) {
					listen_fd.bind(new InetSocketAddress(new_local_addrs[i], Integer.valueOf(localServicename)), server_backlog);
				} else {
					listen_fd.bind(new InetSocketAddress(new_local_addrs[i], 0), server_backlog);
				}
			} catch (NumberFormatException e) {
				try {
					listen_fd.close();
				} catch (IOException e1) {
					log_debug("Cannot bind to port when trying to open the listen port: %s", e1.getMessage());
					listen_fd = null;
					continue;
				}
				log_debug("Cannot bind to port when trying to open the listen port: %s", e.getMessage());
				listen_fd = null;
				continue;
			} catch (IOException e) {
				try {
					listen_fd.close();
				} catch (IOException e1) {
					log_debug("Cannot bind to port when trying to open the listen port: %s", e1.getMessage());
					listen_fd = null;
					continue;
				}
				log_debug("Cannot bind to port when trying to open the listen port: %s", e.getMessage());
				listen_fd = null;
				continue;
			}
			log_debug("Bind successful on server.");
			break;
		}

		if (listen_fd == null) {
			if(use_connection_ASPs) {
				log_warning("Cannot bind to port when trying to open the listen port");
				listen_port_opened(-1);
				return -1;
			}
			else log_error("Cannot bind to port");
		}

		try {
			listen_fd.accept();
		} catch (IOException e) {
			try {
				listen_fd.close();
			} catch (IOException e1) {
				listen_fd = null;
				if (use_connection_ASPs) {
					log_warning("Cannot listen at port when trying to open the listen port: %s", e1.getMessage());
					listen_port_opened(-1);
					return -1;
				}
				log_error("Cannot listen at port");
			}
			listen_fd = null;
			if (use_connection_ASPs) {
				log_warning("Cannot listen at port when trying to open the listen port: %s", e.getMessage());
				listen_port_opened(-1);
				return -1;
			}
			log_error("Cannot listen at port");
		}
		log_debug("Listening on (addr): %s/%s\n", listen_fd.socket().getInetAddress().toString(), listen_fd.socket().getLocalPort());

		Add_Fd_Read_Handler(listen_fd);
		log_debug("Abstract_Socket.open_listen_port(): Handler set to socket %s", listen_fd.socket().toString());

		log_debug("new_local_addr Addr family: %s\n", ((listen_fd.socket().getInetAddress() instanceof Inet4Address) ? "IPv4" : ((listen_fd.socket().getInetAddress() instanceof Inet6Address) ? "IPv6" : "unknown")));

		int listenPort = listen_fd.socket().getLocalPort();

		if (use_connection_ASPs) {
			listen_port_opened(listenPort);
		}

		return listenPort;
	}

	protected abstract void listen_port_opened(int port_number);

	protected void close_listen_port() {
		// close current listening port if it is alive
		if (listen_fd != null && listen_fd.isOpen()) {
			Remove_Fd_Read_Handler(listen_fd);
			try {
				listen_fd.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			log_debug("Closed listening port of fd: %s", listen_fd.toString());
			listen_fd = null;
		}
	}

	protected SelectableChannel get_socket_fd() {
		if (server_mode) {
			return listen_fd;
		}
		if (peer_list_get_nr_of_peers() == 0) {
			return null;
		}
		return peer_list_get_first_peer();
	}

	protected int open_client_connection(final String remoteHostname, final String remoteService, final String localHostname, final String localService) {
		log_debug("Abstract_Socket.open_client_connection(remoteAddr: %s/%s, localAddr: %s/%s) called", remoteHostname, remoteService, (localHostname != null) ? localHostname : "UNSPEC", (localService != null) ? localService : "UNSPEC");

		InetSocketAddress new_remote_addr = new InetSocketAddress(remoteHostname, Integer.valueOf(remoteService));
		InetSocketAddress new_local_addr = null;

		log_debug("Remote address: %s:%d", new_remote_addr.getAddress(), new_remote_addr.getPort());

		//Check every possibility to get a valid local address.
		if (localHostname != null && localService != null) {
			new_local_addr = new InetSocketAddress(localHostname, Integer.valueOf(localService));
		} else if (localHostname == null && localService != null) {
			try {
				new_local_addr = new InetSocketAddress(InetAddress.getLocalHost(), Integer.valueOf(localService));
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (localHostname != null && localService == null) {
			new_local_addr = new InetSocketAddress(localHostname, 0);
		} else if (localHostname == null && localService == null) {
			new_local_addr = new InetSocketAddress(0);
		}

		int TCP_reconnect_counter = TCP_reconnect_attempts;
		SocketChannel socket_fd = null;

		try {
			socket_fd = SocketChannel.open();
		} catch (IOException e) {
			if (use_connection_ASPs) {
				log_warning("Cannot open socket when trying to open client connection: %s", e.getMessage());
				client_connection_opened(-1);
				return -1;
			} else {
				log_error("Cannot open socket: %s", e.getMessage());
			}
		}

		if (!nagling) {
			try {
				socket_fd.setOption(StandardSocketOptions.TCP_NODELAY, true);
			} catch (IOException e) {
				if (use_connection_ASPs) {
					log_warning("setOption(TCP_NODELAY) failed when trying to open client connection: %s", e.getMessage());
					client_connection_opened(-1);
					return -1;
				} else {
					log_error("setOption(TCP_NODELAY) failed when trying to open client connection: %s", e.getMessage());
				}
			}
		}

		// when using client mode there is no separate file_desriptor for listening and target
		log_debug("Connecting to server from address %s:%d", new_local_addr.getHostString(), new_local_addr.getPort());

		if (new_local_addr.getPort() != 0) { // specific port to use
			try {
				socket_fd.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			} catch (IOException e) {
				if(use_connection_ASPs) {
					log_warning("setOption(SO_REUSEADDR) failed when trying to open client connection: %s", e.getMessage());
					client_connection_opened(-1);
					return -1;
				} else {
					log_error("setOption(SO_REUSEADDR) failed.");
				}
			}
			try {
				socket_fd.bind(new_local_addr);
			} catch (IOException e) {
				if(use_connection_ASPs) {
					log_warning("Cannot bind to port when trying to open client connection: %s", e.getMessage());
					client_connection_opened(-1);
					return -1;
				} else { 
					log_error("Cannot bind to port.");
				}
			}
			log_debug("Bind successful on client.");
		}

		try {
			socket_fd.connect(new_remote_addr);
			socket_fd.finishConnect();
			if (client_TCP_reconnect && socket_fd.isConnectionPending()) {
				while(!socket_fd.isConnectionPending() || TCP_reconnect_counter < 0) {
					try {
						Thread.sleep(TCP_reconnect_delay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					socket_fd.finishConnect();
					TCP_reconnect_counter--;
				}
				if (socket_fd.isConnectionPending()) {
					if(use_connection_ASPs) {
						log_warning("Already tried %d times, giving up when trying to open client connection.", TCP_reconnect_attempts - TCP_reconnect_counter);
						client_connection_opened(-1);
						return -1;
					} else {
						log_error("Already tried %d times, giving up", TCP_reconnect_attempts - TCP_reconnect_counter);
					}
				}
			}
		} catch (IOException e) {
			if(use_connection_ASPs) {
				log_warning("Cannot connect to server when trying to open client connection: %s", e.getMessage());
				client_connection_opened(-1);
				return -1;
			} else {
				log_error("Cannot connect to server");
			}
		} catch (UnresolvedAddressException e) {
			if(use_connection_ASPs) {
				log_warning("Cannot connect to server when trying to open client connection: %s", e.getMessage());
				client_connection_opened(-1);
				return -1;
			} else {
				log_error("Cannot connect to server");
			}
		}

		try {
			socket_fd.configureBlocking(false);
		} catch (IOException e) {
			try {
				socket_fd.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				if (use_connection_ASPs){
					client_connection_opened(-1);
					return -1;
				} else { 
					log_error("Set blocking mode failed.");
				}
			}
			if (use_connection_ASPs){
				client_connection_opened(-1);
				return -1;
			} else { 
				log_error("Set blocking mode failed.");
			}
		}

		as_client_struct client_data = peer_list_add_peer(socket_fd);
		Add_Fd_Read_Handler(socket_fd);
		log_debug("Abstract_Socket.open_client_connection(). Handler set to socket fd %s", socket_fd.toString());
		client_data.fd_buff = new TTCN_Buffer();
		client_data.clientAddr = new_remote_addr;
		client_data.tcp_state = TCP_STATES.ESTABLISHED;
		client_data.reading_state = READING_STATES.STATE_NORMAL;
		if (!add_user_data(socket_fd)) {
			remove_client(socket_fd);
			peer_disconnected(socket_fd);
			return -1;
		}

		client_connection_opened(peer_list_root.size() - 1);
		return (peer_list_root.size() - 1);
	}

	protected abstract void client_connection_opened(int client_id);

	protected void unmap_user()
	{
		log_debug("entering Abstract_Socket.unmap_user()");
		remove_all_clients();
		close_listen_port();
		Handler_Uninstall(); // For robustness only
		log_debug("leaving Abstract_Socket.unmap_user()");
	}

	/**
	 * Called after a peer is disconnected.
	 * 
	 * peer_disconnected() needs to be overriden in test ports!
	 */
	protected void peer_disconnected(SelectableChannel fd) {
		if (!use_connection_ASPs) {
			if (halt_on_connection_reset) {
				log_error("Connection was interrupted by the other side.");
			}
			if (client_TCP_reconnect) {
				log_warning("TCP connection was interrupted by the other side, trying to reconnect again...");
				unmap_user();
				map_user();
				log_warning("TCP reconnect successfuly finished");
			}
		}
	}

	/**
	 * Called when a peer shut down its fd for writing
	 * 
	 * @param fd - SelectableChannel
	 */
	protected void peer_half_closed(SelectableChannel fd) {
		log_debug("Entering Abstract_Socket.peer_half_closed()");
		remove_client(fd);
		peer_disconnected(fd);
		log_debug("Leaving Abstract_Socket.peer_half_closed()");
	}

	protected void send_shutdown(final SocketChannel client_id) {
		log_debug("entering Abstract_Socket.send_shutdown()");
		SocketChannel dest_fd = client_id;

		if (dest_fd == null) {
			if (peer_list_get_nr_of_peers() > 1) {
				log_error("Client Id not specified altough not only 1 client exists");
			} else if (peer_list_get_nr_of_peers() == 0) {
				log_error("There is no connection alive, connect before sending anything.");
			}
			dest_fd = peer_list_get_first_peer();
		}
		as_client_struct client_data = get_peer(dest_fd, false);
		if (client_data.tcp_state != TCP_STATES.ESTABLISHED) {
			log_error("TCP state of client %s does not allow to shut down its connection for writing!", dest_fd.toString());
		}

		try {
			dest_fd.shutdownOutput();
			remove_client(dest_fd);
			peer_disconnected(dest_fd);
		} catch (IOException e) {
			log_error("shutdownOutput() call failed");
		}
		client_data.tcp_state = TCP_STATES.FIN_WAIT;

		// dest_fd is not removed from readfds, data can be received

		log_debug("leaving Abstract_Socket.send_shutdown()");
	}

	protected void send_outgoing(final byte[] message_buffer, final int length, final int client_id) {
		log_debug("entering Abstract_Socket::send_outgoing()");
		log_hex("Sending data: ", message_buffer, length);
		SocketChannel dest_socket_channel = null;
		int nrOfBytesSent = 0;

		int dest_fd = client_id;
		if (dest_fd == -1) {
			if (peer_list_get_nr_of_peers() > 1) {
				log_error("Client Id not specified altough not only 1 client exists");
			} else if (peer_list_get_nr_of_peers() == 0) {
				log_error("There is no connection alive, use a Connect ASP before sending anything.");
			}
			dest_socket_channel = peer_list_get_first_peer();
		}
		as_client_struct client_data = dest_socket_channel != null ? get_peer(dest_socket_channel, true) : get_peer(client_id, true);
		dest_socket_channel = client_data.tcp_socket;
		if (client_data == null || ((client_data.tcp_state != TCP_STATES.ESTABLISHED) && (client_data.tcp_state != TCP_STATES.CLOSE_WAIT))) {
			String error_text = String.format("client nr %d has no established connection", client_id);
			System.err.println(error_text);
			log_debug("leaving Abstract_Socket.send_outgoing()");
			return;
		}

		nrOfBytesSent = send_message_on_fd(dest_socket_channel, message_buffer);

		if (nrOfBytesSent == -1) {
			log_debug("Client %d closed connection.", client_id);
			report_unsent(dest_fd, length, nrOfBytesSent, message_buffer, "Client closed the connection");

			if (client_data.tcp_state == TCP_STATES.CLOSE_WAIT) {
				log_debug("Client %d waiting for close ASP.", client_id);
			} else {
				log_debug("Client %d closed connection", client_id);
				client_data.tcp_state = TCP_STATES.CLOSE_WAIT;
				Remove_Fd_Read_Handler(dest_socket_channel);
				peer_half_closed(dest_socket_channel);
			}
		} else if (nrOfBytesSent != length) {
			final String error_text = String.format("Send system call failed: %d bytes were sent instead of %d", nrOfBytesSent, length);
			report_error(client_id, length, nrOfBytesSent, message_buffer, error_text);
		} else {
			log_debug("Nr of bytes sent = %d", nrOfBytesSent);
		}
		log_debug("leaving Abstract_Socket.send_outgoing()");
	}

	protected void report_error(final int client_id, final int msg_length, final int sent_length, byte[] msg, final String error_text) {
		log_error("%s",error_text);
	}

	protected void report_unsent(final int client_id, final int msg_length, final int sent_length, byte[] msg, final String error_text) {
		log_debug("%s",error_text);
	}

	private void all_mandatory_configparameters_present() {
		if(!use_connection_ASPs) {
			if(server_mode) {
				if(local_port_number == 0) {
					log_error("%s is not defined in the configuration file", local_port_name());
				}
			} else { // client mode
				if (remote_host_name == null) {
					log_error("%s is not defined in the configuration file", remote_address_name());
				}
				if(remote_port_number == 0){
					log_error("%s is not defined in the configuration file", remote_port_name());
				}
			}
		}
		user_all_mandatory_configparameters_present();
	}

	// Called when a client shall be removed
	protected void remove_client(SelectableChannel fd) {
		log_debug("entering Abstract_Socket.remove_client(%s)", fd.toString());
		if (!fd.equals(listen_fd)) {
			get_peer(fd, false);
			Add_Fd_Read_Handler(fd);
			Remove_Fd_All_Handlers(fd);
			remove_user_data(fd);
			get_peer(fd, false).fd_buff = null;
			peer_list_remove_peer(fd);
			try {
				fd.close();
			} catch (IOException e) {
				log_error("Abstract_Socket.remove_client: error in channel closing: %s", e.getMessage());
			}
			log_debug("Removed client %s.", fd);
		} else {
			log_warning("Abstract_Socket.remove_client: %s is the server listening port, can not be removed!", fd.toString());
		}
		log_debug("leaving Abstract_Socket.remove_client(%s)", fd);
	}

	// Called when all clients shall be removed
	protected void remove_all_clients() {
		log_debug("entering Abstract_Socket.remove_all_clients");
		if (peer_list_root != null) {
			for (int i = 0; i < peer_list_root.size(); i++) {
				if (peer_list_root.get(i) != null && peer_list_root.get(i).tcp_socket != null) {
					remove_client(peer_list_root.get(i).tcp_socket);
				}
			}

			while (!peer_list_root.isEmpty()) {
				SocketChannel client_id = peer_list_get_first_peer();
				if (client_id != null) {
					log_warning("Client %d has not been removed, programming error", client_id);
				} else {
					log_error("Number of clients<>0 but cannot get first client, programming error");
				}
				peer_list_remove_peer(client_id);
			}
		}
	}

	protected int get_clientId_by_fd(final SelectableChannel fd) {
		for (int i = 0; i < peer_list_root.size(); i++) {
			if (peer_list_root.get(i).tcp_socket.equals(fd)) {
				return i;
			}
		}
		return -1;
	}

	protected void set_ttcn_buffer_usercontrol(final boolean parameter_value) {
		ttcn_buffer_usercontrol = parameter_value;
	}

	protected void set_handle_half_close(final boolean parameter_value) {
		handle_half_close = parameter_value;
	}

	protected void set_server_mode(final boolean parameter_value) {
		server_mode = parameter_value;
	}

	protected boolean get_nagling() {
		return nagling;
	}

	protected boolean get_use_non_blocking_socket() {
		return use_non_blocking_socket;
	}

	protected boolean get_server_mode() {
		return server_mode;
	}

	protected boolean get_socket_debugging() {
		return socket_debugging;
	}

	protected boolean get_halt_on_connection_reset() {
		return halt_on_connection_reset;
	}

	protected boolean get_use_connection_ASPs() {
		return use_connection_ASPs;
	}

	protected boolean get_handle_half_close() {
		return handle_half_close;
	}

	protected String local_port_name() { 
		return "serverPort";
	}

	protected String remote_address_name() { 
		return "destIPAddr";
	}

	protected String local_address_name() {
		return "serverIPAddr";
	}

	protected String remote_port_name() {
		return "destPort";
	}

	protected String ai_family_name() {
		return "ai_family";
	}

	protected String use_connection_ASPs_name() {
		return "use_connection_ASPs";
	}

	protected String halt_on_connection_reset_name() {
		return "halt_on_connection_reset";
	}

	protected String client_TCP_reconnect_name() { 
		return "client_TCP_reconnect";
	}

	protected String TCP_reconnect_attempts_name() {
		return "TCP_reconnect_attempts";
	}

	protected String TCP_reconnect_delay_name() {
		return "TCP_reconnect_delay";
	}

	protected String server_mode_name() {
		return "server_mode";
	}

	protected String socket_debugging_name() {
		return "socket_debugging";
	}

	protected String nagling_name() {
		return "nagling";
	}

	protected String use_non_blocking_socket_name() {
		return "use_non_blocking_socket";
	}

	protected String server_backlog_name() {
		return "server_backlog";
	}

	protected boolean add_user_data(SelectableChannel id) {
		return true;
	}

	protected boolean remove_user_data(SelectableChannel id) {
		return true;
	}

	protected boolean user_all_mandatory_configparameters_present() {
		return true;
	}

	protected TTCN_Buffer get_buffer(final int client_id) {
		return get_peer(client_id, false).fd_buff;
	}

	////////////////////////////////////////////////////////////////////////
	/////    Peer handling functions
	////////////////////////////////////////////////////////////////////////
	// add peer to the list
	protected as_client_struct peer_list_add_peer(final SocketChannel client_id) {
		if (client_id == null) {
			log_error("Invalid Client Id is given.");
		}
		if (peer_list_root == null) {
			peer_list_root = new ArrayList<>();
		}
		log_debug("Abstract_Socket.peer_list_add_peer: Adding client %s to peer list", client_id.toString());
		as_client_struct tmp_client_struct = new as_client_struct();
		tmp_client_struct.user_data = null;
		tmp_client_struct.fd_buff = null;
		tmp_client_struct.tcp_state = TCP_STATES.CLOSED;
		tmp_client_struct.reading_state = READING_STATES.STATE_NORMAL;
		tmp_client_struct.tcp_socket = client_id;
		peer_list_root.add(tmp_client_struct);

		return peer_list_root.get(peer_list_root.size() - 1);
	}

	// returns back the structure of the peer
	protected as_client_struct get_peer(final SelectableChannel client_fd, final boolean no_error) {
		for (int i = 0; i < peer_list_root.size(); i++) {
			if (peer_list_root.get(i).tcp_socket.equals(client_fd)) {
				return peer_list_root.get(i);
			}
		}
		if (!no_error) {
			log_error("Abstract_Socket.get_peer: Client %s does not exist", client_fd);
		}
		return null;
	}

	// returns back the structure of the peer
	protected as_client_struct get_peer(final int client_fd, final boolean no_error) {
		return peer_list_root.get(client_fd);
	}

	// number of peers in the list
	protected int peer_list_get_nr_of_peers() {
		int nr = 0;
		nr = peer_list_root.size();

		log_debug("Abstract_Socket.peer_list_get_nr_of_peers: Number of active peers = %d", nr);
		return nr;
	}

	// channel of the first peer in the list
	protected SocketChannel peer_list_get_first_peer() {
		log_debug("Abstract_Socket.peer_list_get_first_peer: Finding first peer of the peer array");
		for (int i = 0; i < peer_list_root.size(); i++) {
			if (peer_list_root.get(i) != null) {
				log_debug("Abstract_Socket.peer_list_get_first_peer: First peer is %d", i);
				return peer_list_root.get(i).tcp_socket;
			}
		}
		log_debug("Abstract_Socket.peer_list_get_first_peer: No active peer found");
		return null; // this indicates an empty list
	}

	protected SocketChannel peer_list_get_last_peer() {
		log_debug("Abstract_Socket.peer_list_get_last_peer: Finding last peer of the peer array");
		if (peer_list_root.isEmpty()) {
			log_debug("Abstract_Socket.peer_list_get_last_peer: No active peer found");
			return null;
		}
		if (peer_list_root.get(peer_list_root.size() - 1) != null && peer_list_root.get(peer_list_root.size() - 1).tcp_socket != null) {
			log_debug("Abstract_Socket.peer_list_get_last_peer: Last peer is %s", peer_list_root.get(peer_list_root.size() - 1).tcp_socket.toString());
			return peer_list_root.get(peer_list_root.size() - 1).tcp_socket;
		}
		log_debug("Abstract_Socket.peer_list_get_last_peer: No active peer found");
		return null;
	}

	protected void peer_list_remove_peer(final int client_id) {
		log_debug("Abstract_Socket.peer_list_remove_peer: Removing client %d from peer list", client_id);
		if (client_id >= peer_list_root.size() || client_id < 0) {
			log_error("Invalid Client Id is given: %d.", client_id);
		}
		if (peer_list_root.get(client_id) == null) {
			log_error("Peer %d does not exist.", client_id);
		}
		peer_list_root.remove(client_id);
	}

	protected void peer_list_remove_peer(final SelectableChannel fd) {
		if (peer_list_root != null && !peer_list_root.isEmpty()) {
			for (int i = 0; i < peer_list_root.size(); i++) {
				if (peer_list_root.get(i).tcp_socket.equals(fd)) {
					peer_list_remove_peer(i);
				}
			}
		}
	}

	// Called when a message is received
	protected abstract void message_incoming(final byte[] message_buffer, final int length, final int client_id);

	protected abstract void Add_Fd_Read_Handler(SelectableChannel fd);
	protected abstract void Add_Fd_Write_Handler(SelectableChannel fd);
	protected abstract void Remove_Fd_Read_Handler(SelectableChannel fd);
	protected abstract void Remove_Fd_Write_Handler(SelectableChannel fd);
	protected abstract void Remove_Fd_All_Handlers(SelectableChannel fd);
	protected abstract void Handler_Uninstall();

	public static class as_client_struct {
		public byte[] user_data;
		public TTCN_Buffer fd_buff;
		public InetSocketAddress clientAddr;
		public TCP_STATES tcp_state;
		public READING_STATES reading_state;
		public SocketChannel tcp_socket;

		public as_client_struct() {} 

		public as_client_struct(byte[] user_data, TTCN_Buffer fd_buff, InetSocketAddress clientAddr, TCP_STATES tcp_state, READING_STATES reading_state) {
			this.user_data = user_data;
			this.fd_buff = fd_buff;
			this.clientAddr = clientAddr;
			this.tcp_state = tcp_state;
			this.reading_state = reading_state;
		}
	}
}