/*
 * Copyright 2014, 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane;

import java.util.*;

import net.minidev.json.*;

import org.topicquests.hyperbrane.api.IAuthor;
import org.topicquests.hyperbrane.api.IDocument;
import org.topicquests.hyperbrane.api.IHyperMembraneOntology;
import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.hyperbrane.api.IPublication;
import org.topicquests.hyperbrane.api.ISentence;
import org.topicquests.ks.api.ITQCoreOntology;
import org.topicquests.os.asr.api.ISentenceProvider;
import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.support.util.DateUtil;



/**
 * @author park
 * 
 */
public class ConcordanceDocument implements IDocument {
	private DocumizerEnvironment environment;
	private ISentenceProvider sentenceProvider;
	private JSONObject data;
	
	public static final String 
		ID						= "id",
		NODE_TYPE				= "type",

		PMID					= "pmid",
		PMCID					= "pcid",
		TITLE				 	= "title",
		AUTHORS					= "authors", // simple names
		LANGUAGE				= "lang",
		ABSTRACTS				= "abstracts",
		URL						= "url",
		THE_PUBLICATION			= "thePub",
		SENTENCES 				= "sentences",
		SUCCESS_SENTENCES		= "sucSents",
		TAG_NAME_LIST			= "tagList",
		TAG_ID_LIST				= "tagIdList",
		SUBSTANCE_NAME_LIST		= "substanceNameList",
		SUBSTANCE_ID_LIST		= "substanceIdList",
		CITATIONS_OF_ME			= "citationMe",
		MY_CITATIONS			= "myCitations",
		NODE_LOCATOR			= "NodeLocator",
		METADATA				= "MetaData",
		DB_PEDIA_URI_LIST		= "dbpuri",
		ISA_STRUCTURE			= "isaStruct",
		WORDGRAM_HISTOGRAM		= "wordgramHisto";

	
	/**
	 * Used by {@link ConversationModel} newConversation
	 * @param db
	 * @param m can be <code>null</code> if it is not a conversation 
	 */
	public ConcordanceDocument() {
		data = new JSONObject();
		environment = DocumizerEnvironment.getInstance();
		sentenceProvider = environment.getSentenceProvider();

	}
	
	/**
	 * Used only for importing
	 * @return
	 */
	public static ConcordanceDocument newDocument() {
		return new ConcordanceDocument(true);
	}
	
	private ConcordanceDocument(boolean t) {
		data = new JSONObject();
	}
	
	/**
	 * Used by {@link ConversationModel} getConversation
	 * @param db
	 * @param jo
	 * @param m
	 */
	public ConcordanceDocument(JSONObject jo) {
		data = jo;
		environment = DocumizerEnvironment.getInstance();
		sentenceProvider = environment.getSentenceProvider();
		//We must study the data structure and ensure that
		// what comes in is corrected, as necessary, to what this class expects
		String ix = this.getId();
		if (ix == null) 
			ix = data.getAsString("lox");
		if (ix != null) {
			this.setId(ix);
			data.remove("lox");
		} else
			throw new RuntimeException("ConcordanceDoc.badId "+data.toJSONString());
		
		List<String> l = this.listSubstanceNames();
		Iterator<String> itr;
		if (l == null) {
			l = (List<String>)data.get("substanceList");
			if (l != null) {
				itr = l.iterator();
				while (itr.hasNext())
					addSubstanceName(itr.next());
				data.remove("substanceList");
			}
		}
		l = listMyCitations();
		if (l == null) {
			l = (List<String>)data.get("citations");
			if (l != null) {
				itr = l.iterator();
				while (itr.hasNext())
					addMyCitation(itr.next());
				data.remove("citations");
			}
		}
	}


	void startTracer(String id) {
		//if (tracer != null)
		//	tracer = new Tracer("Doc_"+id+"_"+System.currentTimeMillis(), LoggingPlatform.getLiveInstance());
	}


	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.IDocument#listAuthors()
	 */
	@Override
	public List<IAuthor> listAuthors() {
		return (List<IAuthor>)data.get(AUTHORS);
	}

	@Override
	public void addParagraph(String theParagraph, String language) {
		IParagraph p = new ConcordanceParagraph();
		p.setParagraph(theParagraph, language);
		this.addParagraph(p);
	}
	
