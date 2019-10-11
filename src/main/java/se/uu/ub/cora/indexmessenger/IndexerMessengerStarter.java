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

import java.io.InputStream;
import java.util.Properties;

import se.uu.ub.cora.javaclient.cora.CoraClientFactory;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;

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
			Properties properties = new Properties();
			properties.load(input);

			CoraClientFactory coraClientFactory = CoraClientFactoryImp
					.usingAppTokenVerifierUrlAndBaseUrl(
							properties.getProperty("appTokenVerifierUrl"),
							properties.getProperty("baseUrl"));

			MessageParserFactory messageParserFactory = null;
			indexMessengerListener = new AlvinIndexMessengerListener(coraClientFactory, properties,
					messageParserFactory);

		} catch (Exception ex) {
			// TODO: om jag lägger upp den här och kör alla test så funkar inte
			// testErrorHandlingTest
			LoggerProvider.getLoggerForClass(IndexerMessengerStarter.class)
					.logFatalUsingMessageAndException(
							"Error during initializing of AlvinIndexMessengerListener", ex);
		}
	}
}
