package org.cfb.ungentry.census.data;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import json.algorithm.Jenks;
import json.converter.csv.builder.ColumnBuilder;
import json.converter.csv.filter.Filter;
import json.converter.csv.filter.Filter.FilterStep;
import json.converter.csv.CSVReader;
import json.converter.csv.merger.Merger;
import json.converter.csv.merger.Merger.MergeStep;
import json.geojson.Feature;
import json.geojson.FeatureCollection;
import json.geojson.FeatureCollection.RecursiveTileProcessor;
import json.geojson.FeatureCollection.TileElement;
import json.graphic.Colorifier;
import json.graphic.Display;
import json.topojson.algorithm.ArcMap;
import json.topojson.api.TopojsonApi;
import json.topojson.topology.Topology;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.net.util.Base64;
import org.cfb.ungentry.census.data.SequenceAndTableNumber.Element;
import org.cfb.ungentry.census.data.TilesEngine.TilesEngineModule;

import  json.graphic.JenksColorifierGeojson;

import org.cfb.ungentry.census.toolbox.LevelTree;
import org.cfb.ungentry.census.web.jetty.EmbeddedServer;
import org.cfb.ungentry.network.DownloadFTP;
import org.cfb.ungentry.network.DownloadFTPTask.Status;

import com.google.gson.internal.LinkedTreeMap;

public class ACSLoader {

	public final static String SEQUENCE_NUMBER_FILE = "Sequence_Number_and_Table_Number_Lookup";

	public static class ACSLoaderThread implements Runnable {

		TreeMap<String,String> _list = null;

		public ACSLoaderThread(TreeMap<String,String> iFilesToDownload){
			_list =  iFilesToDownload;
		}

		public void run() {

			DownloadFTP aFTP = new DownloadFTP();
			aFTP.connect();

			for (Entry<String,String> aEntry:_list.entrySet()) {
				aFTP.downloadFile(aEntry.getKey(), aEntry.getValue());
			}

			aFTP.disconnect();

		}

	}

	public static TreeMap<String,String> retrieveFiles(String iACSDataPath, String iState){
		TreeMap<String,String> aList =  buildFilesList(iACSDataPath,iState);
		new Thread(new ACSLoaderThread(aList)).start();
		return aList;
	}

	public static String  retrieveLocalSequenceNumberFile(String iACSDataPath){
		String aLocalWorkDir = EmbeddedServer.workDirectory();
		return 	aLocalWorkDir+File.separator+iACSDataPath+File.separator+SEQUENCE_NUMBER_FILE+".csv";
	}

	public static String retrieveLocalACSDataFile(String iACSDataPath, String iState){
		String aLocalWorkDir = EmbeddedServer.workDirectory();	
		// Building local path
		String aStateCode = IndicesBuilder.listStateCodesByName().get(iState);
		return aLocalWorkDir+File.separator+iACSDataPath+File.separator+aStateCode;
	}

	public static TreeMap<String,String> buildFilesList(String iACSDataPath, String iState){

		TreeMap<String,String> aList = new TreeMap<String,String>();

		aList.put("/"+iACSDataPath+"/summaryfile/"+SEQUENCE_NUMBER_FILE+".txt", 
				retrieveLocalSequenceNumberFile(iACSDataPath));

		String aPathLocalState = retrieveLocalACSDataFile(iACSDataPath, iState);
		File aDirectory = new File(aPathLocalState);
		aDirectory.mkdirs();

		if (isYearDependant(iACSDataPath)) {

			// Build path to state data
			String aPathState = "/"+iACSDataPath+"/summaryfile/"+ buildChainMultiYear(iACSDataPath) + "/" + iState + "_Tracts_Block_Groups_Only.zip";
			aList.put(aPathState, aPathLocalState+File.separator+"ACS_data.zip");

		} else {


		}

		/* Removing MAP download
		// Here want also to download SHP files from tiger database
		String aRemoteMap = "/geo/tiger/TIGER"+ aYear +"/TRACT/tl_" + aYear + "_" + String.format("%2d", new Integer(IndicesBuilder.listStateNumberByName().get(iState))) + "_tract.zip";

		String aLocalMap = aPathLocalState+File.separator+"map.zip";

		aList.put(aRemoteMap, aLocalMap);
		 */

		return aList;
	}



	public static final String ACSPATH_MATCHER = "acs([0-9]+)(_([0-9]*)yr)?";

	public static boolean  isYearDependant(String iPath){

		Pattern aPat = Pattern.compile(ACSPATH_MATCHER);
		Matcher aMatch = aPat.matcher(iPath);

		if (aMatch.matches() && (aMatch.group(2)!=null)) {
			return true;
		}
		return false;

	}

	public final static String PATH_MULTI_YEAR = "%4d-%4d_ACSSF_By_State_All_Tables";

