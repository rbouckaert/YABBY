package yabby.app.yabbyapp;


import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import beagle.BeagleFlag;
import beagle.BeagleInfo;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import yabby.app.BeastMCMC;
import yabby.app.util.HTMLPrintStream;
import yabby.app.util.HTTPRequestHandler;
import yabby.core.util.Log;
import yabby.util.XMLParserException;

public class YabbyDialog extends Application implements HTTPRequestHandler {
	final static String YABBY_IS_DONE = "YABBY_is_done";

    private static WebEngine webEngine;

    public YabbyDialog() {
    	if (YabbyMain.server != null) {
    		YabbyMain.server.setRequestHandler(this);
    	}
    }

	@Override
	public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hello YABBY!");
        Button btn = new Button();
        btn.setText("Say 'Hello YABBY'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello YABBY!");
            }
        });
        
        WebView browser = new WebView();
        webEngine = browser.getEngine();
//        browser.setScaleX(1.1);
//        browser.setScaleY(1.1);
        
        //webEngine.load("http://localhost:6789/yabby.html");
        webEngine.load("http://localhost:5000/yabby.html");
        //webEngine.load("http://localhost:5000/");
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        root.getChildren().add(browser);
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();
        
        //JSObject jsobj = (JSObject) webEngine.executeScript("window");
        //jsobj.setMember("java", new YabbyHandler());
	}

	
	@Override
	public String handleRequest(String url, StringBuffer data) throws IOException {
        OutputStream stream;
        if (YabbyMain.stream != null) {
        	stream = YabbyMain.stream; 
        } else {
	        stream = new OutputStream()
	        {
	            private StringBuilder string = new StringBuilder();
	            @Override
	            public void write(int b) throws IOException {
	                this.string.append((char) b );
	            }
	
	            //Netbeans IDE automatically overrides this toString()
	            public String toString(){
	            	String str = this.string.toString();
	            	string.delete(0, string.length());
	                return  str;
	            }
	        };
	        
            Log.trace = new HTMLPrintStream(stream, Log.Level.trace);
            Log.debug = new HTMLPrintStream(stream, Log.Level.debug);
            Log.info = new HTMLPrintStream(stream, Log.Level.info);
            Log.warning = new HTMLPrintStream(stream, Log.Level.warning);
            Log.err = new HTMLPrintStream(stream, Log.Level.error);
            System.setOut(Log.info);
            Log.setLevel(Log.Level.info);
	        
	        //System.setOut(new HTMLPrintStream(stream, Log.Level.info));
	        System.setErr(Log.warning);
	        YabbyMain.stream = stream;
        }
		HTMLPrintStream.currentLevel = null;
		
		if (url.startsWith("quitYabby")) {
			System.err.println("Quiting now");
			System.exit(0);
		} else if (url.startsWith("showBeagleInfo")) {
			BeagleInfo.printResourceList();
		} else if (url.startsWith("poll")) {
			try {
				Thread.sleep(300);
			} catch (Exception e) {
				// ignore
			}
			String output = stream.toString();
			stream.flush();
			HTMLPrintStream.currentLevel = null;
			return output;
		} else if (url.startsWith("runYabby")) {
			
			
			//String argString = url.split("?")[1];
			
			try {
				String filename = "/tmp/yabby.xml";
				FileWriter outfile = new FileWriter(filename);
				outfile.write(data.toString());
				outfile.close();
				
		        final List<String> MCMCargs = processRequest(url);
		        MCMCargs.add(filename);
				
				final BeastMCMC main = new BeastMCMC();
				main.parseArgs(MCMCargs.toArray(new String[]{}));
				new Thread() {
					public void run() {
						try {
							main.run();
						} catch (Exception e) {
							Log.err.println(e.getMessage());
							e.printStackTrace(Log.debug);
						}
						System.out.println(YABBY_IS_DONE);
					};
				}.start();
				return "MCMC started";
	        } catch (XMLParserException e) {
	            Log.err.println(e.getMessage());
				Log.info.println(YABBY_IS_DONE);
	            //e.printStackTrace();
			} catch (Exception e) {
				Log.err.println("Could not start MCMC: " + e.getMessage());
				Log.info.println(YABBY_IS_DONE);
				return "";
			}
			
		}
		
		String output = stream.toString();
		stream.flush();
		HTMLPrintStream.currentLevel = null;
		return output;
	}

	
	private List<String> processRequest(String url) {
		List<String> args = new ArrayList<String>();
		int i = url.indexOf("?");
		url = url.substring(i + 1);
		url = url.replaceAll("%22", "\"");
		JSONObject json = new JSONObject(url);
		
		String logFileMode = json.getString("mode");
		switch (logFileMode) {
			case "batch":
				args.add("-batch");
				break;
			case "overwrite":
				args.add("-overwrite");
				break;
			case "resume":
				args.add("-resume");
				break;
		}
		
		String logLevel = json.getString("logLevel");
		switch (logLevel) {
		case "info":
			Log.setLevel(Log.Level.info);
			break;
		case "debug":
			Log.setLevel(Log.Level.debug);
			break;
		case "trace":
			Log.setLevel(Log.Level.trace);
			break;
		}
		
		String seed = json.getString("seed");
		args.add("-seed");
		args.add(seed);

		String threads = json.getString("threads");
		args.add("-threads");
		args.add(threads);
		
		if (json.has("useBeagleChecked")) {
	        long beagleFlags = 0;

	        String beagleMode= json.getString("beagleMode");
			switch (beagleMode) {
			case "CPU":
	            beagleFlags |= BeagleFlag.PROCESSOR_CPU.getMask();
				break;
			case "SSE":
	            beagleFlags |= BeagleFlag.PROCESSOR_CPU.getMask();
	            beagleFlags |= BeagleFlag.VECTOR_SSE.getMask();
				break;
			case "GPU":
	            beagleFlags |= BeagleFlag.PROCESSOR_GPU.getMask();
				break;
			}
			
			String beaglePrecision = json.getString("beaglePrecision");
			switch (beaglePrecision) {
			case "double":
	            beagleFlags |= BeagleFlag.PRECISION_DOUBLE.getMask();
				break;
			case "single":
	            beagleFlags |= BeagleFlag.PRECISION_SINGLE.getMask();
				break;
			}
			
			String beagleScaling = json.getString("beagleScaling");
            System.setProperty("beagle.scaling", beagleScaling);

            System.setProperty("beagle.preferred.flags", Long.toString(beagleFlags));		
            System.setProperty("java.only", "false");
		} else {
            System.setProperty("java.only", "true");
		}
		return args;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
