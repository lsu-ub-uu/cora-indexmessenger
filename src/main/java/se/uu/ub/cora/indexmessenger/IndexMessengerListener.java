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

import se.uu.ub.cora.messaging.MessageListener;
import se.uu.ub.cora.messaging.MessageReceiver;
import se.uu.ub.cora.messaging.MessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class IndexMessengerListener {
	private MessageRoutingInfo messagingRoutingInfo;

	public IndexMessengerListener(MessageRoutingInfo messagingRoutingInfo,
			MessageReceiver messageReceiver) {
		this.messagingRoutingInfo = messagingRoutingInfo;
		MessageListener topicMessageListener = MessagingProvider
				.getTopicMessageListener(messagingRoutingInfo);
		topicMessageListener.listen(messageReceiver);
	}

}