package org.cfb.ungentry.census.web.jetty;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.ContextHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;




public class EmbeddedServer {

	protected static final Logger LOGGER = Logger.getLogger(EmbeddedServer.class.getName());

	public static String _sDirectory;
	public static String _wDirectory;
	public static final String CONTEXTPATH = "/censusacs";

	public static void buildDirectory(){

		// Create directory if not exists
		_sDirectory = "."+File.separator+"censusacsserver-jetty-tmp";
		File dir = new File(_sDirectory);

		// if the directory does not exist, create it
		if (!dir.exists()) {
			System.out.println("creating directory: " + _sDirectory);
			dir.mkdir();  
		}

		// cleaning directory from preceding runs
		Pattern aPat = Pattern.compile("jetty.*");

		File[] aFiles = dir.listFiles();
		for (File aFile:aFiles){
			if (aPat.matcher(aFile.getName()).matches()) {
				try {
					FileUtils.deleteDirectory(aFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
	public static String DATA_DIRECTORY;
	
	public static String dataDirectory(){
		return DATA_DIRECTORY;
	}

	public static String  workDirectory(){

		// Create directory if not exists
		_wDirectory = "."+File.separator+"censusacsserver-work";
		File dir = new File(_wDirectory);

		// if the directory does not exist, create it
		if (!dir.exists()) {
			System.out.println("creating directory: " + _sDirectory);
			dir.mkdir();  

		}

		return dir.getAbsolutePath();

	}

	public static void addWebAppHandler(Server aServer){

		try {

			// assumes that this directory contains .html and .jsp files
			// This is just a directory within your source tree, and can be exported as part of your normal .jar
			final String WEBAPPDIR = "/servlet";


			// for localhost:port/admin/index.html and whatever else is in the webapp directory
			final URL warUrl = Thread.currentThread().getClass().getResource(WEBAPPDIR);
			LOGGER.debug("Defined class loader :"+warUrl);
			//final URL warUrl = aLoader.getParent().getResource(WEBAPPDIR);
			//final String warUrlString = warUrl.toExternalForm();


			// Changing base directory to prevent tmp folder cleaning
			WebAppContext aContext = new WebAppContext(warUrl.toString(), CONTEXTPATH);
			aContext.setParentLoaderPriority(true);
			aContext.setAttribute("org.eclipse.jetty.webapp.basetempdir", _sDirectory+File.separator);

			// Here we define fake servlet for static file configuration
			ServletContextHandler ctx = new ServletContextHandler();
			ctx.setContextPath("/");

			DefaultServlet defaultServlet = new DefaultServlet();
			ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
			holderPwd.setInitParameter("resourceBase",  workDirectory());

			ctx.addServlet(holderPwd, "/*");
			
			
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			contexts.setHandlers(new Handler[] {aContext, ctx});

			
			aServer.setHandler(contexts);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Server buildServer(int iPort){


		// === jetty.xml ===
		// Setup Threadpool
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(500);

		// Server
		Server server = new Server(threadPool);

		// HTTP Configuration
		HttpConfiguration http_config = new HttpConfiguration();
		http_config.setSecureScheme("https");
		http_config.setSecurePort(iPort+1);
		http_config.setOutputBufferSize(32768);
		http_config.setRequestHeaderSize(8192);
		http_config.setResponseHeaderSize(8192);
		http_config.setSendServerVersion(true);
		http_config.setSendDateHeader(false);
		// httpConfig.addCustomizer(new ForwardedRequestCustomizer());


		// === jetty-http.xml ===
		//ServerConnector http = new ServerConnector(server,new HttpConnectionFactory(http_config));
		//http.setPort(iPort);
		//http.setIdleTimeout(30000);
		//server.addConnector(http);

		// This webapp will use jsps and jstl. We need to enable the
		// AnnotationConfiguration in order to correctly
		// set up the jsp container
		Configuration.ClassList classlist = Configuration.ClassList
				.setServerDefault( server );
		classlist.addBefore(
				"org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
				"org.eclipse.jetty.annotations.AnnotationConfiguration" );


		String jetty_home = System.getProperty("jetty.home",_sDirectory);
		System.setProperty("jetty.home",jetty_home);

		// === jetty-https.xml ===

		// here we try to retrieve the keystore
		InputStream aStream = server.getClass().getResourceAsStream("/servlet/WEB-INF/classes/keystore");
		String aFileKeyStore = _sDirectory+File.separator+"keystore";
		try {
			ByteArrayOutputStream aOStream = new ByteArrayOutputStream();
			IOUtils.copy(aStream, aOStream);
			//System.out.println("Creating file:"+aFileKeyStore);
			FileUtils.writeByteArrayToFile( new File(aFileKeyStore), aOStream.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		// SSL Context Factory
		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(jetty_home + File.separator+ "keystore");
		sslContextFactory.setKeyStorePassword("codeforboston01");
		sslContextFactory.setKeyManagerPassword("codeforboston01");
		sslContextFactory.setTrustStorePath(jetty_home + File.separator +"keystore");
		sslContextFactory.setTrustStorePassword("codeforboston01");
		sslContextFactory.setExcludeCipherSuites(
				"SSL_RSA_WITH_DES_CBC_SHA",
				"SSL_DHE_RSA_WITH_DES_CBC_SHA",
				"SSL_DHE_DSS_WITH_DES_CBC_SHA",
				"SSL_RSA_EXPORT_WITH_RC4_40_MD5",
				"SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
				"SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
				"SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

		// SSL HTTP Configuration
		HttpConfiguration https_config = new HttpConfiguration(http_config);
		https_config.addCustomizer(new SecureRequestCustomizer());

		// SSL Connector
		ServerConnector sslConnector = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory,"http/1.1"),
				new HttpConnectionFactory(https_config));
		sslConnector.setPort(iPort);
		server.addConnector(sslConnector);

		return server;

	}

	public static Options getOptions(){
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption("d", true, "data folder");

		return options;
	}

	public static void main(String[] args) {

		org.apache.log4j.BasicConfigurator.configure();

		LogManager.getRootLogger().setLevel((Level)org.apache.log4j.Level.INFO);

		//org.eclipse.jetty.util.log.Log.getRootLogger().setDebugEnabled(true);

		System.setProperty("jsse.enableSNIExtension", "false");

		System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");

		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse( getOptions(), args);

			if (cmd.hasOption("d")) {
				DATA_DIRECTORY = cmd.getOptionValue("d");
			} else {
				LOGGER.error("Now data map directory defined ...");
				System.exit(1)	;
			}

			try {

				int httpServerPort = 10110;

				buildDirectory();

				Server server = buildServer(httpServerPort);

				addWebAppHandler(server);

				server.start();

				// Waiting the server to start
				Thread.sleep(1000);

				if(Desktop.isDesktopSupported())
				{
					Desktop.getDesktop().browse(new URI("https://localhost:"+httpServerPort+CONTEXTPATH)); // Now using https connection
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}


	}


}
