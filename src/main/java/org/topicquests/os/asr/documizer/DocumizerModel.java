/*
 * Copyright 2018, 2020 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer;

import java.util.*;
import org.topicquests.hyperbrane.ConcordanceDocument;
import org.topicquests.hyperbrane.ConcordanceParagraph;
import org.topicquests.hyperbrane.ConcordanceSentence;
import org.topicquests.hyperbrane.api.IDocument;
import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.hyperbrane.api.ISentence;
import org.topicquests.ks.TicketPojo;
import org.topicquests.ks.api.ITQCoreOntology;
import org.topicquests.ks.api.ITicket;
import org.topicquests.os.asr.JSONDocumentObject;
import org.topicquests.os.asr.api.IDocumentProvider;
import org.topicquests.os.asr.api.IParagraphProvider;
import org.topicquests.os.asr.api.ISentenceProvider;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.os.asr.common.api.IASRFields;
import org.topicquests.os.asr.documizer.api.IDocumizerModel;
import org.topicquests.os.asr.documizer.para.ParagraphReader;
import org.topicquests.os.asr.kafka.KafkaHandler;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class DocumizerModel implements IDocumizerModel {
	private DocumizerEnvironment environment;
	private IParagraphProvider paragraphProvider;
	private IDocumentProvider documentProvider;
	private ISentenceProvider sentenceProvider;
	private KafkaHandler kafka;

	/**
	 * 
	 */
	public DocumizerModel(DocumizerEnvironment env) {
		environment = env;
		paragraphProvider = environment.getParagraphProvider();
		documentProvider = environment.getDocProvider();
		sentenceProvider = environment.getSentenceProvider();
	}

	/* (non-Javadoc)
	 * @see org.topicquests.os.asr.documizer.api.IDocumizerModel#newParagraph(java.lang.String, java.lang.String)
	 */
	@Override
	public IDocument newDocument(JSONObject newDocument) {
		//if there is a url, this might be an annotation doc
		// annotation docs can have the same url but different documentIds
		String url = newDocument.getAsString(JSONDocumentObject._URL);
		environment.logDebug("DM.newDocument "+url+"\n"+newDocument);
		JSONObject existingDoc = null;
		IDocument result = null;
		//List<JSONObject> paragraphs = null;
		List<IParagraph> paragraphs = null;
		if (url != null) {
			existingDoc = this.findDocByURL(url);
			environment.logDebug("DM.newDocument-1 "+"\n"+existingDoc);
			if (existingDoc != null) {
				result = new ConcordanceDocument(existingDoc);
				//MERGE new with existing and update
				//paragraphs = mergeAndUpdate(newDocument, result);
			} //else {
			//	result = new ConcordanceDocument(newDocument);
				//make and ship some paragraphs
				//paragraphs = exploreDocument(result);
				//persist document
			//	documentProvider.putDocument(result);
			//	environment.getStats().addToKey(IASRFields.DOCS_IMPORTED);

			//}
		} else { // new document
			String label = newDocument.getAsString(JSONDocumentObject._TITLE);
			//TODO findByTitle just in case need to merge
			//TODO search by pmid and pmcid
			result = new ConcordanceDocument(newDocument);
			//make and ship some paragraphs
			//paragraphs = exploreDocument(result);
			paragraphs = result.listParagraphs();
			//persist document
			documentProvider.putDocument(result);
			environment.getStats().addToKey(IASRFields.DOCS_IMPORTED);
		}
		//then start shipping paragraphs for processing
		if (paragraphs != null && !paragraphs.isEmpty()) {
			Iterator<IParagraph>itr = paragraphs.iterator();
			while (itr.hasNext()) {
				packageAndShipParagraph(itr.next());
			}
		}
		return result;
	}
	
	
	
	List<JSONObject> mergeAndUpdate(JSONObject newDoc, IDocument doc) {
		JSONObject existingDoc = doc.getData();
		String docId = doc.getId();
		JSONDocumentObject nd = new JSONDocumentObject(newDoc);
		List<JSONObject>paras = nd.listParagraphs();
		List<JSONObject> npx = new ArrayList<JSONObject>();
		if (paras != null && !paras.isEmpty()) {
			Iterator<JSONObject>itr = paras.iterator();
			JSONObject jo;
			IParagraph p;
			while (itr.hasNext()) {
				jo = itr.next();
				environment.logDebug("DM.update\n"+jo);
				p = doc.addParagraph(jo.getAsString("text"), jo.getAsString("lang"));
				npx.add(p.getData());
			}
			environment.logDebug("DM.update-1\n"+doc.getData());
			documentProvider.updateDocument(doc);
		}
		return npx;
	}
	
	List<JSONObject> findDocsByLabel(String label) {
		List<JSONObject> result = null;
		IResult r = environment.getDocumentDatabase().findByLabel(label);
		result = (List<JSONObject>) r.getResultObject();
		return result;
	}
	
	JSONObject findDocByURL(String url) {
		JSONObject result = null;
		IResult r = environment.getDocumentDatabase().findByURL(url);
		result = (JSONObject)r.getResultObject();
		return result;
	}

	/**
	 * No clue what this was about
	 * @param doc
	 * @return
	 */
	List<JSONObject> exploreDocument(IDocument doc) {
		String docId = doc.getId();
		IParagraph p;
		JSONObject jo;
		List<JSONObject> paras = (List<JSONObject>)doc.getData().get(JSONDocumentObject._PARAGRAPH_LIST);
		List<JSONObject> result = toParagraphs(paras, docId, doc);
		//TODO other stuff?
		
		return result;
		
	}
	
	List<JSONObject> toParagraphs(List<JSONObject> paras, String docId, IDocument doc) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		if (paras != null && !paras.isEmpty()) {
			Iterator<JSONObject>itr = paras.iterator();
			JSONObject jo;
			IParagraph p;
			while (itr.hasNext()) {
				jo = itr.next();
				p = newParagraph(jo.getAsString("text"), jo.getAsString("lang"), docId );
				doc.addParagraph(p);
				result.add(p.getData());
			}
		}
		return result;
	}
	
	/*
	 * 
{
	**"annotation": "some text",
	"created": "2019-01-20T18:52:04.276776+00:00",
	**"id": "epJOfBzkEem9G9c4W2oLXw",
	"text": "Here from nutritionfacts on vinegar. This covers 28 foods separated into six food categories, then measured insulin response",
	"title": "An insulin index of foods: the insulin demand generated by 1000-kJ portions of common foods. - PubMed - NCBI",
	"uri": "https:\/\/www.ncbi.nlm.nih.gov\/pubmed\/9356547",
	"user": "Gardener@hypothes.is",
	"group": "6xkx19i3",
	"tags": ["Postprandial Insulin Response", "Protein-rich Foods", "Insulin Response"]
}	 * 
	 */
	
	
	/**
	 * Ship individual sentences out to kafka
	 * @param paragraph
	 */
	void packageAndShipParagraph(IParagraph paragraph) {
		String docId = paragraph.getDocumentId();
		String paraId = paragraph.getID();
		String sentId;
		JSONObject event;
		List<ISentence> sentences = (List<ISentence>) paragraph.listSentences();
		ISentence sx;
		if (sentences != null && !sentences.isEmpty()) {
			Iterator<ISentence> itr = sentences.iterator();
			while (itr.hasNext()) {
				sx = itr.next();
				event = new JSONObject();
				event.put("docId", docId);
				event.put("paraId", paraId);
				event.put("sentId", sx.getID());
				event.put("text", sx.getSentence());
				environment.logDebug("DM.shipParagraph "+"\n"+event);
				kafka.shipEvent(event);
			}
		}
		
	}
	/* (non-Javadoc)
	 * @see org.topicquests.os.asr.documizer.api.IDocumizerModel#newSentence(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public IParagraph newParagraph(String abst, String language, String documentId) {
		IParagraph result = new ConcordanceParagraph();
		result.setDocumentId(documentId);
		result.setID(UUID.randomUUID().toString());
		result.setParagraph(abst, language);
		paragraphProvider.putParagraph(result);
		environment.logDebug("DM.newParagraph "+"\n"+result.getData());

		return result;
	}

	@Override
	public IResult acceptDocumentObject(JSONObject documentObject) {
		IResult result = new ResultPojo();
		result.setResultObject(new Boolean(true));
		IDocument d = this.newDocument(documentObject);
		return result;
	}

	@Override
	public void setKafka(KafkaHandler h) {
		kafka = h;
	}

	@Override
	public ISentence newSentence(String documentLocator, String sentence, String userId) {
		ISentence result = new ConcordanceSentence();
		result.setID(UUID.randomUUID().toString());
		result.setSentence(sentence);
		result.setDocumentId(documentLocator);
		result.setCreatorId(userId);
		sentenceProvider.putSentence(result);
		environment.getStats().addToKey(IASRFields.SENTS_IMPORTED);
		return result;
	}

}
