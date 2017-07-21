package com.namelessmc.NamelessAPI.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.NamelessAPI.NamelessException;

public class RequestUtil {

	/**
	 * @param url Full URL with / at the end
	 * @param postString
	 * @param https
	 * @return
	 */
	public static Request sendPostRequest(URL url, String action, String postString, boolean https) {

		if (url == null) {
			throw new IllegalArgumentException("URL must not be null");
		}
			
		if (postString == null) {
			throw new IllegalArgumentException("Post string must not be null");
		}
		
		try {
			url = new URL(url.toString() + action);
		} catch (MalformedURLException e1) {
			throw new IllegalArgumentException("URL or action is malformed (" + e1.getMessage() + ")");
		}
			
		Exception exception;
		JsonObject response;
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

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();

			if (response.has("error")) {
				// Error with request
				String errorMessage = response.get("message").getAsString();
				exception = new NamelessException(errorMessage);
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
			response = null;
		}
			
		return new Request(exception, response);
	}
	
	public static class Request {
		
		private Exception exception;
		private JsonObject response;
		
		public Request(Exception exception, JsonObject response) {
			this.exception = exception;
		}
		
		public Exception getException() {
			return exception;
		}
		
		public boolean hasSucceeded() {
			return exception == null;
		}
		
		public JsonObject getResponse() {
			return response;
		}
		
	}

}