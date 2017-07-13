package com.namelessmc.namelessplugin.spigot.API.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

public class RequestUtil {
	
	private JsonParser parser = new JsonParser();
	private JsonObject response = new JsonObject();
	private boolean error = false;
	private String errorMessage = "error";
	private boolean succeeded = true;

	public RequestUtil(RequestType type, String urlKey, String postString) {
		if (type.equals(RequestType.Post)) {
			try {
				URL apiConnection = new URL(NamelessPlugin.getInstance().getAPIUrl() + "/" + urlKey);

				if (NamelessPlugin.getInstance().enabledHttps()) {
					HttpsURLConnection connection = (HttpsURLConnection) apiConnection.openConnection();
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Length", Integer.toString(postString.length()));
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					connection.setDoOutput(true);
					connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

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

					response = parser.parse(responseBuilder.toString()).getAsJsonObject();

					if (response.has("error")) {
						// Error with request
						error = true;
						succeeded = false;
						setErrorMessage(response.get("message").getAsString());
						NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, errorMessage);
					}

					// Close output/input stream
					outputStream.flush();
					outputStream.close();
					inputStream.close();

					// Disconnect
					connection.disconnect();
				} else if (!NamelessPlugin.getInstance().enabledHttps()) {
					HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Length", Integer.toString(postString.length()));
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					connection.setDoOutput(true);
					connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

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

					response = parser.parse(responseBuilder.toString()).getAsJsonObject();

					if (response.has("error")) {
						// Error with request
						error = true;
						succeeded = false;
						setErrorMessage(response.get("message").getAsString());
						NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, errorMessage);
					}

					// Close output/input stream
					outputStream.flush();
					outputStream.close();
					inputStream.close();

					// Disconnect
					connection.disconnect();
				}
			} catch (Exception e) {
				// Exception
				// Exception
				error = true;
				setErrorMessage("There was an unknown error whilst executing the Request");
				succeeded = false;
				NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, errorMessage);
				e.printStackTrace();
			}
		}
	}
	
	public JsonObject getResponse(){
		return response;
	}
	
	public JsonParser getParser(){
		return parser;
	}
	
	public boolean hasError(){
		return error;
	}
	
	public boolean hasSucceeded(){
		return succeeded;
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
	
	public void setErrorMessage(String message){
		errorMessage = message;
	}
	
	protected static String getPostString(String id) {
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

	public enum RequestType {
		Post;
	}

}
