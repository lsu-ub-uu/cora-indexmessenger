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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.indexmessenger.log.LoggerFactorySpy;
import se.uu.ub.cora.indexmessenger.parser.MessageParserFactory;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessagingProvider;

public class IndexMessengerListenerTest {
	private LoggerFactorySpy loggerFactorySpy;
	private MessagingFactorySpy messagingFactorySpy;
	private MessageParserFactory messageParserFactory;
	private IndexMessengerListener messageListener;
	private AmqpMessageRoutingInfo routingInfo;
	private CoraCredentials credentials;

	@BeforeMethod
	public void beforeMethod() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		messagingFactorySpy = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactorySpy);

		CoraClientFactorySpy coraClientFactory = new CoraClientFactorySpy();
		messageParserFactory = new MessageParserFactorySpy();

		credentials = new CoraCredentials("userIdForCora", "appTokenForCora");
		routingInfo = new AmqpMessageRoutingInfo("messaging.alvin-portal.org", "5672", "alvin",
				"index", "#");
		messageListener = new IndexMessengerListener(coraClientFactory, messageParserFactory,
				routingInfo, credentials);
	}

	@Test
	public void testInitPassedAmpqMessageRoutingInfoToTopicMessageListener() throws Exception {
		assertEquals(messagingFactorySpy.factorTopicMessageListenerCalled, true);
		AmqpMessageRoutingInfo messagingRoutingInfo = (AmqpMessageRoutingInfo) messagingFactorySpy.messagingRoutingInfo;

		assertSame(messagingRoutingInfo, routingInfo);
	}

	@Test
	public void testReceiverIsIndexReceiver() throws Exception {
		MessageReceiver messageReceiver = messagingFactorySpy.messageListenerSpy.messageReceiver;
		assertTrue(messageReceiver instanceof IndexMessageReceiver);
	}

	@Test
	public void testCoraClientFactoryIsCalledCorrectly() throws Exception {
		CoraClientFactorySpy coraClientFactory = (CoraClientFactorySpy) messageListener
				.getCoraClientFactory();
		assertTrue(coraClientFactory.factoredHasBeenCalled);
		assertEquals(coraClientFactory.userId, credentials.userId);
		assertEquals(coraClientFactory.appToken, credentials.appToken);
	}

	@Test
	public void testMessageReceiverContainsCorrectCoraClient() {
		CoraClientFactorySpy coraClientFactory = (CoraClientFactorySpy) messageListener
				.getCoraClientFactory();
		IndexMessageReceiver messageReceiver = (IndexMessageReceiver) messagingFactorySpy.messageListenerSpy.messageReceiver;
		assertSame(messageReceiver.getCoraClient(), coraClientFactory.factoredClient);
	}

	@Test
	public void testMessageParserFactoryIsSentToCoraClient() {
		IndexMessageReceiver messageReceiver = (IndexMessageReceiver) messagingFactorySpy.messageListenerSpy.messageReceiver;
		assertSame(messageReceiver.getMessageParserFactory(), messageParserFactory);
	}

	@Test
	public void testGetMessageParserFactory() throws Exception {
		assertSame(messageListener.getMessageParserFactory(), messageParserFactory);
	}

	@Test
	public void testGetMessageRoutingInfo() throws Exception {
		assertSame(messageListener.getMessageRoutingInfo(), routingInfo);
	}

	@Test
	public void testGetCredentials() throws Exception {
		assertSame(messageListener.getCredentials(), credentials);
	}

}
