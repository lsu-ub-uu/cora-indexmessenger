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
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.indexmessenger.log.LoggerFactorySpy;
import se.uu.ub.cora.javaclient.cora.CoraClientFactoryImp;
import se.uu.ub.cora.logger.LoggerProvider;
import se.uu.ub.cora.messaging.MessagingProvider;

public class IndexerMessengerStarterTest {

	private LoggerFactorySpy loggerFactorySpy;
	private MessagingFactorySpy messagingFactorySpy;
	private String testedClassName = "IndexerMessengerStarter";

	@BeforeMethod
	public void setUp() {
		loggerFactorySpy = new LoggerFactorySpy();
		LoggerProvider.setLoggerFactory(loggerFactorySpy);
		messagingFactorySpy = new MessagingFactorySpy();
		MessagingProvider.setMessagingFactory(messagingFactorySpy);

	}

	@Test
	public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException {
		Constructor<IndexerMessengerStarter> constructor = IndexerMessengerStarter.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testMainMethodCoraClientFactorySetUpCorrectly()
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException {

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
	public void testMainMethodMessageParserFactorySetUpCorrectly()
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, InstantiationException {

		String args[] = new String[] { "alvinIndexer.properties" };
		IndexerMessengerStarter.main(args);

		AlvinIndexMessengerListener messageListener = IndexerMessengerStarter.indexMessengerListener;
		assertTrue(messageListener.getMessageParserFactory() instanceof AlvinMessageParserFactory);
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

	// TODO: check properties somehow? check they are passed to indexmessanger, or do
	// all check here and create MessagingRoutinginfo at the same time??
}
