import org.cfb.ungentry.network.DownloadFTP;


public class testFTPConnection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DownloadFTP aFTP = new DownloadFTP();
		aFTP.connect();
		String[] aDirs = aFTP.lsDirectoryWithFilter("acs[0-9]+(_[0-9]*yr)?");
		for (String aDir:aDirs){
			System.out.println(aDir);
		}
		aFTP.disconnect();

	}

}