	public static String buildChainMultiYear(String iPath){

		Pattern aPat = Pattern.compile(ACSPATH_MATCHER);
		Matcher aMatch = aPat.matcher(iPath);

		if (aMatch.matches() && (aMatch.group(1)!=null)) {

			int secondYear = new Integer(aMatch.group(1));
			int firstYear = secondYear-new Integer(aMatch.group(3))+1;

			return String.format(PATH_MULTI_YEAR, firstYear, secondYear);

		}

		return null;
	}

	public static String getYear(String iPath){

		Pattern aPat = Pattern.compile(ACSPATH_MATCHER);
		Matcher aMatch = aPat.matcher(iPath);

		if (aMatch.matches() && (aMatch.group(1)!=null)) {
			return aMatch.group(1);
		}

		return null;
	}

	public static LinkedTreeMap<String,Vector<Element>> retrieveLocalSequenceTableNumberStruct(String iACSDataPath, String iState){

		String aFileName = retrieveLocalSequenceNumberFile(iACSDataPath);
		SequenceAndTableNumber aSeq = new SequenceAndTableNumber(aFileName);
		aSeq.read();

		String aACSFileName = retrieveLocalACSDataFile(iACSDataPath, iState);

		String zipfile = aACSFileName+File.separator+"ACS_data.zip";

		ACSReader aReader = new ACSReader(zipfile);
		aReader.read();//unzip files

		GeographyFileReader aGReader = new GeographyFileReader(ACSReader.getGeoFile(zipfile));
		aGReader.read();

		aSeq.evaluate(ACSReader.getFileList(zipfile), aGReader);

		return aSeq._tableMap;
	}

	public static void quickDisplay(FeatureCollection iFeat, String iField){

		int x = 1024;
		Display aDisplay = new Display(x, (int) (x/iFeat._bnd.getRatioXY()));
		aDisplay.start();
		aDisplay.clear();

		aDisplay.setBound(iFeat._bnd);

		iFeat.draw(aDisplay, Color.white);
		iFeat.fill(aDisplay, new JenksColorifierGeojson(iFeat,iField));

		aDisplay.render();

	}

	public static class TopologyProcessor extends RecursiveTileProcessor implements TilesEngineModule {

		ArcMap _map;
		String _saving_path;
		Display _display; 
		Colorifier _colorifier;

		LevelTree<TileElement> _levelTree;

		TopologyProcessor(ArcMap iMap, String iSavingPath,Colorifier iColorifier){

			_map = iMap;
			_saving_path = iSavingPath;

			int x = 256;
			_display = new Display(x, x);
			_display.hide();
			_display.start();
			_colorifier = iColorifier; /* new JenksColorifierGeojson(aFeat ,iField); */

			_levelTree = new LevelTree<TileElement>();

		}

