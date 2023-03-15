package org.eclipse.titan.LaravelTestTTCN3.testcases;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.titan.LaravelTestTTCN3.user_provided.Abstract_Socket.READING_STATES;
import org.eclipse.titan.LaravelTestTTCN3.user_provided.Abstract_Socket.TCP_STATES;
import org.eclipse.titan.LaravelTestTTCN3.user_provided.Abstract_Socket.as_client_struct;
import org.eclipse.titan.runtime.core.TTCN_Buffer;
import org.junit.jupiter.api.Test;

public class AsClientTests {
	
	@Test
	public void asClientEmptyConstructTest() {
		System.out.println("=================== as_client_struct class empty constructor test ===================");
		as_client_struct testClient = new as_client_struct();
		assertNull(testClient.user_data);
		assertNull(testClient.fd_buff);
		assertNull(testClient.tcp_socket);
		assertNull(testClient.reading_state); // enum value/class default value is null
		assertNull(testClient.tcp_state); // enum value/class default value is null
	}
	
	@Test
	public void asClientConstructTest() {
		System.out.println("=================== as_client_struct class constructor test ===================");
		TTCN_Buffer dummyTTCNBuffer = new TTCN_Buffer();
		as_client_struct testClient = new as_client_struct(null, dummyTTCNBuffer, null, TCP_STATES.FIN_WAIT, READING_STATES.STATE_NORMAL);
	
		assertEquals(dummyTTCNBuffer, testClient.fd_buff);
		assertNull(testClient.clientAddr);
		assertEquals(TCP_STATES.FIN_WAIT, testClient.tcp_state);
		assertEquals(READING_STATES.STATE_NORMAL, testClient.reading_state);	
	}
}
