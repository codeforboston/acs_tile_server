package org.cfb.ungentry.census.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Vector;

import json.converter.csv.CSVReader;

import org.cfb.ungentry.census.web.jetty.EmbeddedServer;
import org.cfb.ungentry.network.DownloadFTP;

public class IndicesBuilder {

	IndicesBuilder(){
		
	}
	
	public static String[] listACSFolders(){
		
		String[] aDirs = null;
		
		DownloadFTP aFTP = new DownloadFTP();
		aFTP.connect();
		aDirs = aFTP.lsDirectoryWithFilter("acs[0-9]+(_[0-9]*yr)?");
		aFTP.disconnect();
		
		return aDirs;
	
	}
	
	public static String[] listStatesForACSData(String iACSFolderName){
		
		String[] aStates = null;
		
		File f = new File(EmbeddedServer.workDirectory()+File.separator+iACSFolderName); // current directory

		if (!f.exists()) {
			f.mkdirs();
		}
		
		Vector<String> aListStates = new Vector<String>();
		
	    File[] files = f.listFiles();
	    for (File aFile: files) {
	    	if (aFile.isDirectory()) {
	    		aListStates.add(aFile.getName());
	    	}
	    }
	    
	    aStates = aListStates.toArray(new String[aListStates.size()]);
	    
		return aStates;
	
	}
	
	public static LinkedHashMap<String, String> listStateNumberByName(){
		
		InputStream aStream = Thread.currentThread().getClass().getResourceAsStream("/servlet/WEB-INF/classes/state.csv");
		CSVReader _stateReader = new CSVReader(aStream);
		_stateReader.read();
		
		LinkedHashMap<String, String> aList = new LinkedHashMap<String, String>();
		
		TreeMap<Integer,String[]> aData =  _stateReader._data;
		int indexName = _stateReader._header.indexOf("name_long");
		int indexState = _stateReader._header.indexOf("state_code");
		for (String[] aLine:aData.values()) {
			  aList.put(aLine[indexName],aLine[indexState]);
		}
		
		try {
			aStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aList;
		
	}
	
	public static LinkedHashMap<String, String> listStateCodesByName(){
		
		InputStream aStream = Thread.currentThread().getClass().getResourceAsStream("/servlet/WEB-INF/classes/state.csv");
		CSVReader _stateReader = new CSVReader(aStream);
		_stateReader.read();
		
		LinkedHashMap<String, String> aList = new LinkedHashMap<String, String>();
		
		TreeMap<Integer,String[]> aData =  _stateReader._data;
		int indexName = _stateReader._header.indexOf("name_long");
		int indexNameShort = _stateReader._header.indexOf("name_short");
		for (String[] aLine:aData.values()) {
			  aList.put(aLine[indexName], aLine[indexNameShort]);
		}
		
		try {
			aStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aList;
		
	}
	
	
}
