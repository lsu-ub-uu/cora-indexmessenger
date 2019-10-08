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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.messaging.MessageReceiver;

public class IndexMessageReceiverTest {

	@BeforeMethod
	public void setUp() {
		// Map<String, Object> headers = new HashMap<>();
		// headers.put("__TypeId__", "epc.messaging.amqp.EPCFedoraMessage");
		// headers.put("ACTION", "UPDATE");
		// headers.put("PID", "alvin-place:1");
		// headers.put("messageSentFrom", "Cora");

		// String message = "{\"pid\":\"alvin-place:1\",\"routingKey\":\"alvin.updates.place\","
		// + "\"action\":\"UPDATE\",\"dsId\":null,"
		// + "\"headers\":{\"ACTION\":\"UPDATE\",\"PID\":\"alvin-place:1\"}}";
	}

	@Test
	public void testInit() {
		MessageReceiver receiver = new IndexMessageReceiver();
	}
}
