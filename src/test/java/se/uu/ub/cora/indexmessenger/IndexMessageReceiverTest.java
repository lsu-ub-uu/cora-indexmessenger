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
import static org.testng.Assert.assertFalse;
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
	private Map<String, String> headers;
	private CoraClientSpy coraClientSpy;
	private MessageReceiver receiver;

	private LoggerFactorySpy loggerFactory;
	private String testedClassname = "IndexMessageReceiver";
	private MessageParserFactorySpy messageParserFactorySpy;

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
		messageParserFactorySpy = new MessageParserFactorySpy();
		receiver = new IndexMessageReceiver(coraClientSpy, messageParserFactorySpy);
	}

	@Test
	public void testMessageParserWasFactored() {
		receiver.receiveMessage(headers, message);
		assertTrue(messageParserFactorySpy.factorWasCalled);
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
		MessageParserSpy messageParserSpy = messageParserFactorySpy.messageParserSpy;
		assertSame(messageParserSpy.headers, headers);
		assertSame(messageParserSpy.message, message);
	}

	@Test
	public void testReceiveMessageUsesParserToGetId() throws Exception {
		receiver.receiveMessage(headers, message);
		MessageParserSpy messageParserSpy = messageParserFactorySpy.messageParserSpy;
		assertTrue(messageParserSpy.getParsedIdWasCalled);

		ClientDataGroup createdDataGroup = coraClientSpy.createdDataGroup;
		String recordIdFromWorkOrder = createdDataGroup
				.getFirstAtomicValueWithNameInData("recordId");
		assertEquals(recordIdFromWorkOrder, messageParserSpy.getParsedId());
	}

	@Test
	public void testReceiveMessageUsesParserToGetType() throws Exception {
		receiver.receiveMessage(headers, message);

		MessageParserSpy messageParserSpy = messageParserFactorySpy.messageParserSpy;
		assertTrue(messageParserSpy.getParsedTypeWasCalled);

		ClientDataGroup createdDataGroup = coraClientSpy.createdDataGroup;
		ClientDataGroup recordTypeGroup = createdDataGroup
				.getFirstGroupWithNameInData("recordType");
		String recordTypeFromWorkOrder = recordTypeGroup
				.getFirstAtomicValueWithNameInData("linkedRecordId");
		assertEquals(recordTypeFromWorkOrder, messageParserSpy.getParsedType());
	}

	@Test
	public void testNOWorkOrderCreatedWhenParserReturnsFalse() throws Exception {
		messageParserFactorySpy.createWorkOrder = false;

		receiver.receiveMessage(headers, message);

		assertFalse(coraClientSpy.createWasCalled);
	}

	@Test
	public void testLogInfoWhenWorkOrderCreated() throws Exception {
		assertEquals(loggerFactory.getNoOfInfoLogMessagesUsingClassname(testedClassname), 0);

		receiver.receiveMessage(headers, message);

		assertEquals(loggerFactory.getNoOfInfoLogMessagesUsingClassname(testedClassname), 1);
	}

	@Test
	public void testLogInfoWhenWorkOrderCreatedCorrectMessage() throws Exception {
		receiver.receiveMessage(headers, message);

		String firstInfoLogMessage = loggerFactory
				.getInfoLogMessageUsingClassNameAndNo(testedClassname, 0);
		assertEquals(firstInfoLogMessage,
				"Index workOrder created for type: someParsedTypeFromMessageParserSpy "
						+ "and id: someParsedIdFromMessageParserSpy");
	}

	@Test
	public void testLogErrorWhenWorkOrderFailedToBeCreated() throws Exception {
		coraClientSpy.throwErrorOnCreate = true;
		assertEquals(loggerFactory.getNoOfErrorLogMessagesUsingClassName(testedClassname), 0);

		receiver.receiveMessage(headers, message);

		assertEquals(loggerFactory.getNoOfErrorLogMessagesUsingClassName(testedClassname), 1);
		String firstErrorLogMessage = loggerFactory
				.getErrorLogMessageUsingClassNameAndNo(testedClassname, 0);
		assertEquals(firstErrorLogMessage,
				"Index workOrder NOT created for type: someParsedTypeFromMessageParserSpy "
						+ "and id: someParsedIdFromMessageParserSpy");
		Exception firstErrorException = loggerFactory
				.getErrorLogErrorUsingClassNameAndNo(testedClassname, 0);
		assertEquals(firstErrorException.getMessage(), "Error from CoraClientSpy on create");
		assertEquals(loggerFactory.getNoOfInfoLogMessagesUsingClassname(testedClassname), 0);
	}

	@Test
	public void testLogFatalWhenTopicGetsClosed() throws Exception {
		assertEquals(loggerFactory.getNoOfFatalLogMessagesUsingClassName(testedClassname), 0);

		receiver.topicClosed();

		assertEquals(loggerFactory.getNoOfFatalLogMessagesUsingClassName(testedClassname), 1);
	}

	@Test
	public void testLogFatalWhenTopicGetsClosedCorrectMessage() throws Exception {
		receiver.topicClosed();

		String firstFatalLogMessage = loggerFactory
				.getFatalLogMessageUsingClassNameAndNo(testedClassname, 0);
		assertEquals(firstFatalLogMessage, "Topic closed!");
	}

}