	@Override
	public List<String> listParagraphStrings(String language) {
		List<String>result = new ArrayList<String>();
		List<JSONObject> l = (List<JSONObject>)this.getData().get(IHyperMembraneOntology.PARAGRAPH_PROPERTY_TYPE);
		if (l != null) {
			Iterator<JSONObject>itr = l.iterator();
			JSONObject jo;
			while (itr.hasNext()) {
				jo = itr.next();
				if (jo.getAsString("lang").equals(language))
						result.add(jo.getAsString("text"));
			}
		}
		return result;
	}

	@Override
	public List<IParagraph> listParagraphs() {
		List<IParagraph> result = new ArrayList<IParagraph>();
		JSONObject p = (JSONObject)data.get(IHyperMembraneOntology.PARAGRAPH_PROPERTY_TYPE);
		if (p != null && !p.isEmpty()) {
			Collection<Object> l = p.values();
			Iterator<Object>itr = l.iterator();
			JSONObject jo;
			while (itr.hasNext()) {
				jo = (JSONObject)itr.next(); //TODO will this work?
				result.add(new ConcordanceParagraph(jo));
			}
		}
		return result;
	}


	@Override
	public void traceStatement(String traceMessage) {
	//	synchronized(tracer) {
	//		tracer.trace(System.currentTimeMillis(), traceMessage);
	//	}
	}

	@Override
	public void setMetadata(String key, Object value) {
		JSONObject m = getMetadata();
		if (m == null)
			m = new JSONObject();
		m.put(key, value);
		data.put(METADATA, m);
	}

	@Override
	public JSONObject getMetadata() {
		return (JSONObject)getData().get(METADATA);
	}

	@Override
	public Object getMetadataValue(String key) {
		Object result = null;
		JSONObject m = getMetadata();
		if (m != null)
			result = m.get(key);
		return result;
	}
	
	/////////////////////////
	// The other way is to list paragraphs, then list sentences
	////////////////////////


	@Override
	public void removeSentence(String sentenceLocator) {
		List<String>ss = (List<String>)data.get(SENTENCES);
		if (ss != null)
			ss.remove(sentenceLocator);
		//TODO remove from data
	}


	@Override
	public void setOntologyClassLocator(String locator) {
		setProperty(NODE_LOCATOR, locator);
	}

	@Override
	public String getOntologyClassLocator() {
		return (String)data.get(NODE_LOCATOR);
	}





	//////////////////////////
	// Paragraphs are stored in a struct:
	//  { <id>: <paragraph>, <id>:<paragraph>
	//////////////////////////
	@Override
	public void addParagraph(IParagraph paragraph) {
		//We are studying whole paragraphs
		JSONObject p = (JSONObject)data.get(IHyperMembraneOntology.PARAGRAPH_PROPERTY_TYPE);
		if (p == null)
			p = new JSONObject();
		p.put(paragraph.getID(), paragraph.getData());
		data.put(IHyperMembraneOntology.PARAGRAPH_PROPERTY_TYPE, p);
	}
	
	@Override
	public void updateParagraph(IParagraph paragraph) {
		//Just overwrite what's there
		addParagraph(paragraph);
	}

	@Override
	public void addSuccessfullyReadSentenceId(String sentenceId) {
		List<String>l = (List<String>)data.get(SUCCESS_SENTENCES);
		if (l == null) l = new ArrayList<String>();
		if (!l.contains(sentenceId))
			l.add(sentenceId);
		data.put(SUCCESS_SENTENCES, l);
	}

	@Override
	public List<String> listSuccessfullyReadSentenceIds() {
		return (List<String>)data.get(SUCCESS_SENTENCES);
	}

	@Override
	public void addDbPediaURI(String uri) {
		List<String> l = listDbPediaURIs();
		if (l == null) l = new ArrayList<String>();
		if (!l.contains(uri))
			l.add(uri);
		getData().put(DB_PEDIA_URI_LIST, l);
	}

	@Override
	public List<String> listDbPediaURIs() {
		return (List<String>)getData().get(DB_PEDIA_URI_LIST);
	}

	@Override
	public void addTagName(String tag) {
		List<String>t = listTagNames();
		if (t == null)
			t = new ArrayList<String>();
		if (!t.contains(tag))
			t.add(tag);
		data.put(TAG_NAME_LIST, t);
	}

	@Override
	public List<String> listTagNames() {		// TODO Auto-generated method stub
		return (List<String>)data.get(TAG_NAME_LIST);
	}

	@Override
	public void addTagWordGramId(String id) {
		List<String>t = listTagWordGramIds();
		if (t == null)
			t = new ArrayList<String>();
		if (!t.contains(id))
			t.add(id);
		data.put(TAG_ID_LIST, t);
	}

