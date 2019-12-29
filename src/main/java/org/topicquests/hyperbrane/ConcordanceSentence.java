/*
 * Copyright 2014, 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.hyperbrane;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minidev.json.*;

import org.topicquests.hyperbrane.api.ISentence;
import org.topicquests.hyperbrane.api.IWordGram;
import org.topicquests.ks.api.ITQCoreOntology;

import org.topicquests.support.api.IResult;
import org.topicquests.support.util.DateUtil;


/**
 * @author park
 *
 */
public class ConcordanceSentence  implements ISentence {

	private final String 
		ID			 		= "id",
		PARA_ID				= "paraId",
		DOC_ID				= "docId",
		SENTENCE 			= "sentence",
		STAGE				= "stage",
		PARSE_RESULT		= "parseResult",
		NORMALIZED_SENTENCE	= "normalizedSentence",
		WORDGRAMS			= "wordGrams",
		LOCATED_WORDGRAMS	= "locatedWordGrams",
		LENS_CODES			= "lensCodes",
		VERB_WORDGRAMS		= "verbWordGrams",
		NOUN_WORDGRAMS		= "nounWordGrams",
		PREVIOUS_SENTENCE	= "previousSentence",
		NEXT_SENTENCE		= "nextSentence",
		SENTENCE_TRIPLE		= "sentenceTriple",
		WORKING_SENTENCE	= "wsList",
		TUPLES				= "tuples",
		SYNONYMS			= "syns",
		FIGURES				= "figs",
		DATA_LIST			= "data",
		IS_QUESTION			= "isQ",
		DB_PEDIA			= "dbP";

	private List<IWordGram>finalParseList = null;
	private List<IWordGram> workingSentence;
	private JSONObject data;
	/**
	 * 
	 */
	public ConcordanceSentence() {
		data = new JSONObject();
	}

