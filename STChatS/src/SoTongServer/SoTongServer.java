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

   static HashMap<String, PrintWriter> ST_UserSocket; //접속유저,소켓 관리
   static HashMap<String, String> ST_UserRoom; //접속유저,현재위지 관리
   static ArrayList<String> ST_RoomList_Name; //채팅방 이름 관리
   static ServerSocket ST_ServerSocket; //서버 소켓
   static BufferedWriter ST_ServerLog = null; //서버 로그
   Socket ST_C_Socket = null; //클라이언트 소켓
   
   
   SoTongServer() throws IOException{
      ST_ServerSocket = new ServerSocket(9900); //서버 소켓 객체 생성
      ST_UserSocket = new HashMap<String, PrintWriter>(); //접속유저,소켓을 관리할 해쉬맵 객체 생성
      ST_UserRoom = new HashMap<String, String>(); //접속유저,현재위지를 관리할 해쉬맵 객체 생성
      ST_RoomList_Name = new ArrayList<String>(); //채팅방 관리
      ST_ServerLog = new BufferedWriter(new FileWriter("ST_ServerLog.txt")); //서버 로그 출력

      ST_RoomList_Name.add("Lobby"); //대기실룸 초기값 셋팅
      System.out.println("[S]: 서버가 정상적으로 실행되었습니다. @author: 1Jungeee");
      while(true){
         System.out.println("[S-Client-Accept]: 클라이언트 접속 대기..");
         ST_C_Socket = ST_ServerSocket.accept(); //클라이언트 접속 요구 대기
         System.out.println("[S-Client-Accept]: 클라이언트가 접속하였습니다."+" 접속 IP: "+ST_C_Socket.getInetAddress());
         
         /* 클라이언트 스레드 생성 */
            Thread chat = new Thread(new SoTongClientThread(ST_C_Socket)); 
            chat.start();   
      }
   }
   
   public static void main(String[] args) throws IOException{
      new SoTongServer();
   }
}