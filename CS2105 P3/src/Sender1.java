

	import java.io.ByteArrayOutputStream;
	import java.io.File;
	import java.io.FileInputStream;
	import java.net.*;
	import java.util.zip.CRC32;
	import java.util.zip.Checksum;

	public class Sender1 {
		static int pkt_size = 1000;
		static int send_interval = 10;
		int seq=0;
		int curr=0;
		
		public class OutThread extends Thread {
			private DatagramSocket sk_out;
			private int dst_port;
			private int recv_port;
			private String inPath;
			private String outName;
			
			public OutThread(DatagramSocket sk_out, int dst_port, int recv_port, String inPath, String outName) {
				this.sk_out = sk_out;
				this.dst_port = dst_port;
				this.recv_port = recv_port;
				this.inPath = inPath;
				this.outName = outName;
			}

			public void run() {
				try {
					
					byte[] out_data = new byte[pkt_size];
					InetAddress dst_addr = InetAddress.getByName("127.0.0.1");

					// To register the recv_port at the UnreliNet first
					DatagramPacket out_pkt = new DatagramPacket(
							("REG:" + recv_port).getBytes(),
							("REG:" + recv_port).getBytes().length, dst_addr,
							dst_port);
					sk_out.send(out_pkt);
					int len1 = (int)(new File(inPath).length());
				    FileInputStream fis1 = new FileInputStream(inPath);
					int currbytes= 0;
					int flag = 0;
					byte currentbyte[] = new byte[850];
				    try {
						while(true){
							out_data = outName.getBytes();
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
							    if (currbytes+850 <= len1){
							    	currentbyte = new byte[850];				    	
							    	fis1.read(currentbyte, 0, 850);
								    currbytes+=850;
							    }
							    else{
							    	currentbyte = new byte[len1-currbytes];
							    	fis1.read(currentbyte, 0, len1-currbytes);
							    	flag = 1;
							    }
						    }
						    currentbyte = datasub(currentbyte);
						    outputStream.write(currentbyte);
						    outputStream.write("dAtA".getBytes());
						    byte fin[] = outputStream.toByteArray();
						    System.out.println();
						    System.out.println("CRC pkt size:"+ fin.length);
						    Checksum checksum = new CRC32();
						    // update the current checksum with the specified array of bytes
						    checksum.update(fin, 0, fin.length);
						    // get the current checksum value
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
		
							// print info
							/*for (int i = 0; i < currentbyte.length; ++i)
								System.out.print((char)currentbyte[i]);
							System.out.println();*/
		
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
				    fis1.close();
				    System.exit(0);
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
							//for (int i = 0; i < 50; ++i)
							//	System.out.print((char)in_data[i]);
							//System.out.println();
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

		public Sender1(int sk1_dst_port, int sk4_dst_port,String inPath, String outName) {
			DatagramSocket sk1, sk4;
			System.out.println("sk1_dst_port=" + sk1_dst_port + ", "
					+ "sk4_dst_port=" + sk4_dst_port + ".");
			File f = new File(inPath);
			if (!(f.exists() && f.isFile())) {
				System.err.println("File does not exist.");
				System.exit(-1);
			}		
			try {
				// create sockets
				sk1 = new DatagramSocket();
				sk4 = new DatagramSocket(sk4_dst_port);

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

		public static void main(String[] args) {
			// parse parameters
			if (args.length != 4) {
				System.err.println("Usage: java Sender sk1_dst_port sk4_dst_port inputFilePath outputFileName");
				System.exit(-1);
			} else
				new Sender1(Integer.parseInt(args[0]), Integer.parseInt(args[1]),args[2],args[3]);
		}
		
		
		public static byte[] datasub(byte[] original){
			byte[] subbed = new byte[original.length+50];
			int j=0;
			int counter =0;
			for (int i = 0; i < original.length; ++i){
				//fNm
				if(i<original.length-2 && original[i]==102 && original[i+1]==78 && original[i+2]==109){
					subbed[j] = original[i];
					subbed[j+1] = 123;
					subbed[j+2] = original[i+1];
					subbed[j+3] = original[i+2];
					i+=2;
					j+=3;
					counter++;
				}
				//dAtA
				if(i<original.length-3 && original[i]==100 && original[i+1]==65 && original[i+2]==116 && original[i+3]==65){
					subbed[j] = original[i];
					subbed[j+1] = 123;
					subbed[j+2] = original[i+1];
					subbed[j+3] = original[i+2];
					subbed[j+4] = original[i+3];
					i+=3;
					j+=4;
					counter++;
				}
				//fIlE
				if(i<original.length-3 && original[i]==102 && original[i+1]==73 && original[i+2]==108 && original[i+3]==69){
					subbed[j] = original[i];
					subbed[j+1] = 123;
					subbed[j+2] = original[i+1];
					subbed[j+3] = original[i+2];
					subbed[j+4] = original[i+3];
					i+=3;
					j+=4;
					counter++;
				}
				//cRc
				if(i<original.length-2 && original[i]==99 && original[i+1]==82 && original[i+2]==99){
					subbed[j] = original[i];
					subbed[j+1] = 123;
					subbed[j+2] = original[i+1];
					subbed[j+3] = original[i+2];
					i+=2;
					j+=3;
					counter++;
				}
				//sEq
				if(i<original.length-2 && original[i]==115 && original[i+1]==69 && original[i+2]==113){
					subbed[j] = original[i];
					subbed[j+1] = 123;
					subbed[j+2] = original[i+1];
					subbed[j+3] = original[i+2];
					i+=2;
					j+=4;
					counter++;
				}
				else{
					subbed[j] = original[i];
					j++;
				}
			}
			byte[] shrunk = new byte[original.length+counter];
			for (int i=0;i<shrunk.length;i++)
				shrunk[i] = subbed[i];
			return shrunk;
		}
		
		
	}

