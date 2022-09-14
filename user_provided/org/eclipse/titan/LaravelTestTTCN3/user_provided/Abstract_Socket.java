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
 * The abstract class for TCP socket handling for TITAN Java test ports.
 * 
 * @author Gergo Ujhelyi
 */
public abstract class Abstract_Socket {

	public enum TCP_STATES { CLOSED, LISTEN, ESTABLISHED, CLOSE_WAIT, FIN_WAIT };

	public enum READING_STATES { STATE_DONT_RECEIVE, STATE_WAIT_FOR_RECEIVE_CALLBACK, STATE_BLOCK_FOR_SENDING, STATE_DONT_CLOSE, STATE_NORMAL };

	private static final int AS_TCP_CHUNCK_SIZE = 4096;

	public static final String LOCAL_PORT_NAME = "serverPort";
	public static final String REMOTE_ADDRESS_NAME = "destIPAddr";
	public static final String LOCAL_ADDRESS_NAME = "serverIPAddr";
	public static final String REMOTE_PORT_NAME =  "destPort";
	public static final String AI_FAMILY_NAME = "ai_family";
	public static final String USE_CONNECTION_ASPS_NAME = "use_connection_ASPs";
	public static final String HALT_ON_CONNECTION_RESET_NAME = "halt_on_connection_reset";
	public static final String CLIENT_TCP_RECONNECT_NAME = "client_TCP_reconnect";
	public static final String TCP_RECONNECT_ATTEMPTS_NAME = "TCP_reconnect_attempts";
	public static final String TCP_RECONNECT_DELAY_NAME = "TCP_reconnect_delay";
	public static final String SERVER_MODE_NAME = "server_mode";
	public static final String SOCKET_DEBUGGING_NAME = "socket_debugging";
	public static final String NAGLING_NAME = "nagling";
	public static final String USE_NON_BLOCKING_SOCKET_NAME = "use_non_blocking_socket";
	public static final String SERVER_BACKLOG_NAME = "server_backlog";

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

	/** 
	 * Constructs the class without test port name and test port type.
	 */
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

