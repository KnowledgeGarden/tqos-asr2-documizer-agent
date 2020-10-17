/*
 * Copyright 2018, 2020 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer.para;

import java.util.*;

import org.topicquests.os.asr.documizer.api.IDocumizerModel;
import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.hyperbrane.api.ISentence;
import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

/**
 * @author park
 * <p>This requires the OpenNLP models and tools</p>
 */
public class ParagraphReader {
	private DocumizerEnvironment environment;

	private SentenceDetector sentenceDetector;
	//private SentenceReader sentenceReader;
	private IDocumizerModel model;

	/**
	 * 
	 */
	public ParagraphReader(DocumizerEnvironment env) throws Exception {
		environment = env;
		model = environment.getModel();
		sentenceDetector = new SentenceDetector(environment, model);
	}
	
	/**
	 * may not use
	 * @param para
	 * @param documentId
	 * @param userId
	 * @return
	 */
	public IResult readParagraph(IParagraph para, String documentId, String userId) {
		IResult result = new ResultPojo();
		/////////////////////////
		//TODO  when sentenceReader reads, it should get a DocumentBlackboard and ParagraphBlackboard
		// BUT we must remain threadsafe
		// so we must do everything in this method
		IResult r = digestParagraph(para, documentId, userId);
		if (r.hasError())
			result.addErrorString(r.getErrorString());
		return result;
	}
	
	/**
	 * Turn a paragraph into a collection of {@link ISentence} instances
	 * 
	 * @param para
	 * @param documentLocator
	 * @param userId
	 * @return
	 */
	public IResult digestParagraph(IParagraph para, String documentLocator, String userId) {
		IResult result = new ResultPojo();
		List<ISentence> sentences = para.listSentences();
		if (sentences != null) // already processed?
			return result;
		//////////////////////////////
		// TODO
		//  That's too simplistic:
		//		there might be a rare edge case where we are reprocessing a paragraph
		//		with different sentence count.
		//	THIS NEEDS WORL
		//////////////////////////////
		try {
			//detect sentences in paragraph
			_detectSentences(para, documentLocator, userId);
			sentences = para.listSentences();
			if (sentences == null) {
				//sanity check
				result.addErrorString("PARANOSENTS "+para.getParagraph());
				return result;
			}
			
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			result.addErrorString(e.getMessage());
		}
		return result;
	}

	/**
	 * Populate {@code para} with {@code ISentence} instances
	 * @param para
	 * @param documentLocator
	 * @param userId
	 * @throws Exception
	 */
	private void _detectSentences(IParagraph para, String documentLocator, String userId) throws Exception {
		//String paragraph = para.getParagraph();
		System.out.println("Detecting Sentences: "+para.getData().toJSONString());
		IResult r = sentenceDetector.digestParagraph(para.getParagraph(), documentLocator, userId);
		List<ISentence> sentences = (List<ISentence>)r.getResultObject();
		System.out.println("Detecting Sentences-1: "+sentences);
		if (sentences != null) {
			Iterator<ISentence> itr = sentences.iterator();
			while (itr.hasNext()) {
				para.addSentence(itr.next());
			}
		}
	}

}