	@Override
	public List<String> listTagWordGramIds() {
		return (List<String>)data.get(this.TAG_ID_LIST);
	}

	@Override
	public void addSubstanceName(String name) {
		List<String>t = listSubstanceNames();
		if (t == null)
			t = new ArrayList<String>();
		if (!t.contains(name))
			t.add(name);
		data.put(SUBSTANCE_NAME_LIST, t);
	}

	@Override
	public List<String> listSubstanceNames() {
		return (List<String>)data.get(this.SUBSTANCE_NAME_LIST);
	}

	@Override
	public void addSubstanceWordGramId(String id) {
		List<String>t = listSubstanceWordGramIds();
		if (t == null)
			t = new ArrayList<String>();
		if (!t.contains(id))
			t.add(id);
		data.put(SUBSTANCE_ID_LIST, t);
	}

	@Override
	public List<String> listSubstanceWordGramIds() {
		return (List<String>)data.get(this.SUBSTANCE_ID_LIST);
	}

	@Override
	public void setLanguage(String lang) {
		data.put(LANGUAGE, lang);
	}

	@Override
	public String getLanguage() {
		return data.getAsString(LANGUAGE);
	}
/*
	@Override
	public void setAbstract(String text, String language) {
		JSONObject abs = getAllAbstracts();
		if (abs == null) 
			abs = new JSONObject();
		abs.put(language, text);
		data.put(ABSTRACTS, abs);
	}

	@Override
	public String getAbstract(String language) {
		JSONObject abs = getAllAbstracts();
		if (abs == null)
			return null;
		return abs.getAsString(language);
	}
*/
	@Override
	public JSONObject getAllAbstracts() {
		return (JSONObject)this.data.get(ABSTRACTS);
	}

	@Override
	public void setVersion(String version) {
	    data.put(ITQCoreOntology.VERSION, version);
	}

	@Override
	public String getVersion() {
		return data.getAsString(ITQCoreOntology.VERSION);
	}

	@Override
	public IResult doUpdate() {
		IResult result = new ResultPojo();
	    String newVersion = Long.toString(System.currentTimeMillis());
	    setVersion(newVersion);
		return result;
	}


	@Override
	public void setCreatorId(String id) {
		data.put(ITQCoreOntology.CREATOR_ID_PROPERTY, id);
	}

	@Override
	public String getCreatorId() {
		return data.getAsString(ITQCoreOntology.CREATOR_ID_PROPERTY);
	}



	@Override
	public void setDate(Date date) {
	    data.put(ITQCoreOntology.CREATED_DATE_PROPERTY, DateUtil.formatIso8601(date));
	}

	@Override
	public void setDate(String date) {
	    data.put(ITQCoreOntology.CREATED_DATE_PROPERTY, date);
	}

	@Override
	public Date getDate() {
	    String dx = data.getAsString(ITQCoreOntology.CREATED_DATE_PROPERTY);
	    return DateUtil.fromIso8601(dx);
	}

	@Override
	public String getDateString() {
	    return data.getAsString(ITQCoreOntology.CREATED_DATE_PROPERTY);
	}

	@Override
	public void setLastEditDate(Date date) {
	    data.put(ITQCoreOntology.LAST_EDIT_DATE_PROPERTY, DateUtil.formatIso8601(date));
	    setVersion(Long.toString(System.currentTimeMillis()));
	}

	@Override
	public void setLastEditDate(String date) {
	    data.put(ITQCoreOntology.LAST_EDIT_DATE_PROPERTY, date);
	    setVersion(Long.toString(System.currentTimeMillis()));
	}

	@Override
	public Date getLastEditDate() {
	    String dx = data.getAsString(ITQCoreOntology.LAST_EDIT_DATE_PROPERTY);
	    return DateUtil.fromIso8601(dx);
	}

	@Override
	public String getLastEditDateString() {
		return data.getAsString(ITQCoreOntology.LAST_EDIT_DATE_PROPERTY);
	}

	@Override
	public JSONObject getData() {
		return data;
	}

	@Override
	public String toJSONString() {
		return data.toJSONString();
	}

	@Override
	public void addLabel(String label, String language) {
		JSONObject jo = getLabels();
		if (jo == null)
			jo = new JSONObject();
		List<String>l = (List<String>)jo.get(language);
		if (l == null)
			l = new ArrayList<String>();
		if (!l.contains(label))
			l.add(label);
		jo.put(language, l);
		data.put(ITQCoreOntology.LABEL_PROPERTY, jo);		
	}

