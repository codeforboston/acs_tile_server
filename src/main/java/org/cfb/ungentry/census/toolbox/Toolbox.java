package org.cfb.ungentry.census.toolbox;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Toolbox {
	
	public static  <T> T  readFromJsonFile(File iFile){

		try {
			
			String aJson = IOUtils.toString(new FileInputStream(iFile));
			
			Type type = new TypeToken<T>(){}.getType();
			
			Gson aGson = new Gson();
			return aGson.fromJson(aJson, type );
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;

	}
	
	public static  void writeToJsonFile(File iFile, Object iData){

		Gson aGson = new Gson();
		String aJSonData = aGson.toJson(iData);
		try {
			IOUtils.write(aJSonData, new FileOutputStream(iFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
