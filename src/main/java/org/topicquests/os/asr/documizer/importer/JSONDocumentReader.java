/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer.importer;

import java.io.*;

import java.util.*;

import org.topicquests.asr.sentence.api.ISentenceClient;
import org.topicquests.hyperbrane.ConcordanceDocument;
import org.topicquests.hyperbrane.api.IDocument;

import org.topicquests.hyperbrane.api.IParagraph;

import org.topicquests.hyperbrane.api.IHyperMembraneOntology;

import org.topicquests.hyperbrane.api.IPublication;

import org.topicquests.hyperbrane.api.ISentence;
import org.topicquests.ks.TicketPojo;
import org.topicquests.ks.api.ITQCoreOntology;
import org.topicquests.ks.api.ITicket;

import org.topicquests.os.asr.StatisticsHttpClient;
import org.topicquests.os.asr.api.IDocumentProvider;
import org.topicquests.os.asr.api.IParagraphProvider;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.os.asr.common.api.IASRFields;
import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.os.asr.documizer.api.IDocumizerModel;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;
/**
 * @author jackpark
 *
 */
public class JSONDocumentReader {
	private DocumizerEnvironment environment;
	private IDocumizerModel model;
	private IDocumentProvider provider;
	private ISentenceClient sentenceDatabase;
	private IParagraphProvider paragraphProvider;

	private IStatisticsClient stats;
//	private SentenceDetectorME detector;
	private ITicket credentials;
	private final String DEFAULT_LANGUAGE = "en";


	/**
	 * 
	 */
	public JSONDocumentReader(DocumizerEnvironment env, IDocumizerModel m) throws Exception {
		environment = env;
		model = m;
		provider = environment.getDocProvider();
		sentenceDatabase = environment.getSentenceDatabase();
		paragraphProvider = environment.getParagraphProvider();
		credentials = new TicketPojo(ITQCoreOntology.SYSTEM_USER);
	}
	
	/**
	 * Do the work: import the contents of <code>jo</code>
	 * @param jo
	 */ // code here adapted from PubMedImporter.Phase2Engine
	public void processJSON(JSONObject jo) {
		environment.logDebug("JSONDocumentReader.processJSON "+jo);
		IDocument doc = new ConcordanceDocument(jo);
		String lox = doc.getId();
		String pmcid = doc.getPMCID();
		if (pmcid != null) {
			stats.addToKey("PMCID Count");
		}
		IResult r = provider.getDocument(lox, credentials);
		if (r.getResultObject() != null) {
			environment.logDebug("JSONDocumentReader.processJSON already exists "+lox);
			//TODO must see if that's an abstract already
		} else {

			//Then deal with paragraphs
			environment.logDebug("JSONDocumentReader.processJSON-1 "+doc.toJSONString());
			// which deals with sentences
			processParagraphs(doc);
			environment.logDebug("JSONDocumentReader.processJSON-2 "+doc.toJSONString());
			//When this document is populated, save it
			r = provider.putDocument(doc);
			stats.addToKey(IASRFields.DOCS_IMPORTED);
		}
		provider.putDocument(doc);
	}
	
	/**
	 * Pull the paragraphs out of this document, if any
	 * @param doc
	 */
	void processParagraphs(IDocument doc) {
		String lang = doc.getLanguage();
		if (lang.equals("eng")) { //PubMed does that
			lang = "en";
			doc.setLanguage(lang);
		}
		/**
		//list all abstracts
		//MASSIVE TODO THIS IS NOT FINISHED - EMPTY FIELDS IN THE CONCORDANCEDOCUMENT
		List<JSONObject> abstrs = doc.listAbstracts();
		String abstr = null;
		if (abstrs != null && !abstrs.isEmpty()) {
			JSONObject jo;
			Iterator<JSONObject> itr = abstrs.iterator();
			while (itr.hasNext()) {
				jo = itr.next();
			}
		}
		environment.logDebug("JSONDocumentReader.processParagraphs "+abstr);
		IParagraph p;
		String docId = doc.getId();
		String language = doc.getLanguage();
		if (language == null)
			language = "en";
		if (abstr != null) {
			p = doc.addParagraph(abstr, language);
			paragraphProvider.putParagraph(p);
		}
		////////////////////
		// LOOK FOR OTHER PARAGRAPHS
		// Doc.listDetails returns the text body for all languges in the document
		// here, we are just assuming "english"
		// That's going to cause problems later on
		////////////////////
		List<String> details = doc.listDetails(); // gets all of them
		if (details != null && !details.isEmpty()) {
			Iterator<String> itr = details.iterator();
			String det;
			while (itr.hasNext()) {
				det = itr.next();
				p = doc.addParagraph(det, language);
				paragraphProvider.putParagraph(p);				
			}	
		}
		
		*/
		
/*		List<IParagraph> px = doc.listParagraphs();
		if (px != null && !px.isEmpty()) {
			Iterator<IParagraph> itp = px.iterator();
			while (itp.hasNext()) {
				p = itp.next();
				digestParagraph(p, doc.getLanguage(), doc.getId(), p.getID(), doc.getCreatorId());
				//TODO need doc.updateParagraph()
			}
		}
		//This argues for building IDocuments with details rather than paragraphs
		List<String> lx = doc.listDetails(doc.getLanguage());
		if (lx != null && !lx.isEmpty()) {
			Iterator<String> itr = lx.iterator();
			while (itr.hasNext()) {
				abstr = itr.next();
				p = model.newParagraph(abstr, doc.getLanguage());
				digestParagraph(p, doc.getLanguage(), doc.getId(), p.getID(), doc.getCreatorId());
				doc.addParagraph(p);
			}
		} */
	}
	
	
	
	/**
	 * Processes a paragraph including all of its sentences
	 * @param p
	 * @param language
	 * @param documentLocator
	 * @param userId
	 * @return
	 * /
	IResult digestParagraph(IParagraph p, String language, String documentId, String paragraphId, String userId) {
		String paragraph = p.getParagraph();
		System.out.println("DigPara "+paragraph);
		environment.logDebug("JSONDocumentReader.digestParagraph "+paragraph);
		IResult result = new ResultPojo();
		String [] sentences = detector.sentDetect(paragraph); //detectSentences(paragraph);
		ISentence sx;
		if (sentences != null) {
			String sentence;
			for (int i=0;i<sentences.length;i++) {
				sentence = sentences[i];
				//remove any "oxford comma": ', and"; ", or" --> " and"; " or"
				sentence = deOxfordComma(sentence);
				sx = model.newSentence(documentId, paragraphId, sentence, userId);
				environment.logDebug("JSONDocumentReader.digestParagraph-1 "+sx.toJSONString());
				IResult r = sentenceDatabase.put(documentId, paragraphId, sx.getID(), sx.getData());
				if (r.hasError()) {
					result.addErrorString(r.getErrorString());
				}
				p.addSentence(sx);
				stats.addToKey(IASRFields.SENTS_IMPORTED);
			}
		}
		
		return result;
	}
	*/
	
	String deOxfordComma(String sentence) {
		String result = sentence.replaceAll(", and", " and");
		result = result.replaceAll(", or", " or");
		return result;
	}
}