	@Override
	public JSONObject getLabels() {
		JSONObject result = (JSONObject)data.get(ITQCoreOntology.LABEL_PROPERTY);
		return result;
	}

	@Override
	public String getLabel(String language) {
		JSONObject jo = getLabels();
		String result = null;
		if (jo != null) {
			List<String>l = (List<String>)jo.get(language);
			if (l != null && !l.isEmpty())
				result = l.get(0);
		}
		return result;
	}

	@Override
	public List<String> listLabels() {
		List<String>result = null;
		JSONObject jo = getLabels();
		if (jo != null && !jo.isEmpty()) {
			result = new ArrayList<String>();
			Iterator<String>itr = jo.keySet().iterator();
			List<String> l;
			while (itr.hasNext()) {
				l = (List<String>)jo.get(itr.next());
				if (l != null)
					result.addAll(l);
			}
		}
		return result;
	}

	@Override
	public List<String> listLabels(String language) {
		JSONObject jo = getLabels();
		String result = null;
		if (jo != null)
			return (List<String>)jo.get(language);
		return null;
	}

	@Override
	public void addDetails(String details, String language) {
		JSONObject jo = getDetails();
		if (jo == null)
			jo = new JSONObject();
		List<String>l = (List<String>)jo.get(language);
		if (l == null)
			l = new ArrayList<String>();
		if (!l.contains(details))
			l.add(details);
		jo.put(language, l);
		data.put(ITQCoreOntology.DETAILS_PROPERTY, jo);
	}

	@Override
	public JSONObject getDetails() {
		JSONObject jo = (JSONObject)data.get(ITQCoreOntology.DETAILS_PROPERTY);
		return jo;
	}

	@Override
	public String getDetails(String language) {
		JSONObject jo = getDetails();
		String result = null;
		if (jo != null) {
			List<String>l = (List<String>)jo.get(language);
			if (l != null && !l.isEmpty())
				result = l.get(0);
		}
		return result;
	}

	@Override
	public List<String> listDetails() {
		List<String>result = null;
		JSONObject jo = getDetails();
		if (jo != null && !jo.isEmpty()) {
			result = new ArrayList<String>();
			Iterator<String>itr = jo.keySet().iterator();
			List<String> l;
			while (itr.hasNext()) {
				l = (List<String>)jo.get(itr.next());
				if (l != null)
					result.addAll(l);
			}
		}
		return result;
	}

	@Override
	public List<String> listDetails(String language) {
		JSONObject jo = getDetails();
		String result = null;
		if (jo != null)
			return (List<String>)jo.get(language);
		return null;
	}


	@Override
	public void addPropertyValue(String key, String value) {
		List<String>l = (List<String>)data.get(key);
		if (l == null)
			l = new ArrayList<String>();
		if (!l.contains(value))
			l.add(value);
		data.put(key, l);
		
	}

	@Override
	public Object getProperty(String key) {
		return data.get(key);
	}


	@Override
	public void removeProperty(String key) {
		data.remove(key);
	}

	@Override
	public void removePropertyValue(String key, String value) {
		List<String>l = (List<String>)data.get(key);
		if (l != null) {
			l.remove(value);
			data.put(key, l);
		}
	}

	@Override
	public String getTopicLocator() {
		return data.getAsString(ITQCoreOntology.LOCATOR_PROPERTY);
	}

	@Override
	public void setId(String id) {
		data.put(ID, id);
	}

	@Override
	public String getId() {
		return data.getAsString(ID);
	}


	@Override
	public void setProperty(String key, Object value) {
		data.put(key, value);
	}

	@Override
	public void setTopicLocator(String nodeLocator) {
		data.put(ITQCoreOntology.LOCATOR_PROPERTY, nodeLocator);
	}

	@Override
	public void setNodeType(String typeLocator) {
		data.getAsString(ITQCoreOntology.INSTANCE_OF_PROPERTY_TYPE);
	}


