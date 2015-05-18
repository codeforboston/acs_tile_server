package org.cfb.ungentry.census.toolbox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class LevelTree<T> {

	HashMap<String, Object> _map;
	
	public LevelTree(){
		_map = new HashMap<String,Object>();
	}
	
	public void put(String iPath, Object iValue) throws Exception{
		
		LinkedList<String> aList = new LinkedList<String>(Arrays.asList(iPath.split("/")));
		ListIterator<String> aIt = aList.listIterator();
		
		HashMap<String,Object> aMap = _map; 
		String aDepth = aIt.next();
		while (aIt.hasNext()) {
			Object aCurrentObject = aMap.get(aDepth);
			if (aCurrentObject==null) {
				HashMap<String,Object> aNewMap = new  HashMap<String,Object>();
				aMap.put(aDepth, aNewMap);
				aMap = aNewMap; 
			} else  {
				aMap = (HashMap<String,Object>) aCurrentObject;
				if (aMap==null) throw new Exception("Try inserting new level at object position");
			}
			aDepth = aIt.next();
		}
		aMap.put(aDepth, iValue);
	}
	
	public T get(String iPath) throws Exception{

		LinkedList<String> aList = new LinkedList<String>(Arrays.asList(iPath.split("/")));
		ListIterator<String> aIt = aList.listIterator();
		
		HashMap<String,Object> aMap = _map; 
		String aDepth = aIt.next();
		while (aIt.hasNext()) {
			Object aCurrentObject = aMap.get(aDepth);
			if (aCurrentObject==null) {
				return null;
			} else  {
				aMap = (HashMap<String,Object>) aCurrentObject;
				if (aMap==null) throw new Exception("Try to read new level at object position");
			}
			aDepth = aIt.next();
		}
		return (T) aMap.get(aDepth);
	}
	
}
