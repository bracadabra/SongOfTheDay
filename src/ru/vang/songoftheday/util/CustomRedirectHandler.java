package ru.vang.songoftheday.util;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;

public class CustomRedirectHandler extends DefaultRedirectHandler {
	  private transient URI mLastLocationURI;	
	
	  public URI getLocationURI(final HttpResponse response,  final HttpContext context) throws ProtocolException {		    
		  mLastLocationURI = super.getLocationURI(response, context);
		  return mLastLocationURI;
	  }
	  
	  public URI getLastLocationURI() {
		  return mLastLocationURI;
	  }

}
