import org.cfb.ungentry.census.data.ACSLoader;



public class testFTPDownload {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		org.apache.log4j.BasicConfigurator.configure();
		
		/*
		DownloadFTP aFTP = new DownloadFTP();
		aFTP.connect();
		
		aFTP.downloadFile("/acs2013_5yr/summaryfile/Sequence_Number_and_Table_Number_Lookup.txt", "test.txt");	
		aFTP.downloadFile("/acs2013_5yr/summaryfile/Sequence_Number_and_Table_Number_Lookup.xls", "test.xls");	
		
		aFTP.disconnect();
		*/
		
		ACSLoader.retrieveFiles("acs2013_5yr", "Massachusetts");
		
	}

}
