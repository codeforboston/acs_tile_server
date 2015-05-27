package org.cfb.ungentry.census.data;

import java.util.HashMap;

import json.geojson.FeatureCollection.TileElement;
import json.topojson.topology.Topology;

import org.cfb.ungentry.census.toolbox.LevelTree;

public class TilesEngine {

	public static interface  TilesEngineModule {
		
			byte[] get(String iZoom, String iX, String iY) throws Exception;
			Topology getTopology(String iZoom, String iX, String iY) throws Exception;
	
	}
	
	public static HashMap<String, TilesEngineModule> _tree ;
	
	static {
		
		_tree =  new HashMap<String, TilesEngineModule>();
		
	}
	
	public static void addModule(String iName, TilesEngineModule iModule){
		_tree.put(iName, iModule);
	}
	
	// Generate a buffer that contains data generated for the tile 
	public static byte[] getTile(String iName, String iZoom, String iX, String iY) throws Exception{
		TilesEngineModule aModule = _tree.get(iName);
		if (aModule!=null) {
			return aModule.get(iZoom, iX, iY);
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

}
