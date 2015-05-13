package org.cfb.ungentry.census.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import json.converter.csv.CSVReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

public class SequenceAndTableNumber {

	// This class create an internal structure to represent Sequence_Number_and_Table_Number_Lookup.csv file
	
	public static class Element implements Comparable<Element> {
		
		public String name;
		public String type;
		public String title;
		public int sequence;
		public int position;
		HashMap<String,Integer> eval = new HashMap<String,Integer>(); 
		
		public int compareTo(Element o) {
			return type.compareTo(o.type);
		}
		
	}
	
	String _filename;
	LinkedTreeMap<String,Vector<Element>> _tableMap;
	TreeMap<Integer, TreeMap<Integer, Element> > _sequenceMap;
	LinkedHashMap<String,Element> _tableIDMap;
	
	public SequenceAndTableNumber(String iFileName){
	
		_filename = iFileName;
		_tableMap = new LinkedTreeMap<String,Vector<Element>>();
		_sequenceMap = new TreeMap<Integer, TreeMap<Integer, Element> >();
		_tableIDMap = new  LinkedHashMap<String,Element>();
		
	}
	
	public void read(){
		
		CSVReader aReader = new CSVReader(_filename);
		aReader.read();
		
		int aTableStartPosition = 0;
		String aCurrentTableName = null;
		Vector<Element> aCurrentTable = null;
		
		int cLineNumber = 0;
		
		int aTableIDIndex = aReader._header.indexOf("Table ID");
		int aSeqNumberIndex =  aReader._header.indexOf("Sequence Number");
		if (aSeqNumberIndex==-1) {
			aSeqNumberIndex = aReader._header.indexOf("seq");
		}
		int aStartPositionIndex = aReader._header.indexOf("Start Position");
		if (aStartPositionIndex==-1) {
			aStartPositionIndex = aReader._header.indexOf("position");
		}
		int aLineNumberIndex = aReader._header.indexOf("Line Number");
		if (aLineNumberIndex==-1) {
			aLineNumberIndex = aReader._header.indexOf("Line Number Decimal M Lines");
		}
		
		int aTableTitleIndex =  aReader._header.indexOf("Table Title");
		if (aTableTitleIndex==-1){
			aTableTitleIndex =  aReader._header.indexOf("Long Table Title");
		}
		
		TreeMap<Integer, String[]> aData = aReader._data;
		for (String[] aLine: aData.values()) {
		
			//System.out.println(ArrayUtils.toString(aLine));
			
			String aTableID = aLine[aTableIDIndex];
			String aSequenceNumber = aLine[aSeqNumberIndex];
			String aStartPosition = aLine[aStartPositionIndex];
			String aLineNumber = aLine[aLineNumberIndex];
			String aTableTitle = aLine[aTableTitleIndex];
			
			if (aStartPosition.length()>0 && !(aStartPosition.equals(".") || (aStartPosition.equals(" ")))) {
				
				if (aCurrentTableName!=null){ // if preceding table has been defined
					_tableMap.put(aCurrentTableName, aCurrentTable);
				}
				
				//System.out.println("Table:"+aTableTitle);
				aCurrentTableName = aTableTitle;
				aCurrentTable = new Vector<Element>();
				aTableStartPosition = new Integer(aStartPosition);
				
				cLineNumber = 0;
				
			} else {
				
				if (aLineNumber.length()>0) {
					
						try {
														
							Element aElem = new Element();
							aElem.name = aTableID;
							aElem.position = aTableStartPosition +cLineNumber; // sometimes position = 0.5 don't know why
							aElem.title = aTableTitle;
							aElem.type = "data";
							aElem.sequence = new Integer(aSequenceNumber);
							aCurrentTable.add(aElem);
							
							addToSequenceMap(aElem);
							
							_tableIDMap.put(aElem.name, aElem);
							
							double d = new Double(aLineNumber);
							double x = d - (long) d;
							if (x==0) cLineNumber++; // x.5 values for aLineNumber
							
						} catch (NumberFormatException e){
							
						}
						
						
				} else { // universe
					
						Element aElem = new Element();
						aElem.position = -1;
						aElem.title = aTableTitle;
						aElem.type = "universe";
						aCurrentTable.add(aElem);
					
				}
				
			}
			
		}
		
	}
	
	public void addToSequenceMap(Element iElement){
		
		TreeMap<Integer, Element> aMap = _sequenceMap.get(iElement.sequence);
		if (aMap==null){
			aMap = new TreeMap<Integer, Element>();
			_sequenceMap.put(iElement.sequence, aMap);
		}
		aMap.put(iElement.position, iElement);
		
	}
	
	public void registerEval(HashMap<String,Integer> iEval, String iType ){
		Integer count = iEval.get(iType);
		iEval.put(iType, (count==null?1:count+1));	
	}
	
	
	public static String getName(Element iElement){
		return iElement.name+"_"+iElement.sequence+"_"+iElement.position;
	}
	
	public static LinkedHashMap<String,String> createLine(Vector<Element> iFieldsList,  String iLOGRECLOC){
		LinkedHashMap<String,String> result = new LinkedHashMap<String,String>();
		result.put("LOGRECNO_ACS", iLOGRECLOC);
		for (Element Elem:iFieldsList){
			result.put(getName(Elem), "");
		}
		return result;
	}
	