	@Override
	public IAuthor addAuthor(String title, String initials, String firstName, String middleName, String lastName,
						  String suffix, String degree, String fullName, String authorLocator, 
						  String publicationName, String publicationLocator, 
						  String publisherName, String publisherLocator, 
						  String affiliationName, String affiliationLocator) {
		IAuthor a = new AuthorPojo();
		if (title != null && !title.equals(""))
			a.setAuthorTitle(title);
		if (initials != null && !initials.equals(""))
			a.setAuthorInitials(initials);
		if (firstName != null && !firstName.equals(""))
			a.addAuthorFirstName(firstName);
		if (middleName != null && !middleName.equals(""))
			a.setAuthorMiddleName(middleName);
		if (lastName != null && !lastName.equals(""))
			a.setAuthorLastName(lastName);
		if (suffix != null && !suffix.equals(""))
			a.setAuthorSuffix(suffix);
		if (degree != null && !degree.equals(""))
			a.setAuthorDegree(degree);
		if (fullName != null && !fullName.equals(""))
			a.setAuthorFullName(fullName);
		if (authorLocator != null && !authorLocator.equals(""))
			a.setAuthorLocator(authorLocator);
		if (publicationName != null && !publicationName.equals(""))
			a.setPublicationName(publicationName);
		if (publicationLocator != null && !publicationLocator.equals(""))
			a.setPublicationLocator(publicationLocator);
		if (publisherName != null && !publisherName.equals(""))
			a.setPublisherName(publisherName);
		if (publisherLocator != null && !publisherLocator.equals(""))
			a.setPublisherLocator(publisherLocator);
		if (affiliationName != null && !affiliationName.equals(""))
			a.addAffiliationName(affiliationName);
		if (affiliationLocator != null && !affiliationLocator.equals(""))
			a.setAffiliationLocator(affiliationLocator);
		this.addAuthor(a);
		return a;
	}

	@Override
	public void addAuthor(IAuthor author) {
		List<IAuthor>a = this.listAuthors();
		if (a == null)
			a = new ArrayList<IAuthor>();
		a.add(author);
		this.setAuthorList(a);
	}

	@Override
	public void setPublication(IPublication doc) {
		data.put(THE_PUBLICATION, doc);
	}

	@Override
	public IPublication getPublication() {
		return (IPublication)data.get(THE_PUBLICATION);
	}

	@Override
	public void addMyCitation(String citation) {
		List<String>l = listMyCitations();
		if (l == null)
			l = new ArrayList<String>();
		if (!l.contains(citation))
			l.add(citation);
		setMyCitationList(l);
	}

	@Override
	public List<String> listMyCitations() {
		return (List<String>)data.get(MY_CITATIONS);
	}

	@Override
	public void addCitation(String citation) {
		List<String>l = listCitations();
		if (l == null)
			l = new ArrayList<String>();
		if (!l.contains(citation))
			l.add(citation);
		setCitationList(l);
	}

	@Override
	public List<String> listCitations() {
		return (List<String>)data.get(CITATIONS_OF_ME);
	}

	@Override
	public void setAuthorList(List<IAuthor> authors) {
		data.put(AUTHORS, authors);
	}

	@Override
	public void setMyCitationList(List<String> citations) {
		data.put(MY_CITATIONS, citations);
	}

	@Override
	public void setCitationList(List<String> citations) {
		data.put(CITATIONS_OF_ME, citations);
	}

	@Override
	public void setPMID(String pmid) {
		data.put(PMID, pmid);
	}

	@Override
	public String getPMID() {
		return data.getAsString(PMID);
	}

	@Override
	public void setPMCID(String pmcid) {
		data.put(PMCID, pmcid);
	}

	@Override
	public String getPMCID() {
		return data.getAsString(PMCID);
	}
/*
	@Override
	public void addIsA(String subjectWordGramId, String objectWordGramId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isA(String subjectWordGramId, String objectWordGramId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> listIsAs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(String subjectWordGramId, String objectWordGramId) {
		// TODO Auto-generated method stub
		
	}
*/
	@Override
	public void addToHistogram(String wordgramId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHistogramCount(String wordgramId) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public String getNodeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDocumentType(String type) {
		data.put(NODE_TYPE, type);
	}

	@Override
	public String getDocumentType() {
		return data.getAsString(NODE_TYPE);
	}

	@Override
	public void setURL(String url) {
		data.put(URL, url);
	}

	@Override
	public String getURL() {
		return data.getAsString(URL);
	}

	@Override
	public void addAbstractParagraph(String text, String language) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> listAbstractsForLanguage(String language) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<JSONObject> listAbstracts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addWikidataURI(String uri) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> listWikidataURIs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getEntityHistogram() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSentence(String sentenceId) {
		List<String> l = listSentenceIDs();
		if (l == null) l = new ArrayList<String>();
		if (!l.contains(sentenceId)) 
			l.add(sentenceId);
		data.put(SENTENCES, l);
	}

	@Override
	public List<String> listSentenceIDs() {
		List<String> result = (List<String>)data.get(SENTENCES);
		return result;
	}


}
