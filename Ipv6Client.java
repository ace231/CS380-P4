/*****************************************
*	Alfredo Ceballos
*	CS 380 - Computer Networks
*	Project 3
*	Professor Nima Davarpanah
*****************************************/

import java.io.*;
import java.net.*;

public class Ipv6Client{
	
	public static byte[] IPv6Packet(byte[] data) throws Exception {
		byte[] header = new byte[40];	// Size of IPv6 header
			
		header[0] = 0b01100000;	// 0110 0000 : Version 0110(6), Traffic class 0000
		header[1] = 0;	// Rest of traffic class and Flow label
		
		//Flow Label
		header[2] = 0;
		header[3] = 0;
		
		// Payload length
		header[4] = (byte)(data.length >> 8);
		header[5] = (byte)(data.length);
		
		header[6] = 17; // Next Header = UDP protocol value = 17
		header[7] = 20;	// Hop limit = 20
						
		// Fetching client's actual IP address and converting it into 
		// byte values, then converting 32 bit address into IPv6 IP 
		// address of 128 bits
		URL getIP = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(getIP.openStream(), "UTF-8"));
		String[] src = in.readLine().split("\\.");
		byte[] srcAddress = new byte[4];
		for (int i = 0; i < src.length; i++) {
			int j = Integer.parseInt(src[i]);
			srcAddress[i] = (byte) j;	// source address bytes
		}
		
		// First 80 bits, or 10 elements of a byte array, are 0's
		for(int i = 0; i < 10; i++) {
			header[i + 8] = 0;
		}
		// Next 16 bits, or next 2 elements, are 1's
		header[18] = (byte)0xFF;// 1111 1111
		header[19] = (byte)0xFF;
		
		// Last 32 bits, next 4 elements of a byte array, is the IPv4 Address
		header[20] = srcAddress[0];
		header[21] = srcAddress[1];
		header[22] = srcAddress[2];
		header[23] = srcAddress[2];	// End of source address portion
		
		// Doing the same for the destination address portion of the header
		// First 80 bits, or 10 elements of a byte array, are 0's
		for(int i = 0; i < 10; i++) {
			header[i + 24] = 0;
		}
		// Next 16 bits, or next 2 elements, are 1's
		header[34] = (byte)0xFF;// 1111 1111
		header[35] = (byte)0xFF;
		
		// Last 32 bits, next 4 elements of a byte array, is the IPv4 Address
		header[36] = (byte) 18;
		header[37] = (byte) 221;
		header[38] = (byte) 102;
		header[39] = (byte) 182;	// End of source address portion

		
		// The packet's header data is already in the header byte array, here
		// it is copied into the packet byte array, which is the size of 
		// of the header plus the size of the data generated, that data is then
		// also copied in at the end
		byte[] packet = new byte[header.length + data.length];
		for (int j = 0; j < header.length; j++) { // Copying header to packet
			packet[j] = header[j];
		}
		for (int k = 0; k < data.length; k++) { // Copying data to packet
			packet[header.length + k] = data[k];
		}		
		return packet;
	} // End of IPv6Packet
	
	
	// Since the data to be added at the end of the IPv4 packet is in powers
	// of 2, this method generates a byte array who's size if a power of 2, 
	// filled with random data
	public static byte[] genByteArray(int n) {
		int numBytes = (int)Math.pow(2, n);
		byte[] arr = new byte[numBytes];
		System.out.println("size of data " + arr.length);
		
		for(int i = 0; i  < numBytes; i++){
			arr[i] = (byte)(Math.random() * 255);
		}
		
		return arr;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		try (Socket socket = new Socket("18.221.102.182", 38004))
		{
			// Creating client input/output streams to receive 
			// and send messages from and to server	
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			
			for(int i = 1; i <= 12; i++) {
				byte[] data = genByteArray(i);	// Random data array created
				byte[] packet = IPv6Packet(data);	//IPv6 packet created
				os.write(packet);	// Packet sent to server
				byte[] serverCode = new byte[4];
				is.read(serverCode);
				for(byte b : serverCode) {
					System.out.printf("%02X", b);
				}
				System.out.println();
			}
		}	
	}
	
}