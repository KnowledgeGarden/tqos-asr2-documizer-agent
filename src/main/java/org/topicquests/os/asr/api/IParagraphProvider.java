/*
 * Copyright 2019 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.api;

import java.util.Iterator;

import org.topicquests.hyperbrane.api.IParagraph;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public interface IParagraphProvider {

	IResult getParagraph(String locator);
	
	IResult putParagraph(IParagraph paragraph);
	
	IResult updateParagraph(IParagraph paragraph);

	Iterator<JSONObject> iterateParagraphs(int start, int count);

}
