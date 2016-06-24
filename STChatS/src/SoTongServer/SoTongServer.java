package SoTongServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class SoTongServer {

   static HashMap<String, PrintWriter> ST_UserSocket; //��������,���� ����
   static HashMap<String, String> ST_UserRoom; //��������,�������� ����
   static ArrayList<String> ST_RoomList_Name; //ä�ù� �̸� ����
   static ServerSocket ST_ServerSocket; //���� ����
   static BufferedWriter ST_ServerLog = null; //���� �α�
   Socket ST_C_Socket = null; //Ŭ���̾�Ʈ ����
   
   
   SoTongServer() throws IOException{
      ST_ServerSocket = new ServerSocket(9900); //���� ���� ��ü ����
      ST_UserSocket = new HashMap<String, PrintWriter>(); //��������,������ ������ �ؽ��� ��ü ����
      ST_UserRoom = new HashMap<String, String>(); //��������,���������� ������ �ؽ��� ��ü ����
      ST_RoomList_Name = new ArrayList<String>(); //ä�ù� ����
      ST_ServerLog = new BufferedWriter(new FileWriter("ST_ServerLog.txt")); //���� �α� ���

      ST_RoomList_Name.add("Lobby"); //���Ƿ� �ʱⰪ ����
      System.out.println("[S]: ������ ���������� ����Ǿ����ϴ�. @author: 1Jungeee");
      while(true){
         System.out.println("[S-Client-Accept]: Ŭ���̾�Ʈ ���� ���..");
         ST_C_Socket = ST_ServerSocket.accept(); //Ŭ���̾�Ʈ ���� �䱸 ���
         System.out.println("[S-Client-Accept]: Ŭ���̾�Ʈ�� �����Ͽ����ϴ�."+" ���� IP: "+ST_C_Socket.getInetAddress());
         
         /* Ŭ���̾�Ʈ ������ ���� */
            Thread chat = new Thread(new SoTongClientThread(ST_C_Socket)); 
            chat.start();   
      }
   }
   
   public static void main(String[] args) throws IOException{
      new SoTongServer();
   }
}