import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


public class Sender {
	//ASCII code
	private static final int f = 102;
	private static final int N = 78;
	private static final int m = 109;
	private static final int d = 100;
	private static final int A = 65;
	private static final int t = 116;
	private static final int I = 73;
	private static final int l = 108;
	private static final int E = 69;
	private static final int c = 99;
	private static final int R = 82;
	private static final int s = 115;
	private static final int q = 113;
	
	
	static int pkt_size = 10;
	static int send_interval = 500;
	int seq=0;
	int curr=0;

	public class OutThread extends Thread {
		private DatagramSocket sk_out;
		private int dst_port;
		private int recv_port;
		private String inPath;
		private String outPath;

		public OutThread(DatagramSocket sk_out, int dst_port, int recv_port, String inPath, String outPath) {
			
			this.sk_out = sk_out;
			this.dst_port = dst_port;
			this.recv_port = recv_port;
			this.inPath = inPath;
			this.outPath = outPath;
		}

		public void run() {
			try {
				
				byte[] out_data = new byte[pkt_size];
				InetAddress dst_addr = InetAddress.getByName("127.0.0.1");

				// To register the recv_port at the UnreliNet first
				DatagramPacket out_pkt = new DatagramPacket(
						("REG:" + recv_port).getBytes(),
						("REG:" + recv_port).getBytes().length, dst_addr, dst_port);
				sk_out.send(out_pkt);
				
				int len = (int)(new File(inPath).length());
			    FileInputStream fis = new FileInputStream(inPath);
				int currbytes= 0;
				int flag = 0;
				byte currentbyte[] = new byte[850];
				
				try {
					while (true) {
						
						out_data = outPath.getBytes();
					    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					    outputStream.write("fNm".getBytes());
					    outputStream.write(out_data);
					    outputStream.write("fNm".getBytes());
					    outputStream.write("dAtA".getBytes());
					    if (curr == seq){
					    	if (flag == 1){
								//empty packet to signify finish
								DatagramPacket final_pkt = new DatagramPacket("afIlEdAtAFINdAtAcRc975554582cRcsEq-1sEqafIlE".getBytes(),"afIlEdAtAFINdAtAcRc975554582cRcsEq-1sEqafIlE".getBytes().length, dst_addr, dst_port);
								for (int i=0;i<20;i++)
									sk_out.send(final_pkt);
								System.exit(0);
					    	}
						    if (currbytes+850 <= len){
						    	currentbyte = new byte[850];				    	
						    	fis.read(currentbyte, 0, 850);
							    currbytes+=850;
						    }
						    else{
						    	currentbyte = new byte[len-currbytes];
						    	fis.read(currentbyte, 0, len-currbytes);
						    	flag = 1;
						    }
					    }
					    currentbyte = process(currentbyte);
					    outputStream.write(currentbyte);
					    outputStream.write("dAtA".getBytes());
					    byte fin[] = outputStream.toByteArray();
					    System.out.println();
					    System.out.println("CRC pkt size:"+ fin.length);
					    
					    //update checksum
					    Checksum checksum = new CRC32();	  
					    checksum.update(fin, 0, fin.length);
			
					    long checksumValue = checksum.getValue();
					    outputStream = new ByteArrayOutputStream();
					    outputStream.write("afIlE".getBytes());
					    outputStream.write(fin);
					    outputStream.write("fIlE".getBytes());
					    outputStream.write("cRc".getBytes());
					    outputStream.write(String.valueOf(checksumValue).getBytes());
					    outputStream.write("cRc".getBytes());
					    outputStream.write("sEq".getBytes());
					    outputStream.write(String.valueOf(seq).getBytes());
					    outputStream.write("sEqa".getBytes());
					    byte pkt[] = outputStream.toByteArray();
						curr = seq;
						seq++;
						System.out.println("total size: " + pkt.length);
						// send the packet
					    out_pkt = new DatagramPacket(pkt, pkt.length,dst_addr, dst_port);
					    sk_out.send(out_pkt);
	
						//print info
						for (int i = 0; i < currentbyte.length; ++i)
							System.out.print((char)currentbyte[i]);
						System.out.println();
	
						// wait for a while
					    for (int i=0;i<5;i++){
					    	sleep(send_interval);
					    	if (curr == seq){
					    		break;
					    	}
					    	else if (i==4){
					    		curr--;
					    		seq--;
					    		break;
					    	}
					    }
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					sk_out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public class InThread extends Thread {
		private DatagramSocket sk_in;

		public InThread(DatagramSocket sk_in) {
			this.sk_in = sk_in;
		}

		public void run() {
			try {
				byte[] in_data = new byte[pkt_size];
				DatagramPacket in_pkt = new DatagramPacket(in_data,
						in_data.length);
				try {
					while (true) {
						sk_in.receive(in_pkt);
						System.out.print((new Date().getTime())
								+ ": sender received " + in_pkt.getLength()
								+ "bytes from "
								+ in_pkt.getAddress().toString() + ":"
								+ in_pkt.getPort() + ". data are ");
						for (int i = 0; i < pkt_size; ++i)
							System.out.print(in_data[i]);
						System.out.println();
						try{
							String res = new String(in_data);
							String[] arr = new String[2];
							arr = res.split(" ");
							System.out.println(arr[0]);
							System.out.println(arr[1]);
							if ((arr[0].trim().equalsIgnoreCase(String.valueOf(curr))) && (arr[1].trim().equalsIgnoreCase("ACK") && (arr[2].trim().equalsIgnoreCase(String.valueOf(617709412))))){
								curr++;
							}
						}catch(Exception e){
							
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					sk_in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public Sender(int sk1_dst_port, int sk4_dst_port,String inPath, String outName) {
		DatagramSocket sk1, sk4;
		System.out.println("sk1_dst_port=" + sk1_dst_port + ", "
				+ "sk4_dst_port=" + sk4_dst_port + ".");

		try {
			// create sockets
			sk1 = new DatagramSocket();
			sk4 = new DatagramSocket(sk4_dst_port);
			
			File file = new File(inPath);
			if (!(file.exists() && file.isFile())) {
				System.err.println("File does not exist.");
				System.exit(-1);
			}		

			// create threads to process data
			InThread th_in = new InThread(sk4);
			OutThread th_out = new OutThread(sk1, sk1_dst_port, sk4_dst_port,inPath,outName);
			th_in.start();
			th_out.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static byte[] process(byte[] bytes){
		byte[] data = new byte[bytes.length+50];
		int j=0;
		int counter =0;
		
		for (int i = 0; i < bytes.length; ++i){
			//fNm
			if(i<bytes.length-2 && bytes[i]==f && bytes[i+1]==N && bytes[i+2]==m){
				data[j] = bytes[i];
				data[j+1] = 123;
				data[j+2] = bytes[i+1];
				data[j+3] = bytes[i+2];
				i+=2;
				j+=3;
				counter++;
			}
			//dAtA
			if(i<bytes.length-3 && bytes[i]==d && bytes[i+1]==A && bytes[i+2]==t && bytes[i+3]==A){
				data[j] = bytes[i];
				data[j+1] = 123;
				data[j+2] = bytes[i+1];
				data[j+3] = bytes[i+2];
				data[j+4] = bytes[i+3];
				i+=3;
				j+=4;
				counter++;
			}
			//fIlE
			if(i<bytes.length-3 && bytes[i]==f && bytes[i+1]==I && bytes[i+2]==l && bytes[i+3]==E){
				data[j] = bytes[i];
				data[j+1] = 123;
				data[j+2] = bytes[i+1];
				data[j+3] = bytes[i+2];
				data[j+4] = bytes[i+3];
				i+=3;
				j+=4;
				counter++;
			}
			//cRc
			if(i<bytes.length-2 && bytes[i]==c && bytes[i+1]==R && bytes[i+2]==c){
				data[j] = bytes[i];
				data[j+1] = 123;
				data[j+2] = bytes[i+1];
				data[j+3] = bytes[i+2];
				i+=2;
				j+=3;
				counter++;
			}
			//sEq
			if(i<bytes.length-2 && bytes[i]==s && bytes[i+1]==E && bytes[i+2]==q){
				data[j] = bytes[i];
				data[j+1] = 123;
				data[j+2] = bytes[i+1];
				data[j+3] = bytes[i+2];
				i+=2;
				j+=4;
				counter++;
			}
			else{
				data[j] = bytes[i];
				j++;
			}
		}
		byte[] output = new byte[bytes.length+counter];
		for (int i=0;i<output.length;i++)
			output[i] = data[i];
		return output;
	}
	
	public static void main(String[] args) {
		// parse parameters
		if (args.length != 4) {
			System.err
					.println("Usage: java Sender sk1_dst_port sk4_dst_port inputFilePath outputFileName");
			System.exit(-1);
		} else
			new Sender(Integer.parseInt(args[0]), Integer.parseInt(args[1]),args[2],args[3]);
	}
	
}
