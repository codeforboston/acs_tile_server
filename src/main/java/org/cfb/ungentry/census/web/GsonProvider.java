package org.cfb.ungentry.census.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
 


import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.hamcrest.core.IsNull;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 


@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class GsonProvider implements MessageBodyWriter<Object>,
    MessageBodyReader<Object> {
	
   protected static final Logger LOGGER = Logger.getLogger(GsonProvider.class.getName());
	
   @Context
   UriInfo info;
 
  private static final String UTF_8 = "UTF-8";
 
  private Gson gson;
 
  private Gson getGson() {
    if (gson == null) {
      final GsonBuilder gsonBuilder = new GsonBuilder();
      gson = gsonBuilder.create();
    }
    return gson;
  }
 
  public boolean isReadable(Class<?> type, Type genericType,
      java.lang.annotation.Annotation[] annotations, MediaType mediaType) {
    return true;
  }
 
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
    InputStreamReader streamReader = new InputStreamReader(entityStream, UTF_8);
    try {
      Type jsonType;
      if (type.equals(genericType)) {
        jsonType = type;
      } else {
        jsonType = genericType;
      }
      return getGson().fromJson(streamReader, jsonType);
    } finally {
      streamReader.close();
    }
  }
 
  
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return true;
  }
 
  
  public long getSize(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }
 
  public static final boolean isNull(String s) {
      return ((null == s) || (s.trim().length() <= 0));
  }
  
  public static final String CALLBACK_QUERYSTRING = "callback";

  public static final String callbackParameter(UriInfo info) {
      return (info.getQueryParameters().getFirst(CALLBACK_QUERYSTRING));
  }
  
  public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    
	  boolean modeCallback = false;
	  
	  // TODO switch also reply mime type
	  
	  String callback = callbackParameter(info);
	  //LOGGER.info(info.getQueryParameters());
	  //LOGGER.info("Callback name:"+callback);
      if (!isNull(callback)) {
    	  modeCallback = true; // i.e. we will proceed in jsonp
      }
	  
	OutputStreamWriter writer = new OutputStreamWriter(entityStream, UTF_8);
    try {
      Type jsonType;
      if (type.equals(genericType)) {
        jsonType = type;
      } else {
        jsonType = genericType;
      }
      
      if (modeCallback) { // Adding jsonp callback name
    	  writer.write(callback+"(");
      }
      
      getGson().toJson(object, jsonType, writer);
      
      if (modeCallback) {
    	  writer.write(")");
      }
      
    } finally {
      writer.close();
    }
  }

  
}
