package SoTongClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class SoTongClient {
	Socket ST_C_Socket = null; //클라이언트 소켓
	private final String Server_IP = "127.0.0.1"; //소통 서버 아이피
	private final int Server_Port = 9900; //소통 서버 포트번호
	
	SoTongClient() throws UnknownHostException, IOException{
		System.out.println("[ST-Chat 서버에 접속을 요청합니다]");
		System.out.print("");
		ST_C_Socket = new Socket(Server_IP, Server_Port); //소통 서버소켓 접속요청

		//입력 스트림 스레드 생성
		Thread SoTongListenThread = new Thread(new SoTongListenThread(ST_C_Socket));      
		SoTongListenThread.start();
		//출력 스트림 스레드 생성
		Thread SoTongWriteThread = new Thread(new SoTongWriteThread(ST_C_Socket));      
		SoTongWriteThread.start();
	}
	
	public static void main(String[] args) throws IOException{
		new SoTongClient();
	}	
}