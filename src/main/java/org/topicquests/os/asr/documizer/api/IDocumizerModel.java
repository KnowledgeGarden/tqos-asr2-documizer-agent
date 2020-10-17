/*
 * Copyright 2018, 2020 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer.api;

import org.topicquests.hyperbrane.api.IDocument;
import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.hyperbrane.api.ISentence;
import org.topicquests.os.asr.kafka.KafkaHandler;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public interface IDocumizerModel {
	
	void setKafka(KafkaHandler h);

	/**
	 * Simply turns {@code documentObject} into a new {@code IDocument}
	 * @param documentObject
	 * @return
	 */
	IResult acceptDocumentObject(JSONObject documentObject);

	/**
	 * <p>A new document can come from many different sources. This means that
	 * the <em>newDocument</em> algorithm must be sensitive to the possibility, among other things,
	 * that the the document might be associated with a URL - typical of annotation documents,
	 * such as from, e.g. https://hypothes.is/ clients. In such cases, the same URL may
	 * engender many different documents; this algorithm must attempt to merge the final 
	 * {@code IDocument} over time.</p>
	 * <p>An alternative algorithm will ignore merging - under consideration - Work in progress,
	 * and allow for merging to occur in later processes</p>
	 * @param newDocument
	 * @return
	 */
	IDocument newDocument(JSONObject newDocument);
	
	/**
	 * Required for processing new documents
	 * @param abst
	 * @param language
	 * @param documentId
	 * @return
	 */
	IParagraph newParagraph(String abst, String language, String documentId);

	/**
	 * Required for processing new documents
	 * @param documentLocator
	 * @param sentence
	 * @param userId
	 * @return
	 */
	ISentence newSentence(String documentLocator, String sentence, String userId);

	

}
