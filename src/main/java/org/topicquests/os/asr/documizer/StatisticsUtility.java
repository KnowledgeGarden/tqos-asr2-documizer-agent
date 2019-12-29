/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

//import net.minidev.json.JSONObject;

/**
 * @author park
 *
 */
public class StatisticsUtility {
	private final String PATH = "/data/SystemStatistics.json";
	private JSONObject data;
	private boolean isSaved = false;

	private final String
		PMCID_COUNT				= "PMCID Count",
		DOCUMENT_COUNT			= "Document Count",
		AUTHOR_COUNT			= "Author Count",
		CONNECTION_COUNT		= "Connection Count",
		AFFILIATION_COUNT		= "Affiliation Count",
		DOCUMENT_IMPORTED			= "Documents Imported", // used by importer
		TOPIC_COUNT				= "NumTopics ",
		MISSING_ABSTRACT		= "Missing Abstract";
	
	/**
	 * 
	 */
	public StatisticsUtility() {
		try {
			bootData();
			isSaved = false;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * General purpose: any routine can choose a key
	 * and add to it, avoiding the reserved keys defined here
	 * @param key
	 */
	public void addToKey(String key) {
		//reset isSaved if someone is using this after it was saved
		if (isSaved)
			isSaved = false;
		synchronized(data) {
			Long v = (Long)data.get(key);
			if (v == null)
				v = new Long(1);
			else
				v += 1;
			data.put(key, v);
		}
	}	

	public void addTopic() {
		addToKey(TOPIC_COUNT);
	}
	
	public void addPMCID() {
		addToKey(PMCID_COUNT);
	}

	public void addDocument() {
		addToKey(DOCUMENT_COUNT);
	}

	public void addDocumentImported() {
		addToKey(DOCUMENT_IMPORTED);
	}

	public void addMissingAbstract() {
		addToKey(MISSING_ABSTRACT);
	}

	public void addAuthor() {
		addToKey(AUTHOR_COUNT);
	}
	
	public void addAffiliation() {
		addToKey(AFFILIATION_COUNT);
	}
	
	public void addConnection() {
		addToKey(CONNECTION_COUNT);
	}
	/**
	 * Called as needed, typically at shutDown time
	 * @throws Exception
	 */
	public void saveData() {
		if (isSaved)
			return;
		synchronized(data) {
			try {
				File f = new File(PATH);
				FileOutputStream fos = new FileOutputStream(f);
				OutputStreamWriter out = new OutputStreamWriter(fos);
				data.writeJSONString(out);
				out.close();
				isSaved = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	void bootData() throws Exception {
		File f = new File(PATH);
		if (f.exists()) {
			FileInputStream fis = new FileInputStream(f);
			JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
			data = (JSONObject)p.parse(fis);
			fis.close();
		} else
			data = new JSONObject();
	}

}
