/*
 * Copyright 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.topicquests.asr.paragraph.api.IParagraphClient;
import org.topicquests.hyperbrane.ConcordanceDocument;
import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.os.asr.api.IParagraphProvider;
import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class ParagraphProvider implements IParagraphProvider {
	private DocumizerEnvironment environment;
	private IParagraphClient paragraphDatabase;

	/**
	 * 
	 */
	public ParagraphProvider(DocumizerEnvironment env) {
		environment = env;
		paragraphDatabase = environment.getParagraphDatabase();
	}

	@Override
	public IResult getParagraph(String locator) {
		IResult result = paragraphDatabase.get(locator);
		JSONObject jo = (JSONObject)result.getResultObject();
		if (jo != null)
			result.setResultObject(new ConcordanceDocument(jo));
		return result;
	}

	@Override
	public IResult putParagraph(IParagraph paragraph) {
		IResult result = paragraphDatabase.put(paragraph.getID(), paragraph.getData());
		return result;
	}

	@Override
	public IResult updateParagraph(IParagraph paragraph) {
		IResult result = paragraphDatabase.update(paragraph.getID(), paragraph.getData());
		return result;
	}

	@Override
	public Iterator<JSONObject> iterateParagraphs(int start, int count) {
		IResult r = paragraphDatabase.listParagraphs(start, count);
		List<JSONObject> l = (List<JSONObject>)r.getResultObject();
		if (l == null)
			l = new ArrayList<JSONObject>();
		return l.iterator();
	}

}
