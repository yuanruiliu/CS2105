
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

class FTPServer {
	private static final String SERVER_DIR_IS_EMPTY = "---the server directory is empty---";
	private static final String  UNKNOWN_COMMAND = "500 UNKNOWN COMMAND";
	private static final String INVALID_ARGUMENTS = "501 INVALID ARGUMENTS";
	private static final String DIR_COMMAND_OK = "200 DIR COMMAND OK";
	private static final String OK = "200 OK";
	private static final String PORT_SERVER_IP = "200 PORT 127.0.0.1 ";
	private static final String FILE_NOT_FOUND = "401 FILE NOT FOUND";
	private static final String GET_COMMAND_OK = "200 GET COMMAND OK";
	private static final String PUT_COMMAND_OK = "200 PUT COMMAND OK";


	public static void main(String[] args) throws Exception{
	
	int ctrlPort =-1;
	int dataPort = -1;

	if (args.length == 1){
		try{
			ctrlPort = Integer.parseInt(args[0]);
			dataPort = ctrlPort + 1;
		}
		catch(NumberFormatException e) {
			System.out.println("Invalid port number.");
		}		
			
	}
	else if (args.length == 2){
		try{
			ctrlPort = Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {
			System.out.println("Invalid control port number.");
		}

		try{
			dataPort = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException e) {
			System.out.println("Invalid data port number.");
		}
	}
	else{
		System.out.println("Invalid Input");
	}
	
	String clientInput;
	
	ServerSocket socket = new ServerSocket(ctrlPort);

	while (true){

		Socket ctrlSocket = socket.accept();

		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(ctrlSocket.getInputStream() ));

		DataOutputStream outToClient = new DataOutputStream(ctrlSocket.getOutputStream() );

		clientInput = inFromClient.readLine();
		
		String serverOutput = "";

		if (clientInput.equals("PASV") ){
			serverOutput = PORT_SERVER_IP + dataPort + "\r\n"; 
			outToClient.writeBytes(serverOutput);
		}

		clientInput = inFromClient.readLine();
		String[] InputPara =  clientInput.split(" ");
		String cmd = InputPara[0];
		
		//=========Listing (DIR) the Server Directory========
		
		if ( cmd.equals("DIR") ){
			
			if(InputPara.length != 1){
				serverOutput = INVALID_ARGUMENTS +"\r\n";
			}
			else if (InputPara.length == 1){
				serverOutput = DIR_COMMAND_OK+"\r\n";
				outToClient.writeBytes(serverOutput);
				
				//call func getDirList()
				getDirList(dataPort);

				serverOutput = OK+ "\r\n";		
			}
		}
		
		//=========GET a File from the Server========
		
		else if ( cmd.equals("GET") ){
			
			if (InputPara.length != 2){
				serverOutput = INVALID_ARGUMENTS+"\r\n";
			}
			else if (InputPara.length == 2){
	
				String currDir = System.getProperty("user.dir");
				String svrDir = currDir + "/server-directory/";
				String fullFilePath = svrDir + InputPara[1];		
					
				if (!fileExists(fullFilePath)){
					serverOutput = FILE_NOT_FOUND+"\r\n";		
				}
				
				else{
					serverOutput = GET_COMMAND_OK +"\r\n";
					outToClient.writeBytes(serverOutput);
					
					File dirToFile = new File(fullFilePath);	
					
					ServerSocket dataSocket = new ServerSocket(dataPort);

					boolean openSocket = true;
					while (openSocket == true) {
						Socket connectionSocket = dataSocket.accept();
						byte[] bytes = new byte[ (int) dirToFile.length()];

						BufferedInputStream bufferReader = new BufferedInputStream(new FileInputStream(dirToFile));

						bufferReader.read(bytes, 0, bytes.length);
						
						
						DataOutputStream writer = new DataOutputStream(connectionSocket.getOutputStream());
						writer.write(bytes, 0, bytes.length);
						
						bufferReader.close();
						writer.close();
						openSocket = false;
						connectionSocket.close();
					}
					dataSocket.close();
					serverOutput = OK+"\r\n";
				}
			}
		}
		
		//=========PUT a File to the Server========
		
		else if ( cmd.equals("PUT") ){
			
			//• client − > PUT <file path> [<the directory to put the file>] − > server
			
			if( (InputPara.length < 2) || (InputPara.length > 3) ){
				serverOutput = INVALID_ARGUMENTS +"\r\n";
			}
			else if ( (InputPara.length == 2) || (InputPara.length == 3) ){
				serverOutput = PUT_COMMAND_OK+"\r\n";
				outToClient.writeBytes(serverOutput);
				
				String currDir = System.getProperty("user.dir");
				String svrDir = currDir + "/server-directory/";
				
				String[] clientFilePathArr = InputPara[1].split("/");
				String clientFileName = clientFilePathArr[clientFilePathArr.length - 1];
		
				String destiDir = "";
				if (InputPara.length == 2){
					destiDir = svrDir; 
				}

				else if (InputPara.length == 3){
						
					destiDir = svrDir + InputPara[2];

					File createDir = new File(destiDir);
					
					if(!createDir.exists() ){
						createDir.mkdirs();
					}
				}
				ServerSocket dataSocket = new ServerSocket(dataPort);
				
				if ( ( (destiDir.substring(destiDir.length()-1)).equals("/") ) == false){
					destiDir = destiDir + "/";
				}
				boolean openSocket = true;
				while (openSocket == true) {
					Socket connectionSocket = dataSocket.accept();

					BufferedInputStream bufferReader = new BufferedInputStream(connectionSocket.getInputStream() );

					byte[] buffer = new byte[1000];

					int len;

					while (( len = bufferReader.read(buffer)) > 0){

						new FileOutputStream(destiDir + clientFileName).write(buffer, 0, len);

					}
					
					bufferReader.close();
					openSocket = false;
					connectionSocket.close();

				}
				
				dataSocket.close();
				
				serverOutput = OK+"\r\n";
			}
		}else{
			serverOutput = UNKNOWN_COMMAND+"\r\n";
		}
		outToClient.writeBytes(serverOutput);
        outToClient.close();
        ctrlSocket.close();
		}	
	}

