package org.cfb.ungentry.census.data;

import java.io.File;
import java.util.Vector;
import java.util.regex.Pattern;

public class ACSReader {

	String _filename;
	
	public final static int FILEID = 0;
	public final static int FILETYPE = 1;
	public final static int STUSAB = 2;
	public final static int CHARITER = 3;
	public final static int SEQUENCE = 4;
	public final static int LOGRECNO = 5;
	
	public ACSReader(String iFileName){
		_filename = iFileName;
	}
	
	public void read(){
		
		Unzip.unzipFile(_filename,"out");
		
	}
	
	public static String[] getFileList(String iFileName){
		
		File aFile = new File(iFileName);
		File aDirectory = new File( aFile.getParent()+File.separator+"out" );
		
		File[] aFiles =  aDirectory.listFiles();
		Vector<String> aFileList = new Vector<String>();
		for (File aEntry: aFiles){
			
				if (Pattern.matches("e.*", aEntry.getName())) {
					//System.out.println("Selected:"+aEntry.getName());
					aFileList.add(aEntry.getAbsolutePath());
				}
			
		}
		
		return aFileList.toArray(new String[aFileList.size()]);
	}
	
	public static String[] getFilteredFileList(String iFileName, Integer[] sequence){
		
		File aFile = new File(iFileName);
		File aDirectory = new File( aFile.getParent()+File.separator+"out" );
		
		//System.out.println("Directory:"+aDirectory.getAbsolutePath());
		File[] aFiles =  aDirectory.listFiles();
		Vector<String> aFileList = new Vector<String>();
		for (File aEntry: aFiles){
			
				for (int i:sequence) {
					if (Pattern.matches(String.format("e.*%04d000.txt",i), aEntry.getName())) {
						System.out.println("Selected:"+aEntry.getName());
						aFileList.add(aEntry.getAbsolutePath());
					}
				}
			
		}
		
		return aFileList.toArray(new String[aFileList.size()]);
	}
	
	public static String getGeoFile(String iFileName){
		
		File aFile = new File(iFileName);
		File aDirectory = new File( aFile.getParent()+File.separator+"out" );
		
		System.out.println("Directory:"+aDirectory.getAbsolutePath());
		File[] aFiles =  aDirectory.listFiles();
		for (File aEntry: aFiles){
			
				if (Pattern.matches("g.*txt", aEntry.getName())) {
					return aDirectory.getAbsolutePath()+File.separator+aEntry.getName();
				}
			
		}
		return null;
	}
	
	
}
