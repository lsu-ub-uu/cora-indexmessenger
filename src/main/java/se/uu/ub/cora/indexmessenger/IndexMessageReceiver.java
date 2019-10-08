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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.indexmessenger.parser.MessageParser;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.messaging.MessageReceiver;

public class IndexMessageReceiver implements MessageReceiver {

	private CoraClient coraClient;
	private MessageParser messageParser;

	public IndexMessageReceiver(CoraClient coraClient, MessageParser messageParser) {
		this.coraClient = coraClient;
		this.messageParser = messageParser;
	}

	@Override
	public void receiveMessage(Map<String, Object> headers, String message) {

		// TODO: vad krävs i headers och message för att vi ska göra ngt?
		// kasta exceptions om något saknas??
		// TODO: skapa en workorder?
		// create type: workOrder
		// {"name":"workOrder","children":[{"name":"recordType","children":
		// [{"name":"linkedRecordType","value":"recordType"},
		// {"name":"linkedRecordId","value":"place"}]},
		// {"name":"recordId","value":"alvin-place:1"},{"name":"type","value":"index"}]}

		// WorkOrderCreator workOrderCreator = WorkOrderCreatorProvider.getWorkOrderCreator();
		// ClientDataGroup workOrder = workOrderCreator.createWorkOrder(workOrderCreator, message);

		messageParser.parseHeadersAndMessage(headers, message);
		String recordId = (String) headers.get("PID");

		ClientDataGroup workOrder = ClientDataGroup.withNameInData("workOrder");
		workOrder.addChild(ClientDataAtomic.withNameInDataAndValue("type", "index"));
		workOrder.addChild(ClientDataAtomic.withNameInDataAndValue("recordId", recordId));

		String recordTypeId = extractRecordTypeIdFromMessage(message);

		ClientDataGroup recordTypeGroup = ClientDataGroup
				.asLinkWithNameInDataAndTypeAndId("recordType", "recordType", recordTypeId);
		workOrder.addChild(recordTypeGroup);

		coraClient.create("workOrder", workOrder);
	}

	private String extractRecordTypeIdFromMessage(String message) {
		String recordTypeId = "";
		String pattern = "\"alvin\\.updates\\.(\\S*?)\"";

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(message);
		if (m.find()) {
			recordTypeId = m.group(1);
			// System.out.println("Found value: " + m.group(0));
			// System.out.println("Found value: " + m.group(1));
		} else {
			System.out.println("NO MATCH");
		}
		return recordTypeId;
	}

}
