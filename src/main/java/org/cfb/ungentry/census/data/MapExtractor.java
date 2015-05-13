package org.cfb.ungentry.census.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import json.converter.csv.CSVReader;
import json.converter.dbf.DBFExtractor;
import json.converter.shp.ShpFileReader;
import json.geojson.FeatureCollection;
import json.graphic.Display;
import json.topojson.algorithm.ArcMap;
import json.topojson.api.TopojsonApi;
import json.topojson.topology.Topology;

public class MapExtractor {

	public static void extractMapData(String iMapFile){

		Unzip.unzipFile(iMapFile,"out");

		String aDir = (new File(iMapFile)).getParentFile().getAbsolutePath();

		String aExtractedCSVFile = aDir+File.separator+"dbf.csv";
		String[] DBFFile = Unzip.retrieveFilesFromPattern(iMapFile, ".*\\.dbf");
		for (String aFile:DBFFile) { DBFExtractor.extractDBFDataToCSV(aFile, aExtractedCSVFile); }

		String[] SHPFile = Unzip.retrieveFilesFromPattern(iMapFile, ".*\\.shp");
		ShpFileReader aReader = new ShpFileReader(SHPFile[0],"nad83:2001");
		try {
			aReader.read();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CSVReader aReader3 = new CSVReader(aExtractedCSVFile);
		aReader3.read();
		
		buildTilesDataMap(aDir,  aReader);

	}

	public static void  buildTilesDataMap(String iLocalFolder, ShpFileReader iReader){

		FeatureCollection aCollection = iReader.getGroupRecord();

			

	}

	public static void recordFile(String iFilename, String iJson){

		FileOutputStream out;
		try {

			out = new FileOutputStream(iFilename/*"./data/shp/out.json"*/);
			out.write(iJson.getBytes());
			out.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
