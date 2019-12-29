/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import org.topicquests.os.asr.documizer.DocumizerEnvironment;

/**
 * @author jackpark
 *
 */
public class BootTest {
	private DocumizerEnvironment environment;
	/**
	 * 
	 */
	public BootTest() {
		environment = new DocumizerEnvironment();
		System.out.println("A "+environment.getProperties());
		environment.shutDown();
		System.exit(0);
	}

}
