package ru.vang.songoftheday.api;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public final class ErrorParser {	

	private ErrorParser() {

	}

	public static Response parse(final InputStream in) {
		final Response response = new Response();
		final RootElement root = new RootElement("lfm");
		final Element error = root.getChild("error");
		error.setStartElementListener(new StartElementListener() {

			public void start(final Attributes attributes) {
				response.code = Integer.valueOf(attributes.getValue("code"));				
			}
		});

		try {
			Xml.parse(in, Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}
	
	public static class Response {
		int code;
	}

}
