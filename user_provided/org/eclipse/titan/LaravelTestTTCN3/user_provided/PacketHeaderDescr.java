/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.LaravelTestTTCN3.user_provided;

/********************************
 **  PacketHeaderDescr
 **  used for fragmentation and concatenation
 **  of fixed format messages
 *********************************
 *
 * @author Gergo Ujhelyi
 */
public class PacketHeaderDescr {

	// Byte order in the header
	public enum HeaderByteOrder{ Header_MSB, Header_LSB };

	private long length_offset;
	private long nr_bytes_in_length;
	private HeaderByteOrder byte_order;
	private long value_offset;
	private long length_multiplier;

	public PacketHeaderDescr(long length_offset, long nr_bytes_in_length, HeaderByteOrder byte_order, long value_offset, long length_multiplier) {
		this.length_offset = length_offset;
		this.nr_bytes_in_length = nr_bytes_in_length;
		this.byte_order = byte_order;
		this.value_offset = value_offset;
		this.length_multiplier = length_multiplier;
	}

	public PacketHeaderDescr(long length_offset, long nr_bytes_in_length, HeaderByteOrder byte_order) {
		this.length_offset = length_offset;
		this.nr_bytes_in_length = nr_bytes_in_length;
		this.byte_order = byte_order;
		this.value_offset = 0;
		this.length_multiplier = 1;
	}

	public long Get_Message_Length(final byte[] buffer_pointer) {
		if (buffer_pointer == null) {
			return 0L;
		}
		long m_length = 0;
		for (int i = 0; i < nr_bytes_in_length; i++) {
			long shift_count = byte_order == HeaderByteOrder.Header_MSB ? nr_bytes_in_length - 1 - i : i;
			m_length |= (long) buffer_pointer[(int) (length_offset + i)] << (8 * shift_count);
		}
		m_length *= length_multiplier;
		if (value_offset < 0 && (long)m_length < -value_offset) {
			return 0L;
		} else {
			return m_length + value_offset;
		}
	}

	public long Get_Valid_Header_Length() {
		return length_offset + nr_bytes_in_length;
	}

	public long getLength_offset() {
		return length_offset;
	}

	public long getNr_bytes_in_length() {
		return nr_bytes_in_length;
	}

	public HeaderByteOrder getByte_order() {
		return byte_order;
	}

	public long getValue_offset() {
		return value_offset;
	}

	public long getLength_multiplier() {
		return length_multiplier;
	}
}
