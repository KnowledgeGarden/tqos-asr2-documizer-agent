/*
 * Copyright 2018, 2020 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer.para;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.os.asr.documizer.api.IDocumizerModel;
import org.topicquests.hyperbrane.api.ISentence;

/**
 * @author park
 *
 */
public class SentenceDetector {
	private DocumizerEnvironment environment;
	private SentenceDetectorME detector;
	private IDocumizerModel model;

	/**
	 * @param m TODO
	 * 
	 */
	public SentenceDetector(DocumizerEnvironment env, IDocumizerModel m) throws Exception {
		environment = env;
		model = m;
		//hyperModel = m;
		//database = environment.getDataProvider();
		String modelPath = (String)environment.getProperties().get("OpenNLPModels");
		modelPath += "/en-sent.bin";
		InputStream modelIn = new FileInputStream(modelPath);
		SentenceModel mod = null;
		try {
			mod = new SentenceModel(modelIn);
		} finally {
			modelIn.close();
		}
		if (model != null)
			detector = new SentenceDetectorME(mod);
		environment.logDebug("SentenceDetector "+model+" "+detector);
	}

	/**
	 * Return a list of {@link ISentence} objects
	 * @param paragraph
	 * @param documentLocator
	 * @param userId
	 * @return
	 */
	public IResult digestParagraph(String paragraph, String documentLocator, String userId) {
		System.out.println("DigPara "+paragraph);
		IResult result = new ResultPojo();
		List<ISentence> slist = new ArrayList<ISentence>();
		result.setResultObject(slist);
		String [] sentences = detector.sentDetect(paragraph); 
		ISentence sx;
		if (sentences != null) {
			String sentence;
			for (int i=0;i<sentences.length;i++) {
				sentence = sentences[i];
				//remove any "oxford comma": ', and"; ", or" --> " and"; " or"
				sentence = deOxfordComma(sentence);
				sx = model.newSentence(documentLocator, sentence, userId);
				
				slist.add(sx);
			}
		}
		
		return result;
	}
	
	String deOxfordComma(String sentence) {
		String result = sentence.replaceAll(", and", " and");
		result = result.replaceAll(", or", " or");
		return result;
	}

}
