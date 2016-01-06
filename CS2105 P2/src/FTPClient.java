
import java.io.*;
import java.net.*;

class FTPClient {
	public static void main(String[] args) throws Exception {

		String currDir = System.getProperty("user.dir");
		String clientDir = currDir + "/client-directory/";  
		
		File logFile = new File(currDir + "/log");

		if (!logFile.exists()){
			logFile.createNewFile();
		}
		
		FileWriter fw = new FileWriter(logFile, false);
		BufferedWriter writeToLog = new BufferedWriter(fw);
		
		/*
		 * The client take input:
		 *	• 3 parameters, FTPClient < server-ip > < server-port > DIR
		 *	• 4 parameters, FTPClient < server-ip > < server-port > GET < path of file to get >
		 *	• 5 parameters, FTPClient < server-ip > < server-port > PUT < path of file to put in client > [< path to put in server >]
		 * 
		 */
		
		String serverIp = args[0];
		int svrPort = Integer.parseInt(args[1]);
		String cmd = null;
		// ==============DIR Command============
		if ( (args[2].compareTo("DIR")) == 00 )
			cmd = args[2] + " \r\n";	
			
		// ==============GET Command============
		
		String getFileName = null;
		String getFilePath = null;
 
		if ( (args[2].compareTo("GET")) == 00 ){
			getFilePath = args[3];
			String[] fullFilePathGetArr = getFilePath.split("/");
			getFileName = fullFilePathGetArr[fullFilePathGetArr.length-1];
			cmd = args[2] + " " + args[3] + " \r\n"; 
		}
		
		// ==============PUT Command============
		String putFileName = null;
		String putFilePath = null;
		if ( ( args[2].compareTo("PUT")) == 00  ){
				
			putFileName = args[3];
				
			putFilePath = clientDir + putFileName;
	
			boolean fileExistsResult = false;
				
			fileExistsResult = fileExists(putFileName);
			if ( fileExistsResult == false ){
				writeToLog.write("FILE NOT FOUND");
				System.out.println("FILE NOT FOUND");
				writeToLog.close();
				System.exit(0);
			} 
			if (args.length == 4){
				cmd = args[2] + " " + args[3] + " \r\n"; 
			}else{
				cmd = args[2] + " " + args[3] + " " + args[4] + "\r\n";
			}
		}	
		Socket clientSocket = new Socket(serverIp, svrPort);
		
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream() ));		
		
		String pasvCmd = "PASV\r\n";
		// • client − > PASV − > server
		outToServer.writeBytes(pasvCmd);
		// • server − > 200 PORT <SERVER IP ADDRESS> <DATA PORT> − > client	
		String svrOutput = inFromServer.readLine();
		System.out.println("From SERVER: " + svrOutput);
		//writeToLog.write(svrOutput);
		
		String[] svrOutPts = svrOutput.split(" ");
		int dataPort = Integer.parseInt(svrOutPts[3]);	
		// • client − > DIR/GET/PUT− > server	
		outToServer.writeBytes(cmd);
		
		svrOutput = inFromServer.readLine();
		System.out.println(svrOutput);
		
		svrOutPts = svrOutput.split(" ");
		// server − > 200 DIR/GET/PUT COMMAND OK − > client
		if (svrOutPts.length == 4 && svrOutPts[3].equals("OK")){ 

				//writeToLog.write(svrOutput);
				//Connect to the data port
				Socket dataSocket = new Socket(serverIp, dataPort);
				
				//==============DIR Command============
				if (svrOutPts[1].equals("DIR") ){
			
					BufferedInputStream reader = new BufferedInputStream(dataSocket.getInputStream()); 
					byte[] buffer = new byte[1000];
					int len;

					while (( len = reader.read(buffer)) >0 ){
						new FileOutputStream(clientDir + "directory_listing").write(buffer, 0, len);
					}
					dataSocket.close();
				
				//==============GET Command============
				}else if (svrOutPts[1].equals("GET") ){
					
					System.out.println(getFileName);	
					
					BufferedInputStream reader = new BufferedInputStream(dataSocket.getInputStream());

					int inputLine;
					FileWriter fileWriter = new FileWriter(clientDir + getFileName);
					BufferedWriter writer = new BufferedWriter(fileWriter);
		 
					while ((inputLine = reader.read()) != -1) {
						writer.write((char)(inputLine));
					}
					writer.close();
					reader.close();
					dataSocket.close();

				//==============PUT Command============
				}else if (svrOutPts[1].equals("PUT") ){
					
					File file = new File(putFilePath);
					
					byte[] bytes = new byte[ (int)file.length() ];

					BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));

					reader.read(bytes, 0, bytes.length);

					DataOutputStream writer = new DataOutputStream(dataSocket.getOutputStream());
					writer.write(bytes, 0, bytes.length);

					reader.close();
					writer.close();
					dataSocket.close();
				}
				// • server−>200 OK−>client
				svrOutput = inFromServer.readLine();
				System.out.println(svrOutput);
				writeToLog.write(svrOutput);
			
			
		}else{
			//∗ server − > 500 UNKNOWN COMMAND − > client
			//∗ server − > 501 INVALID ARGUMENTS − > client
			//* ...
			writeToLog.write(svrOutput);
		}	
		writeToLog.close();	
		clientSocket.close();
	}


	public static Boolean fileExists(String path){
		boolean fileExists = false;
		String file = null;	
		String currDir = System.getProperty("user.dir");
		String svrDir = currDir + "/client-directory/";

		String[] fileNamePathArr = path.split("/");
		
		if(fileNamePathArr.length == 1){

			file = fileNamePathArr[0];

		}
		
		else if (fileNamePathArr.length >1){
			
			for (int i = 0; i<(fileNamePathArr.length-1); i++){
				
				svrDir = svrDir + fileNamePathArr[i] + "/";	
			}		
			file = fileNamePathArr[fileNamePathArr.length - 1]; 
		} 
		
		File dir = new File(svrDir);
		
		if(dir.isDirectory()){
			
			fileExists = new File(dir, file).exists();

			if (fileExists){
				
				fileExists = true;
				return fileExists;
			}
			else{
				return fileExists;
			}
		}
		return fileExists;
	} 

}
