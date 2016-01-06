
import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Receiver1 {
	static int pkt_size = 1000;
	int curr =0;
	
	public Receiver1(int sk2_dst_port, int sk3_dst_port, String outputFolderPath) {
		DatagramSocket sk2, sk3;
		System.out.println("sk2_dst_port=" + sk2_dst_port + ", "
				+ "sk3_dst_port=" + sk3_dst_port + ".");	
		File f = new File(outputFolderPath);
		if (!(f.exists() && f.isDirectory())) {
			System.err.println("Folder does not exist.");
			System.exit(-1);
		}
		// create sockets
		try {
			sk2 = new DatagramSocket(sk2_dst_port);
			sk3 = new DatagramSocket();
			try {
				byte[] in_data = new byte[pkt_size];
				DatagramPacket in_pkt = new DatagramPacket(in_data,
						in_data.length);
				InetAddress dst_addr = InetAddress.getByName("127.0.0.1");

				while (true) {
					byte[] filename = new byte[128];
					byte[] dataraw = new byte[pkt_size];
					byte[] pkt= new byte[pkt_size];
					byte[] crc = new byte[32];
					byte[] seq = new byte[32];
					byte[] data;
					DatagramPacket out_pkt;
					int pktsize=0;
					int datasize=0;
					// receive packet
					sk2.receive(in_pkt);
					//extract fNm
					try{
						for (int i = 0; i < pkt_size; ++i){
							if(i<pkt_size-3 && in_data[i+1]==102 && in_data[i+2]==78 && in_data[i+3]==109){
								i+=4;
								int j=0;
								while (!(in_data[i+1]==102 && in_data[i+2]==78 && in_data[i+3]==109)){
									filename[j] = in_data[i];
									j++;
									i++;
								}
								filename[j] = in_data[i];
								break;
							}
						}
						//extract dAtA
						for (int i = 0; i < pkt_size; ++i){
							if(i<pkt_size-4 && in_data[i+1]==100 && in_data[i+2]==65 && in_data[i+3]==116 && in_data[i+4]==65){
								i+=5;
								int j=0;
								while (!(in_data[i+1]==100 && in_data[i+2]==65 && in_data[i+3]==116 && in_data[i+4]==65)){
									dataraw[j] = in_data[i];
									j++;
									i++;
									datasize++;
								}
								dataraw[j] = in_data[i];
								datasize++;
								break;
							}
						}
						data = new byte[datasize];
						for (int i = 0; i < datasize; ++i){
							data[i] = dataraw[i];
						}
						//extract fIlE					
						for (int i = 0; i < pkt_size; ++i){
							if(i<pkt_size-5 && in_data[i+1]==102 && in_data[i+2]==73 && in_data[i+3]==108 && in_data[i+4]==69){
								i+=5;
								int j=0;
								while (!(in_data[i+1]==102 && in_data[i+2]==73 && in_data[i+3]==108 && in_data[i+4]==69)){
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
							if(i<pkt_size-3 && in_data[i+1]==99 && in_data[i+2]==82 && in_data[i+3]==99){
								i+=4;
								int j=0;
								while (!(in_data[i+1]==99 && in_data[i+2]==82 && in_data[i+3]==99)){
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
							if(i<pkt_size-3 && in_data[i+1]==115 && in_data[i+2]==69 && in_data[i+3]==113){
								i+=4;
								int j=0;
								while (!(in_data[i+1]==115 && in_data[i+2]==69 && in_data[i+3]==113)){
									seq[j] = in_data[i];
									j++;
									i++;
								}
								seq[j] = in_data[i];
								break;
							}
						}
					} catch (Exception e){
						//out_pkt = new DatagramPacket("NAK".getBytes(),"NAK".getBytes().length, dst_addr, sk3_dst_port);
					    //sk3.send(out_pkt);
						continue;
					}
					System.out.println();
					System.out.println("filename: " +new String(filename));
					//System.out.println("data: " +new String(data));
					//System.out.println("pkt: " +new String(pkt));
					System.out.println("crc: " +new String(crc));
					System.out.println("seq: " +new String(seq));
					Checksum checksum = new CRC32();
				    // update the current checksum with the specified array of bytes
					System.out.println("CRC pkt size:"+ pktsize);
					checksum.update(pkt, 0, pktsize);
					// get the current checksum value
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
				    	//System.out.println(outputFolderPath+"/"+new String(filename));
				    	System.out.println("seq: "+ new String(seq));
				    	System.out.println("curr: " + curr);
				    	if (String.valueOf(curr).equalsIgnoreCase((new String(seq).trim()))){
				    		System.out.println("write");
					    	File file = new File(outputFolderPath+"/"+new String(filename));
					    	System.out.println(file.toString());
					    	if(!file.exists())
					    	    file.createNewFile();
					    	FileOutputStream fos = new FileOutputStream(outputFolderPath+"/"+new String(filename),true);
							fos.write(dataunsub(data));
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
				    	}else{
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

	public static void main(String[] args) {
		// parse parameters
		if (args.length != 3) {
			System.err.println("Usage: java Receiver sk2_dst_port, sk3_dst_port outputFolderPath");
			System.exit(-1);
		} else
			new Receiver1(Integer.parseInt(args[0]),
					Integer.parseInt(args[1]),args[2]);
	}
	
	public static byte[] dataunsub(byte[] original){
		byte[] subbed = new byte[original.length];
		int j=0;
		int counter=0;
		for (int i = 0; i < original.length; ++i){
			//fNm
			if(i<original.length-3 && original[i]==102 && original[i+1]==123 && original[i+2]==78 && original[i+3]==109){
				subbed[j] = original[i];
				subbed[j+1] = original[i+2];
				subbed[j+2] = original[i+3];
				i+=3;
				j+=2;
				counter++;
			}
			//dAtA
			if(i<original.length-4 && original[i]==100 && original[i+1]==123 && original[i+2]==65 && original[i+3]==116 && original[i+4]==65){
				subbed[j] = original[i];
				subbed[j+1] = original[i+2];
				subbed[j+2] = original[i+3];
				subbed[j+3] = original[i+4];
				i+=4;
				j+=3;
				counter++;
			}
			//fIlE
			if(i<original.length-4 && original[i]==102 && original[i+1]==123 && original[i+2]==73 && original[i+3]==108 && original[i+4]==69){
				subbed[j] = original[i];
				subbed[j+1] = original[i+2];
				subbed[j+2] = original[i+3];
				subbed[j+3] = original[i+4];
				i+=4;
				j+=3;
				counter++;
			}
			//cRc
			if(i<original.length-3 && original[i]==99 && original[i+1]==123 && original[i+2]==82 && original[i+3]==99){
				subbed[j] = original[i];
				subbed[j+1] = original[i+2];
				subbed[j+2] = original[i+3];
				i+=3;
				j+=2;
				counter++;
			}
			//sEq
			if(i<original.length-3 && original[i]==115 && original[i+1]==123 && original[i+2]==69 && original[i+3]==113){
				//System.out.print()
				subbed[j] = original[i];
				subbed[j+1] = original[i+2];
				subbed[j+2] = original[i+3];
				i+=3;
				j+=3;
				counter++;
			}
			else{
				subbed[j] = original[i];
				j++;
			}
		}
		byte[] shrunk = new byte[original.length-counter];
		for (int i=0;i<shrunk.length;i++)
			shrunk[i] = subbed[i];
		return shrunk;
	}
}