	/**
	 * Constructs the class with test port name and test port type.
	 * 
	 * @param test_port_type the type of the test port
	 * @param test_port_name the name of the test port
	 */
	public Abstract_Socket(final String test_port_type, final String test_port_name) {
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

	/**
	 * Default logging function.
	 * 
	 * @param fmt The format string.
	 * @param args The object's array what will be logged.
	 */
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

	/**
	 * Default warning logging function.
	 * 
	 * @param fmt The format string.
	 * @param args The object's array what will be logged.
	 */
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

	/**
	 * Default error logging function.
	 * 
	 * @param fmt The format string.
	 * @param args The object's array what will be logged.
	 * @throws TtcnError with the formatted string as error message.
	 */
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

	/**
	 * Logging function for incoming and outgoing messages.
	 * Log the message in hexadecimal format.
	 * 
	 * @param prompt The prompt string what is logged before message.
	 * @param msg The message in byte format.
	 * @param length The message length.
	 */
	protected void log_hex(final String prompt, final byte[] msg, final int length) {
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

	/**
	 * Set the class parameters' from a configuration file.
	 * Shall be called from <code>set_parameter</code> function.
	 * 
	 * @param parameter_name The parameter name.
	 * @param parameter_value The parameter value.
	 * @return <code>true</code> if the parameter name valid and and set to the given value.
	 * @throws TtcnError in <code>log_error</code> functions.
	 */
	protected boolean parameter_set(final String parameter_name, final String parameter_value) {
		log_debug("entering Abstract_Socket.parameter_set(%s, %s)", parameter_name, parameter_value);

		if (parameter_name.equals(SOCKET_DEBUGGING_NAME)) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				socket_debugging = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				socket_debugging = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SOCKET_DEBUGGING_NAME);
			}
		} else if (parameter_name.equals(SERVER_MODE_NAME)) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				server_mode = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				server_mode = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, SERVER_MODE_NAME);
			}
		} else if (parameter_name.equals(USE_CONNECTION_ASPS_NAME)) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				use_connection_ASPs = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				use_connection_ASPs = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, USE_CONNECTION_ASPS_NAME);
			}
		} else if (parameter_name.equals(HALT_ON_CONNECTION_RESET_NAME)) {
			halt_on_connection_reset_set = true;
			if (parameter_value.equalsIgnoreCase("yes")) {
				halt_on_connection_reset = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				halt_on_connection_reset = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, HALT_ON_CONNECTION_RESET_NAME);
			}
		} else if (parameter_name.equals(CLIENT_TCP_RECONNECT_NAME)) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				client_TCP_reconnect = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				client_TCP_reconnect = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, CLIENT_TCP_RECONNECT_NAME);
			}
		} else if (parameter_name.equals(TCP_RECONNECT_ATTEMPTS_NAME)) {
			try {
				TCP_reconnect_attempts = Integer.valueOf(parameter_value);
			} catch (NumberFormatException e) {
				log_error("Invalid input as TCP_reconnect_attempts counter given: %s", parameter_value);
			}
			if (TCP_reconnect_attempts <= 0) {
				log_error("TCP_reconnect_attempts must be greater than 0, %d is given", TCP_reconnect_attempts);
			}
		} else if (parameter_name.equals(TCP_RECONNECT_DELAY_NAME)) {
			try {
				TCP_reconnect_delay = Integer.valueOf(parameter_value);
			} catch (NumberFormatException e) {
				log_error("Invalid input as TCP_reconnect_delay given: %s", parameter_value);
			}
			if (TCP_reconnect_delay < 0) {
				log_error("TCP_reconnect_delay must not be less than 0, %d is given", TCP_reconnect_delay);
			}
		} else if (parameter_name.equals(REMOTE_ADDRESS_NAME)) {
			remote_host_name = parameter_value;
		} else if (parameter_name.equals(LOCAL_ADDRESS_NAME)) {
			local_host_name = parameter_value;
		} else if (parameter_name.equals(REMOTE_PORT_NAME)) {
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
		} else if (parameter_name.equals(LOCAL_PORT_NAME)) {
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
		} else if (parameter_name.equals(NAGLING_NAME)) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				nagling = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				nagling = false;
			} else {
				log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, NAGLING_NAME);
			}
		} else if (parameter_name.equals(USE_NON_BLOCKING_SOCKET_NAME)) {
			if (parameter_value.equalsIgnoreCase("yes")) {
				use_non_blocking_socket = true;
			} else if (parameter_value.equalsIgnoreCase("no")) {
				use_non_blocking_socket = false;
			}
		} else if (parameter_name.equals(SERVER_BACKLOG_NAME)) {
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

	/**
	 * Handle function for socket events coming from the Channel's selector.
	 * 
	 * Shall be called from <code>Handle_Event</code> function.
	 * 
	 * @param fd the SelectableChannel what has event in the queue.
	 * @param is_readable the event is a reading event.
	 * @param is_writable the event is a writing event.
	 * @param is_error the event is an error event.
	 */
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
					log_debug("Abstract_Socket.Handle_Socket_Event(): state is STATE_DONT_CLOSE, don't close connection.");
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

	/**
	 * Reads <code>AS_TCP_CHUNCK_SIZE</code> bytes from the parameter channel.
	 *  
	 * @param fd The reading channel. 
	 * @return The incoming message length, possibly zero.
	 */
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

	/** 
	 * Writes message on channel in byte array format.
	 * 
	 * @param client_id The writing channel.
	 * @param send_par The message in byte format.
	 * @return The number of bytes written, possibly zero, or -1 if <code>IOException</code> raised.
	 */
	protected int send_message_on_fd(final SelectableChannel client_id, final byte[] send_par) {
		get_peer(client_id, false);
		try {
			return ((SocketChannel) client_id).write(ByteBuffer.wrap(send_par));
		} catch (IOException e) {
			log_error("Error during sending message on socket %s : %s", client_id.toString(), e.getMessage());
			return -1;
		}
	}

	/**
	 * Get the message header descriptor. By default the function returns <code>null</code>.
	 * Need to specify in every protocol or test port what uses this class.
	 * 
	 * @return the descriptor of the message headers
	 */
	protected PacketHeaderDescr Get_Header_Descriptor() {
		return null;
	}

	/**
	 * Called after a peer is connected.
	 * 
	 * Do nothing in here. Override in your test port implementation.
	 * 
	 * @param client_id - int
	 * @param host - String
	 * @param port - int
	 */
	protected void peer_connected(final int client_id, final String host, final int port) {}

	/**
	 * Handle incoming messages with or without header descriptor.
	 * 
	 * @param client_id the SelectableChannel what has an incoming message
	 */
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
					return;
				}
				long message_length = head_descr.Get_Message_Length(recv_tb.get_data());
				if (message_length < valid_header_length) {
					// this is a message with a malformed length
					log_error("Malformed message: invalid length: %l. The length should be at least %l.", message_length, valid_header_length);
				}
				if ((long)recv_tb.get_len() < message_length) {
					// this is a fragmented message with a valid header
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

	/**
	 * Open a server or a client socket.
	 * Shall be called from <code>user_map</code>.
	 */
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

	/**
	 * Open a server socket.
	 * 
	 * @param localHostname the local address.
	 * @param localServicename the local service name or port where the socket will be opened.
	 * @return <b>-1</b> if any error occurred,<br> or the <b>port number</b> where the socket will be opened
	 */
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

	/**
	 * Abstract method to notify user after a server/listening socket opened.
	 * 
	 * @param port_number where to listening socket binded
	 */
	protected abstract void listen_port_opened(int port_number);

	/**
	 * Close listening/server socket and unregister from the Selector.
	 */
	protected void close_listen_port() {
		// close current listening port if it is alive
		if (listen_fd != null && listen_fd.isOpen()) {
			Remove_Fd_Read_Handler(listen_fd);
			try {
				listen_fd.close();
			} catch (IOException e) {
				log_error("Error during closing listening socket: %s", e.getMessage());
				return;
			}
			log_debug("Closed listening port of fd: %s", listen_fd.toString());
			listen_fd = null;
		}
	}

	/**
	 * Return the listening/server socket or the first client socket in the list.
	 * 
	 * @return SelectableChannel what is in use
	 */
	protected SelectableChannel get_socket_fd() {
		if (server_mode) {
			return listen_fd;
		}
		if (peer_list_get_nr_of_peers() == 0) {
			return null;
		}
		return peer_list_get_first_peer();
	}

	/**
	 * Open a client socket and connect to a host.
	 * 
	 * @param remoteHostname the host address or name
	 * @param remoteService the host service name or port number
	 * @param localHostname the local host address or name
	 * @param localService the local service name or port
	 * @return the connection index in the connection handle list
	 */
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
				log_error("%s", e.getMessage());
			} catch (UnknownHostException e) {
				log_error("%s", e.getMessage());
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
						log_error("Interrupted during finishing connection: %s", e.getMessage());
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

	/**
	 * Abstract method to notify user after a server/listening socket opened.
	 * 
	 * @param client_id the index of the connection
	 */
	protected abstract void client_connection_opened(int client_id);

	/**
	 * User called unmap. Close all connection, remove clients, deregister the channels from their Selector.
	 * 
	 */
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
	 * peer_disconnected() needs to be overridden in test ports!
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
	 * Called when a peer shut down its SelectableChannel for writing.
	 * 
	 * @param fd - SelectableChannel
	 */
	protected void peer_half_closed(SelectableChannel fd) {
		log_debug("Entering Abstract_Socket.peer_half_closed()");
		remove_client(fd);
		peer_disconnected(fd);
		log_debug("Leaving Abstract_Socket.peer_half_closed()");
	}

	/**
	 * Send TCP closing/shutdown message on the connection.
	 * 
	 * @param client_id the index of the connection
	 */
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
		log_debug("leaving Abstract_Socket.send_shutdown()");
	}

	/**
	 * Send outgoing TCP message.
	 * 
	 * @param message_buffer the message in byte array format
	 * @param length the message length
	 * @param client_id the index of the connection
	 */
	protected void send_outgoing(final byte[] message_buffer, final int length, final int client_id) {
		log_debug("entering Abstract_Socket.send_outgoing()");
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

	/**
	 * Report message error. I recommend to use <code>log_error</code> function instead of this.
	 * 
	 * @param client_id the index of the connection
	 * @param msg_length the message length
	 * @param sent_length the actual sent message length
	 * @param msg the message
	 * @param error_text the error text for better debugging
	 */
	protected void report_error(final int client_id, final int msg_length, final int sent_length, byte[] msg, final String error_text) {
		log_error("%s",error_text);
	}

	/**
	 * Report unsent bytes of the message.
	 * 
	 * @param client_id the index of the connection
	 * @param msg_length the message length
	 * @param sent_length the actual sent message length
	 * @param msg the message
	 * @param error_text the error text for better debugging
	 */
	protected void report_unsent(final int client_id, final int msg_length, final int sent_length, byte[] msg, final String error_text) {
		log_debug("%s",error_text);
	}

	/**
	 *  Check the mandatory configuration parameters are set.
	 */
	private void all_mandatory_configparameters_present() {
		if(!use_connection_ASPs) {
			if(server_mode) {
				if(local_port_number == 0) {
					log_error("%s is not defined in the configuration file", LOCAL_PORT_NAME);
				}
			} else { // client mode
				if (remote_host_name == null) {
					log_error("%s is not defined in the configuration file", REMOTE_ADDRESS_NAME);
				}
				if(remote_port_number == 0){
					log_error("%s is not defined in the configuration file", REMOTE_PORT_NAME);
				}
			}
		}
		user_all_mandatory_configparameters_present();
	}

	/**
	 * Called when a client shall be removed.
	 * 
	 * @param fd the client or server socket
	 */
	protected void remove_client(SelectableChannel fd) {
		log_debug("entering Abstract_Socket.remove_client(%s)", fd.toString());
		if (!fd.equals(listen_fd)) {
			get_peer(fd, false);
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

	/**
	 * Called when all clients shall be removed
	 */
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

	/**
	 * Getter for client id.
	 * 
	 * @param fd SelectableChannel what searching for.
	 * @return -1 if SelectableChannel is not in the connection list, or the index in the list
	 */
	protected int get_clientId_by_fd(final SelectableChannel fd) {
		for (int i = 0; i < peer_list_root.size(); i++) {
			if (peer_list_root.get(i).tcp_socket.equals(fd)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Setter for ttcn_buffer_usercontrol parameter.
	 * 
	 * @param parameter_value new value
	 */
	protected void set_ttcn_buffer_usercontrol(final boolean parameter_value) {
		ttcn_buffer_usercontrol = parameter_value;
	}

	/**
	 * Setter for handle_half_close parameter.
	 * 
	 * @param parameter_value new value
	 */
	protected void set_handle_half_close(final boolean parameter_value) {
		handle_half_close = parameter_value;
	}

	/**
	 * Setter for server_mode parameter.
	 * 
	 * @param parameter_value new value
	 */
	protected void set_server_mode(final boolean parameter_value) {
		server_mode = parameter_value;
	}

	/**
	 * Getter for nagling parameter.
	 * 
	 * @return nagling
	 */
	protected boolean get_nagling() {
		return nagling;
	}

	/**
	 * Getter for use_non_blocking_socket parameter.
	 * 
	 * @return use_non_blocking_socket
	 */
	protected boolean get_use_non_blocking_socket() {
		return use_non_blocking_socket;
	}

	/**
	 * Getter for server_mode parameter.
	 * 
	 * @return true if server_mode is on
	 */
	protected boolean get_server_mode() {
		return server_mode;
	}

	/**
	 * Getter for socket_debugging parameter.
	 * 
	 * @return socket_debugging
	 */
	protected boolean get_socket_debugging() {
		return socket_debugging;
	}

	/**
	 * Getter for halt_on_connection_reset parameter.
	 * 
	 * @return halt_on_connection_reset
	 */
	protected boolean get_halt_on_connection_reset() {
		return halt_on_connection_reset;
	}

	/**
	 * Getter for use_connection_ASPs parameter.
	 * 
	 * @return true if use extra messages/logging in test port.
	 */
	protected boolean get_use_connection_ASPs() {
		return use_connection_ASPs;
	}

	/**
	 * Getter for get_handle_half_close parameter.
	 * 
	 * @return get_handle_half_close
	 */
	protected boolean get_handle_half_close() {
		return handle_half_close;
	}

	/**
	 * Add user_data to connection. Only needs in HTTPS connection.
	 * 
	 * @param id index of the connection
	 * @return true if user_data successfully added to connection
	 */
	protected boolean add_user_data(SelectableChannel id) {
		return true;
	}

	/**
	 * Remove user_data to connection. Only needs in HTTPS connection.
	 * 
	 * @param id index of the connection
	 * @return true if user_data successfully added to connection
	 */
	protected boolean remove_user_data(SelectableChannel id) {
		return true;
	}

	/**
	 * Don't need this function, only presents because compatibility.
	 * 
	 * @return true
	 */
	protected boolean user_all_mandatory_configparameters_present() {
		return true;
	}

	/**
	 * Get TTCN_Buffer of the connection.
	 * @param client_id the index of connection
	 * @return the connection buffer
	 */
	protected TTCN_Buffer get_buffer(final int client_id) {
		return get_peer(client_id, false).fd_buff;
	}

	////////////////////////////////////////////////////////////////////////
	/////    Peer handling functions
	////////////////////////////////////////////////////////////////////////
	/**
	 * Add peer connection to the list. Client_id is different here, only for the C++ compatible parameter name.
	 * 
	 * @param client_id SocketChannel of the connection
	 * @return client connection in a <code>as_client_struct</code> object
	 */
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

	/**
	 * Returns back the object of the connection.
	 * 
	 * @param client_fd the channel what we looking for
	 * @param no_error not throw error if client doesn't find in the connection list 
	 * @return the connection object what has the SelectableChannel
	 */
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

	/**
	 * Returns the connection object at the specified index.
	 * 
	 * @param client_fd the index
	 * @param no_error throw an error
	 * @return the connection object at the specified index
	 */
	// returns back the structure of the peer
	protected as_client_struct get_peer(final int client_fd, final boolean no_error) {
		return peer_list_root.get(client_fd);
	}

	// 
	/**
	 * Number of peers in the list, and log out it.
	 * 
	 * @return the number of connections
	 */
	protected int peer_list_get_nr_of_peers() {
		int nr = peer_list_root.size();
		log_debug("Abstract_Socket.peer_list_get_nr_of_peers: Number of active peers = %d", nr);
		return nr;
	}

	/**
	 * Returns SocketChannel of the first peer connection in the list.
	 * 
	 * @return the first SocketChannel in the list
	 */
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

	/**
	 * Returns SocketChannel of the last peer connection in the list.
	 * 
	 * @return the last SocketChannel in the list
	 */
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

	/**
	 * Remove the connection object at the specified index from the list.
	 * 
	 * @param client_id the index of the connection
	 */
	protected synchronized void peer_list_remove_peer(final int client_id) {
		log_debug("Abstract_Socket.peer_list_remove_peer: Removing client %d from peer list", client_id);
		if (client_id >= peer_list_root.size() || client_id < 0) {
			log_error("Invalid Client Id is given: %d.", client_id);
		}
		if (peer_list_root.get(client_id) == null) {
			log_error("Peer %d does not exist.", client_id);
		}
		peer_list_root.remove(client_id);
	}

	/**
	 * Remove the connection object at the specified index from the list.
	 * 
	 * @param fd the SelectableChannel of the connection
	 */
	protected void peer_list_remove_peer(final SelectableChannel fd) {
		if (peer_list_root != null && !peer_list_root.isEmpty()) {
			for (int i = 0; i < peer_list_root.size(); i++) {
				if (peer_list_root.get(i).tcp_socket.equals(fd)) {
					peer_list_remove_peer(i);
				}
			}
		}
	}

	/**
	 * Called when a TCP message is received.
	 * 
	 * @param message_buffer the message in byte array format
	 * @param length the message length
	 * @param client_id the connection index in the list
	 */
	protected abstract void message_incoming(final byte[] message_buffer, final int length, final int client_id);

	/**
	 * Add channel to the TTCN_Snapshots' selectors. The system will handle the incoming messages, doesn't need to block the running.
	 * 
	 * @param fd the connection channel what we want to add to the selector
	 */
	protected abstract void Add_Fd_Read_Handler(SelectableChannel fd);

	/**
	 * Add channel to the TTCN_Snapshots' selectors. The system will handle the outgoing messages, doesn't need to block the running.
	 * 
	 * @param fd the connection channel what we want to add to the selector
	 */
	protected abstract void Add_Fd_Write_Handler(SelectableChannel fd);

	/**
	 * Remove the channel from the TTCN_Snapshots' selectors what selector was watching the incoming messages. Prefer to use <code>Handler_Uninstall</code> function.
	 * 
	 * @param fd the connection channel what we want to remove from the selector
	 */
	protected abstract void Remove_Fd_Read_Handler(SelectableChannel fd);

	/**
	 * Remove the channel from the TTCN_Snapshots' selectors what selector was watching the outgoing messages. Prefer to use <code>Handler_Uninstall</code> function.
	 * 
	 * @param fd the connection channel what we want to remove from the selector
	 */
	protected abstract void Remove_Fd_Write_Handler(SelectableChannel fd);

	/**
	 * Remove the channel from the TTCN_Snapshots' selectors what selectors was watching the outgoing and incoming messages. Prefer to use <code>Handler_Uninstall</code> function.
	 * 
	 * @param fd the connection channel what we want to remove from the selectors
	 */
	protected abstract void Remove_Fd_All_Handlers(SelectableChannel fd);

	/**
	 * Remove all connection from TCN_Snapshot system. Call this function before you finish the test case or disconnect the connection. 
	 */
	protected abstract void Handler_Uninstall();

	/**
	 * Class for TCP connection for can be handled by TTCN and Titan Ecosystem.
	 * 
	 * @author Gergo Ujhelyi
	 *
	 */
	public static class as_client_struct {
		public byte[] user_data;
		public TTCN_Buffer fd_buff;
		public InetSocketAddress clientAddr;
		public TCP_STATES tcp_state;
		public READING_STATES reading_state;
		public SocketChannel tcp_socket;

		/**
		 * Empty constructor.
		 */
		public as_client_struct() {} 

		/**
		 * Basic copy constructor.
		 */
		public as_client_struct(byte[] user_data, TTCN_Buffer fd_buff, InetSocketAddress clientAddr, TCP_STATES tcp_state, READING_STATES reading_state) {
			this.user_data = user_data;
			this.fd_buff = fd_buff;
			this.clientAddr = clientAddr;
			this.tcp_state = tcp_state;
			this.reading_state = reading_state;
		}
	}
}
