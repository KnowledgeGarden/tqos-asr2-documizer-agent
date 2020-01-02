/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer;

import java.util.Map;

import org.topicquests.asr.general.GeneralDatabaseEnvironment;
import org.topicquests.asr.general.document.api.IDocumentClient;
import org.topicquests.asr.paragraph.api.IParagraphClient;
import org.topicquests.asr.sentence.api.ISentenceClient;
import org.topicquests.os.asr.DocumentProvider;
import org.topicquests.os.asr.ParagraphProvider;
import org.topicquests.os.asr.SentenceProvider;
import org.topicquests.os.asr.StatisticsHttpClient;
import org.topicquests.os.asr.api.IDocumentProvider;
import org.topicquests.os.asr.api.IParagraphProvider;
import org.topicquests.os.asr.api.ISentenceProvider;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.os.asr.documizer.api.IDocumizerModel;
import org.topicquests.os.asr.documizer.importer.JSONDocumentReader;
import org.topicquests.os.asr.documizer.importer.JSONFileReader;
import org.topicquests.os.asr.kafka.KafkaHandler;
import org.topicquests.support.RootEnvironment;
import org.topicquests.support.config.Configurator;

/**
 * @author jackpark
 *
 */
public class DocumizerEnvironment extends RootEnvironment {
	private static DocumizerEnvironment instance;
	private IDocumentProvider docProvider;
	private JSONDocumentReader jsonDocReader;
	private JSONFileReader jsonFileReader;
	private IParagraphProvider paragraphProvider;
	private IDocumentProvider documentProvider;
	private IParagraphClient paragraphDatabase;
	private IDocumentClient documentDatabase;
	private GeneralDatabaseEnvironment generalEnvironment;
	private IStatisticsClient stats;
	private IDocumizerModel model;
	private Map<String,Object>kafkaProps;
	private ISentenceClient sentenceDatabase;
	private ISentenceProvider sentenceProvider;

	/**
	 * ASRCoreEnvironment reads "asr-props.xml"
	 */
	public DocumizerEnvironment() {
		super("asr-props.xml", "logger.properties");
		String schemaName = getStringProperty("DatabaseSchema");
		stats = new StatisticsHttpClient(this);
		kafkaProps = Configurator.getProperties("kafka-topics.xml");

		generalEnvironment = new GeneralDatabaseEnvironment(schemaName);
		paragraphDatabase = generalEnvironment.getParagraphClient();
		sentenceDatabase = generalEnvironment.getSentenceClient();
		documentDatabase = generalEnvironment.getDocumentClient();
		documentProvider = new DocumentProvider(this);
		paragraphProvider = new ParagraphProvider(this);
		sentenceProvider = new SentenceProvider(this);
		model = new DocumizerModel(this);
		model.setKafka(new KafkaHandler(this));
		try {
			jsonDocReader = new JSONDocumentReader(this, model);
		} catch (Exception e) {
			logError(e.getMessage(), e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		jsonFileReader = new JSONFileReader(this, jsonDocReader);
		instance = this;
	}
	
	public static DocumizerEnvironment getInstance() {
		return instance;
	}

	public IDocumizerModel getModel() {
		return model;
	}
	
	public Map<String, Object> getKafkaTopicProperties() {
		return kafkaProps;
	}

	public IStatisticsClient getStats() {
		return stats;
	}
	public JSONFileReader getJSONFileReader() {
		return jsonFileReader;
	}
	
	public JSONDocumentReader getJSONDocumentReader() {
		return jsonDocReader;
	}
		
	
	public IDocumentProvider getDocumentProvider() {
		return docProvider;
	}
	
	public IDocumentProvider getDocProvider() {
		return documentProvider;
	}
	public IDocumentClient getDocumentDatabase () {
		return documentDatabase;
	}
	public IParagraphClient getParagraphDatabase() {
		return paragraphDatabase;
	}
	
	public IParagraphProvider getParagraphProvider() {
		return paragraphProvider;
	}

	public ISentenceClient getSentenceDatabase() {
		return sentenceDatabase;
	}

	public ISentenceProvider getSentenceProvider() {
		return sentenceProvider;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new DocumizerEnvironment();
	}

	public void shutDown() {
		//TODO
	}
}
