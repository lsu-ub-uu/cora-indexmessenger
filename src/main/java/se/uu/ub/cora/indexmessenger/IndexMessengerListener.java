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

import se.uu.ub.cora.indexmessenger.parser.MessageParserFactory;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class IndexMessengerListener {
	private CoraClientFactory coraClientFactory;
	private MessageParserFactory messageParserFactory;
	private MessageRoutingInfo routingInfo;
	private CoraCredentials credentials;

	public IndexMessengerListener(CoraClientFactory coraClientFactory,
			MessageParserFactory messageParserFactory, MessageRoutingInfo routingInfo,
			CoraCredentials credentials) {

		this.coraClientFactory = coraClientFactory;
		this.messageParserFactory = messageParserFactory;
		this.routingInfo = routingInfo;
		this.credentials = credentials;

		MessageListener topicMessageListener = MessagingProvider
				.getTopicMessageListener(routingInfo);

		CoraClient coraClient = createCoraClient(coraClientFactory, credentials);

		MessageReceiver messageReceiver = new IndexMessageReceiver(coraClient,
				messageParserFactory);

		topicMessageListener.listen(messageReceiver);
	}

	private final CoraClient createCoraClient(CoraClientFactory coraClientFactory,
			CoraCredentials credentials) {
		String coraUserId = credentials.userId;
		String coraAppToken = credentials.appToken;
		return coraClientFactory.factor(coraUserId, coraAppToken);
	}

	public CoraClientFactory getCoraClientFactory() {
		// needed for test
		return coraClientFactory;
	}

	public MessageParserFactory getMessageParserFactory() {
		// needed for test
		return messageParserFactory;
	}

	public MessageRoutingInfo getMessageRoutingInfo() {
		// needed for test
		return routingInfo;
	}

	public CoraCredentials getCredentials() {
		// needed for test
		return credentials;
	}
}
