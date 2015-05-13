package org.cfb.ungentry.census.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import json.converter.csv.CSVReader;

import com.google.gson.Gson;

public class GeographyFileReader extends CSVReader {

	BufferedReader _reader;
	String _filename;
	public TreeMap<String,String[]> _index;
	
	public GeographyFileReader(String iFilename){
		super(iFilename);
		_filename = iFilename;
		_index = new  TreeMap<String,String[]>();
	}
	
	public void read(){
		
		try {
			_reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(_filename))));
			
			int count = 0;
			String line;
			while ((line = _reader.readLine()) != null) {
				//System.out.println(line);
				
				DataInputStream aStream = new DataInputStream(new ByteArrayInputStream(line.getBytes()));
				
				LinkedHashMap<String,String> aGL = new LinkedHashMap<String,String>();
				
				byte[] buffer = new byte[1024];
				
				String[]  aHeaderList = { "FILEID", "STUSAB", "SUMLEVEL", "COMPONENT",  "LOGRECNO",  "US",  "REGION",  "DIVISION",  "STATECE",  "STATE", "COUNTY",  "COUSUB",
														 "PLACE", "TRACT", "BLKGRP","CONCIT","AIANHH","AIANHHFP", "AIHHTLI", "AITSCE", "AITS", "ANRC", "CBSA","CSA", "METDIV", "MACC", "MEMI",
														 "NECTA", "CNECTA","NECTADIV","UA","BLANK","CDCURR","SLDU", "SLDL", "BLANK1","BLANK2", "BLANK3", "SUBMCD","SDELM","SDSEC","SDUNI",
														 "UR", "PCI", "BLANK4","BLANK5","PUMA5","BLANK6", 	"GEOID", "NAME", "BTTR", "BTBG", "BLANK7"};
				
				_header = new LinkedList<String>();
				_header.addAll(Arrays.asList(aHeaderList));
				
				int[] aElemSize = { 6, 2, 3, 2, 7, 1, 1, 1,2,2,3 ,5 ,5,6,1 ,5,4,5,1, 3, 5,5,5,3,5,1,1, 5,3,5,5,5,2,3,3,6,3,5,5,5,5,5,1,1,6,5,5,5,40,200,6,1, 50 };
				
				Vector<String> aTuple = new Vector<String>();
				for (int i:aElemSize){
					aStream.read(buffer,0,i);
					aTuple.add( new String(buffer, 0, i).trim());
				}

				String[] data = aTuple.toArray(new String[aTuple.size()]);
				_data.put(count, data);
				_index.put(data[4], data);
				
				count++;
				
			}
			
			_reader.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	public String[] getRecordFromLogRecNum(String iNum){
			return _index.get(iNum);
	}
	
	public String toJson(){
		Gson aGson = new Gson();
		String aJson = aGson.toJson(_data);
		return aJson;
	}

}
