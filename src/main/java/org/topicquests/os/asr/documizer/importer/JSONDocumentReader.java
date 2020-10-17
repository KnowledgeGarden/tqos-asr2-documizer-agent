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
import org.topicquests.os.asr.documizer.para.ParagraphReader;
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
	private ParagraphReader paraReader;

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
		paraReader = new ParagraphReader(environment);
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
			stats.addToKey("PMCID_Count");
		}
		IResult r = provider.getDocument(lox, credentials);
		if (r.getResultObject() != null) {
			environment.logDebug("JSONDocumentReader.processJSON already exists "+lox);
			// TODO must see if that's an abstract already
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
		String documentId = doc.getId();
		String userId = doc.getCreatorId();
		List<ISentence> mySentences;
		if (lang == null || lang.equals("eng")) { //PubMed does that
			lang = "en";
			doc.setLanguage(lang);
		}
		List<IParagraph> parax = doc.listParagraphs();
		if (parax == null) {
			List<String> paras = doc.listParagraphStrings(lang);
			if (paras != null && !paras.isEmpty()) {
				IResult r;
				//create paragraphs
				parax = new ArrayList<IParagraph>();
				String parx; // a paragraph string
				Iterator<String>itr = paras.iterator();
				IParagraph px = null;
				while (itr.hasNext()) {
					parx = itr.next();
					px = model.newParagraph(parx, lang, documentId );
					// gather the sentences
					r = paraReader.digestParagraph(px, documentId, userId);
					// ignore r; px is now populated with sentences
				}
			}
		}
		
		if (parax != null && !parax.isEmpty()) {
			//process paragraphs just in case
			IParagraph para;
			IResult r;
			Iterator<IParagraph> itx = parax.iterator();
			while (itx.hasNext()) {
				para = itx.next();
				r = paraReader.readParagraph(para, documentId, userId);
				//ignore r;
			}
		}
	}
	
}
