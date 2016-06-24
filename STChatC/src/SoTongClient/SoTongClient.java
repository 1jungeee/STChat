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
	Socket ST_C_Socket = null; //Ŭ���̾�Ʈ ����
	private final String Server_IP = "127.0.0.1"; //���� ���� ������
	private final int Server_Port = 9900; //���� ���� ��Ʈ��ȣ
	
	SoTongClient() throws UnknownHostException, IOException{
		System.out.println("[ST-Chat ������ ������ ��û�մϴ�]");
		System.out.print("");
		ST_C_Socket = new Socket(Server_IP, Server_Port); //���� �������� ���ӿ�û

		//�Է� ��Ʈ�� ������ ����
		Thread SoTongListenThread = new Thread(new SoTongListenThread(ST_C_Socket));      
		SoTongListenThread.start();
		//��� ��Ʈ�� ������ ����
		Thread SoTongWriteThread = new Thread(new SoTongWriteThread(ST_C_Socket));      
		SoTongWriteThread.start();
	}
	
	public static void main(String[] args) throws IOException{
		new SoTongClient();
	}	
}