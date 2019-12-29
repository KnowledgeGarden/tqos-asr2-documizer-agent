/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.os.asr.documizer.api.IListener;
import org.topicquests.os.asr.documizer.importer.JSONFileReader;

/**
 * @author jackpark
 *
 */
public class ImportTest implements IListener {
	private DocumizerEnvironment environment;
	private JSONFileReader jsonFileReader;

	/**
	 * 
	 */
	public ImportTest() {
		environment = new DocumizerEnvironment();
		System.out.println("A "+environment.getProperties());
		jsonFileReader = environment.getJSONFileReader();
		jsonFileReader.setListener(this);
		//start
		jsonFileReader.startReading();
	}

	@Override
	public void did() {
		environment.shutDown();
		System.exit(0);
	}

}
