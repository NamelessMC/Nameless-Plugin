package com.namelessmc.api.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.api.NamelessConnectException;

public class RequestUtil {

	/**
	 * @param type
	 * @param url Full URL
	 * @param postString
	 * @param https
	 * @return
	 */
	public static Request createRequest(RequestType type, URL url, String postString, boolean https) {
		
		if (type == null) {
			throw new IllegalArgumentException("Request type must not be null");
		}
		
		if (url == null) {
			throw new IllegalArgumentException("URL must not be null");
		}
			
		if (postString == null) {
			throw new IllegalArgumentException("Post string must not be null");
		}
			
		if (type.equals(RequestType.POST)) {
			Exception exception;
			try {
				URLConnection connection;
				
				if (https) {
					HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
					
					httpsConnection.setRequestMethod("POST");
					httpsConnection.setRequestProperty("Content-Length", Integer.toString(postString.length()));
					httpsConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					httpsConnection.setDoOutput(true);
					httpsConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
					
					connection = httpsConnection;
				} else {
					HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
					
					httpConnection.setRequestMethod("POST");
					httpConnection.setRequestProperty("Content-Length", Integer.toString(postString.length()));
					httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					httpConnection.setDoOutput(true);
					httpConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
					
					connection = httpConnection;
				}
					

				// Initialise output stream
				DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

				// Write request
				outputStream.writeBytes(postString);

				// Initialise input stream
				InputStream inputStream = connection.getInputStream();

				// Handle response
				BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				StringBuilder responseBuilder = new StringBuilder();

				String responseString;
				while ((responseString = streamReader.readLine()) != null)
					responseBuilder.append(responseString);

				JsonParser parser = new JsonParser();

				JsonObject response = parser.parse(responseBuilder.toString()).getAsJsonObject();

				if (response.has("error")) {
					// Error with request
					String errorMessage = response.get("message").getAsString();
					exception = new NamelessConnectException(errorMessage);
				}

				// Close output/input stream
				outputStream.flush();
				outputStream.close();
				inputStream.close();

				// Disconnect
				if (https) {
					((HttpsURLConnection) connection).disconnect();
				} else {
					((HttpURLConnection) connection).disconnect();
				}

				exception = null;
			} catch (Exception e) {
				exception = e;
			}
			
			return new Request(exception);
		} else {
			throw new IllegalArgumentException("This request type is not yet supported.");
		}
	}
	
	public static String getPostString(String id) {
		String postString = null;
		try {
			if (id.length() >= 17) {
				postString = "uuid=" + URLEncoder.encode(id, "UTF-8");
			} else if (id.length() <= 16) {
				postString = "username=" + URLEncoder.encode(id, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return postString;
	}
	
	public static class Request {
		
		private Exception exception;
		
		public Request(Exception exception) {
			this.exception = exception;
		}
		
		public Exception getException() {
			return exception;
		}
		
		public boolean hasSucceeded() {
			return exception == null;
		}
		
	}
	
	public enum RequestType {
		POST;
	}

}