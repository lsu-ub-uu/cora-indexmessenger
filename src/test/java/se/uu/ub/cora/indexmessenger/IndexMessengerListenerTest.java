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

import org.testng.annotations.Test;

import se.uu.ub.cora.indexmessenger.log.LoggerFactorySpy;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessagingProvider;

public class IndexMessengerListenerTest {
	private LoggerFactorySpy loggerFactorySpy;
	private String testedClassName = "IndexMessengerListener";

	@Test
	public void testInit() throws Exception {
		loggerFactorySpy = LoggerFactorySpy.getInstance();
		loggerFactorySpy.resetLogs(testedClassName);
		LoggerProvider.setLoggerFactory(loggerFactorySpy);

		MessagingFactorySpy messagingFactorySpy = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactorySpy);
		assertEquals(messagingFactorySpy.factorTopicMessageListenerCalled, false);

		IndexMessengerListener iml = new IndexMessengerListener();
		assertEquals(messagingFactorySpy.factorTopicMessageListenerCalled, true);
	}
}
