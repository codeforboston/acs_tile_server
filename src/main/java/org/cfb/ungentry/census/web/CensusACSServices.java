package org.cfb.ungentry.census.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.cfb.ungentry.census.data.ACSLoader;
import org.cfb.ungentry.census.data.IndicesBuilder;
import org.cfb.ungentry.census.data.SequenceAndTableNumber.Element;
import org.cfb.ungentry.census.data.TilesEngine;
import org.cfb.ungentry.census.toolbox.Toolbox;
import org.cfb.ungentry.network.DownloadFTPTask;
import org.cfb.ungentry.network.DownloadFTPTask.Status;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Path("/censusacs")
public class CensusACSServices {

	protected static final Logger LOGGER = Logger.getLogger(CensusACSServices.class.getName());
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/listACSFolders")
	public String[] listACSFolders( @Context SecurityContext sc ) {
		LOGGER.info("List Census ACS available years ...");
		return IndicesBuilder.listACSFolders();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/listStateCodesByName")
	public LinkedHashMap<String,String> listStateCodesByName( @Context SecurityContext sc ) {
		LOGGER.info("List state codes by name ...");
		return IndicesBuilder.listStateCodesByName();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/listStatesForACSData/{path}")
	public String[] listStatesForACSData( @Context SecurityContext sc, @PathParam("path") String iPath  ) {
		LOGGER.info("List already data dowloaded for path :"+iPath);
		return IndicesBuilder.listStatesForACSData(iPath);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/retrieveACSFiles/{path}/{state}")
	public TreeMap<String, String> retrieveACSFiles( @Context SecurityContext sc, @PathParam("path") String iPath , @PathParam("state") String iState ) {
		LOGGER.info("List already data dowloaded for path :"+iPath);
		return ACSLoader.retrieveFiles(iPath, iState);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/retrieveLocalSequenceTableNumberStruct/{path}/{state}")
	public LinkedTreeMap<String, Vector<Element>> retrieveLocalSequenceTableNumberStruct( @Context SecurityContext sc, @PathParam("path") String iPath, @PathParam("state") String iState) {
		LOGGER.info("Retrieve sequence and tables structure :"+iPath);
		
		String aACSFileName = ACSLoader.retrieveLocalACSDataFile(iPath, iState);
		String iFileName = aACSFileName+File.separator+"LSTNS_"+iPath+"_"+iState+".json";
		
		LinkedTreeMap<String, Vector<Element>> aResult = null;
		
		File aFile = new File(iFileName);
		if (aFile.exists()) {
			aResult = Toolbox.<LinkedTreeMap<String, Vector<Element>>>readFromJsonFile(aFile);
		} else {
			aResult = ACSLoader.retrieveLocalSequenceTableNumberStruct(iPath, iState);
			Toolbox.writeToJsonFile(aFile, aResult);
		}
		return aResult;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/buildMap/{path}/{state}/{list}")
	public String buildMap( @Context SecurityContext sc, @PathParam("path") String iPath, @PathParam("state") String iState, @PathParam("list") String iList) {
		LOGGER.info("Building a map");
		return ACSLoader.buildMap(iPath, iState, iList);
	}
	
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/getDownloadStatus")
	public TreeMap<String,Status> getDownloadStatus( @Context SecurityContext sc,
									     		 String[] iList) { 
		return DownloadFTPTask.getDownloadStatus(iList);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/tiles/{state}/{zoom}/{x}/{y}.png")
	public byte[] tiles( @Context SecurityContext sc, @PathParam("state") String iState, @PathParam("zoom") String iZoom, @PathParam("x") String iX, @PathParam("y") String iY) throws Exception {
		try {
			return TilesEngine.getTile(iState, iZoom, iX, iY);
		} catch (Exception e) {
			LOGGER.info("Image not generated ...");
			throw new NotFoundException(e.getMessage());		
		}
		
	}
	
	
}
