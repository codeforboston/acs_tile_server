import java.util.LinkedHashMap;

import json.converter.csv.builder.ColumnBuilder;


import org.cfb.ungentry.census.data.GeographyFileReader;
import org.cfb.ungentry.census.data.SequenceAndTableNumber;
import org.glassfish.jersey.message.internal.Token;


public class testGeographyFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		GeographyFileReader aGeoReader = new GeographyFileReader("./src/test/resources/g20115ma.txt");
		aGeoReader.read();
		
		int indexState = aGeoReader._header.indexOf("STATE");
		int indexCounty = aGeoReader._header.indexOf("COUNTY");
		int indexTract =  aGeoReader._header.indexOf("TRACT");
		
		ColumnBuilder aBuilder = new ColumnBuilder("NHGIS");
		aBuilder.addToken(new ColumnBuilder.Token("G%02d0", ColumnBuilder.Token.TYPE.INTEGER  , indexState));
		aBuilder.addToken(new ColumnBuilder.Token("%03d", ColumnBuilder.Token.TYPE.INTEGER  , indexCounty));
		aBuilder.addToken(new ColumnBuilder.Token("%07d", ColumnBuilder.Token.TYPE.INTEGER  , indexTract));
		
		aBuilder.build(aGeoReader);
		
		int count = 0;
		/*
		for (String[] val: aGeoReader._data.values()) {
		
				String aFilter = val.get("SUMLEVEL")+"_"+val.get("COMPONENT");
			
				if (aFilter.equals("140_00") && val.get("STATE").equals("25")) {		
					count++;
					System.out.println(val.get("LOGRECNO")+" NHGIS:"+val.get("NHGIS")+" "+count);	
				}
				
		}
		*/

	}

}
