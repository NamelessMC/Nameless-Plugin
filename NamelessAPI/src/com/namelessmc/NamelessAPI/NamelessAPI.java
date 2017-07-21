package com.namelessmc.NamelessAPI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NamelessAPI {

	/**
	 * Checks if a web API connection can be established
	 * @return An exception if the connection was unsuccessful, null if the connection was successful.
	 */
	public static NamelessException checkWebAPIConnection(URL url) {
		try {
			URL apiConnection = new URL(url + "/checkConnection");

			HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", Integer.toString(0));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			// Initialise output stream
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
			// Write request
			outputStream.writeBytes("");

			// Initialise input stream
			InputStream inputStream = connection.getInputStream();

			// Handle response
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder responseBuilder = new StringBuilder();

			String responseString;
			while ((responseString = streamReader.readLine()) != null)
				responseBuilder.append(responseString);

			JsonObject response = new JsonObject();
			JsonParser parser = new JsonParser();

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();

			String errorMessage;
			
			if (response.has("success")
					|| response.get("message").getAsString().equalsIgnoreCase("Invalid API method")) {
				errorMessage = null;
			} else if (response.has("error")) {
				errorMessage = response.get("message").getAsString();
			} else {
				errorMessage = "Unknown error";
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();
			
			if (errorMessage == null) {
				//Error message == null - connection successful
				return null;
			} else {
				//Connection unsuccessful
				return new NamelessException(errorMessage);
			}

		} catch (Exception e) {
			return new NamelessException(e);
		}
	}

}