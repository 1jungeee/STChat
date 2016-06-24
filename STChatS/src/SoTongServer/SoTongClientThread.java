package SoTongServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import SoTongDB.SoTongDAO;

public class SoTongClientThread extends Thread {
   /********** 프로토콜 관련 *****************************************
    * [11~20] 클라이언트 접속 관련 //*권한 체크: X
    * [21~30] 유저 정보 관련 - 회원가입, 로그인, 로그아웃 //*권한 체크: X
    * [31~40] 목록 정보 요청 관련 - 채팅방 목록, 사용자 목록 //*권한 체크(C): userId != null
    * [41~50] 룸(채팅방) 관련 생성, 입장 //*권한 체크(C): userId != null
    *       삭제, 추방   //*권한 체크(S): userId == roomNumber.getRoomMaster() 
    *       추   방   //*권한 체크(C): userId.getLocation != 1000 //로비(대기실)이 아닐 경우 즉, 채팅방일 경우만 가능
    *       로비 입장   //*권한 체크(C): userId.getLocation != 1000 //로비(대기실)이 아닐 경우 즉, 채팅방일 경우만 가능
    * [51~60] Email 시스템 관련 - (메일 목록, 메일 읽기, 메일 쓰기, 메일 삭제) IP 기반 DB 조회, 출력, 삭제
    * [61~70] 기본 메시지 전송시스템 관련(특정 명령어가 없을 경우 | 로비 or 룸일 경우)
    * [71~100] 소통: 지능형 채팅 비서 관련(웹 검색 결과 파싱, 파일 및 사진 전송, 이모티콘 지원, 귓속말, 암호화 메시지)
    * [101~120] 관리자 계정 관련 - (사자후 메시지[공지사항], 룸 제거, 특정 사용자 제재) //*권한 체크(S): reqUserID == S-T-Chat.getMasterID() 
    *************************************************************/
   private final int ST_Hello_Ntf = 12;         // 접속성공 환영 메시지 반환
   private final int ST_Join_Req = 21;            // 회원가입 요청
   private final int ST_Join_Res = 22;            // 회원가입 결과 반환
   private final int ST_Login_Req = 23;         // 로그인 요청
   private final int ST_Login_Res = 24;         // 로그인 결과 반환
   private final int ST_Logout_Req = 25;         // 로그아웃 요청
   private final int ST_Logout_Res = 26;         // 로그아웃 결과 반환
   private final int ST_RoomList_Req = 31;         // 채팅방 목록 요청
   private final int ST_RoomList_Res = 32;         // 채팅방 목록 반환
   private final int ST_UserRoomList_Req = 33;         // 사용자 목록 요청
   private final int ST_UserRoomList_Res = 34;         // 사용자 목록 반환
   private final int ST_CreateRoom_Req = 41;      //채팅방 생성 요청
   private final int ST_CreateRoom_Res = 42;      // 채팅방 생성 결과 반환
   private final int ST_EnterRoom_Req = 43;      // 채팅방 입장 요청
   private final int ST_EnterRoom_Res = 44;      // 채팅방 입장 결과 반환
   private final int ST_DeleteRoom_Req = 45;      // 채팅방 삭제 요청
   private final int ST_DeleteRoom_Res = 46;      // 채팅방 삭제 결과 반환
   private final int ST_KickOutRoom_Req = 47;      // 채팅방 추방 요청
   private final int ST_KickOutRoom_Res = 48;      // 채팅방 추방 결과 반환
   private final int ST_EnterLobby_Req = 49;      // 로비 입장 요청
   private final int ST_EnterLobby_Res = 50;      // 로비 입장 결과 반환
   private final int ST_MailList_Req = 51;         // 메일 목록 요청
   private final int ST_MailList_Res = 52;         // 메일 목록 결과 반환
   private final int ST_ReadMail_Req = 53;         // 메일 읽기 요청
   private final int ST_ReadMail_Res = 54;         // 메일 읽기 결과 반환
   private final int ST_WriteMail_Req = 55;      // 메일 쓰기 요청
   private final int ST_WriteMail_Res = 56;      // 메일 쓰기 결과 반환
   private final int ST_DeleteMail_Req = 57;      // 메일 삭제 요청
   private final int ST_DeleteMail_Res = 58;      // 메일 삭제 결과 반환
   private final int ST_SendMessage_Req = 61;      // 메시지 전송 요청
   private final int ST_SendMessage_Res = 62;      // 메시지 전송결과 반환
   private final int ST_WebSearch_Req = 71;      // [소통] 웹검색 전송 요청
   private final int ST_WebSearch_Res = 72;      // [소통] 웹검색 전송결과 반환
   private final int ST_SendFile_Req = 73;         // 파일 전송 요청
   private final int ST_SendFile_Res = 74;         // 파일 전송결과 반환
   private final int ST_DownFile_Req = 75;         // 파일 다운 요청
   private final int ST_DownFile_Res = 76;         // 파일 다운결과 반환
   private final int ST_SendEmoticon_Req = 77;      // [소통] 이모티콘 전송 요청
   private final int ST_SendEmoticon_Res = 78;      // [소통] 이모티콘 전송결과 반환
   private final int ST_SendWhisper_Req = 79;      // [소통] 귓속말 전송 요청
   private final int ST_SendWhisper_Res = 80;      // [소통] 귓속말 전송결과 반환
   private final int ST_SendEncryMessage_Req = 81;   // [소통] 암호화 메시지 전송 요청
   private final int ST_SendEncryMessage_Res = 82;   // [소통] 암호화 메시지 전송결과 반환
   private final int ST_AdminMessage_Req = 101;   // [관리자] 사자후 메시지 전송 요청
   private final int ST_AdminMessage_Res = 102;   // [관리자] 사자후 메시지 전송결과 반환
   private final int ST_AdminDeleteRoom_Req = 103;   // [관리자] 채팅방 삭제 전송 요청
   private final int ST_AdminDeleteRoom_Res = 104;   // [관리자] 채팅방 삭제 전송결과 반환
   private final int ST_UpdateBlackListe_Req = 105;   // [관리자] 블랙리스트 업데이트 전송 요청
   private final int ST_UpdateBlackListe_Res = 106;   // [관리자] 블랙리스트 업데이트 전송결과 반환
   
