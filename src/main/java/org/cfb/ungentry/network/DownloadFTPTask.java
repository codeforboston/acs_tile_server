package org.cfb.ungentry.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.log4j.Logger;


public class DownloadFTPTask implements CopyStreamListener, Runnable {

	protected static final Logger LOGGER = Logger.getLogger(DownloadFTPTask.class.getName());

	public static TreeMap<String,DownloadFTPTask> _allDownloads = new TreeMap<String,DownloadFTPTask>();

	public static class Status {
		long _totalSize;
		long _downloaded;
		long _percent;
		boolean _finish;
	}

	FTPClient  _client;
	TreeMap<String,String> _queue;
	TreeMap<String,Status> _status;
	String _currentDownload;

	public DownloadFTPTask(FTPClient iClient){
		_client = iClient;
		_queue = new TreeMap<String,String>();
		_status = new TreeMap<String,Status>();
	}

	public static TreeMap<String, Status> getDownloadStatus(String[] iFiles){
		
		    TreeMap<String, Status> aList = new  TreeMap<String, Status>();
		
			for (String aFile:iFiles) {
				DownloadFTPTask aTask = _allDownloads.get(aFile);
				if (aTask!=null) {
					Status aStatus = aTask._status.get(aFile);
					aList.put(aFile, aStatus);
				}
			}
			
			return aList;
	}
	
	public void addFileToDownload(String iRemoteFile, String iLocalFile){
		Status aStatus = new Status();
		aStatus._finish = false;
		_status.put(iRemoteFile, aStatus);
		_allDownloads.put(iRemoteFile, this);
		_queue.put(iRemoteFile, iLocalFile);
	}

	private long getFileSize(String filePath) {
		long fileSize = 0;
		FTPFile[] files;
		try {
			files = _client.listFiles(filePath);
			LOGGER.info("File length = "+files.length);
			if (files.length == 1 && files[0].isFile()) {
				fileSize = files[0].getSize();
			}
			LOGGER.info("File size = " + fileSize);
			return fileSize;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public boolean downloadFile(String remoteFilePath, String localFilePath) {
		try {
			LOGGER.info("Trying to Download:"+remoteFilePath);
			Status aStatus = _status.get(_currentDownload);
			aStatus._totalSize = getFileSize(remoteFilePath);
			FileOutputStream fos = new FileOutputStream(localFilePath);
			this._client.setFileType(FTP.BINARY_FILE_TYPE);
			this._client.retrieveFile(remoteFilePath, fos);
			fos.flush();
			fos.close(); // Closing file output stream
			
			// Check local file size
			File aFile = new File(localFilePath);
			long aSize = aFile.length();
			aStatus._downloaded = aSize;
			
			aStatus._finish = true;
			
			LOGGER.info("Terminate transfer:"+remoteFilePath);
			return true;
		} catch (IOException e) {
			LOGGER.error("Error when downloading "+e.getMessage());
			return false;
		}
	}

	public void bytesTransferred(CopyStreamEvent arg0) {
		LOGGER.info("Download:"+_currentDownload+" "+arg0.getBytesTransferred());
		_status.get(_currentDownload)._downloaded = arg0.getBytesTransferred();
	}

	public static Status getStatus(String iPath){
		DownloadFTPTask aFTPT = _allDownloads.get(iPath);
		if (aFTPT!=null) {
			return aFTPT._status.get(iPath);
		} 
		return null;
	}

	public void bytesTransferred(long arg0, int arg1, long arg2) {
		Status aStatus = _status.get(_currentDownload);
		aStatus._downloaded = arg0;
		aStatus._percent = (aStatus._downloaded*100)/aStatus._totalSize;
		//LOGGER.info("Download:"+_currentDownload+" "+(aStatus._downloaded*100)/aStatus._totalSize);
	}

	public void run() {
		while (_client.isConnected()){

			if (!_queue.isEmpty()) {
				Entry<String,String> aElem = _queue.firstEntry();
				_currentDownload = aElem.getKey();
				downloadFile(aElem.getKey(), aElem.getValue());
				 _queue.remove(aElem.getKey());
			} else {
				try {
					Thread.currentThread().sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

}