	public static void getDirList (int dataPort) throws Exception{
	
		ArrayList<String> dirList = new ArrayList<String>();
	
		String currDir = System.getProperty("user.dir");
		String svrDir = currDir + "/server-directory/";
		File dir = new File(svrDir);	
		String outputMsg = "";	
	
		ServerSocket dataSocket = new ServerSocket(dataPort);
	
		File[] items = dir.listFiles();
			if( items.length == 0){
				outputMsg = SERVER_DIR_IS_EMPTY+"\n";
			}
			else{	
				dirList = listDir(dir, dirList);
				outputMsg = sort(dirList);	
			}	
	
			boolean openSocket = true;
			while (openSocket == true){
				Socket connectionSocket = dataSocket.accept();

				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
				outToClient.writeBytes(outputMsg);
			
				outToClient.close();
				openSocket = false;
			}
			dataSocket.close();
	}

	public static ArrayList<String> listDir(File dir, ArrayList<String> dirList ){

		if (dir.isDirectory()){
			File[] items = dir.listFiles();
			if (items.length == 0){
				dirList.add(dir.getName() + "/");
			}
			for (File item:items) {
				if(item.isDirectory() == false){
					String itemAbsPath = item.getAbsolutePath();
					String[] itemName = itemAbsPath.split("server-directory/");
					dirList.add(itemName[1]);
				}
				if (item.isDirectory() ){
					dirList = (listDir(item, dirList)); //making use of recursion
				}
			}
		}

		return dirList;
	}

	public static String sort(ArrayList<String> dirList){
		
		Collections.sort(dirList, String.CASE_INSENSITIVE_ORDER);
		
		for (int i = 0 ; i < (dirList.size()-1); i++){
			
			dirList.set(i, dirList.get(i) + "\n");
		}

		String output = "";
		for (String temp: dirList){
			System.out.println(temp);
			output += temp;
		}	
		return output;
	}

	public static boolean fileExists(String fullFilePath) {
		boolean fileExists = false;

		String[] fullFilePathArr = fullFilePath.split("/");
		String fileName = fullFilePathArr[fullFilePathArr.length -1];

		String dirPath = "";
		for (int i = 0; i < (fullFilePathArr.length-1); i++){
			dirPath = dirPath + fullFilePathArr[i] + "/";
		}

		File dir = new File(dirPath);		 
		if(dir.isDirectory()) {
			fileExists = new File(dir, fileName).exists();
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