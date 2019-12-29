/**
 * 
 */
package org.topicquests.os.asr.documizer;

import java.util.*;
import org.topicquests.hyperbrane.ConcordanceDocument;
import org.topicquests.hyperbrane.ConcordanceParagraph;
import org.topicquests.hyperbrane.api.IDocument;
import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.ks.TicketPojo;
import org.topicquests.ks.api.ITQCoreOntology;
import org.topicquests.ks.api.ITicket;
import org.topicquests.os.asr.JSONDocumentObject;
import org.topicquests.os.asr.api.IDocumentProvider;
import org.topicquests.os.asr.api.IParagraphProvider;
import org.topicquests.os.asr.api.IStatisticsClient;
import org.topicquests.os.asr.documizer.api.IDocumizerModel;
import org.topicquests.os.asr.kafka.KafkaHandler;
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
	private IStatisticsClient stats;
	private ITicket credentials;
	private KafkaHandler kafka;

	/**
	 * 
	 */
	public DocumizerModel(DocumizerEnvironment env) {
		environment = env;
		stats = environment.getStats();
		paragraphProvider = environment.getParagraphProvider();
		documentProvider = environment.getDocProvider();
		credentials = new TicketPojo(ITQCoreOntology.SYSTEM_USER);
		kafka = new KafkaHandler(environment);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.os.asr.documizer.api.IDocumizerModel#newParagraph(java.lang.String, java.lang.String)
	 */
	@Override
	public IDocument newDocument(JSONObject newDocument) {
		String label = newDocument.getAsString(JSONDocumentObject._TITLE);
		if (label == null)
			label = newDocument.getAsString(ITQCoreOntology.LABEL_PROPERTY);
		if (label != null) {
			List<JSONObject> docs = findDocsByLabel(label);
			//TODO
		}
		IDocument result = new ConcordanceDocument(newDocument);
		List<JSONObject> paragraphs = exploreDocument(result);
		//persist document
		documentProvider.putDocument(result);
		//then start shipping paragraphs for processing
		if (paragraphs != null && !paragraphs.isEmpty()) {
			Iterator<JSONObject>itr = paragraphs.iterator();
			while (itr.hasNext()) {
				packageAndShipParagraph(itr.next());
			}
		}
		return result;
	}
	
	List<JSONObject> findDocsByLabel(String label) {
		List<JSONObject> result = null;
		IResult r = environment.getDocumentDatabase().findByLabel(label);
		result = (List<JSONObject>) r.getResultObject();
		return result;
	}

	List<JSONObject> exploreDocument(IDocument doc) {
		String docId = doc.getId();
		IParagraph p;
		JSONObject jo;
		List<JSONObject> paragraphs = (List<JSONObject>)doc.getData().get(JSONDocumentObject._PARAGRAPH_LIST);
		if (paragraphs != null && !paragraphs.isEmpty()) {
			Iterator<JSONObject>itr = paragraphs.iterator();
			while (itr.hasNext()) {
				jo = itr.next();
				p = newParagraph(jo.getAsString("text"), jo.getAsString("lang"), docId );
				doc.addParagraph(p);
				
			}
		}
		//TODO other stuff?
		
		return paragraphs;
		
	}
	
	void packageAndShipParagraph(JSONObject paragraph) {
		//Convert to the appropriate JSONObject and ship
		JSONObject event = new JSONObject();
		//TODO
		kafka.shipEvent(event);
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
		return result;
	}

	@Override
	public IResult acceptDocumentObject(JSONObject documentObject) {
		// TODO Auto-generated method stub
		return null;
	}

}
