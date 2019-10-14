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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.indexmessenger.log.LoggerFactorySpy;
import se.uu.ub.cora.indexmessenger.log.LoggerSpy;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.AmqpMessageRoutingInfo;
import se.uu.ub.cora.messaging.MessagingProvider;

public class IndexerMessengerStarterTest {

	private LoggerFactorySpy loggerFactorySpy;
	private MessagingFactorySpy messagingFactorySpy;
	private String testedClassName = "IndexerMessengerStarter";
	Map<String, String> defaultProperties = new HashMap<>();

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		messagingFactorySpy = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactorySpy);
		setUpDefaultProperties();
	}

	private void setUpDefaultProperties() {
		defaultProperties.put("messaging.hostname", "messaging.alvin-portal.org");
		defaultProperties.put("messaging.port", "5672");
		defaultProperties.put("messaging.virtualHost", "alvin");
		defaultProperties.put("messaging.exchange", "index");
		defaultProperties.put("messaging.routingKey", "#");
		defaultProperties.put("appTokenVerifierUrl", "someAppTokenVerifierUrl");
		defaultProperties.put("baseUrl", "someBaseUrl");
		defaultProperties.put("cora.userId", "userIdForCora");
		defaultProperties.put("cora.appToken", "appTokenForCora");
	}

	@Test
	public void testConstructorIsPrivate() throws Exception {
		Constructor<IndexerMessengerStarter> constructor = IndexerMessengerStarter.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testMainMethod() {
		String args[] = new String[] { "alvinIndexer.properties" };
		IndexerMessengerStarter.main(args);
		assertNoFatalErrorMessages();
	}

	private void assertNoFatalErrorMessages() {
		LoggerSpy loggerSpy = loggerFactorySpy.createdLoggers.get(testedClassName);
		assertNull(loggerSpy);
	}

	@Test
	public void testMainMethodCoraClientFactorySetUpCorrectly() throws Exception {
		String args[] = new String[] { "alvinIndexer.properties" };
		IndexerMessengerStarter.main(args);

		AlvinIndexMessengerListener messageListener = IndexerMessengerStarter.indexMessengerListener;
		CoraClientFactoryImp coraClientFactory = (CoraClientFactoryImp) messageListener
				.getCoraClientFactory();

		// assert same as in alvinindexer.properties
		assertEquals(coraClientFactory.getAppTokenVerifierUrl(), "someAppTokenVerifierUrl");
		assertEquals(coraClientFactory.getBaseUrl(), "someBaseUrl");
	}

	@Test
	public void testMainMethodMessageParserFactorySetUpCorrectly() throws Exception {
		String args[] = new String[] { "alvinIndexer.properties" };
		IndexerMessengerStarter.main(args);

		AlvinIndexMessengerListener messageListener = IndexerMessengerStarter.indexMessengerListener;
		assertTrue(messageListener.getMessageParserFactory() instanceof AlvinMessageParserFactory);
	}

	@Test
	public void testMainMethodMessagingRoutingInfoSetUpCorrectly()
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException {

		String args[] = new String[] { "alvinIndexer.properties" };
		IndexerMessengerStarter.main(args);

		AlvinIndexMessengerListener messageListener = IndexerMessengerStarter.indexMessengerListener;
		AmqpMessageRoutingInfo messagingRoutingInfo = (AmqpMessageRoutingInfo) messageListener
				.getMessageRoutingInfo();
		// assert same as in alvinindexer.properties
		assertNotNull(messagingRoutingInfo);
		assertEquals(messagingRoutingInfo.hostname, "messaging.alvin-portal.org");
		assertEquals(messagingRoutingInfo.port, "5672");
		assertEquals(messagingRoutingInfo.virtualHost, "alvin");
		assertEquals(messagingRoutingInfo.exchange, "index");
		assertEquals(messagingRoutingInfo.routingKey, "#");

	}

	@Test
	public void testMainMethodCoraCredentialsSetUpCorrectly()
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException {

		String args[] = new String[] { "alvinIndexer.properties" };
		IndexerMessengerStarter.main(args);

		AlvinIndexMessengerListener messageListener = IndexerMessengerStarter.indexMessengerListener;
		CoraCredentials credentials = messageListener.getCredentials();

		// assert same as in alvinindexer.properties
		assertEquals(credentials.userId, "userIdForCora");
		assertEquals(credentials.appToken, "appTokenForCora");
	}

	@Test
	public void testErrorHandling() throws Exception {
		String args[] = new String[] { "someNoneExistingFile" };

		IndexerMessengerStarter.main(args);

		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
		Exception exception = loggerFactorySpy.getFatalLogErrorUsingClassNameAndNo(testedClassName,
				0);
		assertTrue(exception instanceof RuntimeException);
		assertEquals(exception.getMessage(), "inStream parameter is null");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start IndexerMessengerStarter ");
	}

	@Test
	public void testErrorHandlingNoAppTokenVerifierUrl() throws Exception {
		String args[] = new String[] { "alvinIndexerMissingApptokenUrl.properties" };

		IndexerMessengerStarter.main(args);

		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
		Exception exception = loggerFactorySpy.getFatalLogErrorUsingClassNameAndNo(testedClassName,
				0);
		assertTrue(exception instanceof RuntimeException);
		assertEquals(exception.getMessage(),
				"Property with name appTokenVerifierUrl not found in properties");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start IndexerMessengerStarter ");
	}

	@Test
	public void testErrorHandlingNoBaseUrl() throws Exception {
		String args[] = new String[] { "alvinIndexerMissingBaseUrl.properties" };

		IndexerMessengerStarter.main(args);

		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
		Exception exception = loggerFactorySpy.getFatalLogErrorUsingClassNameAndNo(testedClassName,
				0);
		assertTrue(exception instanceof RuntimeException);
		assertEquals(exception.getMessage(), "Property with name baseUrl not found in properties");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start IndexerMessengerStarter ");
	}

	@Test
	public void testPropertiesErrorWhenHostnameIsMissing() {
		String propertyName = "messaging.hostname";
		String fileName = "propertiesForTestingMissingParameterHostname.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	private Properties addDefaultPropertiesToTestFile(String fileName) {
		Properties properties = new Properties();
		for (Map.Entry<String, String> entry : defaultProperties.entrySet()) {
			properties.put(entry.getKey(), entry.getValue());
		}
		try {
			storeProperties(properties, fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}

	@Test
	public void testPropertiesErrorWhenPortIsMissing() {
		String fileName = "propertiesForTestingMissingParameterPort.properties";
		String propertyName = "messaging.port";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	private void testPropertiesErrorWhenPropertyIsMissing(String fileName, String propertyName) {
		removePropertyFromPoperties(propertyName, fileName);

		String args[] = new String[] { fileName };

		IndexerMessengerStarter.main(args);
		assertCorrectErrorForMissingProperty(propertyName);
	}

	@Test
	public void testPropertiesErrorWhenVirtualHostIsMissing() {
		String propertyName = "messaging.virtualHost";
		String fileName = "propertiesForTestingMissingParameterVirtualHost.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	@Test
	public void testPropertiesErrorWhenExchangeIsMissing() {
		String propertyName = "messaging.exchange";
		String fileName = "propertiesForTestingMissingParameterExchange.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	@Test
	public void testPropertiesErrorWhenRoutingKeyIsMissing() {
		String propertyName = "messaging.routingKey";
		String fileName = "propertiesForTestingMissingParameterRoutingKey.properties";
		testPropertiesErrorWhenPropertyIsMissing(fileName, propertyName);
	}

	private void assertCorrectErrorForMissingProperty(String propertyName) {
		assertEquals(loggerFactorySpy.getNoOfFatalLogMessagesUsingClassName(testedClassName), 1);
		Exception exception = loggerFactorySpy.getFatalLogErrorUsingClassNameAndNo(testedClassName,
				0);
		assertTrue(exception instanceof RuntimeException);
		assertEquals(exception.getMessage(),
				"Property with name " + propertyName + " not found in properties");
		assertEquals(loggerFactorySpy.getFatalLogMessageUsingClassNameAndNo(testedClassName, 0),
				"Unable to start IndexerMessengerStarter ");
	}

	private void removePropertyFromPoperties(String propertyName, String fileName) {
		Properties properties = addDefaultPropertiesToTestFile(fileName);
		try {
			properties.remove(propertyName);
			storeProperties(properties, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void storeProperties(Properties properties, String fileName)
			throws IOException, FileNotFoundException {
		properties.store(new FileOutputStream("src/test/resources/" + fileName), null);
	}

}
