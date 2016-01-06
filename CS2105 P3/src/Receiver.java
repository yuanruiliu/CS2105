import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Receiver {
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

	public Receiver(int sk2_dst_port, int sk3_dst_port, String outputFolderPath) {
		DatagramSocket sk2, sk3;
		System.out.println("sk2_dst_port=" + sk2_dst_port + ", "
				+ "sk3_dst_port=" + sk3_dst_port + ".");
		
		File file = new File(outputFolderPath);
		
		if (!(file.exists() && file.isDirectory())) {
			System.err.println("Folder does not exist.");
			System.exit(-1);
		}

		// create sockets
		try {
			sk2 = new DatagramSocket(sk2_dst_port);
			sk3 = new DatagramSocket();
			sk2.setSoTimeout(2000);
			try {
				byte[] in_data = new byte[pkt_size];
				DatagramPacket in_pkt = new DatagramPacket(in_data,
						in_data.length);
				InetAddress dst_addr = InetAddress.getByName("127.0.0.1");

				while (true) {
					// receive packet
					try{
						sk2.receive(in_pkt);
					}catch (SocketTimeoutException e) {
						// timeout exception.
						System.out.println("Timeout reached!!! " + e);
						sk2.close();
					}

					
					byte[] pkt= new byte[pkt_size];
					byte[] crc = new byte[32];
					byte[] seq = new byte[32];
					byte[] data;
					DatagramPacket out_pkt;
					int pktsize = 0;
					int datasize = 0;
					
					int curr = 0;
					//extract fNm
					byte[] filename = new byte[128];
					try{
						for (int i = 0; i < pkt_size; i++){
							if(i<pkt_size-3 && in_data[i+1]==f && in_data[i+2]==N && in_data[i+3]==m){
								i+=4;
								int j=0;
								while (!(in_data[i+1]==f && in_data[i+2]==N && in_data[i+3]==m)){
									filename[j] = in_data[i];
									j++;
									i++;
								}
								filename[j] = in_data[i];
								break;
							}
						}
						//extract dAtA	
						byte[] rawdata = new byte[pkt_size];
						for (int i = 0; i < pkt_size; ++i){
							if(i<pkt_size-4 && in_data[i+1]==d && in_data[i+2]==A && in_data[i+3]==t && in_data[i+4]==A){
								i+=5;
								int j=0;
								while (!(in_data[i+1]==d && in_data[i+2]==A && in_data[i+3]==t && in_data[i+4]==A)){
									rawdata[j] = in_data[i];
									j++;
									i++;
									datasize++;
								}
								rawdata[j] = in_data[i];
								datasize++;
								break;
							}
						}
						data = new byte[datasize];
						for (int i = 0; i < datasize; ++i){
							data[i] = rawdata[i];
						}
						//extract fIlE					
						for (int i = 0; i < pkt_size; ++i){
							if(i<pkt_size-5 && in_data[i+1]==f && in_data[i+2]==I && in_data[i+3]==l && in_data[i+4]==E){
								i+=5;
								int j=0;
								while (!(in_data[i+1]==f && in_data[i+2]==I && in_data[i+3]==l && in_data[i+4]==E)){
									pkt[j] = in_data[i];
									j++;
									i++;
									pktsize++;
								}
								pkt[j] = in_data[i];
								++pktsize;
								break;
							}
						}
						//extract cRc
						for (int i = 0; i < pkt_size; ++i){
							if(i<pkt_size-3 && in_data[i+1]==c && in_data[i+2]==R && in_data[i+3]==c){
								i+=4;
								int j=0;
								while (!(in_data[i+1]==c && in_data[i+2]==R && in_data[i+3]==c)){
									crc[j] = in_data[i];
									j++;
									i++;
								}
								crc[j] = in_data[i];
								break;
							}
						}
						//extract sEq
						for (int i = 0; i < pkt_size; ++i){
							if(i<pkt_size-3 && in_data[i+1]==s && in_data[i+2]==E && in_data[i+3]==q){
								i+=4;
								int j=0;
								while (!(in_data[i+1]==s && in_data[i+2]==E && in_data[i+3]==q)){
									seq[j] = in_data[i];
									j++;
									i++;
								}
								seq[j] = in_data[i];
								break;
							}
						}
					} catch (Exception e){
						continue;
					}
					// print info
					System.out.println();
					System.out.println("filename: " +new String(filename));
					System.out.println("data: " +new String(data));
					System.out.println("pkt: " +new String(pkt));
					System.out.println("crc: " +new String(crc));
					System.out.println("seq: " +new String(seq));
					Checksum checksum = new CRC32();
					System.out.println("CRC pkt size:"+ pktsize);
					checksum.update(pkt, 0, pktsize);
				    long checksumValue = checksum.getValue();
				    System.out.println("Actual CRC: "+checksumValue);
					System.out.println();
					System.out.println();
					
				    // send ack/nack
					byte[] out_pkt_cc;
					byte[] out_pkt_fin;
					if (String.valueOf("-1").equalsIgnoreCase((new String(seq).trim()))){
			    		System.exit(0);
			    	}
				    if (String.valueOf(checksumValue).trim().equals(new String(crc).trim())){
				    	System.out.println(outputFolderPath+"/"+new String(filename));
				    	System.out.println("seq: "+ new String(seq));
				    	System.out.println("curr: " + curr);
				    	if (String.valueOf(curr).equalsIgnoreCase((new String(seq).trim()))){
				    		System.out.println("write");
					    	File f = new File(outputFolderPath+"/"+new String(filename));
					    	System.out.println(f.toString());
					    	if(!f.exists())
					    	    f.createNewFile();
					    	FileOutputStream fos = new FileOutputStream(outputFolderPath+"/"+new String(filename),true);
							fos.write(process(data));
							fos.close();
							curr++;
						}
						out_pkt_cc = new byte[seq.length + " ACK ".getBytes().length];
						checksum = new CRC32();
						checksum.update(out_pkt_cc, 0, out_pkt_cc.length);
						// get the current checksum value
					    checksumValue = checksum.getValue();
					    out_pkt_fin = new byte[seq.length + " ACK ".getBytes().length + (String.valueOf(checksumValue)).getBytes().length];
						System.arraycopy(seq, 0, out_pkt_fin, 0, seq.length);
						System.arraycopy(" ACK ".getBytes(), 0, out_pkt_fin, seq.length, " ACK ".getBytes().length);
						System.arraycopy((String.valueOf(checksumValue)).getBytes(), 0, out_pkt_fin, seq.length+ " ACK ".getBytes().length,(String.valueOf(checksumValue)).getBytes().length);
				    		out_pkt = new DatagramPacket(out_pkt_fin,out_pkt_fin.length, dst_addr, sk3_dst_port);
				    		sk3.send(out_pkt);
				    }
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			} finally {
				sk2.close();
				sk3.close();
			}
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static byte[] process(byte[] bytes){
		byte[] data = new byte[bytes.length];
		int j=0;
		int counter=0;
		for (int i = 0; i < bytes.length; ++i){
			//fNm
			if(i<bytes.length-3 && bytes[i]==102 && bytes[i+1]==123 && bytes[i+2]==78 && bytes[i+3]==109){
				data[j] = bytes[i];
				data[j+1] = bytes[i+2];
				data[j+2] = bytes[i+3];
				i+=3;
				j+=2;
				counter++;
			}
			//dAtA
			if(i<bytes.length-4 && bytes[i]==100 && bytes[i+1]==123 && bytes[i+2]==65 && bytes[i+3]==116 && bytes[i+4]==65){
				data[j] = bytes[i];
				data[j+1] = bytes[i+2];
				data[j+2] = bytes[i+3];
				data[j+3] = bytes[i+4];
				i+=4;
				j+=3;
				counter++;
			}
			//fIlE
			if(i<bytes.length-4 && bytes[i]==102 && bytes[i+1]==123 && bytes[i+2]==73 && bytes[i+3]==108 && bytes[i+4]==69){
				data[j] = bytes[i];
				data[j+1] = bytes[i+2];
				data[j+2] = bytes[i+3];
				data[j+3] = bytes[i+4];
				i+=4;
				j+=3;
				counter++;
			}
			//cRc
			if(i<bytes.length-3 && bytes[i]==99 && bytes[i+1]==123 && bytes[i+2]==82 && bytes[i+3]==99){
				data[j] = bytes[i];
				data[j+1] = bytes[i+2];
				data[j+2] = bytes[i+3];
				i+=3;
				j+=2;
				counter++;
			}
			//sEq
			if(i<bytes.length-3 && bytes[i]==115 && bytes[i+1]==123 && bytes[i+2]==69 && bytes[i+3]==113){
				//System.out.print()
				data[j] = bytes[i];
				data[j+1] = bytes[i+2];
				data[j+2] = bytes[i+3];
				i+=3;
				j+=3;
				counter++;
			}
			else{
				data[j] = bytes[i];
				j++;
			}
		}
		byte[] output = new byte[bytes.length-counter];
		for (int i=0;i<output.length;i++)
			output[i] = data[i];
		return output;
	}

	public static void main(String[] args) {
		// parse parameters
		if (args.length != 3) {
			System.err
					.println("Usage: java Receiver sk2_dst_port, sk3_dst_port outputFolderPath");
			System.exit(-1);
		} else
			new Receiver(Integer.parseInt(args[0]),
					Integer.parseInt(args[1]),args[2]);
	}
	
}


