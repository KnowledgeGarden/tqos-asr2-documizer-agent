/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer.api;

import org.topicquests.hyperbrane.api.IDocument;
import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.os.asr.kafka.KafkaHandler;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public interface IDocumizerModel {
	
	void setKafka(KafkaHandler h);

	IResult acceptDocumentObject(JSONObject documentObject);

	IDocument newDocument(JSONObject newDocument);
	
	IParagraph newParagraph(String abst, String language, String documentId);


}