		@Override
		public void process(TileElement iTile) {

			System.out.println("Have to proceed for:"+iTile.zoom+ " " +iTile.x +" "+iTile.y );

			try {
				_levelTree.put(iTile.zoom+"/" +iTile.x +"/"+iTile.y , iTile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
				aTopo.simplify(1000);
				aTopo.quantize(4);

				String aJson = TopojsonApi.getJson(aTopo, false);
				try {
					IOUtils.write(aJson,new FileOutputStream(new File(aSavingName+ aTopo._meta_properties.get("y") + ".json")));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 */

		}

		@Override
		public byte[] get(String iZoom, String iX, String iY) throws Exception  {

			TileElement aTile = _levelTree.get(iZoom+"/"+iX+"/"+iY);
			if (aTile!=null) {
				FeatureCollection aReducedFeat = aTile.collection;
				System.out.println("Nb of shapes"+ aReducedFeat._shapes.size() + " " + Arrays.toString(aReducedFeat._shapes.keySet().toArray()) );
				Topology aTopo =  TopojsonApi.featureToTopology(aReducedFeat, _map, "MAP");

				synchronized (_display) {

					// Image must be done before quantization
					_display.clear();

					_display.setBound(aTopo._bnd);

					aTopo.fill(_display, _colorifier); // Provide in init the global feature

					_display.bufferGraphics.setColor(new Color(255,0,0));
					_display.bufferGraphics.drawLine(0,0, 256, 0 );
					_display.bufferGraphics.drawLine(0,0, 0, 256 );

					_display.render();

					return _display.getImageData();

				}

			}	
			throw new Exception("Unable to find tile "+iZoom+" "+iX+" "+iY);
		}

		@Override
		public Topology getTopology(String iZoom, String iX, String iY)
				throws Exception {

			TileElement aTile = _levelTree.get(iZoom+"/"+iX+"/"+iY);
			if (aTile!=null) {
				FeatureCollection aReducedFeat = aTile.collection;
				System.out.println("Nb of shapes"+ aReducedFeat._shapes.size() + " " + Arrays.toString(aReducedFeat._shapes.keySet().toArray()) );
				Topology aTopo =  TopojsonApi.featureToTopology(aReducedFeat, _map, "MAP");

				aTopo.simplify(50);
				aTopo.quantize(4);

				return aTopo;

			}	
			throw new Exception("Unable to find tile "+iZoom+" "+iX+" "+iY);
		}

	}

	public static void buildTopoJson(String iSavingPath, String iGeoCSVFile, String iState, String iField){

		String aStateCode = String.format("%02d0", new Integer(IndicesBuilder.listStateNumberByName().get(iState) ) );
		System.out.println("Proceed for state:"+aStateCode);

		String[][] aFilter = {{"STATEA", aStateCode }, {"SUMLEVEL", "140" } };  // SUM LEVEL == Census Tract = 140

		Merger aMerger = new Merger();
		aMerger.addStep(new MergeStep("GISJOIN","%s", iGeoCSVFile, "NHGISCODE","%s", true));

		System.out.println("Proceed for field:"+iField);

		FeatureCollection aFeat;
		try {

			aFeat = TopojsonApi.shpToGeojsonFeatureCollection(
					EmbeddedServer.DATA_DIRECTORY+File.separator+"map"+File.separator+"US_tract_2010.shp", "esri:102003", 
					aFilter,  aMerger);

			ArcMap aMap = TopojsonApi.joinCollection(aFeat);

			aFeat._bnd = aFeat.getMergedBound();

			quickDisplay(aFeat, iField);

			TopologyProcessor aProcessor = new TopologyProcessor(aMap, iSavingPath,  new JenksColorifierGeojson(aFeat ,iField));

			FeatureCollection.processTiles(aFeat, 1, 15, aProcessor);

			TilesEngine.addModule(IndicesBuilder.listStateCodesByName().get(iState), aProcessor);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String buildMap(String iACSDataPath, String iState, String iList){


		String aFileName = retrieveLocalSequenceNumberFile(iACSDataPath);
		SequenceAndTableNumber aSeq = new SequenceAndTableNumber(aFileName);
		aSeq.read();

		String aACSFileName = retrieveLocalACSDataFile(iACSDataPath, iState);

		String zipfile = aACSFileName+File.separator+"ACS_data.zip";

		ACSReader aReader = new ACSReader(zipfile);
		aReader.read();//unzip files

		String aList = new String(Base64.decodeBase64(iList));

		String[] aFilter = aList.split(":");

		HashSet<Integer> aSet = new HashSet<Integer>();
		for (String aField:aFilter){
			String[] aFieldRange = aField.split("_");
			aSet.add(new Integer(aFieldRange[1]).intValue());
		}
		Integer[] aSequences = aSet.toArray(new Integer[aSet.size()]);

		String extractCSVFile = aACSFileName+File.separator+"extractACSData.csv";
		aSeq.extractCSVwithFilter(ACSReader.getFilteredFileList(zipfile, aSequences), aFilter, extractCSVFile);

		// Extract geo files
		GeographyFileReader aGReader = new GeographyFileReader(ACSReader.getGeoFile(zipfile));
		aGReader.read();

		int indexState = aGReader._header.indexOf("STATE");
		int indexCounty = aGReader._header.indexOf("COUNTY");
		int indexTract =  aGReader._header.indexOf("TRACT");

		ColumnBuilder aBuilder = new ColumnBuilder("NHGIS");
		aBuilder.addToken(new ColumnBuilder.Token("G%02d0", ColumnBuilder.Token.TYPE.INTEGER  , indexState));
		aBuilder.addToken(new ColumnBuilder.Token("%03d", ColumnBuilder.Token.TYPE.INTEGER  ,indexCounty));
		aBuilder.addToken(new ColumnBuilder.Token("%07d", ColumnBuilder.Token.TYPE.INTEGER  , indexTract));

		aBuilder.build(aGReader);

		String geoNHGISCSVFile = aACSFileName+File.separator+"GEONHGISData.csv";
		aGReader.write(geoNHGISCSVFile);

		// Merging with the extract
		Merger aMerger = new Merger();
		aMerger.addStep(new MergeStep("LOGRECNO","%s",extractCSVFile, "LOGRECNO_ACS","%s", true)); // true == removing columns that doesn't contains this reference
		aMerger.addStep(new MergeStep("NHGIS","%s",
				EmbeddedServer.DATA_DIRECTORY+File.separator+"map"+File.separator+"nhgis0002_ts_tract.csv", "NHGISCODE","%s", true));

		aMerger.process(aGReader);

		// Filtering only census tract areas
		Filter aFiltering = new Filter();
		aFiltering.addStep(new FilterStep("SUMLEVEL","140"));

		aFiltering.process(aGReader);

		String geoAcsCSVFile = aACSFileName+File.separator+"GEOACSData.csv";
		aGReader.write(geoAcsCSVFile);

		// At that point we have extracted data from ACS data
		// We need to continue to build the map
		buildTopoJson(aACSFileName, geoAcsCSVFile, iState, aFilter[0]);

		return "OK";
	}

}
