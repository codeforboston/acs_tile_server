package org.cfb.ungentry.census.data;

import java.util.HashMap;

import json.geojson.FeatureCollection.TileElement;
import json.topojson.topology.Topology;

import org.cfb.ungentry.census.toolbox.LevelTree;

public class TilesEngine {

	public static class Item {
		String categorie;
		String title;
	}
	
	public static interface  TilesEngineModule {
		
			/* Get an image representation of the tile */
			byte[] get(String iField, String iZoom, String iX, String iY) throws Exception;
			
			/* Get a topology of the defined tile */
			Topology getTopology(String iZoom, String iX, String iY) throws Exception;
			
			/* Retrieve properties defined for this module */
			HashMap<String,Item> getProperties();
			
			/* Get for a module classes definition for a particular field */ 
			double[] getClasses(String iField);
	
	}
	
	public static HashMap<String, TilesEngineModule> _tree ;
	
	static {
		
		_tree =  new HashMap<String, TilesEngineModule>();
		
	}
	
	public static void addModule(String iName, TilesEngineModule iModule){
		_tree.put(iName, iModule);
	}
	
	// Generate a buffer that contains data generated for the tile 
	public static byte[] getTile(String iName, String iField, String iZoom, String iX, String iY) throws Exception{
		TilesEngineModule aModule = _tree.get(iName);
		if (aModule!=null) {
			return aModule.get(iField,iZoom, iX, iY);
		}
		throw new Exception("Unable to find module name:"+iName); 
	}
	
	// Generate a buffer that contains data generated for the tile 
	public static Topology getToplogyTile(String iName, String iZoom, String iX, String iY) throws Exception{
		TilesEngineModule aModule = _tree.get(iName);
		if (aModule!=null) {
			return aModule.getTopology(iZoom, iX, iY);
		}
		throw new Exception("Unable to find module name:"+iName); 
	}
	
	// Generate a buffer that contains data generated for the tile 
	public static double[] getClasses(String iName, String iField) throws Exception{
		TilesEngineModule aModule = _tree.get(iName);
		if (aModule!=null) {
			return aModule.getClasses(iField);
		}
		throw new Exception("Unable to find module name:"+iName); 
	}

	// Generate a buffer that contains data generated for the tile 
	public static HashMap<String,Item> getProperties(String iName) throws Exception{
		TilesEngineModule aModule = _tree.get(iName);
		if (aModule!=null) {
			return aModule.getProperties();
		}
		throw new Exception("Unable to find module name:"+iName); 
	}
	
}
