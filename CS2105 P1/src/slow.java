import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

class DLURL {

        public static void main(String[] args) {

                // Check for valid command syntax
                if (args.length != 2) {
                        System.out.println("Arg: HostName Filename");
                        System.exit(1);
                }
                
                // Configure URL address syntax
                String urlString = args[0].trim();      
                if (!urlString.startsWith("http://")) {
                        urlString = "http://" + urlString;
                }
                
                String outputFileName = args[1].trim();

                try {           
                        FileOutputStream outputStream = new FileOutputStream(outputFileName);

                        URL urlAddress = new URL(urlString);
                        HttpURLConnection urlConnection = (HttpURLConnection) urlAddress.openConnection();
                        int responseCode = urlConnection.getResponseCode();
                        
                        if (responseCode == 200) {
                                InputStream inputStream = urlAddress.openStream();
                                
                                int i = inputStream.read();
                               
                                while (i != -1) {
                                        outputStream.write(i);
                                        System.out.print(i);
                                        System.out.print("^^");
                                        System.out.println();
                                        i = inputStream.read();
                                        
                                }
                                
                                inputStream.close();
                        } else {
                                String responseCodeString = new Integer(responseCode).toString();
                                byte[] responseCodeBytes = responseCodeString.getBytes();
                                outputStream.write(responseCodeBytes);
                        }
                        
                        outputStream.close();
                } catch (MalformedURLException e) {
                        e.printStackTrace();
                        System.exit(1);
                } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                }
        }
}