	public ConcordanceSentence(JSONObject jo) {
		data = jo;
	}
	
	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.ISentence#setID(java.lang.String)
	 */
	@Override
	public void setID(String id) {
		data.put(ID, id);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.ISentence#getID()
	 */
	@Override
	public String getID() {
		return (String)data.get(ID);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.ISentence#setSentence(java.lang.String)
	 */
	@Override
	public void setSentence(String sentence) {
		String x = sentence; // TODO might need to escape this
		x = JSONObject.escape(x);
		getData().put(SENTENCE, x);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.ISentence#getSentence()
	 */
	@Override
	public String getSentence() {
		return (String)getData().get(SENTENCE);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.concordance.api.ISentence#setTuple(org.topicquests.concordance.api.ITuple)
	 */
	@Override
	public void addTupleId(String tupleId) {
		JSONArray tups = (JSONArray)getData().get(TUPLES);
		if (tups == null)
			tups = new JSONArray();
		if (!tups.contains(tupleId)) {
			tups.add(tupleId);
			getData().put(TUPLES, tups);
		}
	}

	@Override
	public List<String> listTupleIds() {
		JSONArray sentences = (JSONArray)getData().get(TUPLES);
		List<String> result = null;
		if (sentences != null) {
			result = new ArrayList<String>();
			Iterator<Object>itr = sentences.iterator();
			while(itr.hasNext()) {
				result.add((String)itr.next());
			}
		}
		return result;
	}


	@Override
	public void setDocumentId(String id) {
		getData().put(DOC_ID, id);
	}

	@Override
	public String getDocumentId() {
		return (String)getData().get(DOC_ID);
	}


	@Override
	public IResult normalize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNormalizedSentence(String normalizedSentence) {
		getData().put(NORMALIZED_SENTENCE, normalizedSentence);
	}

	@Override
	public String getNormalizedSentence() {
		return (String)getData().get(NORMALIZED_SENTENCE);
	}

	@Override
	public void addWordGramId(String gramId) {
		if (isInteger(gramId))
			throw new RuntimeException("BAD GRAMID "+gramId);
		JSONArray wgs = (JSONArray)getData().get(WORDGRAMS);
		if (wgs == null)
			wgs = new JSONArray();
		if (!wgs.contains(gramId)) {
			wgs.add(gramId);
			getData().put(WORDGRAMS, wgs);
		}
	}
	
	boolean isInteger(String id) {
		if (!id.endsWith(".")) {
			if (id.indexOf('.') == -1)
				return true;
		}
		return false;
	}

	@Override
	public List<String> listWordGramIds() {
		JSONArray sentences = (JSONArray)getData().get(WORDGRAMS);
		List<String> result = null;
		if (sentences != null) {
			result = new ArrayList<String>();
			Iterator<Object>itr = sentences.iterator();
			while(itr.hasNext()) {
				result.add((String)itr.next());
			}
		}
		return result;
	}

	@Override
	public void removeWordGram(String gramId) {
		JSONArray sentences = (JSONArray)getData().get(WORDGRAMS);
		if (sentences != null)
			sentences.remove(gramId);
	}

	@Override
	public void removeTuple(String tupleId) {
		JSONArray sentences = (JSONArray)getData().get(TUPLES);
		if (sentences != null)
			sentences.remove(tupleId);
	}

	@Override
	public void setSentenceTripleId(String tripleId) {
		getData().put(SENTENCE_TRIPLE, tripleId);
	}

	@Override
	public String getSentenceTripleId() {
		return (String)getData().get(SENTENCE_TRIPLE);
	}

	@Override
	public void setPreviousSentenceId(String id) {
		getData().put(PREVIOUS_SENTENCE, id);
	}

	@Override
	public String getPreviousSentenceId() {
		return (String)getData().get(PREVIOUS_SENTENCE);
	}

	@Override
	public void setNextSentenceId(String id) {
		getData().put(NEXT_SENTENCE, id);
	}

	@Override
	public String getNextSentenceId() {
		return (String)getData().get(NEXT_SENTENCE);
	}

	@Override
	public void addWordGramWithLocatorId(String gramId) {
		JSONArray tups = (JSONArray)getData().get(LOCATED_WORDGRAMS);
		if (tups == null)
			tups = new JSONArray();
		if (!tups.contains(gramId)) {
			tups.add(gramId);
			getData().put(LOCATED_WORDGRAMS, tups);
		}
	}

	@Override
	public List<String> listWordGramsWithLocators() {
		JSONArray sentences = (JSONArray)getData().get(LOCATED_WORDGRAMS);
		List<String> result = null;
		if (sentences != null) {
			result = new ArrayList<String>();
			Iterator<Object>itr = sentences.iterator();
			while(itr.hasNext()) {
				result.add((String)itr.next());
			}
		}
		return result;
	}
	@Override
	public void addVerbWordGramId(String gramId) {
		JSONArray tups = (JSONArray)getData().get(VERB_WORDGRAMS);
		if (tups == null)
			tups = new JSONArray();
		if (!tups.contains(gramId)) {
			tups.add(gramId);
			getData().put(VERB_WORDGRAMS, tups);
		}	
	}

	@Override
	public List<String> listVerbWordGramIds() {
		JSONArray sentences = (JSONArray)getData().get(VERB_WORDGRAMS);
		List<String> result = null;
		if (sentences != null) {
			result = new ArrayList<String>();
			Iterator<Object>itr = sentences.iterator();
			while(itr.hasNext()) {
				result.add((String)itr.next());
			}
		}
		return result;
	}
	@Override
	public void addNounWordGramId(String gramId) {
		JSONArray tups = (JSONArray)getData().get(NOUN_WORDGRAMS);
		if (tups == null)
			tups = new JSONArray();
		if (!tups.contains(gramId)) {
			tups.add(gramId);
			getData().put(NOUN_WORDGRAMS, tups);
		}	
	}

	@Override
	public List<String> listNounWordGramIds() {
		JSONArray sentences = (JSONArray)getData().get(NOUN_WORDGRAMS);
		List<String> result = null;
		if (sentences != null) {
			result = new ArrayList<String>();
			Iterator<Object>itr = sentences.iterator();
			while(itr.hasNext()) {
				result.add((String)itr.next());
			}
		}
		return result;
	}

	///////////////////////////////////////////
	//The game is to fill in finalParseList
	//through parsing processes. This list will be used
	//for further harvesting, including topic map, conceptual graph, etc.
	///////////////////////////////////////////
	@Override
	public List<IWordGram> listFinalParse() {
		return finalParseList;
	}

	@Override
	public void setStage(String stage) {
		getData().put(STAGE, stage);
	}

	@Override
	public String updateToNextStage() {
		String stage = getStage();
		if (!stage.equals(ISentence.SIXTH_STAGE)) {
			int st = Integer.parseInt(stage);
			stage = Integer.toString(++st);
			setStage(stage);
		}
		return stage;
	}

	@Override
	public String getStage() {
		return (String)getData().get(STAGE);
	}

	@Override
	public void setLinkGrammarParseResult(Map<String, Object> result) {
		getData().put(PARSE_RESULT, result);
	}

	@Override
	public Map<String, Object> getLinkGrammarParseResult() {
		return (Map<String,Object>)getData().get(PARSE_RESULT);
	}


	@Override
	public void setParagraphId(String id) {
		data.put(PARA_ID, id);
	}

	@Override
	public String getParagraphId() {
		return data.getAsString(PARA_ID);
	}

	@Override
	public void addFigureNumberWordGramId(String id) {
		List<String> l = (List<String>)data.get(FIGURES);
		if (l == null) l = new ArrayList<String>();
		if (!l.contains(id))
			l.add(id);
		data.put(FIGURES, l);
	}

	@Override
	public List<String> listFigureNumberWordGramIds() {
		return (List<String>)data.get(FIGURES);
	}

	@Override
	public boolean hasFigureNumbers() {
		return (listFigureNumberWordGramIds() != null);
	}

	@Override
	public void addSynonymWordGramId(String id) {
		List<String> l = (List<String>)data.get(SYNONYMS);
		if (l == null) l = new ArrayList<String>();
		if (!l.contains(id))
			l.add(id);
		data.put(SYNONYMS, l);
	}

	@Override
	public List<String> listSynonymWordGramIds() {
		return (List<String>)data.get(SYNONYMS);
	}

	@Override
	public boolean hasSynonyms() {
		return (listSynonymWordGramIds() != null);
	}

	@Override
	public void addDataWordGramId(String id) {
		List<String> l = (List<String>)data.get(DATA_LIST);
		if (l == null) l = new ArrayList<String>();
		if (!l.contains(id))
			l.add(id);
		data.put(DATA_LIST, l);
	}

	@Override
	public List<String> listDataWordGramIds() {
		return (List<String>)data.get(DATA_LIST);
	}

	@Override
	public boolean hasData() {
		return (listDataWordGramIds() != null);
	}

	@Override
	public boolean isMeta() {
		return (hasData() || hasSynonyms() || hasFigureNumbers());
	}

	@Override
	public boolean isQuestion() {
		// not null means true
		return (data.get(IS_QUESTION) != null);
	}

	@Override
	public void setIsQuestion(boolean t) {
		if (t)
			data.put(IS_QUESTION, "T");
	}

	@Override
	public void addDbPediaData(JSONObject dbPedia) {
		data.put(DB_PEDIA, dbPedia);
	}

	@Override
	public JSONObject getDbPediaData() {
		return (JSONObject)data.get(DB_PEDIA);
	}

	@Override
	public void setWorkingSentence(List<IWordGram> ws) {
		workingSentence = ws;
		//Everytime ws is sent in, update the internal collection
		List<String>wgids = new ArrayList<String>();
		Iterator<IWordGram> itr = ws.iterator();
		IWordGram wg;
		while (itr.hasNext()) {
			wg = itr.next();
			if (wg != null)
				wgids.add((String)wg.getId());
			else
				wgids.add(null);
		}
		data.put(WORKING_SENTENCE, wgids);
	}

	@Override
	public List<IWordGram> getWorkingSentence() {
		return workingSentence;
	}

	@Override
	public List<String> getWorkingSentenceIds() {
		return (List<String>)data.get(WORKING_SENTENCE);
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
	}

	@Override
	public void setLastEditDate(String date) {
	    data.put(ITQCoreOntology.LAST_EDIT_DATE_PROPERTY, date);
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
	public void setNodeType(String typeLocator) {
		data.put(ITQCoreOntology.INSTANCE_OF_PROPERTY_TYPE, typeLocator);
	}

	@Override
	public String getNodeType() {
		return data.getAsString(ITQCoreOntology.INSTANCE_OF_PROPERTY_TYPE);
	}


}
