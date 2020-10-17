/**
 * Copyright 2019, TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.topicquests.backside.kafka.consumer.StringConsumer;
import org.topicquests.backside.kafka.consumer.api.IMessageConsumerListener;
import org.topicquests.backside.kafka.producer.MessageProducer;
import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.os.asr.documizer.api.IDocumizerModel;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class KafkaHandler implements IMessageConsumerListener {
	private DocumizerEnvironment environment;
	private StringConsumer consumer;
	private MessageProducer producer;
	private IDocumizerModel model;
	private final boolean isRewind = false;
	private final int pollSeconds = 2;
	private final String
		CONSUMER_TOPIC,
		PRODUCER_TOPIC,
		PRODUCER_KEY,
		AGENT_GROUP = "DocumizerAgent";

	/**
	 * 
	 */
	public KafkaHandler(DocumizerEnvironment env) {
		environment = env;
		model = environment.getModel();
		CONSUMER_TOPIC = (String)environment.getKafkaTopicProperties().get("DocumizerTopic");
		PRODUCER_TOPIC = (String)environment.getKafkaTopicProperties().get("SpacyOutput");
		consumer = new StringConsumer(environment, AGENT_GROUP,
					CONSUMER_TOPIC, this, isRewind, pollSeconds);
		producer = new MessageProducer(environment, AGENT_GROUP, true);
		PRODUCER_KEY = AGENT_GROUP; 
	}

	@Override
	public boolean acceptRecord(ConsumerRecord record) {
		boolean result = false;
		String json = (String)record.value();
		environment.logDebug("KafkaHandler.acceptRecord "+json);
		try {
			JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
			JSONObject jo = (JSONObject)p.parse(json);
			IResult r = model.acceptDocumentObject(jo);
			result = ((Boolean)r.getResultObject()).booleanValue();
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
		}
		return result;
	}
	
	public void shipEvent(JSONObject event) {
		producer.sendMessage(PRODUCER_TOPIC, event.toJSONString(), PRODUCER_KEY, new Integer(0));
	}

}
