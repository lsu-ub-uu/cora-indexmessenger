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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageRoutingInfo;

public class IndexerMessengerStarter {

	// private static Logger logger =
	// LoggerProvider.getLoggerForClass(IndexerMessengerStarter.class);

	protected static AlvinIndexMessengerListener indexMessengerListener;

	private IndexerMessengerStarter() {
	}

	public static void main(String[] args) {
		String propertiesFileName = args[0];

		try (InputStream input = IndexerMessengerStarter.class.getClassLoader()
				.getResourceAsStream(propertiesFileName)) {

			Properties properties = loadProperites(input);
			createIndexMessengerListener(properties);

		} catch (Exception ex) {
			// TODO: om jag lägger upp den här och kör alla test så funkar inte
			// testErrorHandlingTest
			LoggerProvider.getLoggerForClass(IndexerMessengerStarter.class)
					.logFatalUsingMessageAndException("Unable to start IndexerMessengerStarter ",
							ex);
		}
	}

	private static Properties loadProperites(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}

	private static void createIndexMessengerListener(Properties properties) {
		CoraClientFactory coraClientFactory = createCoraClientFactory(properties);
		MessageParserFactory messageParserFactory = new AlvinMessageParserFactory();
		AmqpMessageRoutingInfo routingInfo = createMessageRoutingInfo(properties);
		String userId = properties.getProperty("cora.userId");
		String apptoken = properties.getProperty("cora.appToken");

		indexMessengerListener = new AlvinIndexMessengerListener(coraClientFactory,
				messageParserFactory, routingInfo, new CoraCredentials(userId, apptoken));
	}

	private static CoraClientFactory createCoraClientFactory(Properties properties) {
		String appTokenVerifierUrl = extractPropertyThrowErrorIfNotFound(properties,
				"appTokenVerifierUrl");
		String baseUrl = extractPropertyThrowErrorIfNotFound(properties, "baseUrl");
		return CoraClientFactoryImp.usingAppTokenVerifierUrlAndBaseUrl(appTokenVerifierUrl,
				baseUrl);
	}

	private static String extractPropertyThrowErrorIfNotFound(Properties properties,
			String propertyName) {
		throwErrorIfPropertyNameIsMissing(properties, propertyName);
		return properties.getProperty(propertyName);
	}

	private static void throwErrorIfPropertyNameIsMissing(Properties properties,
			String propertyName) {
		if (!properties.containsKey(propertyName)) {
			throw new RuntimeException(
					"Property with name " + propertyName + " not found in properties");
		}
	}

	private static AmqpMessageRoutingInfo createMessageRoutingInfo(Properties properties) {
		String hostname = properties.getProperty("messaging.hostname");
		String port = properties.getProperty("messaging.port");
		String virtualHost = properties.getProperty("messaging.virtualHost");
		String exchange = properties.getProperty("messaging.exchange");
		String routingKey = properties.getProperty("messaging.routingKey");
		return new AmqpMessageRoutingInfo(hostname, port, virtualHost, exchange, routingKey);
	}
}
