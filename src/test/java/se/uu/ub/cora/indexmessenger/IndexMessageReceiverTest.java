/*
 * Copyright 2019 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.indexmessenger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.indexmessenger.log.LoggerFactorySpy;
import se.uu.ub.cora.indexmessenger.parser.MessageParserSpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessageReceiver;

public class IndexMessageReceiverTest {
	private String message;
	private Map<String, Object> headers;
	private CoraClientSpy coraClientSpy;
	private MessageReceiver receiver;

	private LoggerFactorySpy loggerFactory;
	private String testedClassname = "IndexMessageReceiver";
	private MessageParserSpy messageParserSpy;

	@BeforeMethod
	public void setUp() {
		loggerFactory = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactory);

		headers = new HashMap<>();
		headers.put("__TypeId__", "epc.messaging.amqp.EPCFedoraMessage");
		headers.put("ACTION", "UPDATE");
		headers.put("PID", "alvin-place:1");
		headers.put("messageSentFrom", "Cora");

		message = "{\"pid\":\"alvin-place:1\",\"routingKey\":\"alvin.updates.place\","
				+ "\"action\":\"UPDATE\",\"dsId\":null,"
				+ "\"headers\":{\"ACTION\":\"UPDATE\",\"PID\":\"alvin-place:1\"}}";
		coraClientSpy = new CoraClientSpy();
		messageParserSpy = new MessageParserSpy();
		receiver = new IndexMessageReceiver(coraClientSpy, messageParserSpy);
	}

	@Test
	public void testReceiveMessageCreatesCoraClientForWorkOrder() {
		receiver.receiveMessage(headers, message);

		assertTrue(coraClientSpy.createWasCalled);
		assertEquals(coraClientSpy.createdRecordType, "workOrder");
	}

	@Test
	public void testReceiveMessageUsesMessageParserToGetTypeAndId() throws Exception {
		receiver.receiveMessage(headers, message);
		assertSame(messageParserSpy.headers, headers);
		assertSame(messageParserSpy.message, message);
	}

	@Test
	public void testReceiveMessageUsesParserToGetId() throws Exception {
		receiver.receiveMessage(headers, message);

	}

	@Test
	public void testReceiveMessageUsesCorrectRecordTypeAndId() throws Exception {
		receiver.receiveMessage(headers, message);

		ClientDataGroup createdDataGroup = coraClientSpy.createdDataGroup;
		assertCorrectNameInDataAndTypeForIndexOrder(createdDataGroup);
		assertCorrectRecordId(createdDataGroup, "alvin-place:1");
		assertCorrectLinkedRecordType(createdDataGroup, "place");
	}

	@Test
	public void testReceiveMessageUsesCorrectPersonRecordTypeAndId() throws Exception {
		headers.put("PID", "alvin-person:2");
		message = "{\"pid\":\"alvin-person:2\",\"routingKey\":\"alvin.updates.person\","
				+ "\"action\":\"UPDATE\",\"dsId\":null,"
				+ "\"headers\":{\"ACTION\":\"UPDATE\",\"PID\":\"alvin-person:2\"}}";

		receiver.receiveMessage(headers, message);

		ClientDataGroup createdDataGroup = coraClientSpy.createdDataGroup;
		assertCorrectNameInDataAndTypeForIndexOrder(createdDataGroup);
		assertCorrectRecordId(createdDataGroup, "alvin-person:2");
		assertCorrectLinkedRecordType(createdDataGroup, "person");
	}

	// @Test(expectedExceptions = IndexMessageException.class)
	// public void testLoggingErrorNoPidInHeader() throws Exception {
	// headers.remove("PID");
	//
	// receiver.receiveMessage(headers, message);
	// }

	// TODO: do not send index order for message from cora (messageSentFromCora).

	private void assertCorrectNameInDataAndTypeForIndexOrder(ClientDataGroup createdDataGroup) {
		assertEquals(createdDataGroup.getNameInData(), "workOrder");
		String type = createdDataGroup.getFirstAtomicValueWithNameInData("type");
		assertEquals(type, "index");
	}

	private void assertCorrectRecordId(ClientDataGroup createdDataGroup, String expectedRecordId) {
		String recordId = createdDataGroup.getFirstAtomicValueWithNameInData("recordId");
		assertEquals(recordId, expectedRecordId);
	}

	private void assertCorrectLinkedRecordType(ClientDataGroup createdDataGroup,
			String recordType) {
		ClientDataGroup recordTypeGroup = createdDataGroup
				.getFirstGroupWithNameInData("recordType");
		String linkedRecordType = recordTypeGroup
				.getFirstAtomicValueWithNameInData("linkedRecordType");
		assertEquals(linkedRecordType, "recordType");
		String linkedRecordId = recordTypeGroup.getFirstAtomicValueWithNameInData("linkedRecordId");
		assertEquals(linkedRecordId, recordType);
	}
}
