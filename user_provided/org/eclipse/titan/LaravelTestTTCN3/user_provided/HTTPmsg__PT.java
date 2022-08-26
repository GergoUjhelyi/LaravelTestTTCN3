package org.eclipse.titan.LaravelTestTTCN3.user_provided;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__PortType.HTTPmsg__PT_BASE;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.Close;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.Connect;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPMessage;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPRequest;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPRequest__binary__body;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPResponse;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HTTPResponse__binary__body;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.Half__close;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.HeaderLines;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.Listen;
import org.eclipse.titan.LaravelTestTTCN3.generated.HTTPmsg__Types.Shutdown;
import org.eclipse.titan.runtime.core.AdditionalFunctions;
import org.eclipse.titan.runtime.core.TTCN_Buffer;
import org.eclipse.titan.runtime.core.TTCN_Logger;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;
import org.eclipse.titan.runtime.core.TTCN_Snapshot;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanNull_Type;
import org.eclipse.titan.runtime.core.TitanOctetString;
import org.eclipse.titan.runtime.core.TtcnError;

/**
 * Main class for the TTCN-3 HTTP testport. Contains all the function you need in a HTTP connection.
 * 
 * Almost the same Abstract_Socket used in constructor. In the future it can be reduced to one constructor.
 * Now it needs for C++ compatibility. The reason I choose this solution is because C++ solution uses multiple
 * inheritence.
 * 
 * @author gujhelyi
 *
 */
public class HTTPmsg__PT extends HTTPmsg__PT_BASE {
	
	public static final String SERVER_BACKLOG_NAME = "server_backlog";
	public static final String USE_NOTIFICATION_ASPS_NAME = "use_notification_ASPs";
	public static final String SOCKET_DEBUGGING_NAME = "http_debugging";

	private boolean adding_ssl_connection;
	private boolean adding_client_connection;
	private boolean server_use_ssl;

	private boolean use_notification_ASPs;

	private boolean use_send_failed;
	private HTTPMessage last_msg;

	private Abstract_Socket abstract_Socket;

	private static final int BUFFER_FAIL = 2;
	private static final int BUFFER_CRLF = 3;

	private static boolean report_lf = true;

