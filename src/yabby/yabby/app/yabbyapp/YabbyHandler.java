package yabby.app.yabbyapp;

import yabby.app.util.HTMLPrintStream;
import beagle.BeagleInfo;

public class YabbyHandler {
	public void showBeagleInfo() {
		System.err.println("showBeagleInfo");				
			BeagleInfo.printResourceList();
			String output = YabbyMain.stream.toString();
			//stream.flush();
			HTMLPrintStream.currentLevel = null;
			//return output;
	}		
}
