import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import json.converter.csv.CSVReader;

import org.cfb.ungentry.census.data.ACSLoader;
import org.junit.Test;


public class testRegExp {

	

	@Test
	public void test() {

		/*
		Pattern pattern = Pattern.compile("");

	    Matcher matcher = pattern.matcher("");
	    // check all occurance
	    while (matcher.find()) {

	         System.out.print("Start index: " + matcher.start());
	         System.out.print(" End index: " + matcher.end() + " ");
	         System.out.println(matcher.group(1));

	    }*/

		/*
		String aPath  = "acs2013_5yr";
	    if (ACSLoader.isYearDependant(aPath)) {
	    	System.out.println("yes");
	    	System.out.println(ACSLoader.buildChainMultiYear(aPath));

	    }
		 */

		/*
		Pattern aPat = Pattern.compile("(?:\\s*(?:\"([^\"]*)\"|([^,]+))\\s*,?|(),?)+?");
		String iLine = "970,\"ta,ta\",ABCD";
		Matcher aMatch = aPat.matcher(iLine);
		if (aMatch.matches()) {

			for (int i=0; i<=aMatch.groupCount(); i++) {
				System.out.println(i+":"+aMatch.group(i));
			}

		} else {
			System.out.println("No match");
		}*/
		
		String iLine = "970,\"ta,ta\",ABCD";

		try {
			String[] aElems = CSVReader.readCSVLine(iLine);
			for (int i=0; i<aElems.length; i++) {
				System.out.println(i+":"+aElems[i]);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