   private Socket ST_C_Socket = null;
   private String roomName;
   private PrintWriter ST_S_PW = null; //클라이언트 출력 스트림
   private BufferedReader ST_S_BR = null; //클라이언트 입력 스트림
   private SoTongDAO DAO;
   private static int check = 0; //값 체크를 위한 변수
   private static String temp = ""; //값을 담을 템프 변수
   public SoTongClientThread(Socket ST_C_Socket) throws IOException{
      this.ST_C_Socket = ST_C_Socket;

      DAO = new SoTongDAO();
      // [C->S] 입력 스트림 객체 생성
      ST_S_BR = new BufferedReader(new InputStreamReader(this.ST_C_Socket.getInputStream()));   
      
      // [S->C] 출력 스트림 객체 생성 
      ST_S_PW = new PrintWriter(new OutputStreamWriter(this.ST_C_Socket.getOutputStream()));
      
      /* 접속 환영(ST_Hello_Ntf) 메시지 전송 */
      ST_S_PW.println(ST_Hello_Ntf());
      ST_S_PW.flush();
   }
   
   @Override
   public void run(){
      try{
         String line = null;
         while((line = ST_S_BR.readLine()) != null){ controller(line); }
      }catch(Exception ex){
         
      }finally{
         
      }
   }
   
