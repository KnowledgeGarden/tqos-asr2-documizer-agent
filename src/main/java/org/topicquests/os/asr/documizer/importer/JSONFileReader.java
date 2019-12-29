/*
 * Copyright 2018 TopicQuests Foundation
 *  This source code is available under the terms of the Affero General Public License v3.
 *  Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
 */
package org.topicquests.os.asr.documizer.importer;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;

import org.topicquests.os.asr.documizer.DocumizerEnvironment;
import org.topicquests.os.asr.documizer.api.IListener;
import org.topicquests.support.util.TextFileHandler;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class JSONFileReader {
	private DocumizerEnvironment environment;
	private JSONDocumentReader jsonDocReader;
	private TextFileHandler handler;
	private IListener host;
	private Set<String>filesRead;
	private String [] fileNames;
	private int cursor = 0;
	private Worker thread;
	private boolean isRunning = true;
	//a simple text file listing all files already read
	private final String FILES_READ = "FilesRead.txt";
	private final String FILES_TO_READ;
	private final String BASE_DATA_PATH;
	

	/**
	 * 
	 */
	public JSONFileReader(DocumizerEnvironment env, JSONDocumentReader rdr) {
		environment = env;
		FILES_TO_READ = environment.getStringProperty("JSONFileBase");
		jsonDocReader = rdr;
		handler = new TextFileHandler();
		filesRead = new HashSet<String>();
		BASE_DATA_PATH = environment.getStringProperty("BaseDataPath");

	}
	
	/**
	 * REQUIRED since this is threaded
	 * @param h
	 */
	public void setListener(IListener h) {
		host = h;
	}
	
	public void startReading() {
		initializeFilesRead();
		String path = FILES_TO_READ;
		File dir = new File(path);
		environment.logDebug("JSONFileReader.startReading-1 "+dir);
		fileNames = dir.list();
		environment.logDebug("JSONFileReader.startReading-2 "+fileNames);
		cursor = 0;
		isRunning = true;
		thread = new Worker();
		thread.start();	}
	
	void processDirectory(File dir) {
		fileNames = dir.list();
		String path = dir.getAbsolutePath();
		
		
	}
	public void stopReading() {
		isRunning = false;
	}
	
	/**
	 * Can return <code>null</code>
	 * @return
	 */
	File getNextFile() {
		File f = null;
		environment.logDebug("JSONFileReader.getNextFile "+cursor+" "+fileNames);
		while (cursor < fileNames.length && !filesRead.contains(fileNames[cursor++])) {
			String name = fileNames[--cursor];
			System.out.println("JSONFileReader.getNexFile "+name);
			f = new File(FILES_TO_READ+name);
			cursor++;
		}
		return f;
	}
	
	class Worker extends Thread {
		
		public void run() {
			while(isRunning) {
				File f = getNextFile();
				if (f == null)
					break; // done
				processFile(f);
			}
			saveFilesRead();
			host.did();
		}
		
		/**
		 * Read lines from <code>f</code> and process them
		 * @param f
		 */
		void processFile(File f) {
			System.out.println("ProcessingA "+f);
			try {
				InputStream is = new GZIPInputStream(new FileInputStream(f));
				BufferedReader rdr = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				System.out.println("ProcessingAA "+rdr);
				String line;
				while ((line = rdr.readLine()) != null) {
					processLine(line.trim());
				}
				filesRead.add(f.getName());
			} catch (Exception e) {
				environment.logError(e.getMessage(), e);
			}
		}
		
		/**
		 * Convert <code>json</code> to a {@link JSONObject} and process it
		 * @param json
		 * @throws Exception
		 */
		void processLine(String json) throws Exception {
			System.out.println("ProcessingB "+json);
			JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
			JSONObject jo = (JSONObject)p.parse(json);
			jsonDocReader.processJSON(jo);
		}
		
	}
	/**
	 * <p> We read from a collection of files until either:<br/>
	 * a) we run out of files to read, or<br/>
	 * b) we are told to stop reading.</p>
	 * <p> We use filesRead to remember filenames successfully read</p>
	 */
	void initializeFilesRead() {
		String path = BASE_DATA_PATH + FILES_READ;
		File f = new File(path);
		environment.logDebug("JSONFileReader.initializeFilesRead "+f);
		if (f.exists()) {
			String line = handler.readFirstLine(f);
			while (line != null) {
				filesRead.add(line.trim());
				line = handler.readNextLine();
			}
		}
	}
	
	void saveFilesRead() {
		String path = BASE_DATA_PATH + FILES_READ;
		File f = new File(path);
		Iterator<String>itr = filesRead.iterator();
		StringBuilder buf = new StringBuilder();
		while (itr.hasNext()) {
			buf.append(itr.next()+"\n");
		}
		handler.writeFile(f, buf.toString());
	}
}