	public static  void addValue(TreeMap<String, LinkedHashMap<String,String>> iMap, 
													 Vector<Element> iFieldsList, 
													 String iLOGRECLOC, 
													 Element iElement, 
													 String iValue){
		
		LinkedHashMap<String,String> aLine = iMap.get(iLOGRECLOC);
		if (aLine==null){
			aLine = createLine(iFieldsList, iLOGRECLOC);
			iMap.put(iLOGRECLOC, aLine);
		}
		aLine.put(getName(iElement), iValue);
	}
	
	public void extractCSVwithFilter(String[] iFileList, String[] iFieldsList, String iOutCSVFilename){
		
			Vector<Element> aElements = new Vector<Element>();
			for (String aFilter:iFieldsList) {
				
				String[] spl = aFilter.split("_");
				
				if (spl.length==3) {
					
					Element aElement = new Element();
					aElement.name = spl[0];
					aElement.sequence = new Integer(spl[1]);
					aElement.position = new Integer(spl[2]);
					aElements.add(aElement);
				
				}
				
			}
		
			extractCSV(iFileList, aElements, iOutCSVFilename);
	}
	
	public String buildCSV(TreeMap<String, LinkedHashMap<String,String>> iData){
		
		StringBuffer aBuffer = new StringBuffer();
		// Builds the header
		LinkedHashMap<String,String> aHeaderLine = iData.firstEntry().getValue();
		for(Iterator<String> i = aHeaderLine.keySet().iterator(); i.hasNext(); ) {
			aBuffer.append(i.next());
			if (i.hasNext()) aBuffer.append(",");
		}
		aBuffer.append("\n");
			
		for (LinkedHashMap<String,String> aLine:iData.values()){
			
			for (Iterator<String> i = aLine.keySet().iterator(); i.hasNext(); ){
				aBuffer.append(aLine.get(i.next()));
				if (i.hasNext()) aBuffer.append(",");
			}
			aBuffer.append("\n");
			
		}
		
		return aBuffer.toString();
	}
	
	public void extractCSV(String[] iFileList, Vector<Element> iFieldsList, String iOutCSVFilename){
		
		// First index by LOGRECNO then we will move it in CSV type;
		TreeMap<String, LinkedHashMap<String,String>> aSaved = new TreeMap<String, LinkedHashMap<String,String>>();
		
		for (String aFile:iFileList){
			
			CSVReader aReader = new CSVReader(aFile);
			aReader.readWithoutHeader();
			
			for (String[] aLine:aReader._data.values()){
				
				int readenSequence = new Integer(aLine[ACSReader.SEQUENCE]);
				for (Element Elem:iFieldsList){
					
					if (readenSequence==Elem.sequence) { // Same sequence
						String aLOGRECNO = aLine[ACSReader.LOGRECNO]; // There we know line LOGRECNO 
						String aValue = aLine[Elem.position-1]; // the field value we want to extract
						addValue(aSaved, iFieldsList, aLOGRECNO, Elem, aValue);
					}		
							
				}
				
			}	
				
		}		

		String aCSVData = buildCSV(aSaved);
		try {
			IOUtils.write(aCSVData, new FileOutputStream(new File(iOutCSVFilename)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void evaluate(String[] iFileList, GeographyFileReader iReader){
		
		for (String aFile:iFileList){
			
			CSVReader aReader = new CSVReader(aFile);
			aReader.readWithoutHeader();
			
			for (String[] aLine:aReader._data.values()){
				   //System.out.println(aFile+" "+aStrs[0]+" "+aStrs[ACSReader.SEQUENCE]+" "+aStrs[ACSReader.LOGRECNO]);
				  
				  //System.out.println("Sequence:"+aLine[ACSReader.SEQUENCE]);
				  TreeMap<Integer,Element> aElementSequence = _sequenceMap.get(new Integer(aLine[ACSReader.SEQUENCE]));
				  
				  //System.out.println(iReader._header);
				  int indexSumlevel =  iReader._header.indexOf("SUMLEVEL");
		  		  int indexComponent =  iReader._header.indexOf("COMPONENT");
				  
				  for (Integer aPosition:aElementSequence.keySet()){
					  	if (aLine.length>=aPosition) {
					  		String aValue = aLine[aPosition-1];
					  		if (aValue.trim().length()>0) {
					  			String[] _geoRecord = iReader.getRecordFromLogRecNum(aLine[ACSReader.LOGRECNO]);  			
					  			registerEval(aElementSequence.get(aPosition).eval,  _geoRecord[indexSumlevel]+"_"+_geoRecord[indexComponent]);
					  		}
					  	} else {
					  		System.out.println("Not available:"+aLine[ACSReader.SEQUENCE]+" "+aPosition);
					  	}
				  }
				
			}
			
		}
		
		Vector<String> aThemeToRemove = new Vector<String>();
		Vector<Element> aToRemove = new Vector<Element>();
		for (Map.Entry<String,Vector<Element>> aEntry:_tableMap.entrySet()) {
			
			Vector<Element> aElems = aEntry.getValue();
			
			for (Element aElem:aElems){
				if (aElem.eval.size()==0) {
					aToRemove.add(aElem);
				}
			}
			aElems.removeAll(aToRemove);
			aToRemove.clear();
			
			if (aElems.size()==0){
				aThemeToRemove.add(aEntry.getKey());
			}
		}
		
		for (String aKey:aThemeToRemove){
			_tableMap.remove(aKey);
		}
		
	}
	
	public String toJson(){
		Gson aGson = new Gson();
		String aJson = aGson.toJson(_tableMap);
		return aJson;
	}
	
}
