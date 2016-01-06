import java.net.*;
import java.io.*;
import java.lang.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

class DownloadUrl{

   public static void main(String argv[]) throws Exception{
        if (argv.length!=2){
                System.out.println("Usage: java DownloadURL <URL> <Outfile>");
                System.exit(0);
                }
                String url = argv[0];
                String outfile = argv[1];
                String s = url.substring(0,7); 
                int status;
                if (!s.equals("http://"))
                        url = ("http://") + url;

                String outputLine;
                File file = new File(outfile);

                if (!file.exists())
                        file.createNewFile();
                
                URL myURL = new URL(url);
                HttpURLConnection connection = (HttpURLConnection)myURL.openConnection();
                connection.setRequestMethod("GET");
                status = connection.getResponseCode();
        
                if (status == 200)
                {
                        int size = connection.getContentLength();
                        
                        byte[] bytes = new byte[size];
                        InputStream input = new ByteArrayInputStream(bytes);
                        FileOutputStream out = new FileOutputStream(file);
                        input = connection.getInputStream();
                        
                        int data = input.read(bytes);
      
                        while(data != -1){
                        	out.write(bytes, 0, data);
                        	data = input.read(bytes);
                        }
                        out.close();
                        input.close();
                } else {
                        outputLine = Integer.toString(status);
                        BufferedWriter out = new BufferedWriter(new FileWriter(file));
                        out.write(outputLine);
                        out.close();
                } 
   }
}

