package SoTongClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SoTongWriteThread extends Thread {
	private Socket ST_C_Socket = null;
	private static BufferedReader ST_C_BR_Keyboard = null;
	private SoTongController ST_Controller; 
			
	public SoTongWriteThread(Socket ST_C_Socket) throws IOException{
		this.ST_C_Socket = ST_C_Socket;
		ST_C_BR_Keyboard = new BufferedReader(new InputStreamReader(System.in));
		ST_Controller = new SoTongController(this.ST_C_Socket);
	}
	
	public void run(){
		try{
			String line = null;
			while((line = ST_C_BR_Keyboard.readLine()) != null){
				ST_Controller.controllerReq(line);
			}
		}catch(Exception ex){
			
		}finally{
			
		}
	}
}