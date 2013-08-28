package yabby.app.util;
//package webs;
import java.io.*;
import java.net.*;

import yabby.app.Yabby;

/**
 * 
 * Title: A simple Webserver Tutorial NO warranty, NO guarantee, MAY DO damage
 * to FILES, SOFTWARE, HARDWARE!! Description: This is a simple tutorial on
 * making a webserver posted on http://turtlemeat.com . Go there to read the
 * tutorial! This program and sourcecode is free for all, and you can copy and
 * modify it as you like, but you should give credit and maybe a link to
 * turtlemeat.com, you know R-E-S-P-E-C-T. You gotta respect the work that has
 * been put down.
 * 
 * Copyright: Copyright (c) 2002 Company: TurtleMeat
 * 
 * @author: Jon Berg <jon.berg[on_server]turtlemeat.com
 * @version 1.0
 */

// file: server.java
// the real (http) serverclass
// it extends thread so the server is run in a different
// thread than the gui, that is to make it responsive.
// it's really just a macho coding thing.
public class HTTPServer extends Thread {
	private int port; // port we are going to listen to
	
	HTTPRequestHandler handler = new Yabby();
	public void setRequestHandler(HTTPRequestHandler handler) {
		this.handler = handler;
	}

	// the constructor method
	// the parameters it takes is what port to bind to, the default tcp port
	// for a httpserver is port 6789
	public HTTPServer(int listen_port) {
		port = listen_port;

		// this makes a new thread, as mentioned before,it's to keep gui in
		// one thread, server in another. You may argue that this is totally
		// unnecessary, but we are gonna have this on the web so it needs to
		// be a bit macho! Another thing is that real pro webservers handles
		// each request in a new thread. This server dosen't, it handles each
		// request one after another in the same thread. This can be a good
		// assignment!! To redo this code so that each request to the server
		// is handled in its own thread. The way it is now it blocks while
		// one client access the server, ex if it transferres a big file the
		// client have to wait real long before it gets any response.
		this.start();
	}

	private void log(String s2) { 
		System.err.println(s2);
	}


