package com.namelessmc.namelessplugin.bungeecord.mcstats;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

public class Metrics {
	
	private static final int REVISION = 7;
	private static final String BASE_URL = "http://report.mcstats.org";
	private static final String REPORT_URL = "/plugin/%s";
	private static final int PING_INTERVAL = 10;
	private final Plugin plugin;
	private final Set<Graph> graphs = Collections.synchronizedSet(new HashSet<Graph>());
	private final Properties properties = new Properties();
	private final File configurationFile;
	private final String guid;
	private final boolean debug;
	private final Object optOutLock = new Object();
	private Thread thread = null;
	private static ByteArrayOutputStream baos;
	
	public Metrics(Plugin plugin) throws IOException {
		if (plugin == null) {
			throw new IllegalArgumentException("Plugin cannot be null");
		}
		this.plugin = plugin;
		
		this.configurationFile = getConfigFile();
		if (!this.configurationFile.exists()) {
			if ((this.configurationFile.getPath().contains("/")) || (this.configurationFile.getPath().contains("\\"))) {
				File parent = new File(this.configurationFile.getParent());
				if (!parent.exists()) {
					parent.mkdir();
				}
			}
			this.configurationFile.createNewFile();
			this.properties.put("opt-out", "false");
			this.properties.put("guid", UUID.randomUUID().toString());
			this.properties.put("debug", "false");
			this.properties.store(new FileOutputStream(this.configurationFile), "http://mcstats.org");
		} else {
			this.properties.load(new FileInputStream(this.configurationFile));
		}
		this.guid = this.properties.getProperty("guid");
		this.debug = Boolean.parseBoolean(this.properties.getProperty("debug"));
	}
	
	public Graph createGraph(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Graph name cannot be null");
		}
		Graph graph = new Graph(name);
		
		this.graphs.add(graph);
		
