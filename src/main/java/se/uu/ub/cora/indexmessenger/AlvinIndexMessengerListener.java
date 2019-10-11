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

import java.util.Properties;

import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessagingProvider;

public class AlvinIndexMessengerListener {
	private Logger logger = LoggerProvider.getLoggerForClass(AlvinIndexMessengerListener.class);
	private CoraClientFactory coraClientFactory;
	private MessageParserFactory messageParserFactory;

	// routingInfo, coraClientFactory, messageParserFactory
	public AlvinIndexMessengerListener(CoraClientFactory coraClientFactory, Properties properties,
			MessageParserFactory messageParserFactory) {
		// try (InputStream input = AlvinIndexMessengerListener.class.getClassLoader()
		// .getResourceAsStream("alvinIndexer.properties")) {
		// Properties prop = new Properties();
		// prop.load(input);

		this.coraClientFactory = coraClientFactory;
		this.messageParserFactory = messageParserFactory;

		AmqpMessageRoutingInfo routingInfo = createMessageRoutingInfo(properties);
		MessageListener topicMessageListener = MessagingProvider
				.getTopicMessageListener(routingInfo);

		String userId = properties.getProperty("userId");
		String appToken = properties.getProperty("appToken");
		CoraClient coraClient = coraClientFactory.factor(userId, appToken);
		MessageReceiver messageReceiver = new IndexMessageReceiver(coraClient,
				messageParserFactory);
		topicMessageListener.listen(messageReceiver);

		// } catch (Exception ex) {
		// logger.logFatalUsingMessageAndException(
		// "Error during initializing of AlvinIndexMessengerListener", ex);
		// }
	}

	private AmqpMessageRoutingInfo createMessageRoutingInfo(Properties prop) {
		String hostname = prop.getProperty("messaging.hostname");
		String port = prop.getProperty("messaging.port");
		String virtualHost = prop.getProperty("messaging.virtualHost");
		String exchange = prop.getProperty("messaging.exchange");
		String routingKey = prop.getProperty("messaging.routingKey");
		return new AmqpMessageRoutingInfo(hostname, port, virtualHost, exchange, routingKey);
	}

	public CoraClientFactory getCoraClientFactory() {
		// needed for test
		return coraClientFactory;
	}

	public MessageParserFactory getMessageParserFactory() {
		// needed for test
		return messageParserFactory;
	}

}
// hostname
// port
// virtualHost
// exchange
// routingKey

// vilken factory MessageParserFactory

// appTokenVerifierUrl
// baseUrl
// userId
// appToken
