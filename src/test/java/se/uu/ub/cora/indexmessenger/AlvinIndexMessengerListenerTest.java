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
import static org.testng.Assert.assertTrue;

import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.indexmessenger.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessagingProvider;

public class AlvinIndexMessengerListenerTest {
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "AlvinIndexMessengerListener";
	private MessagingFactorySpy messagingFactorySpy;
	private CoraClientFactorySpy coraClientFactory;
	private AlvinIndexMessengerListener iml;
	private Properties properties;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		messagingFactorySpy = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactorySpy);

		coraClientFactory = new CoraClientFactorySpy();
		properties = new Properties();
		properties.put("messaging.hostname", "messaging.alvin-portal.org");
		properties.put("messaging.port", "5672");
		properties.put("messaging.virtualHost", "alvin");
		properties.put("messaging.exchange", "index");
		properties.put("messaging.routingKey", "#");

		iml = new AlvinIndexMessengerListener(coraClientFactory, properties);
	}

	@Test
	public void testInitCreatesAmpqMessageRoutingInfoFromProperties() throws Exception {
		// assertEquals(messagingFactorySpy.factorTopicMessageListenerCalled, false);

		assertEquals(messagingFactorySpy.factorTopicMessageListenerCalled, true);
		AmqpMessageRoutingInfo messagingRoutingInfo = (AmqpMessageRoutingInfo) messagingFactorySpy.messagingRoutingInfo;
		assertEquals(messagingRoutingInfo.hostname, "messaging.alvin-portal.org");
		assertEquals(messagingRoutingInfo.port, "5672");
		assertEquals(messagingRoutingInfo.virtualHost, "alvin");
		assertEquals(messagingRoutingInfo.exchange, "index");
		assertEquals(messagingRoutingInfo.routingKey, "#");

		// todo: new test
	}
	// MessageReceiver messageReceiver = new MessageReceiverSpy();

	// @Test
	// public void testErrorHandling() throws Exception {
	// MessagingFactory messagingFactorySpy = new MessagingFactoryErrorThrowingSpy();
	// MessagingProvider.setMessagingFactory(messagingFactorySpy);
	// AlvinIndexMessengerListener iml = new AlvinIndexMessengerListener(clientFactory,
	// properties);
	// assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
	// Exception exception = loggerFactorySpy.getFatalLogErrorUsingClassNameAndNo(testedClassName,
	// 0);
	// assertTrue(exception instanceof RuntimeException);
	// assertEquals(exception.getMessage(), "Error from MessagingFactoryErrorThrowingSpy");
	// assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
	// "Error during initializing of AlvinIndexMessengerListener");
	// }

	@Test
	public void testReceiverIsIndexReceiver() throws Exception {
		MessageReceiver messageReceiver = messagingFactorySpy.messageListenerSpy.messageReceiver;
		assertTrue(messageReceiver instanceof IndexMessageReceiver);
		// assertEquals(messageReceiver.getCoraClient(), clientFactory.factoredClient);

	}

	@Test
	public void testReceiverContainsCoraClientFromCoraClientFactory() throws Exception {
		// TODO: how is this true yet?
		assertTrue(coraClientFactory.factoredHasBeenCalled);
		// IndexMessageReceiver messageReceiver = (IndexMessageReceiver)
		// messagingFactorySpy.messageListenerSpy.messageReceiver;
		// assertSame(messageReceiver.getCoraClient(), coraClientFactory.factoredClient);
		// assertEquals(messageReceiver.getCoraClient(), clientFactory.factoredClient);

	}
}