		return graph;
	}
	
	public void addGraph(Graph graph) {
		if (graph == null) {
			throw new IllegalArgumentException("Graph cannot be null");
		}
		this.graphs.add(graph);
	}
	
	public boolean start() {
		synchronized (this.optOutLock) {
			if (isOptOut()) {
				return false;
			}
			if (this.thread != null) {
				return true;
			}
			this.thread = new Thread(new Runnable(){
				
				private boolean firstPost = true;
				private long nextPost = 0L;
				
				public void run(){
					while (Metrics.this.thread != null) {
						if ((this.nextPost == 0L) || (PING_INTERVAL > this.nextPost)) {
							try {
								synchronized (Metrics.this.optOutLock) {
									if ((Metrics.this.isOptOut()) && (Metrics.this.thread != null)) {
										Thread temp = Metrics.this.thread;
										Metrics.this.thread = null;
										for (Metrics.Graph graph : Metrics.this.graphs) {
											graph.onOptOut();
										}
										temp.interrupt();
										return;
									}
								}
								Metrics.this.postPlugin(!this.firstPost);
								
								this.firstPost = false;
								this.nextPost = (System.currentTimeMillis() + 900000L);
							} catch (IOException e) {
								if (Metrics.this.debug) {
									System.out.println("[Metrics] " + e.getMessage());
								}
							}
						}
						try {
							Thread.sleep(100L);
						} catch (InterruptedException e) {}
					}
				}
			}, "MCStats / Plugin Metrics");
			
			this.thread.start();
			
			return true;
		}
	}
	
	public boolean isOptOut() {
		synchronized (this.optOutLock) {
			try {
				this.properties.load(new FileInputStream(this.configurationFile));
			} catch (IOException ex) {
				if (this.debug) {
					ProxyServer.getInstance().getLogger().log(Level.INFO, "[Metrics] " + ex.getMessage());
				}
				return true;
			}
			return Boolean.parseBoolean(this.properties.getProperty("opt-out"));
		}
	}
	
	public void enable()
		throws IOException {
		synchronized (this.optOutLock) {
			if (isOptOut()) {
				this.properties.setProperty("opt-out", "false");
				this.properties.store(new FileOutputStream(this.configurationFile), "http://mcstats.org");
			}
			if (this.thread == null) {
				start();
			}
		}
	}
	
	public void disable() throws IOException {
		synchronized (this.optOutLock) {
			if (!isOptOut()) {
				this.properties.setProperty("opt-out", "true");
				this.properties.store(new FileOutputStream(this.configurationFile), "http://mcstats.org");
			}
			if (this.thread != null) {
				this.thread.interrupt();
				this.thread = null;
			}
		}
	}
	
	public File getConfigFile() {
		return new File(new File("plugins", "PluginMetrics"), "config.properties");
	}
	
	private void postPlugin(boolean isPing) throws IOException {
		PluginDescription description = this.plugin.getDescription();
		String pluginName = description.getName();
		boolean onlineMode = ProxyServer.getInstance().getConfigurationAdapter().getBoolean("online_mode", true);
		String pluginVersion = description.getVersion();
		String serverVersion = ProxyServer.getInstance().getVersion();
		int playersOnline = ProxyServer.getInstance().getOnlineCount();
		
		StringBuilder json = new StringBuilder(1024);
		json.append('{');
		
		appendJSONPair(json, "guid", this.guid);
		appendJSONPair(json, "plugin_version", pluginVersion);
		appendJSONPair(json, "server_version", serverVersion);
		appendJSONPair(json, "players_online", Integer.toString(playersOnline));
		
		String osname = System.getProperty("os.name");
		String osarch = System.getProperty("os.arch");
		String osversion = System.getProperty("os.version");
		String java_version = System.getProperty("java.version");
		int coreCount = Runtime.getRuntime().availableProcessors();
		if (osarch.equals("amd64")) {
			osarch = "x86_64";
		}
		appendJSONPair(json, "osname", osname);
		appendJSONPair(json, "osarch", osarch);
		appendJSONPair(json, "osversion", osversion);
		appendJSONPair(json, "cores", Integer.toString(coreCount));
		appendJSONPair(json, "auth_mode", onlineMode ? "1" : "0");
		appendJSONPair(json, "java_version", java_version);
		if (isPing) {
			appendJSONPair(json, "ping", "1");
		}
		if (this.graphs.size() > 0) {
			synchronized (this.graphs)
			{
				json.append(',');
				json.append('"');
				json.append("graphs");
				json.append('"');
				json.append(':');
				json.append('{');
				
				boolean firstGraph = true;
				
				Iterator<Graph> iter = this.graphs.iterator();
				while (iter.hasNext())
				{
					Graph graph = (Graph)iter.next();
					
					StringBuilder graphJson = new StringBuilder();
					graphJson.append('{');
					for (Plotter plotter : graph.getPlotters()) {
						appendJSONPair(graphJson, plotter.getColumnName(), Integer.toString(plotter.getValue()));
					}
					graphJson.append('}');
					if (!firstGraph) {
						json.append(',');
					}
					json.append(escapeJSON(graph.getName()));
					json.append(':');
					json.append(graphJson);
					
					firstGraph = false;
				}
				json.append('}');
			}
		}
		json.append('}');
		
		URL url = new URL(BASE_URL + String.format(REPORT_URL, urlEncode(pluginName)));
		URLConnection connection;
		if (isMineshafterPresent()) {
			connection = url.openConnection(Proxy.NO_PROXY);
		} else {
			connection = url.openConnection();
		}
		byte[] uncompressed = json.toString().getBytes();
		byte[] compressed = gzip(json.toString());
		
		connection.addRequestProperty("User-Agent", "MCStats/" + REVISION);
		connection.addRequestProperty("Content-Type", "application/json");
		connection.addRequestProperty("Content-Encoding", "gzip");
		connection.addRequestProperty("Content-Length", Integer.toString(compressed.length));
		connection.addRequestProperty("Accept", "application/json");
		connection.addRequestProperty("Connection", "close");
		
		connection.setDoOutput(true);
		if (this.debug) {
			System.out.println("[Metrics] Prepared request for " + pluginName + " uncompressed=" + uncompressed.length + " compressed=" + compressed.length);
		}
		OutputStream os = connection.getOutputStream();
		os.write(compressed);
		os.flush();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String response = reader.readLine();
		
		os.close();
		reader.close();
		if ((response == null) || (response.startsWith("ERR")) || (response.startsWith("7"))) {
			if (response == null) {
				response = "null";
			} else if (response.startsWith("7")) {
				response = response.substring(response.startsWith("7,") ? 2 : 1);
			}
			throw new IOException(response);
		}
		if ((response.equals("1")) || (response.contains("This is your first update this hour"))) {
			synchronized (this.graphs) {
				Iterator<Graph> iter = this.graphs.iterator();
				while (iter.hasNext()) {
					Graph graph = (Graph)iter.next();
					for (Plotter plotter : graph.getPlotters()) {
						plotter.reset();
					}
				}
			}
		}
	}
	
	public static byte[] gzip(String input) {
		
		baos = new ByteArrayOutputStream();
		GZIPOutputStream gzos = null;
		
		try {
			gzos = new GZIPOutputStream(baos);
			gzos.write(input.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (gzos != null) try {
				gzos.close();
			} catch (IOException ignore) {}
		}

		return baos.toByteArray();
	}
	 
	
	private boolean isMineshafterPresent() {
		try {
			Class.forName("mineshafter.MineServer");
			return true;
		}
		catch (Exception e) {}
		return false;
	}
	
	private static void appendJSONPair(StringBuilder json, String key, String value) throws UnsupportedEncodingException {
		boolean isValueNumeric = false;
		try {
			if ((value.equals("0")) || (!value.endsWith("0"))) {
				Double.parseDouble(value);
				isValueNumeric = true;
			}
		} catch (NumberFormatException e) {
			isValueNumeric = false;
		}
		if (json.charAt(json.length() - 1) != '{') {
			json.append(',');
		}
		json.append(escapeJSON(key));
		json.append(':');
		if (isValueNumeric) {
			json.append(value);
		} else {
			json.append(escapeJSON(value));
		}
	}
	
	private static String escapeJSON(String text) {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append('"');
		for (int index = 0; index < text.length(); index++) {
			char chr = text.charAt(index);
			switch (chr) {
			case '"': 
			case '\\': 
				builder.append('\\');
				builder.append(chr);
				break;
			case '\b': 
				builder.append("\\b");
				break;
			case '\t': 
				builder.append("\\t");
				break;
			case '\n': 
				builder.append("\\n");
				break;
			case '\r': 
				builder.append("\\r");
				break;
			default: 
				if (chr < ' ') {
					String t = "000" + Integer.toHexString(chr);
					builder.append("\\u" + t.substring(t.length() - 4));
				} else {
					builder.append(chr);
				}
				break;
			}
		}
		builder.append('"');
		
		return builder.toString();
	}
	
	private static String urlEncode(String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}
	
	public static class Graph {
		
		private final String name;
		private final Set<Plotter> plotters = new LinkedHashSet<Plotter>();
		
		private Graph(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return this.name;
		}
		
		public void addPlotter(Metrics.Plotter plotter)
		{
			this.plotters.add(plotter);
		}
		
		public void removePlotter(Metrics.Plotter plotter)
		{
			this.plotters.remove(plotter);
		}
		
		public Set<Metrics.Plotter> getPlotters()
		{
			return Collections.unmodifiableSet(this.plotters);
		}
		
		public int hashCode()
		{
			return this.name.hashCode();
		}
		
		public boolean equals(Object object)
		{
			if (!(object instanceof Graph)) {
				return false;
			}
			Graph graph = (Graph)object;
			return graph.name.equals(this.name);
		}
		
		protected void onOptOut() {}
	}
	
	public static abstract class Plotter{
		
		private final String name;
		
		public Plotter(){
			this("Default");
		}
		
		public Plotter(String name){
			this.name = name;
		}
		
		public abstract int getValue();
		
		public String getColumnName(){
			return this.name;
		}
		
		public void reset() {}
		
		public int hashCode(){
			return getColumnName().hashCode();
		}
		
		public boolean equals(Object object){
			if (!(object instanceof Plotter)) {
				return false;
			}
			Plotter plotter = (Plotter)object;
			return (plotter.name.equals(this.name)) && (plotter.getValue() == getValue());
		}
	}
}