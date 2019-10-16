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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.indexmessenger.parser.MessageParser;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.logger.Logger;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessageReceiver;

public class IndexMessageReceiver implements MessageReceiver {

	private static final String RECORD_TYPE = "recordType";
	private static final String RECORD_ID = "recordId";
	private Logger logger = LoggerProvider.getLoggerForClass(IndexMessageReceiver.class);
	private CoraClient coraClient;
	private MessageParserFactory messageParserFactory;

	public IndexMessageReceiver(CoraClient coraClient, MessageParserFactory messageParserFactory) {
		this.coraClient = coraClient;
		this.messageParserFactory = messageParserFactory;
	}

	@Override
	public void receiveMessage(Map<String, String> headers, String message) {
		MessageParser messageParser = messageParserFactory.factor();
		messageParser.parseHeadersAndMessage(headers, message);
		if (messageParser.shouldWorkOrderBeCreatedForMessage()) {
			createWorkOrder(messageParser);
		}
	}

	private void createWorkOrder(MessageParser messageParser) {
		Map<String, String> logValues = new HashMap<>();
		ClientDataGroup workOrder = createWorkOrderDataGroup(messageParser, logValues);
		try {
			coraClient.create("workOrder", workOrder);
			writeLogMessage(logValues);
		} catch (Exception e) {
			String logM = "Index workOrder NOT created for type: {0} and id: {1}";
			String formattedLogMessage = MessageFormat.format(logM, logValues.get(RECORD_TYPE),
					logValues.get(RECORD_ID));
			logger.logErrorUsingMessageAndException(formattedLogMessage, e);
		}
	}

	private ClientDataGroup createWorkOrderDataGroup(MessageParser messageParser,
			Map<String, String> logValues) {
		ClientDataGroup workOrder = createIndexWorkOrder();
		addRecordType(messageParser, workOrder, logValues);
		addRecordId(messageParser, workOrder, logValues);
		return workOrder;
	}

	private ClientDataGroup createIndexWorkOrder() {
		ClientDataGroup workOrder = ClientDataGroup.withNameInData("workOrder");
		workOrder.addChild(ClientDataAtomic.withNameInDataAndValue("type", "index"));
		return workOrder;
	}

	private void addRecordId(MessageParser messageParser, ClientDataGroup workOrder,
			Map<String, String> logValues) {
		String parsedId = messageParser.getParsedId();
		workOrder.addChild(ClientDataAtomic.withNameInDataAndValue(RECORD_ID, parsedId));
		logValues.put(RECORD_ID, parsedId);
	}

	private void addRecordType(MessageParser messageParser, ClientDataGroup workOrder,
			Map<String, String> logValues) {
		String parsedType = messageParser.getParsedType();
		ClientDataGroup recordTypeGroup = ClientDataGroup
				.asLinkWithNameInDataAndTypeAndId(RECORD_TYPE, RECORD_TYPE, parsedType);
		workOrder.addChild(recordTypeGroup);
		logValues.put(RECORD_TYPE, parsedType);
	}

	private void writeLogMessage(Map<String, String> logValues) {
		String logM = "Index workOrder created for type: {0} and id: {1}";
		logger.logInfoUsingMessage(
				MessageFormat.format(logM, logValues.get(RECORD_TYPE), logValues.get(RECORD_ID)));
	}

	@Override
	public void topicClosed() {
		logger.logFatalUsingMessage("Topic closed!");
	}

	public CoraClient getCoraClient() {
		// needed for test
		return coraClient;
	}

	public MessageParserFactory getMessageParserFactory() {
		// needed for test
		return messageParserFactory;
	}

}
