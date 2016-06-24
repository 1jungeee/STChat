package SoTongClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public class SoTongController {
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

   private final String ST_Join_Req = "#Join";                        // 21 회원가입 요청
   private final String ST_Login_Req = "#Login";                     // 23 로그인 요청
   private final String ST_Logout_Req = "#Logout";                     // 25 로그아웃 요청
   private final String ST_RoomList_Req = "#RoomList";                  // 채팅방 목록 요청
   private final String ST_UserList_Req = "#UserList";                  // 사용자 목록 요청
   private final String ST_CreateRoom_Req = "#CreateRoom";               // 채팅방 생성 요청
   private final String ST_EnterRoom_Req = "#EnterRoom";               // 채팅방 입장 요청
   private final String ST_DeleteRoom_Req = "45";                     // 채팅방 삭제 요청
   private final String ST_KickOutRoom_Req = "47";                     // 채팅방 추방 요청
   private final String ST_EnterLobby_Req = "49";                     // 로비 입장 요청
   private final String ST_MailList_Req = "#MailList";                  // 메일 목록 요청
   private final String ST_ReadMail_Req = "#MailRead";                  // 메일 읽기 요청
   private final String ST_WriteMail_Req = "#MailWrite";               // 메일 쓰기 요청
   private final String ST_DeleteMail_Req = "#MailDelete";               // 메일 삭제 요청
   private final String ST_SendMessage_Req = "61";                     // 메시지 전송 요청
   private final String ST_WebSearch_Req = "#ST-nowWeather";            // [소통] 웹검색 전송 요청(현재날씨)
   private final String ST_SendFile_Req = "73";                     // 파일 전송 요청
   private final String ST_DownFile_Req = "75";                     // 파일 다운 요청
   private final String ST_SendEmoticon_Req = "#ST-*";                  // [소통] 이모티콘 전송 요청
   private final String ST_SendWhisper_Req = "#ST-/w";                  // [소통] 귓속말 전송 요청
   private final String ST_SendEncryMessage_Req = "#ST-/s";            // [소통] 암호화 메시지 전송 요청(AES알고리즘 사용, 클라이언트 단에서 암/복호화)
   private final String ST_AdminMessage_Req = "$Ntf";                  // [관리자] 사자후 메시지 전송 요청
   private final String ST_AdminDeleteRoom_Req = "$DeleteRoom";         // [관리자] 채팅방 삭제 전송 요청
   private final String ST_AdminUpdateBlackListe_Req = "$UpdateBlackList";   // [관리자] 블랙리스트 업데이트 전송 요청
   private final int ST_Hello_Ntf = 12;                           // 접속성공 환영 메시지 반환
   private final int ST_Join_Res = 22;                              // 회원가입 결과 반환
   private final int ST_Login_Res = 24;                           // 로그인 결과 반환
   private final int ST_Logout_Res = 26;                           // 로그아웃 결과 반환
   private final int ST_RoomList_Res = 32;                           // 채팅방 목록 반환
   private final int ST_UserList_Res = 34;                           // 사용자 목록 반환
   private final int ST_CreateRoom_Res = 42;                        // 채팅방 생성 결과 반환
   private final int ST_EnterRoom_Res = 44;                        // 채팅방 입장 결과 반환
   private final int ST_DeleteRoom_Res = 46;                        // 채팅방 삭제 결과 반환
   private final int ST_KickOutRoom_Res = 48;                        // 채팅방 추방 결과 반환
   private final int ST_EnterLobby_Res = 50;                        // 로비 입장 결과 반환
   private final int ST_MailList_Res = 52;                           // 메일 목록 결과 반환
   private final int ST_ReadMail_Res = 54;                           // 메일 읽기 결과 반환
   private final int ST_WriteMail_Res = 56;                        // 메일 쓰기 결과 반환
   private final int ST_DeleteMail_Res = 58;                        // 메일 삭제 결과 반환
   private final int ST_SendMessage_Res = 62;                        // 메시지 전송결과 반환
   private final int ST_WebSearch_Res = 72;                        // [소통] 웹검색 전송결과 반환(현재날씨)
   private final int ST_SendFile_Res = 74;                           // 파일 전송결과 반환
   private final int ST_DownFile_Res = 76;                           // 파일 다운결과 반환
   private final int ST_SendEmoticon_Res = 78;                        // [소통] 이모티콘 전송결과 반환
   private final int ST_SendWhisper_Res = 80;                        // [소통] 귓속말 전송결과 반환
   private final int ST_SendEncryMessage_Res = 82;                     // [소통] 암호화 메시지 전송결과 반환
   private final int ST_AdminMessage_Res = 102;                     // [관리자] 사자후 메시지 전송결과 반환
   private final int ST_AdminDeleteRoom_Res = 104;                     // [관리자] 채팅방 삭제 전송결과 반환
   private final int ST_AdminUpdateBlackListe_Res = 106;               // [관리자] 블랙리스트 업데이트 전송결과 반환
   
   private Socket ST_C_Socket = null;
   private static PrintWriter ST_C_PW = null; //[C->S 출력 스트림]
   private static String ST_C_Location = "guest"; //사용자 위치 guest=비로그인 
   private static String ST_C_Id = null; //사용자 로그인 아이디
   SoTongController(){
      
   }
   SoTongController(Socket ST_C_Socket) throws IOException{
      this.ST_C_Socket = ST_C_Socket;
      ST_C_PW = new PrintWriter(new OutputStreamWriter(this.ST_C_Socket.getOutputStream()));
   }
   
   public void controllerReq(String msg) throws Exception{
      String[] ArrayMsg = msg.split(" ");
      switch (ArrayMsg[0]) {
         case ST_Join_Req:             //21 회원가입 요청
            if(ArrayMsg.length==3){
               ST_C_PW.println("21|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 회원가입 명령어 형식을 확인해주세요. \"#Join [JoinID] [JoinPwd]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_Login_Req:            //23 로그인 요청
            if(ST_C_Id != null){ //이미 로그인 한 상태에서 로그인을 요청하였을 경우
               System.out.println("[error]: 로그아웃을 먼저 해주세요. \"#Logout\"");
            }else if(ArrayMsg.length==3){
               ST_C_PW.println("23|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 로그인 명령어 형식을 확인해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_Logout_Req:            //25 로그아웃 요청
            if(ST_C_Id != null){
               ST_C_PW.println("25|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_RoomList_Req:         //31 채팅방 목록 요청
            if(ST_C_Id != null){
               ST_C_PW.println("31|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_UserList_Req:         //33 사용자 목록 요청
            if(ST_C_Id == null){ 
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("33|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 사용자 목록 요청 명령어 형식을 확인해주세요. \"#UserList [RoomName]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_CreateRoom_Req:         //41채팅방 생성 요청
            if(ST_C_Id == null){ 
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("41|"+ArrayMsg[1]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else if(ArrayMsg.length==3){
               ST_C_PW.println("41|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 채팅방 생성 명령어 형식을 확인해주세요. \"#CreateRoom [RoomName]\" ('[',']'는 생략)");
               System.out.println("[error]: 비공개 채팅방의 경우  \"#CreateRoom [RoomName] [RoomPwd]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_EnterRoom_Req:         //43 채팅방 입장 요청
            if(ST_C_Id == null){ 
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("43|"+ArrayMsg[1]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 채팅방 입장 명령어 형식을 확인해주세요. \"#EnterRoom [RoomName]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_DeleteRoom_Req:         //45 채팅방 삭제 요청
            break;
         case ST_KickOutRoom_Req:      //47 채팅방 추방 요청
            break;
         case ST_EnterLobby_Req:         //49 로비 입장 요청
            break;
         case ST_MailList_Req:         //51 메일 목록 요청
            ST_C_PW.println("51|");
            ST_C_PW.flush();
            break;
         case ST_ReadMail_Req:         //53 메일 읽기 요청
            if(ArrayMsg.length==2){
               ST_C_PW.println("53|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 메일 읽기 명령어 형식을 확인해주세요. \"#ReaMail [MailName]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_WriteMail_Req:         //55 메일 쓰기 요청
            if(ArrayMsg.length==4){
               ST_C_PW.println("55|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 메일 작성 명령어 형식을 확인해주세요. \"#WriteMail [DestinationUserIP] [MailTitle] [MailContent]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_DeleteMail_Req:         //57 메일 삭제 요청
            if(ArrayMsg.length==2){
               ST_C_PW.println("57|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 메일 삭제 명령어 형식을 확인해주세요. \"#MailDelete [MailName]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_WebSearch_Req:         //71 [소통] 웹검색 전송 요청(현재날씨)
            controllerRes("72|");
            break;
         case ST_SendFile_Req:         //73 파일 전송 요청
            break;
         case ST_DownFile_Req:         //75 파일 다운 요청
            break;
         case ST_SendEmoticon_Req:      //77 [소통] 이모티콘 전송 요청
            if(ST_C_Id == null){ //비회원 접속에서 불가능한 명령어 임
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("77|"+ArrayMsg[1]+"|"+ST_C_Location+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 이모티콘 전송 명령어 형식을 확인해주세요. \"#ST-* [EmoticonType]\" ('[',']'는 생략, 현재 사용가능 이모티콘: 사랑해, 졸려)");
               getState();
            }
            break;
         case ST_SendWhisper_Req:      //79 [소통] 귓속말 전송 요청
            if(ST_C_Id == null){ //비회원 접속에서 불가능한 명령어 임
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }else if(ArrayMsg.length==3){
               ST_C_PW.println("79|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 귓속말 전송 명령어 형식을 확인해주세요. \"#ST-/w [DesUserId] [Msg]\" ('[',']'는 생략, 현재 사용가능 이모티콘: 사랑해, 졸려)");
               getState();
            }
            break;
         case ST_SendEncryMessage_Req:   //81 [소통] 암호화 메시지 전송 요청
            if(ST_C_Id == null){ //비회원 접속에서 불가능한 명령어 임
               System.out.println("[error]: 로그인을 먼저 해주세요. \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("81|"+getEncryption(ArrayMsg[1])+"|"+ST_C_Location+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: 암호화 메시지 전송 명령어 형식을 확인해주세요. \"#ST-/s [Msg]\" ('[',']'는 생략)");
               getState();
            }
            break;
         case ST_AdminMessage_Req:      //101 [관리자] 사자후 메시지 전송 요청
            if(ST_C_Id.equals("admin")){ //관리자 ID일 경우
               ST_C_PW.println("101|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{  //관리자 ID가 아닐 경우
               System.out.println("[error]: 권한이 없습니다. 관리자 명령어 입니다.");
               getState();
            }
            break;
         case ST_AdminDeleteRoom_Req:   //103 [관리자] 채팅방 삭제 전송 요청
            if(ST_C_Id.equals("admin")){ //관리자 ID일 경우
               ST_C_PW.println("103|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{  //관리자 ID가 아닐 경우
               System.out.println("[error]: 권한이 없습니다. 관리자 명령어 입니다.");
               getState();
            }
            break;
         case ST_AdminUpdateBlackListe_Req:   //105 [관리자] 블랙리스트 업데이트 전송 요청
            if(ST_C_Id.equals("admin")){ //관리자 ID일 경우
               if(ArrayMsg[1].equals("into")){ //블랙리스트 추가일 경우
                  ST_C_PW.println("105|1|"+ArrayMsg[2]);
                  ST_C_PW.flush();
               }else if(ArrayMsg[1].equals("delete")){ //블랙리스트 제거일 경우
                  ST_C_PW.println("105|2|"+ArrayMsg[2]);
                  ST_C_PW.flush();
               }else{
                  System.out.println("[error]: 블랙리스트 업데이트 명령어 형식을 확인해주세요. \"$UpdateBlackList [type] [userId]\" ('[',']'는 생략, 추가:into, 삭제:delete)");
                  getState();
               }
            }else{  //관리자 ID가 아닐 경우
               System.out.println("[error]: 권한이 없습니다. 관리자 명령어 입니다.");
               getState();
            }
            break;
         default:                  //61 기본 메시지 전송 [ST_SendMessage_Req] 현재 위치에서의 기본 채팅 요청(등록된 특정 명령어에 해당하지 않을 경우)
            if(ST_C_Id == null){ //비회원 접속에서 등록되지 않은 명령어 메시지를 보냈을 경우
               System.out.println("[error]: 현재 상태(비접속)에서 지원하지 않는 명령어 입니다. 로그인 또는 회원가입을 먼저 해주세요.");
               System.out.println("[error]: 로그인 명령어 : \"#Login [LoginID] [LoginPwd]\" ('[',']'는 생략)");
               System.out.println("[error]: 회원가입 명령어 :\"#Join [JoinID] [JoinPwd]\" ('[',']'는 생략)");
               getState();
            }else{ 
               //[ST_SendMessage_Req] 현재 위치에서 기본 채팅(브로드캐스트) 요청(61|메시지|요청위치|요청아이디)
               ST_C_PW.println("61|"+msg+"|"+ST_C_Location+"|"+ST_C_Id);
               ST_C_PW.flush();
               getState();
            }
            break;
      }
   }
   
   public void controllerRes(String msg) throws Exception{
      String[] ArrayMsg = msg.split("\\|");
      switch (Integer.parseInt(ArrayMsg[0])) {
         case ST_Hello_Ntf: //접속성공 환영 메시지 반환
            System.out.println("[Server로 부터 전달된 메시지]: "+ArrayMsg[1]);
            getState();
            break;
         case ST_Join_Res:   // 회원가입 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"님 환영합니다. 회원가입에 성공하셨습니다. 로그인 해주세요. ");
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"는 이미 등록된 아이디입니다. 다른 아이디로 회원가입 해주세요. ");
                     getState();
                     break;
               case 3: System.out.println("[Server로 부터 전달된 메시지]: 서버 점검 중입니다. 잠시 후에 다시 시도하여 주세요. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_Login_Res:   // 로그인 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"님 환영합니다. "+ArrayMsg[3]+"만에 접속 하셨습니다.");
                     System.out.println("[Server로 부터 전달된 메시지]: \"Lobby\"로 이동합니다.");
                     ST_C_Location = "Lobby"; //현재 위치 로비로 변경
                     ST_C_Id = ArrayMsg[2];
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: 서버 점검 중입니다. 잠시 후에 다시 시도하여 주세요. ");
                     getState();
                     break;
               case 3: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"는 등록되지 않은 아이디입니다. 아이디를 확인해 주세요. ");
                     getState();
                     break;
               case 4: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"에 대한 패스워드가 일치하지 않습니다. 패스워드를 확인해 주세요. ");
                     getState();
                     break;
               case 5: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"님은 현재 블랙리스트에 등록된 상태입니다. 관리자에게 문의하시기 바랍니다. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_Logout_Res:   // 로그아웃 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"님 로그아웃 되셨습니다.");
                     System.out.println("[Server로 부터 전달된 메시지]: \"비회원접속\"으로 이동합니다.");
                     ST_C_Location = "guest"; //현재 위치 게스트로 변경
                     ST_C_Id = null;
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: 서버 점검 중입니다. 잠시 후에 다시 시도하여 주세요. ");
                     getState();
                     break;
               case 3: System.out.println("[error]: 로그인 상태가 아닙니다. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_RoomList_Res:   // 채팅방 목록 반환
            System.out.println("[Server로 부터 전달된 메시지]: 현재 생성되어 있는 채팅방 리스트 = "+ArrayMsg[1]);
            getState();
            break;
         case ST_UserList_Res:   // 사용자 목록 반환
            if(ArrayMsg[1].equals("2")){
               System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"은 존재하지 않는 채팅방 입니다. 확인 후 다시 요청해주세요.");
            }else{
               if(ArrayMsg.length==3){
                  System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"에 접속되어 있는 사용자가 없습니다.");
               }else{
                  System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"에 접속되어 있는 사용자 리스트 = "+ArrayMsg[3]);
               }
            }
            getState();
            break;
         case ST_CreateRoom_Res:   // 채팅방 생성 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\" 채팅방이 생성되었습니다.");
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"은 이미 존재하는 채팅방 입니다. 다른 이름으로 채팅방을 생성해 주세요.");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_EnterRoom_Res:   // 채팅방 입장 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\" 채팅방에 입장 하셨습니다.");
                     ST_C_Location = ArrayMsg[2]; //현재 위치 변경
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[2]+"\"은 존재하는 않는 채팅방 입니다. 채팅방 이름을 확인해주세요.");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_DeleteRoom_Res:   // 채팅방 삭제 결과 반환
            break;
         case ST_KickOutRoom_Res:   // 채팅방 추방 결과 반환
            break;
         case ST_EnterLobby_Res:   // 로비 입장 결과 반환
            break;
         case ST_MailList_Res:   // 메일 목록 결과 반환
            System.out.println("[Server로 부터 전달된 메시지]: \""+ArrayMsg[1]+"\" 메일 목록 = "+ArrayMsg[2]);
            getState();
            break;
         case ST_ReadMail_Res:   // 메일 읽기 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: 메일 읽기에 성공하였습니다. [DesAddress]"+ArrayMsg[2]+" [Title]"+ArrayMsg[3]+" [Content]"+ArrayMsg[4]);
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: 해당 메일이 존재하지 않습니다. 확인 후 다시 요청해주세요. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_WriteMail_Res:   // 메일 쓰기 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: 메일 쓰기에 성공하였습니다. [DesAddress]"+ArrayMsg[2]+" [Title]"+ArrayMsg[3]+" [Content]"+ArrayMsg[4]);
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: 서버 점검 중입니다. 잠시 후에 다시 시도하여 주세요. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_DeleteMail_Res:   // 메일 삭제 결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server로 부터 전달된 메시지]: 메일삭제에 성공하였습니다. [DesAddress]"+ArrayMsg[2]+" [Title]"+ArrayMsg[3]);
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: 해당 메일이 존재하지 않습니다. 확인 후 다시 요청해주세요. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_SendMessage_Res:   // 메시지 전송받음
            if(ST_C_Id.equals(ArrayMsg[2])){ // 자신이 보낸 메시지면 출력하지 않음
            }else{ // 다른 클라이언트로부터 온 메시지면 출력
               System.out.println("");
               System.out.println(ArrayMsg[1]);
               getState();
            }
            break;
         case ST_WebSearch_Res:   // [소통] 웹검색 전송결과 반환(현재날씨)
            try {
               String nowWeather = getWeather();
               String[] ArrayNowWeather = nowWeather.split("\\|");
               System.out.println("[ST-현재날씨]: #접속동네:"+ArrayNowWeather[0]+"#--#현재날씨:"+ArrayNowWeather[1]+"#--#정보제공:기상청 동네날씨 #");
            } catch (IOException e) {
               e.printStackTrace();
            }
            getState();
            break;
         case ST_SendFile_Res:   // 파일 전송결과 반환
            break;
         case ST_DownFile_Res:   // 파일 다운결과 반환
            break;
         case ST_SendEmoticon_Res:   // [소통] 이모티콘 전송결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: getState();System.out.println(ArrayMsg[2]);
                     getState();
                     break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: 지원 가능한 이모티콘이 아닙니다. #지원가능 이모티콘:사랑해,졸려# ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_SendWhisper_Res:   // [소통] 귓속말 전송결과 반환 (클라이언트 단에서의 암호화 진행)
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: getState();break;
               case 2: System.out.println("[Server로 부터 전달된 메시지]: 현재 접속하지 않은 사용자 입니다. 확인 후 다시 귓속말을 요청해주세요.");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_SendEncryMessage_Res:   // [소통] 암호화 메시지 전송결과 반환 (클라이언트 단에서의 복호화 진행)
            if(ArrayMsg[3].equals(ST_C_Id)){ //자신이 보낸 암호화 메시지일 경우
               System.out.println("[ST-Chat]["+ArrayMsg[2]+"]["+ArrayMsg[3]+"]: "+getDecryption(ArrayMsg[1]));
               getState();
               break;
            }else{
               System.out.println();
               System.out.println("[ST-Chat]["+ArrayMsg[2]+"]["+ArrayMsg[3]+"]: "+getDecryption(ArrayMsg[1]));
               getState();
               break;
            }
         case ST_AdminMessage_Res:      // [관리자] 사자후 메시지 전송결과 반환
            System.out.println();
            System.out.println("[관리자로 부터 전달된 전체 메시지]: "+ArrayMsg[1]);
            getState();
            break;
         case ST_AdminDeleteRoom_Res:   // [관리자] 채팅방 삭제 전송결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: 
                  System.out.println();
                  System.out.println("[Server로 부터 전달된 메시지]: 관리자님께서 방을 삭제시키셨습니다. \"Lobby\"로 이동합니다.");
                  ST_C_Location = "Lobby"; //현재 위치 로비로 변경
                  getState();
                  break;
               case 2: 
                  System.out.println();
                  System.out.println("[Server로 부터 전달된 메시지]: 존재하지 않는 방입니다. 확인 후 다시 요청을 해주세요.");
                  getState();
                  break;
               case 3: 
                  System.out.println();
                  System.out.println("[Server로 부터 전달된 메시지]: 성공적으로 \""+ArrayMsg[2]+"\"방이 삭제되었습니다.");
                  getState();
                  break;
               default:
                     break;
            }
            break;
         case ST_AdminUpdateBlackListe_Res:   // [관리자] 블랙리스트 업데이트 전송결과 반환
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: //블랙리스트 추가 성공
                  System.out.println("[Server로 부터 전달된 메시지]: 성공적으로 블랙리스트에 \""+ArrayMsg[2]+"\"님을 추가하였습니다.");
                  System.out.println("[Server로 부터 전달된 메시지]: 현재 블랙리스트: "+ArrayMsg[3]);
                  ST_C_Location = "Lobby"; //현재 위치 로비로 변경
                  getState();
                  break;
               case 2: //블랙리스트 추가 실패
                  System.out.println("[Server로 부터 전달된 메시지]: 블랙리스트에 \""+ArrayMsg[2]+"\"님이 이미 존재합니다.");
                  System.out.println("[Server로 부터 전달된 메시지]: 현재 블랙리스트: "+ArrayMsg[3]);
                  getState();
                  break;
               case 3: //블랙리스트 삭제 성공
                  System.out.println("[Server로 부터 전달된 메시지]: 성공적으로 블랙리스트에서 \""+ArrayMsg[2]+"\"님을 삭제하였습니다.");
                  System.out.println("[Server로 부터 전달된 메시지]: 현재 블랙리스트: "+ArrayMsg[3]);
                  getState();
                  break;
               case 4: //블랙리스트 삭제 실패
                  System.out.println("[Server로 부터 전달된 메시지]: 블랙리스트에 \""+ArrayMsg[2]+"\"님이 존재하지 않습니다.");
                  System.out.println("[Server로 부터 전달된 메시지]: 현재 블랙리스트: "+ArrayMsg[3]);
                  getState();
                  break;
               default:
                     break;
            }
            break;
         default:
            break;
      }
   }
   
   //클라이언트 현재상태(위치) 반환
   private String getLocation(){
      return ST_C_Location;
   }
   
   //클라이언트 현재상태 콘솔출력
   private void getState(){
      if(ST_C_Id==null) System.out.print("[ST-Chat][비회원접속]: ");
      else {
         System.out.print("[ST-Chat]["+getLocation()+"]["+ST_C_Id+"]: ");
      }
   }
   
   //ST-오늘날씨-클라이언트접속위치를 기반으로 기상청 동네날씨 페이지 정보 http 파싱
   private String getWeather() throws IOException{
         URLConnection urlConn = null;
         URL url = new URL("http://www.kma.go.kr/weather/forecast/timeseries.jsp"); //기상청 동네날씨 주소
         String temp = null;
         String tempAll = "";
         String result = "";
         String location = ""; //동네이름
         String nowWeather = ""; //현재날씨
         urlConn = url.openConnection();
         urlConn.setDoOutput(true);
         BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
         while((temp = br.readLine()) != null){
            tempAll += temp;
         }
         br.close();
         int start = tempAll.indexOf("<span class=\"text\" id=\"addressName\" style=\"width:436px !important; margin-top:2px !important;\">");
         int end = tempAll.indexOf("<a href=\"#\" class=\"layor\" onclick=\"visibleDiv('layor_area',true, null, this); return false;\">");
         start = start+95;
         end = end-12;
         location = tempAll.substring(start, end); 
         location = location.replaceAll("&nbsp;", " "); //동네이름
         nowWeather = tempAll.substring(tempAll.indexOf("<dd class=\"now_weather1_right temp1 MB10\">")+42,
               + tempAll.indexOf("<dd class=\"now_weather1_right temp1 MB10\">")+45);
         return location+"|"+nowWeather;
   }
   
   //ST-메시지 암호화(RSA)
   private String getEncryption(String Encryption) throws Exception{
      String temp = Encryption;
      String result = StringCrypto.encrypt("GEOSERVICE", temp);
      return result;
   }
   //ST-메시지 복호화(RSA)
   private String getDecryption(String Decryption) throws Exception{
      String temp = Decryption;
      String result2 = StringCrypto.decrypt("GEOSERVICE", temp);
      return result2;
   }
   
}