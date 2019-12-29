/**
 * 
 */
package org.topicquests.os.asr.documizer;

import org.topicquests.os.asr.documizer.api.IListener;
import org.topicquests.os.asr.documizer.importer.JSONFileReader;

/**
 * @author jackpark
 *
 */
public class Main implements IListener {
	private DocumizerEnvironment environment;
	private JSONFileReader jsonFileReader;

	/**
	 * 
	 */
	public Main() {
		environment = new DocumizerEnvironment();
		jsonFileReader = environment.getJSONFileReader();
		jsonFileReader.setListener(this);
		//start
		jsonFileReader.startReading();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void did() {
		environment.shutDown();
		System.exit(0);
	}

}
