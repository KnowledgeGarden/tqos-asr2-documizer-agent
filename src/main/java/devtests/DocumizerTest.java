/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package devtests;

import org.topicquests.pg.PostgresConnectionFactory;
import org.topicquests.pg.api.IPostgresConnection;
import org.topicquests.pg.api.IPostgresConnectionFactory;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 * This test presumes the following:<br/>
 * <ol><li>A postgreSQL db named "test_library" is created
 * and booted with asr_schema.sql</li>
 * <li>A postgreSQL db named 'test_working" is created and initialized
 * with the same schema</li></ol>
 * For this test, those databases were created without owners or roles
 */
public class DocumizerTest {
	private IPostgresConnectionFactory library;
	private IPostgresConnectionFactory working;
    //private IPostgresConnection li = null;

	private static final String
		LIB_DB_NAME 	= "test_library",
		WORK_DB_NAME	= "test_working",
		SCHEMA_NAME		= "tqos_asr";
	/**
	 * 
	 */
	public DocumizerTest() {
		library = new PostgresConnectionFactory(LIB_DB_NAME, SCHEMA_NAME);
		working = new PostgresConnectionFactory(WORK_DB_NAME, SCHEMA_NAME);
		System.out.println("A "+library);
		System.out.println("B "+working);
	}

	void runTest() {
		JSONObject firstGram = new JSONObject();
	}
	
	
	IResult post(String id, JSONObject data, IPostgresConnection conn) {
		IResult result = new ResultPojo();
		
		return result;
	}
	
	IResult get(String id, IPostgresConnection conn) {
		IResult result = new ResultPojo();
		
		return result;
	}
}