	/**
	 * Default constructor: create an Abstract_socket and set up default values
	 * 
	 */
	public HTTPmsg__PT() {
		abstract_Socket = new Abstract_Socket() {
			@Override
			protected void message_incoming(byte[] message_buffer, int length, int client_id) {
				log_debug("entering HTTPmsg__PT.message_incoming()");

				TTCN_Buffer buf_p = get_buffer(client_id);

				while (buf_p.get_read_len() > 0) {
					log_debug("HTTPmsg__PT.message_incoming(): decoding next message, len: %d", buf_p.get_read_len());
					if (!HTTP_decode(buf_p, client_id, false)) {
						break;
					}
				}

				log_debug("leaving HTTPmsg__PT.message_incoming()");
			}

			@Override
			protected void listen_port_opened(int port_number) {
				log_debug("entering HTTPmsg__PT.listen_port_opened(%d)", port_number);

				if (use_notification_ASPs) {
					HTTPmsg__Types.Listen__result asp = new HTTPmsg__Types.Listen__result();
					asp.get_field_portnumber().operator_assign(new TitanInteger(port_number));
					incoming_message(asp);
				} else if (port_number < 0) {
					log_error("Cannot listen at port");
				}

				log_debug("leaving HTTPmsg__PT.listen_port_opened()");
			}

			@Override
			protected void client_connection_opened(int client_id) {
				log_debug("entering HTTPmsg__PT.client_connection_opened(%d)", client_id);

				if (use_notification_ASPs) {
					HTTPmsg__Types.Connect__result asp = new HTTPmsg__Types.Connect__result();
					asp.get_field_client__id().operator_assign(new TitanInteger(client_id));
					incoming_message(asp);
				} else if (client_id < 0) {
					log_error("Cannot connect to server");
				}
				log_debug("leaving HTTPmsg__PT.client_connection_opened()");
			}

			@Override
			protected void Add_Fd_Read_Handler(SelectableChannel fd) {
				try {
					Install_Handler(Set.of(abstract_Socket.get_peer(fd, false).tcp_socket), null, 0.0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void Add_Fd_Write_Handler(SelectableChannel fd) {
				try {
					Install_Handler(null, Set.of(abstract_Socket.get_peer(fd, false).tcp_socket), 0.0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void Remove_Fd_Read_Handler(SelectableChannel fd) {
				Remove_Fd_All_Handlers(fd);
			}

			@Override
			protected void Remove_Fd_Write_Handler(SelectableChannel fd) {
				Remove_Fd_All_Handlers(fd);
			}

			@Override
			protected void Remove_Fd_All_Handlers(SelectableChannel fd) {
				try {
					TTCN_Snapshot.selector.get().selectNow();
					Set<SelectionKey> selectedKeys = TTCN_Snapshot.selector.get().selectedKeys();
					Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {
						SelectionKey selectionKey = keyIterator.next();
						if (selectionKey.channel().equals(fd)) {
							selectionKey.cancel();
						}
						keyIterator.remove();
					}
					TTCN_Snapshot.channelMap.get().remove(fd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void Handler_Uninstall() {
				try {
					Uninstall_Handler();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void report_unsent(int client_id, int msg_length, int sent_length, byte[] msg, String error_text) {
				if (use_send_failed && last_msg != null) {
					HTTPmsg__Types.Send__failed asp = new HTTPmsg__Types.Send__failed();
					asp.get_field_msg().operator_assign(last_msg);
					asp.get_field_already__half__closed().operator_assign(get_peer(client_id, true).tcp_state == TCP_STATES.CLOSE_WAIT);

					incoming_message(asp);
				}
			}

			@Override
			protected void peer_half_closed(SelectableChannel fd) {
				log_debug("entering HTTPmsg__PT.peer_half_closed(client_id: %s)", fd.toString());

				TTCN_Buffer buf_p = get_peer(fd, false).fd_buff;
				buf_p.rewind();

				while (buf_p.get_read_len() > 0) {
					log_debug("HTTPmsg__PT.remove_client(): decoding next message, len: %d", buf_p.get_read_len());
					if (!HTTP_decode(buf_p, get_clientId_by_fd(fd), true)) {
						break;
					}
				}

				HTTPmsg__Types.Half__close asp = new HTTPmsg__Types.Half__close();
				asp.get_field_client__id().get().operator_assign(get_clientId_by_fd(fd));
				incoming_message(asp);

				log_debug("leaving HTTPmsg__PT.peer_disconnected(client_id: %d)", get_clientId_by_fd(fd));
			}

			@Override
			protected void peer_connected(int client_id, String host, int port) {
				log_debug("entering HTTPmsg__PT.peer_connected(%d)", client_id);

				if (use_notification_ASPs) {
					HTTPmsg__Types.Client__connected asp = new HTTPmsg__Types.Client__connected();
					asp.get_field_hostname().operator_assign(host);
					asp.get_field_portnumber().operator_assign(port);
					asp.get_field_client__id().operator_assign(client_id);

					incoming_message(asp);
				} else {
					super.peer_connected(client_id, host, port);
				}

				log_debug("leaving HTTPmsg__PT.peer_connected()");
			}

			@Override
			protected boolean add_user_data(SelectableChannel id) {
				log_debug("entering HTTPmsg__PT.add_user_data(client_id: %d, use_ssl: %s)", get_clientId_by_fd(id), (adding_client_connection && adding_ssl_connection) || (server_use_ssl && !adding_ssl_connection) ? "yes" : "no");

				abstract_Socket.set_server_mode(!adding_client_connection);

				if ((adding_client_connection && !adding_ssl_connection) || (!adding_client_connection && !server_use_ssl)) {
					log_debug("leaving HTTPmsg__PT.add_user_data() with returning Abstract_Socket.add_user_data()");
					return super.add_user_data(id);
				} else {
					log_debug("leaving HTTPmsg__PT.add_user_data() with returning SSL_Socket.add_user_data()");
					//TODO: implement SSL
					return super.add_user_data(id);
				}
			}

			@Override
			protected boolean remove_user_data(SelectableChannel id) {
				log_debug("entering HTTPmsg__PT.remove_user_data(client_id: %d", get_clientId_by_fd(id));
				//TODO: implement SSL
				log_debug("leaving HTTPmsg__PT.remove_user_data() with returning Abstract_Socket.remove_user_data()");

				return super.remove_user_data(id);
			}

			@Override
			protected int receive_message_on_fd(SelectableChannel fd) {
				log_debug("entering HTTPmsg__PT.receive_message_on_fd(client_id: %d)", get_clientId_by_fd(fd));
				//TODO: implement SSL
				log_debug("leaving HTTPmsg__PT.receive_message_on_fd() with returning Abstract_Socket.receive_message_on_fd()");

				return super.receive_message_on_fd(fd);
			}

			@Override
			protected void remove_client(SelectableChannel fd) {
				log_debug("entering HTTPmsg__PT.remove_client(client_id: %d)", get_clientId_by_fd(fd));

				TTCN_Buffer buf_p = get_peer(fd, false).fd_buff;

				while (buf_p.get_read_len() > 0) {
					log_debug("HTTPmsg__PT.remove_client(): decoding next message, len: %d", buf_p.get_read_len());
					if (!HTTP_decode(buf_p, get_clientId_by_fd(fd), true)) {
						break;
					}
				}
				//TODO: implement SSL

				log_debug("leaving HTTPmsg__PT.remove_client() with returning Abstract_Socket.remove_client()");
				super.remove_client(fd);
			}

			@Override
			protected int send_message_on_fd(SelectableChannel client_id, byte[] send_par) {
				log_debug("entering HTTPmsg__PT.send_message_on_fd(client_id: %d)", get_clientId_by_fd(client_id));
				//TODO: implement SSL
				log_debug("leaving HTTPmsg__PT.send_message_on_fd() with returning Abstract_Socket.send_message_on_fd()");
				return super.send_message_on_fd(client_id, send_par);
			}
		};
		abstract_Socket.parameter_set(Abstract_Socket.USE_CONNECTION_ASPS_NAME, "yes");
		abstract_Socket.parameter_set(SERVER_BACKLOG_NAME, "1024");
		use_notification_ASPs = false;
		abstract_Socket.set_ttcn_buffer_usercontrol(true);
		abstract_Socket.set_handle_half_close(true);
		adding_client_connection = false;
		adding_ssl_connection = false;
		server_use_ssl = false;
		use_send_failed = false;
		last_msg = null;
	}

	/**
	 * Constructor: create an Abstract_socket and set up default values with port port name.
	 * 
	 * @param par_port_name the testport name
	 */
	public HTTPmsg__PT(final String par_port_name) {
		super(par_port_name);
		abstract_Socket = new Abstract_Socket() {
			@Override
			protected void message_incoming(byte[] message_buffer, int length, int client_id) {
				log_debug("entering HTTPmsg__PT.message_incoming()");

				TTCN_Buffer buf_p = get_buffer(client_id);

				while (buf_p.get_read_len() > 0) {
					log_debug("HTTPmsg__PT.message_incoming(): decoding next message, len: %d", buf_p.get_read_len());
					if (!HTTP_decode(buf_p, client_id, false)) {
						break;
					}
				}

				log_debug("leaving HTTPmsg__PT.message_incoming()");
			}

			@Override
			protected void listen_port_opened(int port_number) {
				log_debug("entering HTTPmsg__PT.listen_port_opened(%d)", port_number);

				if (use_notification_ASPs) {
					HTTPmsg__Types.Listen__result asp = new HTTPmsg__Types.Listen__result();
					asp.get_field_portnumber().operator_assign(new TitanInteger(port_number));
					incoming_message(asp);
				} else if (port_number < 0) {
					log_error("Cannot listen at port");
				}

				log_debug("leaving HTTPmsg__PT.listen_port_opened()");
			}

			@Override
			protected void client_connection_opened(int client_id) {
				log_debug("entering HTTPmsg__PT.client_connection_opened(%d)", client_id);

				if (use_notification_ASPs) {
					HTTPmsg__Types.Connect__result asp = new HTTPmsg__Types.Connect__result();
					asp.get_field_client__id().operator_assign(new TitanInteger(client_id));
					incoming_message(asp);
				} else if (client_id < 0) {
					log_error("Cannot connect to server");
				}
				log_debug("leaving HTTPmsg__PT.client_connection_opened()");
			}

			@Override
			protected void Add_Fd_Read_Handler(SelectableChannel fd) {
				try {
					Install_Handler(Set.of(abstract_Socket.get_peer(fd, false).tcp_socket), null, 0.0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void Add_Fd_Write_Handler(SelectableChannel fd) {
				try {
					Install_Handler(null, Set.of(abstract_Socket.get_peer(fd, false).tcp_socket), 0.0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void Remove_Fd_Read_Handler(SelectableChannel fd) {
				Remove_Fd_All_Handlers(fd);
			}

			@Override
			protected void Remove_Fd_Write_Handler(SelectableChannel fd) {
				Remove_Fd_All_Handlers(fd);
			}

			@Override
			protected void Remove_Fd_All_Handlers(SelectableChannel fd) {
				try {
					TTCN_Snapshot.selector.get().selectNow();
					Set<SelectionKey> selectedKeys = TTCN_Snapshot.selector.get().selectedKeys();
					Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {
						SelectionKey selectionKey = keyIterator.next();
						if (selectionKey.channel().equals(fd)) {
							selectionKey.cancel();
						}
						keyIterator.remove();
					}
					TTCN_Snapshot.channelMap.get().remove(fd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void Handler_Uninstall() {
				try {
					Uninstall_Handler();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			protected void report_unsent(int client_id, int msg_length, int sent_length, byte[] msg, String error_text) {
				if (use_send_failed && last_msg != null) {
					HTTPmsg__Types.Send__failed asp = new HTTPmsg__Types.Send__failed();
					asp.get_field_msg().operator_assign(last_msg);
					asp.get_field_already__half__closed().operator_assign(get_peer(client_id, true).tcp_state == TCP_STATES.CLOSE_WAIT);

					incoming_message(asp);
				}
			}

			@Override
			protected void peer_half_closed(SelectableChannel fd) {
				log_debug("entering HTTPmsg__PT.peer_half_closed(client_id: %s)", fd.toString());

				TTCN_Buffer buf_p = get_peer(fd, false).fd_buff;
				buf_p.rewind();

				while (buf_p.get_read_len() > 0) {
					log_debug("HTTPmsg__PT.remove_client(): decoding next message, len: %d", buf_p.get_read_len());
					if (!HTTP_decode(buf_p, get_clientId_by_fd(fd), true)) {
						break;
					}
				}

				HTTPmsg__Types.Half__close asp = new HTTPmsg__Types.Half__close();
				asp.get_field_client__id().get().operator_assign(get_clientId_by_fd(fd));
				incoming_message(asp);

				log_debug("leaving HTTPmsg__PT.peer_disconnected(client_id: %d)", get_clientId_by_fd(fd));
			}

			@Override
			protected void peer_connected(int client_id, String host, int port) {
				log_debug("entering HTTPmsg__PT.peer_connected(%d)", client_id);

				if (use_notification_ASPs) {
					HTTPmsg__Types.Client__connected asp = new HTTPmsg__Types.Client__connected();
					asp.get_field_hostname().operator_assign(host);
					asp.get_field_portnumber().operator_assign(port);
					asp.get_field_client__id().operator_assign(client_id);

					incoming_message(asp);
				} else {
					super.peer_connected(client_id, host, port);
				}

				log_debug("leaving HTTPmsg__PT.peer_connected()");
			}

			@Override
			protected boolean add_user_data(SelectableChannel id) {
				log_debug("entering HTTPmsg__PT.add_user_data(client_id: %d, use_ssl: %s)", get_clientId_by_fd(id), (adding_client_connection && adding_ssl_connection) || (server_use_ssl && !adding_ssl_connection) ? "yes" : "no");

				abstract_Socket.set_server_mode(!adding_client_connection);

				if ((adding_client_connection && !adding_ssl_connection) || (!adding_client_connection && !server_use_ssl)) {
					log_debug("leaving HTTPmsg__PT.add_user_data() with returning Abstract_Socket.add_user_data()");
					return super.add_user_data(id);
				} else {
					log_debug("leaving HTTPmsg__PT.add_user_data() with returning SSL_Socket.add_user_data()");
					//TODO: implement SSL
					return super.add_user_data(id);
				}
			}

			@Override
			protected boolean remove_user_data(SelectableChannel id) {
				log_debug("entering HTTPmsg__PT.remove_user_data(client_id: %d", get_clientId_by_fd(id));
				//TODO: implement SSL
				log_debug("leaving HTTPmsg__PT.remove_user_data() with returning Abstract_Socket.remove_user_data()");

				return super.remove_user_data(id);
			}

			@Override
			protected int receive_message_on_fd(SelectableChannel fd) {
				log_debug("entering HTTPmsg__PT.receive_message_on_fd(client_id: %d)", get_clientId_by_fd(fd));
				//TODO: implement SSL
				log_debug("leaving HTTPmsg__PT.receive_message_on_fd() with returning Abstract_Socket.receive_message_on_fd()");

				return super.receive_message_on_fd(fd);
			}

			@Override
			protected void remove_client(SelectableChannel fd) {
				log_debug("entering HTTPmsg__PT.remove_client(client_id: %d)", get_clientId_by_fd(fd));

				TTCN_Buffer buf_p = get_peer(fd, false).fd_buff;

				while (buf_p.get_read_len() > 0) {
					log_debug("HTTPmsg__PT.remove_client(): decoding next message, len: %d", buf_p.get_read_len());
					if (!HTTP_decode(buf_p, get_clientId_by_fd(fd), true)) {
						break;
					}
				}
				//TODO: implement SSL

				log_debug("leaving HTTPmsg__PT.remove_client() with returning Abstract_Socket.remove_client()");
				super.remove_client(fd);
			}

			@Override
			protected int send_message_on_fd(SelectableChannel client_id, byte[] send_par) {
				log_debug("entering HTTPmsg__PT.send_message_on_fd(client_id: %d)", get_clientId_by_fd(client_id));
				//TODO: implement SSL
				log_debug("leaving HTTPmsg__PT.send_message_on_fd() with returning Abstract_Socket.send_message_on_fd()");
				return super.send_message_on_fd(client_id, send_par);
			}
		};
		abstract_Socket.parameter_set(Abstract_Socket.USE_CONNECTION_ASPS_NAME, "yes");
		abstract_Socket.parameter_set(SERVER_BACKLOG_NAME, "1024");
		use_notification_ASPs = false;
		abstract_Socket.set_ttcn_buffer_usercontrol(true);
		abstract_Socket.set_handle_half_close(true);
		adding_client_connection = false;
		adding_ssl_connection = false;
		server_use_ssl = false;
		use_send_failed = false;
		last_msg = null;
	}

	@Override
	/**
	 * Set the class parameters' from a config file.
	 * 
	 * @param parameter_name The parameter name.
	 * @param parameter_value The parameter value.
	 * @return <code>true</code> if the parameter name valid and and set to the given value.
	 * @throws TtcnError in <code>log_error</code> functions.
	 */
	public void set_parameter(String parameter_name, String parameter_value) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.set_parameter(%s, %s)", parameter_name, parameter_value);
		if (parameter_name.toLowerCase().equals(USE_NOTIFICATION_ASPS_NAME.toLowerCase())) {
			if (parameter_value.toLowerCase().equals("yes")) {
				use_notification_ASPs = true;
			} else if (parameter_value.toLowerCase().equals("no")) {
				use_notification_ASPs = false;
			} else {
				abstract_Socket.log_error("Parameter value '%s' not recognized for parameter '%s'", parameter_value, USE_NOTIFICATION_ASPS_NAME);
			}
		}
	}

	/**
	 * Open an HTTP/TCP socket, and register it.
	 * 
	 * @param system_port the port/socket name
	 * 
	 */
	@Override
	protected void user_map(String system_port) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.user_map(%s)", system_port);
		if (TTCN_Logger.log_this_event(Severity.DEBUG_TESTPORT)) {
			if(!abstract_Socket.get_socket_debugging())
				abstract_Socket.log_warning("%s: to switch on HTTP test port debugging, set the '*.%s.http_debugging := \"yes\" in the port's parameters.", this.get_name(), this.get_name());
		}
		abstract_Socket.map_user();
		abstract_Socket.log_debug("leaving HTTPmsg__PT.user_map()");
	}

	/**
	 * Close HTTP/TCP socket.
	 * 
	 */
	@Override
	protected void user_unmap(String system_port) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.user_unmap(%s)", system_port);

		abstract_Socket.unmap_user();

		abstract_Socket.log_debug("leaving HTTPmsg__PT.user_unmap()");
	}

	@Override
	protected void user_start() {
		//Do nothing
	}

	@Override
	protected void user_stop() {
		//Do nothing
	}

	/**
	 * Handle socket events.
	 * 
	 * @param channel the socket what has an event
	 * @param is_readable the event is an incoming event
	 * @param is_writeable the event is an outgoing event
	 */
	@Override
	public void Handle_Event(SelectableChannel channel, boolean is_readable, boolean is_writeable) {
		abstract_Socket.log_debug("-------------- entering HTTPmsg__PT.Handle_Event() - event received on a connection");
		abstract_Socket.Handle_Socket_Event(channel, is_readable, is_writeable, false);
		abstract_Socket.log_debug("leaving HTTPmsg__PT.Handle_Event()");
	}

	/**
	 * Send a Connect type message and open a client connection.
	 * 
	 * @param send_par the connect message what is coming from TTCN-3
	 */
	@Override
	protected void outgoing_send(Connect send_par) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.outgoing_send(Connect)");

		int client_id = abstract_Socket.open_client_connection(send_par.constGet_field_hostname().get_value().toString(),  String.valueOf(send_par.constGet_field_portnumber().get_int()) , null, null);
		adding_ssl_connection = false;
		adding_client_connection = false;

		abstract_Socket.log_debug("leaving HTTPmsg__PT.outgoing_send(Connect),client_id: %d", client_id);
	}

	/**
	 * Send a Listen type message and open a sever listening connection.
	 * 
	 * @param send_par the listen message what is coming from TTCN-3
	 */
	@Override
	protected void outgoing_send(Listen send_par) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.outgoing_send(Listen)");

		server_use_ssl = send_par.get_field_use__ssl().get_value();

		//TODO: implement SSL
		if (server_use_ssl) {
			TtcnError.TtcnWarning("SSL not supported at this time in HTTP test port!");
		}

		if (send_par.get_field_local__hostname().is_present()) {
			abstract_Socket.open_listen_port(send_par.constGet_field_local__hostname().constGet().get_value().toString(), AdditionalFunctions.int2str(send_par.constGet_field_portnumber()).get_value().toString());
		} else {
			abstract_Socket.log_debug("using IN_ADDR_ANY as local host name");
			abstract_Socket.open_listen_port(null, AdditionalFunctions.int2str(send_par.constGet_field_portnumber()).get_value().toString());
		}

		abstract_Socket.log_debug("leaving HTTPmsg__PT.outgoing_send(Listen)");
	}

	/**
	 * Send a Close type message and close connection.
	 * 
	 * @param send_par the close message what is coming from TTCN-3
	 */
	@Override
	protected void outgoing_send(Close send_par) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.outgoing_send(Close)");

		if(send_par.constGet_field_client__id().is_present()) {
			abstract_Socket.remove_client(abstract_Socket.get_peer(send_par.constGet_field_client__id().get().get_int(), false).tcp_socket);
		} else {
			abstract_Socket.remove_all_clients();
		}

		abstract_Socket.log_debug("leaving HTTPmsg__PT.outgoing_send(Close)");
	}

	/**
	 * Send a Half__close type message and close connection.
	 * 
	 * @param send_par the Half__close message what is coming from TTCN-3
	 */
	@Override
	protected void outgoing_send(Half__close send_par) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.outgoing_send(Half_close)");

		if (send_par.get_field_client__id().is_present()) {
			abstract_Socket.send_shutdown(abstract_Socket.get_peer(send_par.constGet_field_client__id().constGet().get_int(), false).tcp_socket);
		} else {
			abstract_Socket.send_shutdown(abstract_Socket.get_peer(0, false).tcp_socket);
		}

		abstract_Socket.log_debug("leaving HTTPmsg__PT.outgoing_send(Half_close)");
	}

	/**
	 * Send HTTP message in the socket. This function calls f_HTTP_encodeCommon function to encode message to byte format.
	 * 
	 * @param send_par the HTTP message what is coming from TTCN-3
	 * 
	 */
	@Override
	protected void outgoing_send(HTTPMessage send_par) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.outgoing_send(HTTPMessage)");
		final TTCN_Buffer snd_buf = new TTCN_Buffer();
		int client_id = -1;

		switch (send_par.get_selection()) {
		case ALT_request:
			if (send_par.constGet_field_request().constGet_field_client__id().is_present()) {
				client_id = send_par.constGet_field_request().constGet_field_client__id().get().get_int();
			}
			break;
		case ALT_request__binary:
			if (send_par.constGet_field_request__binary().constGet_field_client__id().is_present()) {
				client_id = send_par.constGet_field_request__binary().constGet_field_client__id().get().get_int();
			}
			break;
		case ALT_response:
			if (send_par.constGet_field_response().constGet_field_client__id().is_present()) {
				client_id = send_par.constGet_field_response().constGet_field_client__id().get().get_int();
			}
			break;
		case ALT_response__binary:
			if (send_par.constGet_field_response__binary().constGet_field_client__id().is_present()) {
				client_id = send_par.constGet_field_response__binary().constGet_field_client__id().get().get_int();
			}
			break;
		case ALT_erronous__msg:
			if (send_par.constGet_field_erronous__msg().get_field_client__id().is_present()) {
				client_id = send_par.constGet_field_erronous__msg().constGet_field_client__id().get().get_int();
			}
			break;
		default:
			throw new TtcnError("Unknown HTTP_Message type to encode and send!");
		}

		f_HTTP_encodeCommon(send_par, snd_buf);

		if (client_id >= 0) {
			abstract_Socket.send_outgoing(snd_buf.get_data(), snd_buf.get_len(), client_id);
		} else {
			abstract_Socket.send_outgoing(snd_buf.get_data(), snd_buf.get_len(), -1);
		}

		abstract_Socket.log_debug("leaving HTTPmsg__PT.outgoing_send(HTTPMessage)");
	}

	/**
	 * Send a Shutdown type message and close server connection.
	 * 
	 * @param send_par the Shutdown message what is coming from TTCN-3
	 */
	@Override
	protected void outgoing_send(Shutdown send_par) {
		abstract_Socket.log_debug("entering HTTPmsg__PT.outgoing_send(Shutdown)");

		abstract_Socket.close_listen_port();

		abstract_Socket.log_debug("leaving HTTPmsg__PT.outgoing_send(Shutdown)");
	}

	/**
	 * Encode TTCN-3 HTTP message into byte array and put into a TTCN_Buffer.
	 * Use TTCN_Buffer instead of Java default buffers allows us to use TITAN/TTCN features.
	 * 
	 * @param msg the HTTP message what needs to be encoded into byte format
	 * @param buf the buffer where we put the encoded HTTP message
	 */
	public static void f_HTTP_encodeCommon(final HTTPMessage msg, final TTCN_Buffer buf) {
		buf.clear();
		if (msg.get_selection() == HTTPMessage.union_selection_type.ALT_erronous__msg) {
			buf.put_cs(msg.constGet_field_erronous__msg().constGet_field_msg());
		} else {
			HeaderLines header = null;
			HTTPRequest request = null;
			HTTPResponse response = null;
			HTTPRequest__binary__body request_binary = null;
			HTTPResponse__binary__body response_binary = null;
			TitanCharString body = null;
			TitanOctetString body_binary = null;
			if (msg.get_selection() == HTTPMessage.union_selection_type.ALT_request) {
				request = msg.constGet_field_request();
				header = request.constGet_field_header();
				body = request.constGet_field_body();
				buf.put_cs(request.constGet_field_method());
				buf.put_cs(new TitanCharString(" "));
				buf.put_cs(request.constGet_field_uri());
				buf.put_cs(new TitanCharString(" HTTP/"));
				buf.put_cs(new TitanCharString(String.valueOf(request.constGet_field_version__major())));
				buf.put_c((byte)'.');
				buf.put_cs(new TitanCharString(String.valueOf(request.constGet_field_version__minor())));
				buf.put_cs(new TitanCharString("\r\n"));
			} else if (msg.get_selection() == HTTPMessage.union_selection_type.ALT_response) {
				response = msg.constGet_field_response();
				header = response.constGet_field_header();
				body = response.constGet_field_body();
				buf.put_cs(new TitanCharString(" HTTP/"));
				buf.put_cs(new TitanCharString(String.valueOf(response.constGet_field_version__major())));
				buf.put_c((byte)'.');
				buf.put_cs(new TitanCharString(String.valueOf(response.constGet_field_version__minor())));
				buf.put_cs(new TitanCharString(" "));
				buf.put_cs(new TitanCharString(String.valueOf(response.constGet_field_statuscode().get_int())));
				buf.put_cs(new TitanCharString(" "));
				buf.put_cs(new TitanCharString(String.valueOf(response.constGet_field_statustext())));
				buf.put_cs(new TitanCharString("\r\n"));
			} else if (msg.get_selection() == HTTPMessage.union_selection_type.ALT_request__binary) {
				request_binary = msg.constGet_field_request__binary();
				header = request_binary.constGet_field_header();
				body_binary = request_binary.constGet_field_body();
				buf.put_cs(request_binary.constGet_field_method());
				buf.put_cs(new TitanCharString(" "));
				buf.put_cs(request_binary.constGet_field_uri());
				buf.put_cs(new TitanCharString(" HTTP/"));
				buf.put_cs(new TitanCharString(String.valueOf(request_binary.constGet_field_version__major())));
				buf.put_c((byte)'.');
				buf.put_cs(new TitanCharString(String.valueOf(request_binary.constGet_field_version__minor())));
				buf.put_cs(new TitanCharString("\r\n"));
			} else if (msg.get_selection() == HTTPMessage.union_selection_type.ALT_response__binary) {
				response_binary = msg.constGet_field_response__binary();
				header = response_binary.constGet_field_header();
				body_binary = response_binary.constGet_field_body();
				buf.put_cs(new TitanCharString(" HTTP/"));
				buf.put_cs(new TitanCharString(String.valueOf(response_binary.constGet_field_version__major())));
				buf.put_c((byte)'.');
				buf.put_cs(new TitanCharString(String.valueOf(response_binary.constGet_field_version__minor())));
				buf.put_cs(new TitanCharString(" "));
				buf.put_cs(new TitanCharString(String.valueOf(response_binary.constGet_field_statuscode().get_int())));
				buf.put_cs(new TitanCharString(" "));
				buf.put_cs(new TitanCharString(String.valueOf(response_binary.constGet_field_statustext())));
				buf.put_cs(new TitanCharString("\r\n"));
			}
			for (int i = 0; i < header.size_of().get_int(); i++) {
				buf.put_cs(header.constGet_at(i).constGet_field_header__name());
				buf.put_cs(new TitanCharString(": "));
				buf.put_cs(header.constGet_at(i).constGet_field_header__value());
				buf.put_cs(new TitanCharString("\r\n"));
			}
			buf.put_cs(new TitanCharString("\r\n"));
			if (body != null && body.lengthof().is_greater_than(0)) {
				buf.put_cs(body);
			} else if (body_binary != null && body_binary.lengthof().is_greater_than(0)) {
				buf.put_os(body_binary);
			}
		}
	}

	/**
	 * Decode byte format messages into a HTTP message.
	 * If buffer contains valid message, msg will contain the first decoded HTTP message, the decoded part will be removed from the buffer.
	 * 
	 * @param buffer what contains data in byte format
	 * @param msg a HTTP message variable, where the function puts the decoded data
	 * @param connection_closed flag to check is this the last message
	 * @param socket_debugging flag to check debugging is enabled
	 * @param test_port_type the test port type
	 * @param test_port_name the test port name
	 * @return true if the buffer is not empty and it contain valid message
	 */
	public static boolean f_HTTP_decodeCommon(TTCN_Buffer buffer, HTTPMessage msg, final boolean connection_closed, final boolean socket_debugging, final String test_port_type, final String test_port_name) {
		TTCN_Logger.log(Severity.DEBUG_TESTPORT, "starting f_HTTP_decodeCommon ");
		if (buffer.get_read_len() <= 0) {
			return false;
		}
		buffer.rewind();

		Decoding_Params decoding_params = new Decoding_Params();
		decoding_params.non_persistent_connection = false;
		decoding_params.chunked_body = false;
		decoding_params.content_length = -1;
		decoding_params.error = false;
		decoding_params.isMessage = true;

		if (TTCN_Logger.log_this_event(Severity.DEBUG_TESTPORT)) {
			if (test_port_name != null) {
				TTCN_Logger.log(Severity.DEBUG_TESTPORT, "%s DECODER: <%s>\n", test_port_name, new String(buffer.get_read_data()));
			} else {
				TTCN_Logger.log(Severity.DEBUG_TESTPORT, "DECODER: <%s>\n", new String(buffer.get_read_data()));
			}
		}

		TitanCharString first = new TitanCharString("");
		boolean isResponse = false;

		switch (get_line(buffer, first, false)) {
		case 1: // The first line is available
			HTTPmsg__Types.HeaderLines header = new HeaderLines(TitanNull_Type.NULL_VALUE);
			TitanOctetString body = new TitanOctetString("");
			String cc_first = first.get_value().toString();
			int version__major = 0;
			int version__minor = 0;
			int statusCode = 0;
			String stext = "";

			String method_name = "";
			int pos = cc_first.stripLeading().indexOf(' ');
			if (pos == -1) {
				TTCN_Logger.log(Severity.DEBUG_TESTPORT, "could not find space in the first line of response: <%s>", cc_first);
				decoding_params.isMessage = false;
				decoding_params.error = true;
				break;
			}
			method_name = cc_first.stripLeading().substring(0, pos);
			TTCN_Logger.log(Severity.DEBUG_TESTPORT, "method_name: <%s>", method_name);
			if (method_name.matches("(.*)(HTTP|http)(.*)")) {
				// The first line contains a response like HTTP/1.1 200 OK
				isResponse = true;

				Pattern responsePattern = Pattern.compile(".*(HTTP|http)/(\\d{1})\\.(\\d{1})\\s+(\\d{3})(.*)");
				Matcher responseMatcher = responsePattern.matcher(cc_first);
				if (responseMatcher.lookingAt()) {
					version__major = Integer.valueOf(responseMatcher.group(2));
					version__minor = Integer.valueOf(responseMatcher.group(3));
					statusCode = Integer.valueOf(responseMatcher.group(4));
					stext = responseMatcher.group(5).strip();
				} else {
					decoding_params.isMessage = false;
					decoding_params.error = true;
					break;
				}
				if (version__minor == 0) {
					decoding_params.non_persistent_connection = true;
				}
			} else {
				isResponse = false;
				// The first line contains a request
				// like "POST / HTTP/1.0"
				Pattern responsePatternWithRequest = Pattern.compile("(.*)\\s+(HTTP|http)/(\\d{1}).(\\d{1})");
				Matcher responseMatcherWithRequest = responsePatternWithRequest.matcher(cc_first.substring(pos + 1, cc_first.indexOf("\r\n")));
				if (responseMatcherWithRequest.lookingAt()) {
					stext = responseMatcherWithRequest.group(1);
					version__major = Integer.valueOf(responseMatcherWithRequest.group(3));
					version__minor = Integer.valueOf(responseMatcherWithRequest.group(4));
				} else {
					decoding_params.isMessage = false;
					decoding_params.error = true;
					break;
				}
			}
			// Additional header lines
			TTCN_Logger.log(Severity.DEBUG_TESTPORT, "Decoding the headers");
			HTTP_decode_header(buffer, header, decoding_params, socket_debugging, isResponse, test_port_type, test_port_name);
			TTCN_Logger.log(Severity.DEBUG_TESTPORT, "Headers decoded. %s headers.", decoding_params.isMessage ? "Valid" : "Invalid");

			if (isResponse && decoding_params.content_length == -1) {
				if ((statusCode > 99 && statusCode < 200) || statusCode == 204 || statusCode == 304 ) {
					decoding_params.content_length = 0;
				}
			}
			if (decoding_params.isMessage) {
				HTTP_decode_body(buffer, body, decoding_params, connection_closed, socket_debugging, test_port_type, test_port_name);
			}
			if (decoding_params.isMessage) {
				TTCN_Logger.log(Severity.DEBUG_TESTPORT, "Message successfully decoded");
				boolean foundBinaryCharacter = false;
				byte[] ptr = body.get_value();
				for (int i = 0; i < ptr.length && !foundBinaryCharacter; i++) {
					if ((ptr[i] & 0xFF) > 0x7F) {
						foundBinaryCharacter = true;
					}
				}
				if (foundBinaryCharacter) {
					TTCN_Logger.log(Severity.DEBUG_TESTPORT, "Binary data found");
				}
				if (isResponse) {
					if (foundBinaryCharacter) {
						HTTPmsg__Types.HTTPResponse__binary__body response_binary = msg.get_field_response__binary();
						response_binary.get_field_client__id().set_to_omit();
						response_binary.get_field_version__major().operator_assign(version__major);
						response_binary.get_field_version__minor().operator_assign(version__minor);
						response_binary.get_field_statuscode().operator_assign(statusCode);
						if (stext != null && !stext.isEmpty()) {
							response_binary.get_field_statustext().operator_assign(new TitanCharString(stext));
						} else {
							response_binary.get_field_statustext().operator_assign("");
						}
						response_binary.get_field_header().operator_assign(header);
						response_binary.get_field_body().operator_assign(body);
					} else {
						HTTPmsg__Types.HTTPResponse response = msg.get_field_response();
						response.get_field_client__id().set_to_omit();
						response.get_field_version__major().operator_assign(version__major);
						response.get_field_version__minor().operator_assign(version__minor);
						response.get_field_statuscode().operator_assign(statusCode);
						if (stext != null && !stext.isEmpty()) {
							response.get_field_statustext().operator_assign(new TitanCharString(stext));
						} else {
							response.get_field_statustext().operator_assign("");
						}
						response.get_field_header().operator_assign(header);
						response.get_field_body().operator_assign(AdditionalFunctions.oct2char(body));
					}
				} else {
					if (foundBinaryCharacter) {
						HTTPmsg__Types.HTTPRequest__binary__body request_binary = msg.get_field_request__binary();
						request_binary.get_field_client__id().set_to_omit();
						request_binary.get_field_method().operator_assign(new TitanCharString(method_name));
						request_binary.get_field_uri().operator_assign(new TitanCharString(stext));
						request_binary.get_field_version__major().operator_assign(version__major);
						request_binary.get_field_version__minor().operator_assign(version__minor);
						request_binary.get_field_header().operator_assign(header);
						request_binary.get_field_body().operator_assign(body);
					} else {
						HTTPmsg__Types.HTTPRequest request = msg.get_field_request();
						request.get_field_client__id().set_to_omit();
						request.get_field_method().operator_assign(new TitanCharString(method_name));
						request.get_field_uri().operator_assign(new TitanCharString(stext));
						request.get_field_version__major().operator_assign(version__major);
						request.get_field_version__minor().operator_assign(version__minor);
						request.get_field_header().operator_assign(header);
						request.get_field_body().operator_assign(AdditionalFunctions.oct2char(body));
					}
				}
			}
			method_name = null;
			stext = null;
			break;
		case BUFFER_CRLF:
		case BUFFER_FAIL:
			decoding_params.error = true;
		case -1:
			decoding_params.isMessage = false;
		default:
			break;
		}
		if (decoding_params.error) {
			if (buffer.get_read_len() > 0) {
				msg.get_field_erronous__msg().get_field_msg().operator_assign(new TitanCharString(String.valueOf(buffer.get_read_data())));
			} else {
				msg.get_field_erronous__msg().get_field_msg().operator_assign(new TitanCharString("The previous message is erronous."));
			}
			msg.get_field_erronous__msg().get_field_client__id().set_to_omit();
			buffer.clear();
			decoding_params.isMessage = true;
		}
		if (decoding_params.isMessage) {
			buffer.cut();
		}
		return decoding_params.isMessage;
	}

	/**
	 * Decode a HTTP headers from the da
	 * 
	 * @param buffer
	 * @param headers
	 * @param decoding_params
	 * @param socket_debugging
	 * @param resp
	 * @param test_port_type
	 * @param test_port_name
	 */
	public static void HTTP_decode_header(TTCN_Buffer buffer, HTTPmsg__Types.HeaderLines headers, Decoding_Params decoding_params, final boolean socket_debugging, final boolean resp, final String test_port_type, final String test_port_name) {
		TitanCharString cstr = new TitanCharString("");
		final char separator = ':';
		String header_name = null;
		String header_value = null;
		boolean length_received = false;

		for (int i = 0; ; i++) {
			switch (get_line(buffer, cstr, true)) {
			//TRUE
			case 1:
				String h = cstr.get_value().toString();
				if (h.indexOf(separator) != -1) {
					header_name = h.substring(0, h.indexOf(separator)).strip();
					int index = h.indexOf(separator) + 1;
					header_value = h.substring(index);
					header_value = header_value.split("\r\n")[0].strip();
					headers.get_at(i).operator_assign(new HTTPmsg__Types.HeaderLine(new TitanCharString(header_name), new TitanCharString(header_value)));
					log_debug(socket_debugging, test_port_type, test_port_name, "+Header line: <%s: %s>", header_name, header_value);

					if (header_name.equalsIgnoreCase("Content-Length")) {
						decoding_params.content_length = Integer.valueOf(header_value);
						length_received = true;
					} else if (header_name.equalsIgnoreCase("Connection") && header_value.equalsIgnoreCase("close")) {
						decoding_params.non_persistent_connection = true;
					} else if (header_name.equalsIgnoreCase("Connection") && header_value.equalsIgnoreCase("keep-alive")) {
						decoding_params.non_persistent_connection = false;
					} else if (header_name.equalsIgnoreCase("Transfer-Encoding") && header_value.equalsIgnoreCase("chunked")) {
						decoding_params.chunked_body = true;
					}
				}
				continue;
			case BUFFER_FAIL:
				log_debug(socket_debugging, test_port_type, test_port_name, "BUFFER_FAIL in HTTP_decode_header!");
				log_debug(socket_debugging, test_port_type, test_port_name, "whole bufer now: <%s>", Arrays.toString(buffer.get_data()));;
				decoding_params.error = true;
			case -1:
				decoding_params.isMessage = false;
			case BUFFER_CRLF:
				break;
			default:
				break;
			}
			break;
		}
		if (decoding_params.isMessage && !resp && !length_received && !decoding_params.chunked_body) {
			decoding_params.content_length = 0;
		}
		header_name = null;
	}

	public static void HTTP_decode_body(final TTCN_Buffer buffer, TitanOctetString body, final Decoding_Params decoding_params, final boolean connection_closed, final boolean socket_debugging, final String test_port_type, final String test_port_name) {
		if (buffer.get_read_len() > 0) {
			log_debug(socket_debugging, test_port_type, test_port_name, "Decoding body, buffer length: %d", buffer.get_read_len());
		}

		if (decoding_params.chunked_body) {
			HTTP_decode_chunked_body(buffer, body, decoding_params, socket_debugging, test_port_type, test_port_name);
			log_debug(socket_debugging, test_port_type, test_port_name,  "--------- After chunked body decoding:");
			log_debug(socket_debugging, test_port_type, test_port_name,  "--------- non_persistent_connection: %s",  decoding_params.non_persistent_connection ? "yes" : "no");
			log_debug(socket_debugging, test_port_type, test_port_name,  "--------- chunked_body: %s",  decoding_params.chunked_body ? "yes" : "no");
			log_debug(socket_debugging, test_port_type, test_port_name,  "--------- content_length: %d",  decoding_params.content_length);
			log_debug(socket_debugging, test_port_type, test_port_name,  "--------- error: %s",  decoding_params.error ? "yes" : "no");
			log_debug(socket_debugging, test_port_type, test_port_name,  "--------- isMessage: %s",  decoding_params.isMessage ? "yes" : "no");
		} else if (decoding_params.content_length >= 0) {
			log_debug(socket_debugging, test_port_type, test_port_name,  "lengthof body: %d, content_length given: %d", buffer.get_read_len(), decoding_params.content_length);
			if (buffer.get_read_len() >= decoding_params.content_length) {
				body.operator_assign(new TitanOctetString(buffer.get_read_data()));
				buffer.set_pos(buffer.get_pos() + decoding_params.content_length);
			} else {
				decoding_params.isMessage = false;
				log_debug(socket_debugging, test_port_type, test_port_name,  "The decoder's body length %d is less than the Content_length in the message header %d; The HTTP port is waiting for additional data.", buffer.get_read_len(), decoding_params.content_length);
				buffer.set_pos(buffer.get_pos() + buffer.get_read_len());
			}
		} else if (connection_closed) {
			body.operator_assign(new TitanOctetString(buffer.get_read_data()));
			buffer.set_pos(buffer.get_pos() + buffer.get_read_len());
		} else {
			decoding_params.isMessage = false;
			log_debug(socket_debugging, test_port_type, test_port_name,  "The HTTP port is waiting for additional data.");
			buffer.set_pos(buffer.get_pos() + buffer.get_read_len());
		}
	}

	public static void HTTP_decode_chunked_body(final TTCN_Buffer buffer, TitanOctetString body, final Decoding_Params decoding_params, final boolean socket_debugging, final String test_port_type, final String test_port_name) {
		TitanCharString line = new TitanCharString("");
		int chunk_size = 1;

		while (chunk_size > 0) {
			switch (get_line(buffer, line, false)) {
			//TRUE
			case 1:
				log_debug(socket_debugging, test_port_type, test_port_name, "line: <%s>", line.get_value().toString());
				try {
					chunk_size = Integer.parseInt(line.get_value().toString(), 16);
				} catch (NumberFormatException e) {
					log_debug(socket_debugging, test_port_type, test_port_name, "No chunksize found");
					body.operator_assign(body.operator_concatenate(new TitanOctetString(line.get_value().toString().getBytes())));
					chunk_size = 0;
					decoding_params.error = true;
				}
				if (chunk_size == 0) {
					log_debug(socket_debugging, test_port_type, test_port_name, "chunk_size 0 -> closing chunk");
					if (get_line(buffer, line, false) == BUFFER_CRLF) {
						log_debug(socket_debugging, test_port_type, test_port_name,  "Trailing \\r\\n ok!");
					} else {
						TTCN_Logger.log(Severity.WARNING_UNQUALIFIED, "Trailing \\r\\n after the closing chunk is not present, instead it is <%s>!", line.get_value().toString());
					}
				} else {
					// chunk_size > 0
					log_debug(socket_debugging, test_port_type, test_port_name,  "processing next chunk, size: %d", chunk_size);
					if (buffer.get_read_len() < chunk_size) {
						log_debug(socket_debugging, test_port_type, test_port_name,  "chunk size is greater than the buffer length, more data is needed");
						decoding_params.isMessage = false;
						chunk_size = 0;
					}
				}
				break;
				//FALSE
			case -1:
				log_debug(socket_debugging, test_port_type, test_port_name,  "buffer does not contain a whole line, more data is needed");
				decoding_params.isMessage = false;
				chunk_size = 0;
				break;
			case BUFFER_CRLF:
				log_debug(socket_debugging, test_port_type, test_port_name,  "beginning CRLF removed");
				continue;
			case BUFFER_FAIL:
				log_debug(socket_debugging, test_port_type, test_port_name,  "BUFFER_FAIL");
				decoding_params.error = false;
				chunk_size = 0;
				break;
			default:
				decoding_params.isMessage = false;
				chunk_size = 0;
				log_debug(socket_debugging, test_port_type, test_port_name,  "more data is needed");
			}

			body.operator_assign(body.operator_concatenate(new TitanOctetString(buffer.get_read_data())));
			log_debug(socket_debugging, test_port_type, test_port_name,  "pull %d bytes from %d", chunk_size, buffer.get_read_len());
			buffer.set_pos(buffer.get_pos() + chunk_size);
			//hack - It would be fine If we don't need these hacks. (Gergo Ujhelyi)
			if (buffer.get_read_len() > 0 && buffer.get_read_data()[0] == '\n') { // don't read from the buffer if there is nothing in it.
				log_debug(socket_debugging, test_port_type, test_port_name,"hack: adjusted buffer position after the '\\n'");
				buffer.set_pos(buffer.get_pos() + 1);
			}
			log_debug(socket_debugging, test_port_type, test_port_name,  "remaining data: <%s>, len: %d", String.valueOf(buffer.get_read_len()) + Arrays.toString(buffer.get_read_data()), buffer.get_read_len());
		}
	}

	public static int get_line(TTCN_Buffer buffer, TitanCharString to, final boolean concatenate_header_lines) {
		int i = 0;
		final byte[] cc_to = buffer.get_read_data();

		if (buffer.get_read_len() <= 0) {
			return 0;
		}

		while (true) {
			for (; i < buffer.get_read_len() && cc_to[i] != '\0' && cc_to[i] != '\r' && cc_to[i] != '\n'; i++) {}

			if (i >= buffer.get_read_len()) {
				to = new TitanCharString("");
				return 0;
			} else {
				if (cc_to[i] == '\n') {
					if (report_lf) {
						switch (HTTPmsg__Types.crlf__mode.enum_value) {
						case ERROR_:
							return BUFFER_FAIL;
						case WARNING__ONCE:
							report_lf = false;
							//no break
						case WARNING:
							TtcnError.TtcnWarning("Missing '\\r'.");
							break;
						default:
							break;
						}
					}

					if (i> 0 && (i + 1) < buffer.get_read_len() && concatenate_header_lines && (cc_to[i+1] == ' ' || cc_to[i+1] == '\t')) {
						i += 1;
					} else {
						to.operator_assign(new String(cc_to));
						buffer.set_pos(buffer.get_pos() + i + 1);
						return i == 0 ? BUFFER_CRLF : 1;
					}
				} else {
					if ((i + 1) < buffer.get_read_len() && cc_to[i + 1] != '\n') {
						return BUFFER_FAIL;
					} else if (i > 0 && (i + 2) < buffer.get_read_len() && concatenate_header_lines && (cc_to[i+2] == ' ' || cc_to[i+2] == '\t')) {
						i += 2;
					} else {
						to.operator_assign(new String(cc_to));
						buffer.set_pos(buffer.get_pos() + i + 2);
						return i == 0 ? BUFFER_CRLF : 1;
					}
				}
			}
		}
	}

	public static void f_setClientId(HTTPmsg__Types.HTTPMessage msg, final int client_id) {
		switch (msg.get_selection()) {
		case ALT_request:
			msg.get_field_request().get_field_client__id().get().operator_assign(client_id);
			break;
		case ALT_request__binary:
			msg.get_field_request__binary().get_field_client__id().get().operator_assign(client_id);
			break;
		case ALT_response:
			msg.get_field_response().get_field_client__id().get().operator_assign(client_id);
			break;
		case ALT_response__binary:
			msg.get_field_response__binary().get_field_client__id().get().operator_assign(client_id);
			break;
		case ALT_erronous__msg:
			msg.get_field_erronous__msg().get_field_client__id().get().operator_assign(client_id);
			break;
		default:
			break;
		}
	}

	protected boolean HTTP_decode(final TTCN_Buffer buffer, final int client_id, final boolean connection_closed) {

		HTTPmsg__Types.HTTPMessage msg = new HTTPMessage();

		if (f_HTTP_decodeCommon(buffer, msg, connection_closed, abstract_Socket.get_socket_debugging(), abstract_Socket.test_port_name, abstract_Socket.test_port_type)) {
			TTCN_Logger.log(Severity.DEBUG_TESTPORT, "HTTPmsg__PT.HTTP_decode, before calling incoming_message");
			f_setClientId(msg,client_id);
			incoming_message(msg);
			TTCN_Logger.log(Severity.DEBUG_TESTPORT, "HTTPmsg__PT.HTTP_decode, after calling incoming_message");
			return true;
		}
		return false;
	}

	/**
	 * Log debug information function.
	 * 
	 * @param socket_debugging debugging flag
	 * @param test_port_type test port type
	 * @param test_port_name test port name
	 * @param fmt the format string
	 * @param args the arguments for the format string
	 */
	public static void log_debug(final boolean socket_debugging, final String test_port_type, final String test_port_name, final String fmt, Object... args) {
		if (socket_debugging) {
			TTCN_Logger.begin_event(Severity.DEBUG_TESTPORT);
			if ((test_port_type != null && test_port_name != null) && (!test_port_type.isEmpty() && !test_port_name.isEmpty())) {
				TTCN_Logger.log_event("%s test port (%s): ", test_port_type, test_port_name);
			}
			TTCN_Logger.log_event_va_list(fmt, args);
			TTCN_Logger.end_event();
		}
	}

	public static class Decoding_Params {

		public boolean non_persistent_connection;
		public boolean chunked_body;
		public int content_length;
		public boolean error;
		public boolean isMessage;
	}
}