	// this is a overridden method from the Thread class we extended from
	@SuppressWarnings("resource")
	public void run() {
		// we are now inside our own thread separated from the gui.
		ServerSocket serversocket = null;
		// To easily pick up lots of girls, change this to your name!!!
		log("The simple httpserver v. 0000000000\nCoded by Jon Berg" + "<jon.berg[on server]turtlemeat.com>\n");
		// Pay attention, this is where things starts to cook!
		try {
			// print/send message to the guiwindow
			log("Trying to bind to localhost on port " + Integer.toString(port) + "...");
			// make a ServerSocket and bind it to given port,
			serversocket = new ServerSocket(port);
		} catch (Exception e) { // catch any errors and print errors to gui
			log("Fatal Error:" + e.getMessage());
			return;
		}
		log("OK!");
		// go in a infinite loop, wait for connections, process request, send
		// response
		while (true) {
			log("Ready, Waiting for requests...");
			try {
				// this call waits/blocks until someone connects to the port we
				// are listening to
				Socket connectionsocket = serversocket.accept();
				// figure out what ipaddress the client commes from, just for
				// show!
				InetAddress client = connectionsocket.getInetAddress();
				// and print it to gui
				log(client.getHostName() + " connected to server.");
				// Read the http request from the client from the socket
				// interface
				// into a buffer.
				BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
				// Prepare a outputstream from us to the client,
				// this will be used sending back our response
				// (header + requested file) to the client.
				DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());

				// as the name suggest this method handles the http request, see
				// further down.
				// abstraction rules
				http_handler(input, output);
			} catch (Exception e) { // catch any errors, and print them
				log("\nError:" + e.getMessage());
			}

		} // go back in loop, wait for next request
	}

	// our implementation of the hypertext transfer protocol
	// its very basic and stripped down
	private void http_handler(BufferedReader input, DataOutputStream output) {
		int method = 0; // 1 get, 2 head, 0 not supported
		//String http = new String(); // a bunch of strings to hold
		String path = new String(); // the various things, what http v, what
									// path,
		//String file = new String(); // what file
		//String user_agent = new String(); // what user_agent
		try {
			// These are the types of request we can handle
			// GET /index.html HTTP/1.0
			// POST /index.html HTTP/1.0
			// HEAD /index.html HTTP/1.0
			String tmp = input.readLine(); // read from the stream
			String tmp2 = new String(tmp);
			tmp.toUpperCase(); // convert it to uppercase
			if (tmp.startsWith("GET")) { // compare it is it GET
				method = 1;
			} // if we set it to method 1
			if (tmp.startsWith("POST")) { // compare it is it POST
				method = 3;
				
			    System.out.println("POST request");
			    
				do {
					String currentLine = input.readLine();
					System.err.println(currentLine);
					//if (currentLine.indexOf("Content-Type: multipart/form-data") != -1) {
					if (currentLine.length() == 0) {
						currentLine = input.readLine();
					//if (currentLine.indexOf("----------------------------")  != -1) {
						String boundary = currentLine;//currentLine.split("boundary=")[1];
						// The POST boundary
						String filename = input.readLine().split("filename=")[1].replaceAll("\"", "");
	
						String fileContentType = input.readLine().split(" ")[1];
						System.out.println("File content type = " + fileContentType);
	
						input.readLine(); // assert(inFromClient.readLine().equals(""))
													// :
													// "Expected line in POST request is "" ";
	
						// fout = new PrintWriter(filename);
						String prevLine = input.readLine();
						currentLine = input.readLine();
	
						// Here we upload the actual file contents
						while (true) {
							if (currentLine.equals(boundary + "--")) {
								System.out.print(prevLine);
								break;
							} else {
								System.out.println(prevLine);
							}
							prevLine = currentLine;
							currentLine = input.readLine();
						}
	
						sendResponse(200, "File " + filename + " Uploaded..", false, output);
						return;
						// fout.close();
					} // if
				} while (input.ready()); // End of do-while
			    				      
				sendResponse(200, "Unknown file  Uploaded..", false, output);
			} // if we set it to method 1
			if (tmp.startsWith("HEAD")) { // same here is it HEAD
				method = 2;
			} // set method to 2

			if (method == 0) { // not supported
				try {
					output.writeBytes(construct_http_header(501, null));
					output.close();
					return;
				} catch (Exception e3) { // if some error happened catch it
					log("\n\nerror: " + e3.getMessage());
				} // and display error
			}
			// }

			// tmp contains "GET /index.html HTTP/1.0 ......."
			// find first space
			// find next space
			// copy whats between minus slash, then you get "index.html"
			// it's a bit of dirty code, but bear with me...
			int start = 0;
			int end = 0;
			for (int a = 0; a < tmp2.length(); a++) {
				if (tmp2.charAt(a) == ' ' && start != 0) {
					end = a;
					break;
				}
				if (tmp2.charAt(a) == ' ' && start == 0) {
					start = a;
				}
			}
			path = tmp2.substring(start + 2, end); // fill in the path
			
			// test that request came from local machine
			boolean isLocalClient = false;
			while (input.ready()) {
				tmp = input.readLine(); // read from the stream
				//System.out.println(tmp);
				if (tmp.equals("Host: localhost:" + port)) {
					isLocalClient = true;
				}
			}

			if (!isLocalClient) {
				path = "";
				throw new Exception(" Will not serve non-local clients");
			}
		} catch (Exception e) {
			log("error: " + e.getMessage());
		} // catch any exception

		// path do now have the filename to what to the file it wants to open
		log("Client requested:" + new File(path).getAbsolutePath() + " " + path );
		FileInputStream requestedfilex = null;
		InputStream in;

		try {
			// NOTE that there are several security consideration when passing
			// the untrusted string "path" to FileInputStream.
			// You can access all files the current user has read access to!!!
			// current user is the user running the javaprogram.
			// you can do this by passing "../" in the url or specify absoulute
			// path
			// or change drive (win)

			// try to open the file,
			//requestedfile = new FileInputStream(path);
			in = getClass().getResourceAsStream(path);
			if (in == null) {
				in = getClass().getResourceAsStream("../../../"+path);
				if (in == null) {
				
					if (handler != null) {
						String result = handler.handleRequest(path, null);
						output.writeBytes(result);
					}
				
//				String cmd = (path.contains("?") ? path.substring(0, path.indexOf('?')) : path);
//				String [] vars = path.substring(cmd.length() + 1).split("&");
//				
//				String str = vars[1].replaceAll("%20", " ");
//				JSONObject o = new JSONObject(str);
//				System.out.println(o.get("msg"));
//				
//				output.writeBytes(construct_http_header(200, "application/json"));
//				if (vars[0].equals("number=123")) {
//					output.writeBytes("{\"name\":\"Johny\"}");
//				} else if (vars[0].equals("number=124")) {
//					output.writeBytes("{\"name\":\"Dave\"}");
//				} else {
//					output.writeBytes("{\"name\":\"Dunno\"}");
//				}
					output.close();
					return;
				}
//				output.writeBytes(construct_http_header(200, "application/json"));
//				output.writeBytes("{\"name\":\"John\"}");
//				output.close();
//				return;
			}
		} catch (Exception e) {
			try {
				// if you could not open the file send a 404
				output.writeBytes(construct_http_header(404, null));
				// close the stream
				output.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				log("error 1: " + e2.getMessage());
			}
			log("error 2: " + e.getMessage());
			return;
		} // print error to gui

		// happy day scenario
		try {
			String type_is = "text/html";
			// find out what the filename ends with,
			// so you can construct a the right content type
			if (path.endsWith(".zip")) {
				type_is = "application/x-zip-compressed";
			}
			if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
				type_is = "image/jpeg";
			}
			if (path.endsWith(".gif")) {
				type_is = "image/gif";
			}
			if (path.endsWith(".png")) {
				type_is = "image/png";
			}
			if (path.endsWith(".ico")) {
				type_is = "image/x-icon";
			}
			if (path.endsWith(".css")) {
				type_is = "text/css";
			}
			if (path.endsWith(".js")) {
				type_is = "text/javascript";
			}
			
			// write out the header, 200 ->everything is ok we are all happy.
			output.writeBytes(construct_http_header(200, type_is));

			// if it was a HEAD request, we don't print any BODY
			if (method == 1) { // 1 is GET 2 is head and skips the body
				byte[] buffer = new byte[1024];
				while (true) {
					// read the file from filestream, and print out through the
					// client-outputstream on a byte per byte base.
					//int b = requestedfile.read(buffer, 0, 1024);
					int b = in.read(buffer, 0, 1024);
					if (b == -1) {
						break; // end of file
					}
					output.write(buffer, 0, b);
				}
				// clean up the files, close open handles

			}
			output.close();
			in.close();
			//requestedfile.close();
		}

		catch (Exception e) {
		}

	}

	// this method makes the HTTP header for the response
	// the headers job is to tell the browser the result of the request
	// among if it was successful or not.
	private String construct_http_header(int return_code, String file_type) {
		String s = "HTTP/1.0 ";
		// you probably have seen these if you have been surfing the web a while
		switch (return_code) {
		case 200:
			s = s + "200 OK";
			break;
		case 400:
			s = s + "400 Bad Request";
			break;
		case 403:
			s = s + "403 Forbidden";
			break;
		case 404:
			s = s + "404 Not Found";
			break;
		case 500:
			s = s + "500 Internal Server Error";
			break;
		case 501:
			s = s + "501 Not Implemented";
			break;
		}

		s = s + "\r\n"; // other header fields,
		s = s + "Connection: close\r\n"; // we can't handle persistent
											// connections
		s = s + "Server: HTTPServer v0\r\n"; // server name

		// Construct the right Content-Type for the header.
		// This is so the browser knows what to do with the
		// file, you may know the browser dosen't look on the file
		// extension, it is the servers job to let the browser know
		// what kind of file is being transmitted. You may have experienced
		// if the server is miss configured it may result in
		// pictures displayed as text!
		if (file_type != null) {
			s = s + "Content-Type: " + file_type + "\r\n";
		}

		s = s + "\r\n"; // this marks the end of the httpheader
		// and the start of the body
		// ok return our newly created header!
		return s;
	}
	
	public void sendResponse (int statusCode, String responseString, boolean isFile, DataOutputStream outToClient) throws Exception {
		
		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String fileName = null;		
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;
		
		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";	
			
//		if (isFile) {
//			fileName = responseString;			
//			fin = new FileInputStream(fileName);
//			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
//			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
//				contentTypeLine = "Content-Type: \r\n";	
//		}						
//		else {
			responseString = HTTPPostServer.HTML_START + responseString + HTTPPostServer.HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";	
//		}			
		 
		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");		
		
		//if (isFile) sendFile(fin, outToClient);
		//else 
		outToClient.writeBytes(responseString);
		
		outToClient.close();
	}

	
	public static void main(String[] args) {
		
	    new HTTPServer(6789);
	}

} // class phhew caffeine yes please!