   public void controller(String msg) throws IOException{
      String[] ArrayMsg = msg.split("\\|");
      switch (Integer.parseInt(ArrayMsg[0])) {
         case ST_Join_Req:   // 회원가입 요청
            serverLog("[S-ST_Join_Req]: 클라이언트로부터 회원가입 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Join_Req]: 클라이언트로부터 회원가입 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller(DAO.Join(ArrayMsg[1],ArrayMsg[2]));
            break;
         case ST_Join_Res:   // 회원가입 결과 반환
            serverLog("[S-ST_Join_Res]: 회원가입 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_Join_Res]: 회원가입 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_Login_Req:   // 로그인 요청
            serverLog("[S-ST_Login_Req]: 클라이언트로부터 로그인 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Login_Req]: 클라이언트로부터 로그인 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller(DAO.Login(ArrayMsg[1],ArrayMsg[2]));
            break;
         case ST_Login_Res:   // 로그인 결과 반환
            serverLog("[S-ST_Login_Res]: 로그인 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_Login_Res]: 로그인 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("1")){ //로그인에 성공하였을 경우
               SoTongServer.ST_UserRoom.put(ArrayMsg[2], "Lobby"); //해쉬맵 객체에 유저 및 위치 등록
               SoTongServer.ST_UserSocket.put(ArrayMsg[2], ST_S_PW); //해쉬맵 객체에 유저 및 소켓출력 등록
            }
            serverLog("[S-ST_UserRoomList]: 현재 접속자 리스트 업데이트"+SoTongServer.ST_UserRoom.keySet());System.out.println("[S-ST_UserRoomList]: 현재 접속자 리스트 업데이트"+SoTongServer.ST_UserRoom.keySet()); //현재 접속유저 리스트 
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_Logout_Req:   // 로그아웃 요청
            serverLog("[S-ST_Logout_Req]: 클라이언트로부터 로그아웃 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Logout_Req]: 클라이언트로부터 로그아웃 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("26|1|"+ArrayMsg[1]);
            break;
         case ST_Logout_Res:   // 로그아웃 결과 반환
            serverLog("[S-ST_Logout_Res]: 로그아웃 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_Logout_Res]: 로그아웃 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("1")){ //로그아웃에 성공하였을 경우
               SoTongServer.ST_UserRoom.remove(ArrayMsg[2]); //해쉬맵 객체에 로그아웃 된 유저 정보 제거
               SoTongServer.ST_UserSocket.remove(ArrayMsg[2]); //해쉬맵 객체에 로그아웃 된 유저 및 소켓출력 정보 제거등록
            }
            serverLog("[S-ST_UserRoomList]: 현재 접속자 리스트 업데이트"+SoTongServer.ST_UserRoom.keySet());System.out.println("[S-ST_UserRoomList]: 현재 접속자 리스트 업데이트"+SoTongServer.ST_UserRoom.keySet()); //현재 접속유저 리스트 
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_RoomList_Req:   // 채팅방 목록 요청
            serverLog("[S-ST_Logout_Req]: 클라이언트로부터 채팅방 목록 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Logout_Req]: 클라이언트로부터 채팅방 목록 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("32|"+ArrayMsg[1]);
            break;
         case ST_RoomList_Res:   // 채팅방 목록 반환
            temp = "32|";
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){
               temp += "["+SoTongServer.ST_RoomList_Name.get(i)+"]"; 
            }
            serverLog("[S-ST_RoomList_Res]: 채팅방 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+temp);System.out.println("[S-ST_RoomList_Res]: 채팅방 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+temp);
            ST_S_PW.println(temp);
            ST_S_PW.flush();
            break;
         case ST_UserRoomList_Req:   // 사용자 목록 요청
            serverLog("[S-ST_UserRoomList_Req]: 클라이언트로부터 사용자 목록 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_UserRoomList_Req]: 클라이언트로부터 사용자 목록 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("34|"+ArrayMsg[1]);
            break;
         case ST_UserRoomList_Res:   // 사용자 목록 반환
            check = 0; // 사용자 목록 반환에 앞서 채팅방 존재 확인에 사용될 변수 초기화
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){ // 기존 채팅방 리스트에서 입장 요청 채팅방이 존재하는지 확인
               if(ArrayMsg[1].equals(SoTongServer.ST_RoomList_Name.get(i))){
                  check = 1; // 채팅방이 존재할 경우 check값 1로 변환
               }
            }
            if(check==1){ // 채팅방이 존재할 경우
               temp = getUserList(ArrayMsg[1]); // 사용자 목록 반환 함수 호출 및 반환 값 temp변수에 저장
               serverLog("[S-ST_UserRoomList_Res]: 사용자 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|1|"+ArrayMsg[1]+"|"+temp);System.out.println("[S-ST_UserRoomList_Res]: 사용자 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|1|"+ArrayMsg[1]+"|"+temp);
               ST_S_PW.println("34|1|"+ArrayMsg[1]+"|"+temp);
               ST_S_PW.flush();
            }else{ // 채팅방이 존재하지 않을 경우
               ST_S_PW.println("34|2|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_UserRoomList_Res]: 사용자 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|2"+ArrayMsg[1]);System.out.println("[S-ST_UserRoomList_Res]: 사용자 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|2"+ArrayMsg[1]);
            }
            break;
         case ST_CreateRoom_Req:   // 채팅방 생성 요청
            serverLog("[S-ST_CreateRoom_Req]: 클라이언트로부터 채팅방 생성 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_CreateRoom_Req]: 클라이언트로부터 채팅방 생성 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            if(ArrayMsg.length==3){
               controller("42|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
            }else{
               controller("42|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            }
            break;
         case ST_CreateRoom_Res:   // 채팅방 생성 결과 반환
            check = 0; // 채팅방 이름 중복 확인
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){ // 기존 채팅방 리스트에서 요청 채팅방 이름이 존재하는지 확인
               if(ArrayMsg[1].equals(SoTongServer.ST_RoomList_Name.get(i))){
                  check = 1; // 채팅방 이름이 중복될 경우 check값 1로 변환
               }
            }
            if(check==1){ // 채팅방 이름이 중복 될 경우
               ST_S_PW.println("42|2|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: 채팅방 생성 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|2|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: 채팅방 생성 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|2|"+ArrayMsg[1]);
            }else{
               SoTongServer.ST_RoomList_Name.add(ArrayMsg[1]); // 채팅방 관리 객체에 채팅방 추가
               ST_S_PW.println("42|1|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: 채팅방 생성 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|1|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: 채팅방 생성 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|1|"+ArrayMsg[1]);
            }
            break;
         case ST_EnterRoom_Req:   // 채팅방 입장 요청
            serverLog("[S-ST_EnterRoom_Req]: 클라이언트로부터 채팅방 입장 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_EnterRoom_Req]: 클라이언트로부터 채팅방 입장 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("44|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
            break;
         case ST_EnterRoom_Res:   // 채팅방 입장 결과 반환
            check = 0; // 입장 요청 채팅방 존재 확인에 사용될 변수 초기화
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){ // 기존 채팅방 리스트에서 입장 요청 채팅방이 존재하는지 확인
               if(ArrayMsg[1].equals(SoTongServer.ST_RoomList_Name.get(i))){
                  check = 1; // 채팅방 이름이 중복될 경우 check값 1로 변환
               }
            }
            if(check==1){ // 채팅방이 존재할 경우
               SoTongServer.ST_UserRoom.put(ArrayMsg[2], ArrayMsg[1]); //해쉬맵 객체에 유저 및 위치 등록
               intoRoomBroadcast(ArrayMsg[1], ArrayMsg[2]); //새로운 클라이언트 입장 정보 해당 채팅방에 브로드캐스트
               ST_S_PW.println("44|1|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: 채팅방 입장 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|1|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: 채팅방 입장 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|1|"+ArrayMsg[1]);
               }else{ // 채팅방이 존재하지 않을 경우
               ST_S_PW.println("44|2|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: 채팅방 입장 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|2|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: 채팅방 입장 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|2|"+ArrayMsg[1]);
            }
            break;
         case ST_DeleteRoom_Req:   // 채팅방 삭제 요청
            break;
         case ST_DeleteRoom_Res:   // 채팅방 삭제 결과 반환
            break;
         case ST_KickOutRoom_Req:   // 채팅방 추방 요청
            break;
         case ST_KickOutRoom_Res:   // 채팅방 추방 결과 반환
            break;
         case ST_EnterLobby_Req:   // 로비 입장 요청
            break;
         case ST_EnterLobby_Res:   // 로비 입장 결과 반환
            break;
         case ST_MailList_Req:   // 메일 목록 요청
            serverLog("[S-ST_MailList_Req]: 클라이언트로부터 메일 목록 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_MailList_Req]: 클라이언트로부터 메일 목록 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //메일 목록 요청 클라이언트 IP(Destination IP Address)
            controller(DAO.MailList(temp));
            break;
         case ST_MailList_Res:   // 메일 목록 결과 반환
            serverLog("[S-ST_MailList_Res]: 메일 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_MailList_Res]: 메일 목록 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_ReadMail_Req:   // 메일 읽기 요청
            serverLog("[S-ST_ReadMail_Req]: 클라이언트로부터 메일 읽기 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_ReadMail_Req]: 클라이언트로부터 메일 읽기 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //메일 목록 요청 클라이언트 IP(Destination IP Address)
            controller(DAO.ReadMail(temp, ArrayMsg[1])); 
            break;
         case ST_ReadMail_Res:   // 메일 읽기 결과 반환
            serverLog("[S-ST_ReadMail_Res]: 메일 읽기 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_ReadMail_Res]: 메일 읽기 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_WriteMail_Req:   // 메일 쓰기 요청
            serverLog("[S-ST_WriteMail_Req]: 클라이언트로부터 메일 쓰기 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_WriteMail_Req]: 클라이언트로부터 메일 쓰기 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //메일 쓰기 요청 클라이언트 IP(Source IP Address)
            controller(DAO.WriteMail(ArrayMsg[1], ""+temp, ArrayMsg[2], ArrayMsg[3]));
            break;
         case ST_WriteMail_Res:   // 메일 쓰기 결과 반환
            serverLog("[S-ST_WriteMail_Res]: 메일 쓰기 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_WriteMail_Res]: 메일 쓰기 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_DeleteMail_Req:   // 메일 삭제 요청
            serverLog("[S-ST_DeleteMail_Req]: 클라이언트로부터 메일 삭제 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_DeleteMail_Req]: 클라이언트로부터 메일 삭제 요청이 들어왔습니다. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //메일 쓰기 요청 클라이언트 IP(Source IP Address)
            controller(DAO.DeleteMail(""+temp, ArrayMsg[1]));
            break;
         case ST_DeleteMail_Res:   // 메일 삭제 결과 반환
            serverLog("[S-ST_DeleteMail_Res]: 메일 삭제 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_DeleteMail_Res]: 메일 삭제 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_SendMessage_Req:   // 메시지 전송 요청
            serverLog("[S-ST_SendMessage_Req]: 클라이언트로부터 메시지 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendMessage_Req]: 클라이언트로부터 메시지 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("62|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendMessage_Res:   // 메시지 전송결과 반환
            serverLog("[S-ST_SendMessage_Res]: 메시지 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendMessage_Res]: 메시지 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            broadcast(ArrayMsg[1], ArrayMsg[2], ArrayMsg[3]);
            break;
         case ST_WebSearch_Req:   // [소통] 웹검색 전송 요청(현재날씨) - 클라이언트 단에서 접속위치를 기반으로 수행
            break;
         case ST_WebSearch_Res:   // [소통] 웹검색 전송결과 반환(현재날씨) - 클라이언트 단에서 접속위치를 기반으로 수행
            break;
         case ST_SendFile_Req:   // 파일 전송 요청
            break;
         case ST_SendFile_Res:   // 파일 전송결과 반환
            break;
         case ST_DownFile_Req:   // 파일 다운 요청
            break;
         case ST_DownFile_Res:   // 파일 다운결과 반환
            break;
         case ST_SendEmoticon_Req:   // [소통] 이모티콘 전송 요청
            serverLog("[S-ST_SendEmoticon_Req]: 클라이언트로부터 이모티콘 전송 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEmoticon_Req]: 클라이언트로부터 이모티콘 전송 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("78|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendEmoticon_Res:   // [소통] 이모티콘 전송결과 반환
            serverLog("[S-ST_SendEmoticon_Res]: 이모티콘 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEmoticon_Res]: 이모티콘 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("사랑해")){
               temp = "♥♥♥사랑해~~♥♥♥";
               broadcast(temp, ArrayMsg[2], ArrayMsg[3]);
               ST_S_PW.println("78|1|♥♥♥사랑해~~♥♥♥");
               ST_S_PW.flush();
            }else if(ArrayMsg[1].equals("졸려")){
               temp = "...zZ..zZ...졸렬...zZ..";
               broadcast(temp, ArrayMsg[2], ArrayMsg[3]);
               ST_S_PW.println("78|1|...zZ..zZ...졸렬...zZ..");
               ST_S_PW.flush();
            }else{
               ST_S_PW.println("78|2");
               ST_S_PW.flush();
            }
            break;
         case ST_SendWhisper_Req:   // [소통] 귓속말 전송 요청
            serverLog("[S-ST_SendWhisper_Req]: 클라이언트로부터 귓속말 전송 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendWhisper_Req]: 클라이언트로부터 귓속말 전송 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("80|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendWhisper_Res:   // [소통] 귓속말 전송결과 반환
            temp = sendWhisper(ArrayMsg[1], ArrayMsg[3], ArrayMsg[2]);
            if(temp.equals("1")){ // 귓속말 상대가 존재할 경우(귓속말 성공)
               msg = "80|1|"+ArrayMsg[1]+"|"+ArrayMsg[3];
               serverLog("[S-ST_SendWhisper_Res]: 귓속말 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendWhisper_Res]: 귓속말 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
               ST_S_PW.println(msg);
               ST_S_PW.flush();
            }else{ // 귓속말 상대가 존재하지 않을 경우(귓속말 실패)
               msg = "80|2|"+ArrayMsg[1]+"|"+ArrayMsg[3];
               serverLog("[S-ST_SendWhisper_Res]: 귓속말 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendWhisper_Res]: 귓속말 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
               ST_S_PW.println(msg);
               ST_S_PW.flush();
            }
            break;
         case ST_SendEncryMessage_Req:   // [소통] 암호화 메시지 전송 요청
            serverLog("[S-ST_SendEncryMessage_Req]: 클라이언트로부터 암호화 메시지 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEncryMessage_Req]: 클라이언트로부터 암호화 메시지 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("82|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendEncryMessage_Res:   // [소통] 암호화 메시지 전송결과 반환
            serverLog("[S-ST_SendEncryMessage_Res]: 암호화 메시지 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEncryMessage_Res]: 암호화 메시지 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            EncryBroadcast(ArrayMsg[1], ArrayMsg[2], ArrayMsg[3]);
            break;
         case ST_AdminMessage_Req:   // [관리자] 사자후 메시지 전송 요청
            serverLog("[S-ST_AdminMessage_Req]: 관리자로부터 사자후 메시지 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminMessage_Req]: 관리자로부터 사자후 메시지 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("102|"+ArrayMsg[1]);
            break;
         case ST_AdminMessage_Res:   // [관리자] 사자후 메시지 전송결과 반환
            serverLog("[S-ST_AdminMessage_Res]: 사자후 메시지 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminMessage_Res]: 사자후 메시지 전송 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            AdminBroadcast(ArrayMsg[1]);
            break;
         case ST_AdminDeleteRoom_Req:   // [관리자] 채팅방 삭제 전송 요청
            serverLog("[S-ST_AdminDeleteRoom_Req]: 관리자로부터 채팅방 삭제 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminDeleteRoom_Req]: 관리자로부터 채팅방 삭제 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("104|"+ArrayMsg[1]);
            break;
         case ST_AdminDeleteRoom_Res:   // [관리자] 채팅방 삭제 전송결과 반환
            serverLog("[S-ST_AdminDeleteRoom_Res]: 채팅방 삭제 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminDeleteRoom_Res]: 채팅방 삭제 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(getAdminDeleteRoomResult(ArrayMsg[1])==0){
               ST_S_PW.println("104|2");
               ST_S_PW.flush();
            }else{
               ST_S_PW.println("104|3|"+ArrayMsg[1]);
               ST_S_PW.flush();
            }
            break;   
         case ST_UpdateBlackListe_Req:   // [관리자] 블랙리스트 업데이트 전송 요청
            serverLog("[S-ST_UpdateBlackListe_Req]: 관리자로부터 블랙리스트 업데이트 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_UpdateBlackListe_Req]: 관리자로부터 블랙리스트 업데이트 요청이 들어왔습니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("1")){ //블랙리스트 추가일 경우
               controller(DAO.BlackListUpdateInto(ArrayMsg[2]));
            }else{ //블랙리스트 삭제일 경우(2)
               controller(DAO.BlackListUpdateDelete(ArrayMsg[2]));
            }
            break;
         case ST_UpdateBlackListe_Res:   // [관리자] 블랙리스트 업데이트 전송결과 반환
            msg=msg+"|"+DAO.BlackListUpdateList();
            serverLog("[S-ST_UpdateBlackListe_Res]: 블랙리스트 업데이트 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_UpdateBlackListe_Res]: 블랙리스트 업데이트 요청에 대한 결과 값을 반환합니다. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            serverLog("[S-ST_BlackListList]: 블랙 리스트를 업데이트 합니다."+DAO.BlackListUpdateList());System.out.println("[S-ST_BlackListList]: 블랙 리스트를 업데이트 합니다."+DAO.BlackListUpdateList()); //현재 블랙리스트 업데이트 
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         default:
            break;
      }
   }
   
   /* ST_Hello_Ntf: 접속성공 환영메시지 반환 */
   private String ST_Hello_Ntf(){
      return "12|Hello So-Tong Chat!";
   }
   
   /* 브로드 캐스트(채팅방 명을 기준으로 기본채팅에 사용) */
   public void broadcast(String msg, String roomName, String reqUserId) {
      
      /*
       * SoTongServer.ST_UserRoom = 유저아이디 / 유저위치
       * SoTongServer.ST_UserSocket = 유저아이디 / 유저소켓출력객체
       */
      synchronized (SoTongServer.ST_UserSocket) {
         Collection collection = SoTongServer.ST_UserSocket.values();
         Iterator iter = collection.iterator();

         Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();

         int i = 0;
         while (iter.hasNext()) {
            if(SoTongServer.ST_UserRoom.get(arr[i].toString()).equals(roomName)){
               PrintWriter pwb = (PrintWriter) iter.next();
               pwb.println("62|[ST-Chat]["+roomName+"]["+reqUserId+"]: "+msg+"|"+reqUserId);
               pwb.flush();
            }else{
               iter.next();
            }
            i++;
         }
      }
   }
   
   /* 채팅방에 새로운 클라이언트가 입장하였을 경우 입장메시지 브로드 캐스트 전달 */
   public void intoRoomBroadcast(String roomName, String reqUserId) {
      /*
       * SoTongServer.ST_UserRoom = 유저아이디 / 유저위치
       * SoTongServer.ST_UserSocket = 유저아이디 / 유저소켓출력객체
       */
      synchronized (SoTongServer.ST_UserSocket) {
         Collection collection = SoTongServer.ST_UserSocket.values();
         Iterator iter = collection.iterator();

         Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();

         int i = 0;
         while (iter.hasNext()) {
            if(SoTongServer.ST_UserRoom.get(arr[i].toString()).equals(roomName)){
               PrintWriter pwb = (PrintWriter) iter.next();
               pwb.println("62|[ST-Chat]["+roomName+"]["+reqUserId+"]: 님이 입장하셨습니다.|"+reqUserId); //입장 해당 채팅방 브로드캐스트 메시지 전달
               pwb.flush();
            }else{
               iter.next();
            }
            i++;
         }
      }
   }
   
   /* 채팅방 이름을 매개변수로 입력받아 사용자 리스트를 반환하는 함수 */
   public String getUserList(String roomName){
      temp = "";
      Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();
      for(int i=0; i<SoTongServer.ST_UserRoom.size(); i++){ //유저 수 만큼 만복
         if(SoTongServer.ST_UserRoom.get(arr[i].toString()).equals(roomName)){ //유저 키값으로 현재 채팅방 위치를 검색했을 때 요청 채팅방하고 일치하였을 경우
            temp += "["+arr[i].toString()+"]";
         }else{
         }
      }
      return temp;
   }
   
   /* [소통] 귓속말 요청에 따른 1:1통신 */
   public String sendWhisper(String desUserId, String srcUserId, String msg) {
      /*
       * SoTongServer.ST_UserSocket = 유저아이디 / 유저소켓출력객체
       */
     String result="2";
      synchronized (SoTongServer.ST_UserSocket) {
         PrintWriter pwb;
         if(SoTongServer.ST_UserSocket.get(desUserId) != null){
            pwb = SoTongServer.ST_UserSocket.get(desUserId);
            pwb.println("62|[ST-Chat][귓속말받음]["+srcUserId+"]: "+msg+"|"+srcUserId); //귓속말 전달
            pwb.flush();
            result="1"; //귓속말에 성공하였을 경우
         }else{
            result="2"; //귓속말에 실패하였을 경우
         }
      }
   return result;
   }
   
   /* [소통] 암호화 메시지 전송 브로드캐스트 */
   public void EncryBroadcast(String msg, String roomName, String reqUserId) {
      
      /*
       * SoTongServer.ST_UserRoom = 유저아이디 / 유저위치
       * SoTongServer.ST_UserSocket = 유저아이디 / 유저소켓출력객체
       */
      synchronized (SoTongServer.ST_UserSocket) {
         Collection collection = SoTongServer.ST_UserSocket.values();
         Iterator iter = collection.iterator();

         Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();

         int i = 0;
         while (iter.hasNext()) {
            if(SoTongServer.ST_UserRoom.get(arr[i].toString()).equals(roomName)){
               PrintWriter pwb = (PrintWriter) iter.next();
               pwb.println("82|"+msg+"|"+roomName+"|"+reqUserId);
               pwb.flush();
            }else{
               iter.next();
            }
            i++;
         }
      }
   }

   /* [관리자] 사자후 메시지 전송 브로드캐스트 */
   public void AdminBroadcast(String msg) {
      
      /*
       * SoTongServer.ST_UserRoom = 유저아이디 / 유저위치
       * SoTongServer.ST_UserSocket = 유저아이디 / 유저소켓출력객체
       */
      synchronized (SoTongServer.ST_UserSocket) {
         Collection collection = SoTongServer.ST_UserSocket.values();
         Iterator iter = collection.iterator();

         Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();

         int i = 0;
         while (iter.hasNext()) {
            PrintWriter pwb = (PrintWriter) iter.next();
            pwb.println("102|"+msg);
             pwb.flush();
            i++;
         }
      }
   }
   
   /* [관리자] 채팅방 삭제 결과를 반환하는 함수 */
   public int getAdminDeleteRoomResult(String roomName){

         /*
          * SoTongServer.ST_UserRoom = 유저아이디 / 유저위치
          * SoTongServer.ST_UserSocket = 유저아이디 / 유저소켓출력객체
          */
      check = 0; // 채팅방 삭제에 앞서 채팅방 존재 확인에 사용될 변수 초기화
      for(int a=0; a<SoTongServer.ST_RoomList_Name.size(); a++){ // 기존 채팅방 리스트에서 삭제 요청 채팅방이 존재하는지 확인
         if(roomName.equals(SoTongServer.ST_RoomList_Name.get(a))){
            check = 1; // 채팅방이 존재할 경우 check값 1로 변환
            SoTongServer.ST_RoomList_Name.remove(a); //해당 채팅방 삭제
         }
      }
      if(check==1){
         synchronized (SoTongServer.ST_UserSocket) {
               Collection collection = SoTongServer.ST_UserSocket.values();
               Iterator iter = collection.iterator();

               Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();

               int i = 0;
               while (iter.hasNext()) {
                  if(SoTongServer.ST_UserRoom.get(arr[i].toString()).equals(roomName)){
                     SoTongServer.ST_UserRoom.put(arr[i].toString(), "Lobby"); //해쉬맵 객체에 해당 유저 로비로 업데이트
                     PrintWriter pwb = (PrintWriter) iter.next();
                     pwb.println("104|1|");
                     pwb.flush();
                  }else{
                     iter.next();
                  }
                  i++;
               }
            }
      }else{
      }
      return check;
   }
   
   public void serverLog(String str) throws IOException{
      SoTongServer.ST_ServerLog.write(str);
      SoTongServer.ST_ServerLog.newLine();
      SoTongServer.ST_ServerLog.flush();
   }
}