import java.util.Vector;

import org.cfb.ungentry.census.data.ACSReader;
import org.cfb.ungentry.census.data.GeographyFileReader;
import org.cfb.ungentry.census.data.SequenceAndTableNumber;
import org.cfb.ungentry.census.data.SequenceAndTableNumber.Element;


public class testACSReader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
			String aFileName =  "./src/test/resources/ACS_data.zip";
		
			SequenceAndTableNumber aSeq = new SequenceAndTableNumber("./src/test/resources/Sequence_Number_and_Table_Number_Lookup.csv");
			aSeq.read();
			
			ACSReader aReader = new ACSReader(aFileName);
			aReader.read();
			
			GeographyFileReader aGReader = new GeographyFileReader(ACSReader.getGeoFile(aFileName));
			aGReader.read();
			
			aSeq.evaluate(ACSReader.getFileList(aFileName), aGReader);
			
		    String[] aFilter = {"B00050_50_7"};
		    
		    aSeq.extractCSVwithFilter(ACSReader.getFileList(aFileName), aFilter, "./src/test/resources/extract_B00050_50_7.csv");

	}

}
