package org.cfb.ungentry.network;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


public class DownloadFTP {

	public final static String ACSFTP = "ftp2.census.gov" ;
	
	FTPClient _connection;
	DownloadFTPTask _downloadTask;
	
	public void connect(){
		
		_connection = new FTPClient();
	    FTPClientConfig config = new FTPClientConfig();
	    //config.setXXX(YYY); // change required options
	    _connection.configure(config );
	    boolean error = false;
	    try {
	      int reply;
	      
	      _connection.setBufferSize(1024*1024);
	      
	      //_connection.setFileType(FTP.BINARY_FILE_TYPE);
		
	      
	      _connection.connect(ACSFTP);
	      
	  
	      
	      System.out.println("Connected to " + ACSFTP + ".");
	      System.out.print(_connection.getReplyString());

	      // After connection attempt, you should check the reply code to verify
	      // success.
	      reply = _connection.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	    	_connection.disconnect();
	        System.err.println("FTP server refused connection.");
	      }
	     
	      
	    } catch(IOException e) {
	      error = true;
	      e.printStackTrace();
	    } 
	    
	    // Also doing login part 
	    
	    try {
			if (!_connection.login("anonymous", ""))
			{
				_connection.logout();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    _connection.enterLocalPassiveMode();
	    
	    _downloadTask = new DownloadFTPTask(_connection);
	    
	    _connection.setCopyStreamListener(_downloadTask);
	    
	    (new Thread(_downloadTask)).start();

	}

    public void downloadFile(String iRemoteFile, String iLocalFile){
    	_downloadTask.addFileToDownload(iRemoteFile, iLocalFile);
    }
	
	public String[] lsDirectoryWithFilter(String iReg){
		
		Vector<String> aDIRS = new Vector<String>();
		
		Pattern aPat = Pattern.compile(iReg);
		
		try {
			FTPFile[] aDirectories = _connection.listDirectories();
				
			for (int i=0; i<aDirectories.length; i++){
				
				if (aPat.matcher(aDirectories[i].getName()).matches()) {
					aDIRS.add(aDirectories[i].getName());
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return aDIRS.toArray(new String[aDIRS.size()]);
		
	}
	
	public void disconnect(){
		
		while (!_downloadTask._queue.isEmpty()) { // Waiting until transfer ends
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			_connection.logout();
		} catch (IOException e) {			
			
		} finally {
			
			if(_connection.isConnected()) {
		        try {
		        	_connection.disconnect();
		        } catch(IOException ioe) {
		          // do nothing
		        }
		    }
			
		}
		
		
	}

}
