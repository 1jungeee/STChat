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
   /********** �������� ���� *****************************************
    * [11~20] Ŭ���̾�Ʈ ���� ���� //*���� üũ: X
    * [21~30] ���� ���� ���� - ȸ������, �α���, �α׾ƿ� //*���� üũ: X
    * [31~40] ��� ���� ��û ���� - ä�ù� ���, ����� ��� //*���� üũ(C): userId != null
    * [41~50] ��(ä�ù�) ���� ����, ���� //*���� üũ(C): userId != null
    *       ����, �߹�   //*���� üũ(S): userId == roomNumber.getRoomMaster() 
    *       ��   ��   //*���� üũ(C): userId.getLocation != 1000 //�κ�(����)�� �ƴ� ��� ��, ä�ù��� ��츸 ����
    *       �κ� ����   //*���� üũ(C): userId.getLocation != 1000 //�κ�(����)�� �ƴ� ��� ��, ä�ù��� ��츸 ����
    * [51~60] Email �ý��� ���� - (���� ���, ���� �б�, ���� ����, ���� ����) IP ��� DB ��ȸ, ���, ����
    * [61~70] �⺻ �޽��� ���۽ý��� ����(Ư�� ��ɾ ���� ��� | �κ� or ���� ���)
    * [71~100] ����: ������ ä�� �� ����(�� �˻� ��� �Ľ�, ���� �� ���� ����, �̸�Ƽ�� ����, �ӼӸ�, ��ȣȭ �޽���)
    * [101~120] ������ ���� ���� - (������ �޽���[��������], �� ����, Ư�� ����� ����) //*���� üũ(S): reqUserID == S-T-Chat.getMasterID() 
    *************************************************************/
   private final int ST_Hello_Ntf = 12;         // ���Ӽ��� ȯ�� �޽��� ��ȯ
   private final int ST_Join_Req = 21;            // ȸ������ ��û
   private final int ST_Join_Res = 22;            // ȸ������ ��� ��ȯ
   private final int ST_Login_Req = 23;         // �α��� ��û
   private final int ST_Login_Res = 24;         // �α��� ��� ��ȯ
   private final int ST_Logout_Req = 25;         // �α׾ƿ� ��û
   private final int ST_Logout_Res = 26;         // �α׾ƿ� ��� ��ȯ
   private final int ST_RoomList_Req = 31;         // ä�ù� ��� ��û
   private final int ST_RoomList_Res = 32;         // ä�ù� ��� ��ȯ
   private final int ST_UserRoomList_Req = 33;         // ����� ��� ��û
   private final int ST_UserRoomList_Res = 34;         // ����� ��� ��ȯ
   private final int ST_CreateRoom_Req = 41;      //ä�ù� ���� ��û
   private final int ST_CreateRoom_Res = 42;      // ä�ù� ���� ��� ��ȯ
   private final int ST_EnterRoom_Req = 43;      // ä�ù� ���� ��û
   private final int ST_EnterRoom_Res = 44;      // ä�ù� ���� ��� ��ȯ
   private final int ST_DeleteRoom_Req = 45;      // ä�ù� ���� ��û
   private final int ST_DeleteRoom_Res = 46;      // ä�ù� ���� ��� ��ȯ
   private final int ST_KickOutRoom_Req = 47;      // ä�ù� �߹� ��û
   private final int ST_KickOutRoom_Res = 48;      // ä�ù� �߹� ��� ��ȯ
   private final int ST_EnterLobby_Req = 49;      // �κ� ���� ��û
   private final int ST_EnterLobby_Res = 50;      // �κ� ���� ��� ��ȯ
   private final int ST_MailList_Req = 51;         // ���� ��� ��û
   private final int ST_MailList_Res = 52;         // ���� ��� ��� ��ȯ
   private final int ST_ReadMail_Req = 53;         // ���� �б� ��û
   private final int ST_ReadMail_Res = 54;         // ���� �б� ��� ��ȯ
   private final int ST_WriteMail_Req = 55;      // ���� ���� ��û
   private final int ST_WriteMail_Res = 56;      // ���� ���� ��� ��ȯ
   private final int ST_DeleteMail_Req = 57;      // ���� ���� ��û
   private final int ST_DeleteMail_Res = 58;      // ���� ���� ��� ��ȯ
   private final int ST_SendMessage_Req = 61;      // �޽��� ���� ��û
   private final int ST_SendMessage_Res = 62;      // �޽��� ���۰�� ��ȯ
   private final int ST_WebSearch_Req = 71;      // [����] ���˻� ���� ��û
   private final int ST_WebSearch_Res = 72;      // [����] ���˻� ���۰�� ��ȯ
   private final int ST_SendFile_Req = 73;         // ���� ���� ��û
   private final int ST_SendFile_Res = 74;         // ���� ���۰�� ��ȯ
   private final int ST_DownFile_Req = 75;         // ���� �ٿ� ��û
   private final int ST_DownFile_Res = 76;         // ���� �ٿ��� ��ȯ
   private final int ST_SendEmoticon_Req = 77;      // [����] �̸�Ƽ�� ���� ��û
   private final int ST_SendEmoticon_Res = 78;      // [����] �̸�Ƽ�� ���۰�� ��ȯ
   private final int ST_SendWhisper_Req = 79;      // [����] �ӼӸ� ���� ��û
   private final int ST_SendWhisper_Res = 80;      // [����] �ӼӸ� ���۰�� ��ȯ
   private final int ST_SendEncryMessage_Req = 81;   // [����] ��ȣȭ �޽��� ���� ��û
   private final int ST_SendEncryMessage_Res = 82;   // [����] ��ȣȭ �޽��� ���۰�� ��ȯ
   private final int ST_AdminMessage_Req = 101;   // [������] ������ �޽��� ���� ��û
   private final int ST_AdminMessage_Res = 102;   // [������] ������ �޽��� ���۰�� ��ȯ
   private final int ST_AdminDeleteRoom_Req = 103;   // [������] ä�ù� ���� ���� ��û
   private final int ST_AdminDeleteRoom_Res = 104;   // [������] ä�ù� ���� ���۰�� ��ȯ
   private final int ST_UpdateBlackListe_Req = 105;   // [������] ������Ʈ ������Ʈ ���� ��û
   private final int ST_UpdateBlackListe_Res = 106;   // [������] ������Ʈ ������Ʈ ���۰�� ��ȯ
   
   private Socket ST_C_Socket = null;
   private String roomName;
   private PrintWriter ST_S_PW = null; //Ŭ���̾�Ʈ ��� ��Ʈ��
   private BufferedReader ST_S_BR = null; //Ŭ���̾�Ʈ �Է� ��Ʈ��
   private SoTongDAO DAO;
   private static int check = 0; //�� üũ�� ���� ����
   private static String temp = ""; //���� ���� ���� ����
   public SoTongClientThread(Socket ST_C_Socket) throws IOException{
      this.ST_C_Socket = ST_C_Socket;

      DAO = new SoTongDAO();
      // [C->S] �Է� ��Ʈ�� ��ü ����
      ST_S_BR = new BufferedReader(new InputStreamReader(this.ST_C_Socket.getInputStream()));   
      
      // [S->C] ��� ��Ʈ�� ��ü ���� 
      ST_S_PW = new PrintWriter(new OutputStreamWriter(this.ST_C_Socket.getOutputStream()));
      
      /* ���� ȯ��(ST_Hello_Ntf) �޽��� ���� */
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
         case ST_Join_Req:   // ȸ������ ��û
            serverLog("[S-ST_Join_Req]: Ŭ���̾�Ʈ�κ��� ȸ������ ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Join_Req]: Ŭ���̾�Ʈ�κ��� ȸ������ ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller(DAO.Join(ArrayMsg[1],ArrayMsg[2]));
            break;
         case ST_Join_Res:   // ȸ������ ��� ��ȯ
            serverLog("[S-ST_Join_Res]: ȸ������ ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_Join_Res]: ȸ������ ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_Login_Req:   // �α��� ��û
            serverLog("[S-ST_Login_Req]: Ŭ���̾�Ʈ�κ��� �α��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Login_Req]: Ŭ���̾�Ʈ�κ��� �α��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller(DAO.Login(ArrayMsg[1],ArrayMsg[2]));
            break;
         case ST_Login_Res:   // �α��� ��� ��ȯ
            serverLog("[S-ST_Login_Res]: �α��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_Login_Res]: �α��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("1")){ //�α��ο� �����Ͽ��� ���
               SoTongServer.ST_UserRoom.put(ArrayMsg[2], "Lobby"); //�ؽ��� ��ü�� ���� �� ��ġ ���
               SoTongServer.ST_UserSocket.put(ArrayMsg[2], ST_S_PW); //�ؽ��� ��ü�� ���� �� ������� ���
            }
            serverLog("[S-ST_UserRoomList]: ���� ������ ����Ʈ ������Ʈ"+SoTongServer.ST_UserRoom.keySet());System.out.println("[S-ST_UserRoomList]: ���� ������ ����Ʈ ������Ʈ"+SoTongServer.ST_UserRoom.keySet()); //���� �������� ����Ʈ 
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_Logout_Req:   // �α׾ƿ� ��û
            serverLog("[S-ST_Logout_Req]: Ŭ���̾�Ʈ�κ��� �α׾ƿ� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Logout_Req]: Ŭ���̾�Ʈ�κ��� �α׾ƿ� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("26|1|"+ArrayMsg[1]);
            break;
         case ST_Logout_Res:   // �α׾ƿ� ��� ��ȯ
            serverLog("[S-ST_Logout_Res]: �α׾ƿ� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_Logout_Res]: �α׾ƿ� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("1")){ //�α׾ƿ��� �����Ͽ��� ���
               SoTongServer.ST_UserRoom.remove(ArrayMsg[2]); //�ؽ��� ��ü�� �α׾ƿ� �� ���� ���� ����
               SoTongServer.ST_UserSocket.remove(ArrayMsg[2]); //�ؽ��� ��ü�� �α׾ƿ� �� ���� �� ������� ���� ���ŵ��
            }
            serverLog("[S-ST_UserRoomList]: ���� ������ ����Ʈ ������Ʈ"+SoTongServer.ST_UserRoom.keySet());System.out.println("[S-ST_UserRoomList]: ���� ������ ����Ʈ ������Ʈ"+SoTongServer.ST_UserRoom.keySet()); //���� �������� ����Ʈ 
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_RoomList_Req:   // ä�ù� ��� ��û
            serverLog("[S-ST_Logout_Req]: Ŭ���̾�Ʈ�κ��� ä�ù� ��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_Logout_Req]: Ŭ���̾�Ʈ�κ��� ä�ù� ��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("32|"+ArrayMsg[1]);
            break;
         case ST_RoomList_Res:   // ä�ù� ��� ��ȯ
            temp = "32|";
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){
               temp += "["+SoTongServer.ST_RoomList_Name.get(i)+"]"; 
            }
            serverLog("[S-ST_RoomList_Res]: ä�ù� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+temp);System.out.println("[S-ST_RoomList_Res]: ä�ù� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+temp);
            ST_S_PW.println(temp);
            ST_S_PW.flush();
            break;
         case ST_UserRoomList_Req:   // ����� ��� ��û
            serverLog("[S-ST_UserRoomList_Req]: Ŭ���̾�Ʈ�κ��� ����� ��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_UserRoomList_Req]: Ŭ���̾�Ʈ�κ��� ����� ��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("34|"+ArrayMsg[1]);
            break;
         case ST_UserRoomList_Res:   // ����� ��� ��ȯ
            check = 0; // ����� ��� ��ȯ�� �ռ� ä�ù� ���� Ȯ�ο� ���� ���� �ʱ�ȭ
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){ // ���� ä�ù� ����Ʈ���� ���� ��û ä�ù��� �����ϴ��� Ȯ��
               if(ArrayMsg[1].equals(SoTongServer.ST_RoomList_Name.get(i))){
                  check = 1; // ä�ù��� ������ ��� check�� 1�� ��ȯ
               }
            }
            if(check==1){ // ä�ù��� ������ ���
               temp = getUserList(ArrayMsg[1]); // ����� ��� ��ȯ �Լ� ȣ�� �� ��ȯ �� temp������ ����
               serverLog("[S-ST_UserRoomList_Res]: ����� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|1|"+ArrayMsg[1]+"|"+temp);System.out.println("[S-ST_UserRoomList_Res]: ����� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|1|"+ArrayMsg[1]+"|"+temp);
               ST_S_PW.println("34|1|"+ArrayMsg[1]+"|"+temp);
               ST_S_PW.flush();
            }else{ // ä�ù��� �������� ���� ���
               ST_S_PW.println("34|2|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_UserRoomList_Res]: ����� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|2"+ArrayMsg[1]);System.out.println("[S-ST_UserRoomList_Res]: ����� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: 34|2"+ArrayMsg[1]);
            }
            break;
         case ST_CreateRoom_Req:   // ä�ù� ���� ��û
            serverLog("[S-ST_CreateRoom_Req]: Ŭ���̾�Ʈ�κ��� ä�ù� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_CreateRoom_Req]: Ŭ���̾�Ʈ�κ��� ä�ù� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            if(ArrayMsg.length==3){
               controller("42|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
            }else{
               controller("42|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            }
            break;
         case ST_CreateRoom_Res:   // ä�ù� ���� ��� ��ȯ
            check = 0; // ä�ù� �̸� �ߺ� Ȯ��
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){ // ���� ä�ù� ����Ʈ���� ��û ä�ù� �̸��� �����ϴ��� Ȯ��
               if(ArrayMsg[1].equals(SoTongServer.ST_RoomList_Name.get(i))){
                  check = 1; // ä�ù� �̸��� �ߺ��� ��� check�� 1�� ��ȯ
               }
            }
            if(check==1){ // ä�ù� �̸��� �ߺ� �� ���
               ST_S_PW.println("42|2|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|2|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|2|"+ArrayMsg[1]);
            }else{
               SoTongServer.ST_RoomList_Name.add(ArrayMsg[1]); // ä�ù� ���� ��ü�� ä�ù� �߰�
               ST_S_PW.println("42|1|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|1|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"42|1|"+ArrayMsg[1]);
            }
            break;
         case ST_EnterRoom_Req:   // ä�ù� ���� ��û
            serverLog("[S-ST_EnterRoom_Req]: Ŭ���̾�Ʈ�κ��� ä�ù� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_EnterRoom_Req]: Ŭ���̾�Ʈ�κ��� ä�ù� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            controller("44|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
            break;
         case ST_EnterRoom_Res:   // ä�ù� ���� ��� ��ȯ
            check = 0; // ���� ��û ä�ù� ���� Ȯ�ο� ���� ���� �ʱ�ȭ
            for(int i=0; i<SoTongServer.ST_RoomList_Name.size(); i++){ // ���� ä�ù� ����Ʈ���� ���� ��û ä�ù��� �����ϴ��� Ȯ��
               if(ArrayMsg[1].equals(SoTongServer.ST_RoomList_Name.get(i))){
                  check = 1; // ä�ù� �̸��� �ߺ��� ��� check�� 1�� ��ȯ
               }
            }
            if(check==1){ // ä�ù��� ������ ���
               SoTongServer.ST_UserRoom.put(ArrayMsg[2], ArrayMsg[1]); //�ؽ��� ��ü�� ���� �� ��ġ ���
               intoRoomBroadcast(ArrayMsg[1], ArrayMsg[2]); //���ο� Ŭ���̾�Ʈ ���� ���� �ش� ä�ù濡 ��ε�ĳ��Ʈ
               ST_S_PW.println("44|1|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|1|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|1|"+ArrayMsg[1]);
               }else{ // ä�ù��� �������� ���� ���
               ST_S_PW.println("44|2|"+ArrayMsg[1]);
               ST_S_PW.flush();
               serverLog("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|2|"+ArrayMsg[1]);System.out.println("[S-ST_CreateRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+"44|2|"+ArrayMsg[1]);
            }
            break;
         case ST_DeleteRoom_Req:   // ä�ù� ���� ��û
            break;
         case ST_DeleteRoom_Res:   // ä�ù� ���� ��� ��ȯ
            break;
         case ST_KickOutRoom_Req:   // ä�ù� �߹� ��û
            break;
         case ST_KickOutRoom_Res:   // ä�ù� �߹� ��� ��ȯ
            break;
         case ST_EnterLobby_Req:   // �κ� ���� ��û
            break;
         case ST_EnterLobby_Res:   // �κ� ���� ��� ��ȯ
            break;
         case ST_MailList_Req:   // ���� ��� ��û
            serverLog("[S-ST_MailList_Req]: Ŭ���̾�Ʈ�κ��� ���� ��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_MailList_Req]: Ŭ���̾�Ʈ�κ��� ���� ��� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //���� ��� ��û Ŭ���̾�Ʈ IP(Destination IP Address)
            controller(DAO.MailList(temp));
            break;
         case ST_MailList_Res:   // ���� ��� ��� ��ȯ
            serverLog("[S-ST_MailList_Res]: ���� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_MailList_Res]: ���� ��� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_ReadMail_Req:   // ���� �б� ��û
            serverLog("[S-ST_ReadMail_Req]: Ŭ���̾�Ʈ�κ��� ���� �б� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_ReadMail_Req]: Ŭ���̾�Ʈ�κ��� ���� �б� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //���� ��� ��û Ŭ���̾�Ʈ IP(Destination IP Address)
            controller(DAO.ReadMail(temp, ArrayMsg[1])); 
            break;
         case ST_ReadMail_Res:   // ���� �б� ��� ��ȯ
            serverLog("[S-ST_ReadMail_Res]: ���� �б� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_ReadMail_Res]: ���� �б� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_WriteMail_Req:   // ���� ���� ��û
            serverLog("[S-ST_WriteMail_Req]: Ŭ���̾�Ʈ�κ��� ���� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_WriteMail_Req]: Ŭ���̾�Ʈ�κ��� ���� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //���� ���� ��û Ŭ���̾�Ʈ IP(Source IP Address)
            controller(DAO.WriteMail(ArrayMsg[1], ""+temp, ArrayMsg[2], ArrayMsg[3]));
            break;
         case ST_WriteMail_Res:   // ���� ���� ��� ��ȯ
            serverLog("[S-ST_WriteMail_Res]: ���� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_WriteMail_Res]: ���� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_DeleteMail_Req:   // ���� ���� ��û
            serverLog("[S-ST_DeleteMail_Req]: Ŭ���̾�Ʈ�κ��� ���� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);System.out.println("[S-ST_DeleteMail_Req]: Ŭ���̾�Ʈ�κ��� ���� ���� ��û�� ���Խ��ϴ�. requestIP: "+ST_C_Socket.getInetAddress()+" requestMsg: "+msg);
            temp = ST_C_Socket.getInetAddress().toString().substring(1, ST_C_Socket.getInetAddress().toString().length()); //���� ���� ��û Ŭ���̾�Ʈ IP(Source IP Address)
            controller(DAO.DeleteMail(""+temp, ArrayMsg[1]));
            break;
         case ST_DeleteMail_Res:   // ���� ���� ��� ��ȯ
            serverLog("[S-ST_DeleteMail_Res]: ���� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_DeleteMail_Res]: ���� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         case ST_SendMessage_Req:   // �޽��� ���� ��û
            serverLog("[S-ST_SendMessage_Req]: Ŭ���̾�Ʈ�κ��� �޽��� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendMessage_Req]: Ŭ���̾�Ʈ�κ��� �޽��� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("62|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendMessage_Res:   // �޽��� ���۰�� ��ȯ
            serverLog("[S-ST_SendMessage_Res]: �޽��� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendMessage_Res]: �޽��� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            broadcast(ArrayMsg[1], ArrayMsg[2], ArrayMsg[3]);
            break;
         case ST_WebSearch_Req:   // [����] ���˻� ���� ��û(���糯��) - Ŭ���̾�Ʈ �ܿ��� ������ġ�� ������� ����
            break;
         case ST_WebSearch_Res:   // [����] ���˻� ���۰�� ��ȯ(���糯��) - Ŭ���̾�Ʈ �ܿ��� ������ġ�� ������� ����
            break;
         case ST_SendFile_Req:   // ���� ���� ��û
            break;
         case ST_SendFile_Res:   // ���� ���۰�� ��ȯ
            break;
         case ST_DownFile_Req:   // ���� �ٿ� ��û
            break;
         case ST_DownFile_Res:   // ���� �ٿ��� ��ȯ
            break;
         case ST_SendEmoticon_Req:   // [����] �̸�Ƽ�� ���� ��û
            serverLog("[S-ST_SendEmoticon_Req]: Ŭ���̾�Ʈ�κ��� �̸�Ƽ�� ���� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEmoticon_Req]: Ŭ���̾�Ʈ�κ��� �̸�Ƽ�� ���� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("78|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendEmoticon_Res:   // [����] �̸�Ƽ�� ���۰�� ��ȯ
            serverLog("[S-ST_SendEmoticon_Res]: �̸�Ƽ�� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEmoticon_Res]: �̸�Ƽ�� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("�����")){
               temp = "�����������~~������";
               broadcast(temp, ArrayMsg[2], ArrayMsg[3]);
               ST_S_PW.println("78|1|�����������~~������");
               ST_S_PW.flush();
            }else if(ArrayMsg[1].equals("����")){
               temp = "...zZ..zZ...����...zZ..";
               broadcast(temp, ArrayMsg[2], ArrayMsg[3]);
               ST_S_PW.println("78|1|...zZ..zZ...����...zZ..");
               ST_S_PW.flush();
            }else{
               ST_S_PW.println("78|2");
               ST_S_PW.flush();
            }
            break;
         case ST_SendWhisper_Req:   // [����] �ӼӸ� ���� ��û
            serverLog("[S-ST_SendWhisper_Req]: Ŭ���̾�Ʈ�κ��� �ӼӸ� ���� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendWhisper_Req]: Ŭ���̾�Ʈ�κ��� �ӼӸ� ���� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("80|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendWhisper_Res:   // [����] �ӼӸ� ���۰�� ��ȯ
            temp = sendWhisper(ArrayMsg[1], ArrayMsg[3], ArrayMsg[2]);
            if(temp.equals("1")){ // �ӼӸ� ��밡 ������ ���(�ӼӸ� ����)
               msg = "80|1|"+ArrayMsg[1]+"|"+ArrayMsg[3];
               serverLog("[S-ST_SendWhisper_Res]: �ӼӸ� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendWhisper_Res]: �ӼӸ� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
               ST_S_PW.println(msg);
               ST_S_PW.flush();
            }else{ // �ӼӸ� ��밡 �������� ���� ���(�ӼӸ� ����)
               msg = "80|2|"+ArrayMsg[1]+"|"+ArrayMsg[3];
               serverLog("[S-ST_SendWhisper_Res]: �ӼӸ� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendWhisper_Res]: �ӼӸ� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
               ST_S_PW.println(msg);
               ST_S_PW.flush();
            }
            break;
         case ST_SendEncryMessage_Req:   // [����] ��ȣȭ �޽��� ���� ��û
            serverLog("[S-ST_SendEncryMessage_Req]: Ŭ���̾�Ʈ�κ��� ��ȣȭ �޽��� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEncryMessage_Req]: Ŭ���̾�Ʈ�κ��� ��ȣȭ �޽��� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("82|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
            break;
         case ST_SendEncryMessage_Res:   // [����] ��ȣȭ �޽��� ���۰�� ��ȯ
            serverLog("[S-ST_SendEncryMessage_Res]: ��ȣȭ �޽��� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_SendEncryMessage_Res]: ��ȣȭ �޽��� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            EncryBroadcast(ArrayMsg[1], ArrayMsg[2], ArrayMsg[3]);
            break;
         case ST_AdminMessage_Req:   // [������] ������ �޽��� ���� ��û
            serverLog("[S-ST_AdminMessage_Req]: �����ڷκ��� ������ �޽��� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminMessage_Req]: �����ڷκ��� ������ �޽��� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("102|"+ArrayMsg[1]);
            break;
         case ST_AdminMessage_Res:   // [������] ������ �޽��� ���۰�� ��ȯ
            serverLog("[S-ST_AdminMessage_Res]: ������ �޽��� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminMessage_Res]: ������ �޽��� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            AdminBroadcast(ArrayMsg[1]);
            break;
         case ST_AdminDeleteRoom_Req:   // [������] ä�ù� ���� ���� ��û
            serverLog("[S-ST_AdminDeleteRoom_Req]: �����ڷκ��� ä�ù� ���� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminDeleteRoom_Req]: �����ڷκ��� ä�ù� ���� ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            controller("104|"+ArrayMsg[1]);
            break;
         case ST_AdminDeleteRoom_Res:   // [������] ä�ù� ���� ���۰�� ��ȯ
            serverLog("[S-ST_AdminDeleteRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_AdminDeleteRoom_Res]: ä�ù� ���� ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(getAdminDeleteRoomResult(ArrayMsg[1])==0){
               ST_S_PW.println("104|2");
               ST_S_PW.flush();
            }else{
               ST_S_PW.println("104|3|"+ArrayMsg[1]);
               ST_S_PW.flush();
            }
            break;   
         case ST_UpdateBlackListe_Req:   // [������] ������Ʈ ������Ʈ ���� ��û
            serverLog("[S-ST_UpdateBlackListe_Req]: �����ڷκ��� ������Ʈ ������Ʈ ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_UpdateBlackListe_Req]: �����ڷκ��� ������Ʈ ������Ʈ ��û�� ���Խ��ϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            if(ArrayMsg[1].equals("1")){ //������Ʈ �߰��� ���
               controller(DAO.BlackListUpdateInto(ArrayMsg[2]));
            }else{ //������Ʈ ������ ���(2)
               controller(DAO.BlackListUpdateDelete(ArrayMsg[2]));
            }
            break;
         case ST_UpdateBlackListe_Res:   // [������] ������Ʈ ������Ʈ ���۰�� ��ȯ
            msg=msg+"|"+DAO.BlackListUpdateList();
            serverLog("[S-ST_UpdateBlackListe_Res]: ������Ʈ ������Ʈ ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);System.out.println("[S-ST_UpdateBlackListe_Res]: ������Ʈ ������Ʈ ��û�� ���� ��� ���� ��ȯ�մϴ�. responsIP: "+ST_C_Socket.getInetAddress()+" responseMsg: "+msg);
            serverLog("[S-ST_BlackListList]: �� ����Ʈ�� ������Ʈ �մϴ�."+DAO.BlackListUpdateList());System.out.println("[S-ST_BlackListList]: �� ����Ʈ�� ������Ʈ �մϴ�."+DAO.BlackListUpdateList()); //���� ������Ʈ ������Ʈ 
            ST_S_PW.println(msg);
            ST_S_PW.flush();
            break;
         default:
            break;
      }
   }
   
   /* ST_Hello_Ntf: ���Ӽ��� ȯ���޽��� ��ȯ */
   private String ST_Hello_Ntf(){
      return "12|Hello So-Tong Chat!";
   }
   
   /* ��ε� ĳ��Ʈ(ä�ù� ���� �������� �⺻ä�ÿ� ���) */
   public void broadcast(String msg, String roomName, String reqUserId) {
      
      /*
       * SoTongServer.ST_UserRoom = �������̵� / ������ġ
       * SoTongServer.ST_UserSocket = �������̵� / ����������°�ü
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
   
   /* ä�ù濡 ���ο� Ŭ���̾�Ʈ�� �����Ͽ��� ��� ����޽��� ��ε� ĳ��Ʈ ���� */
   public void intoRoomBroadcast(String roomName, String reqUserId) {
      /*
       * SoTongServer.ST_UserRoom = �������̵� / ������ġ
       * SoTongServer.ST_UserSocket = �������̵� / ����������°�ü
       */
      synchronized (SoTongServer.ST_UserSocket) {
         Collection collection = SoTongServer.ST_UserSocket.values();
         Iterator iter = collection.iterator();

         Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();

         int i = 0;
         while (iter.hasNext()) {
            if(SoTongServer.ST_UserRoom.get(arr[i].toString()).equals(roomName)){
               PrintWriter pwb = (PrintWriter) iter.next();
               pwb.println("62|[ST-Chat]["+roomName+"]["+reqUserId+"]: ���� �����ϼ̽��ϴ�.|"+reqUserId); //���� �ش� ä�ù� ��ε�ĳ��Ʈ �޽��� ����
               pwb.flush();
            }else{
               iter.next();
            }
            i++;
         }
      }
   }
   
   /* ä�ù� �̸��� �Ű������� �Է¹޾� ����� ����Ʈ�� ��ȯ�ϴ� �Լ� */
   public String getUserList(String roomName){
      temp = "";
      Object arr[] = SoTongServer.ST_UserRoom.keySet().toArray();
      for(int i=0; i<SoTongServer.ST_UserRoom.size(); i++){ //���� �� ��ŭ ����
         if(SoTongServer.ST_UserRoom.get(arr[i].toString()).equals(roomName)){ //���� Ű������ ���� ä�ù� ��ġ�� �˻����� �� ��û ä�ù��ϰ� ��ġ�Ͽ��� ���
            temp += "["+arr[i].toString()+"]";
         }else{
         }
      }
      return temp;
   }
   
   /* [����] �ӼӸ� ��û�� ���� 1:1��� */
   public String sendWhisper(String desUserId, String srcUserId, String msg) {
      /*
       * SoTongServer.ST_UserSocket = �������̵� / ����������°�ü
       */
     String result="2";
      synchronized (SoTongServer.ST_UserSocket) {
         PrintWriter pwb;
         if(SoTongServer.ST_UserSocket.get(desUserId) != null){
            pwb = SoTongServer.ST_UserSocket.get(desUserId);
            pwb.println("62|[ST-Chat][�ӼӸ�����]["+srcUserId+"]: "+msg+"|"+srcUserId); //�ӼӸ� ����
            pwb.flush();
            result="1"; //�ӼӸ��� �����Ͽ��� ���
         }else{
            result="2"; //�ӼӸ��� �����Ͽ��� ���
         }
      }
   return result;
   }
   
   /* [����] ��ȣȭ �޽��� ���� ��ε�ĳ��Ʈ */
   public void EncryBroadcast(String msg, String roomName, String reqUserId) {
      
      /*
       * SoTongServer.ST_UserRoom = �������̵� / ������ġ
       * SoTongServer.ST_UserSocket = �������̵� / ����������°�ü
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

   /* [������] ������ �޽��� ���� ��ε�ĳ��Ʈ */
   public void AdminBroadcast(String msg) {
      
      /*
       * SoTongServer.ST_UserRoom = �������̵� / ������ġ
       * SoTongServer.ST_UserSocket = �������̵� / ����������°�ü
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
   
   /* [������] ä�ù� ���� ����� ��ȯ�ϴ� �Լ� */
   public int getAdminDeleteRoomResult(String roomName){

         /*
          * SoTongServer.ST_UserRoom = �������̵� / ������ġ
          * SoTongServer.ST_UserSocket = �������̵� / ����������°�ü
          */
      check = 0; // ä�ù� ������ �ռ� ä�ù� ���� Ȯ�ο� ���� ���� �ʱ�ȭ
      for(int a=0; a<SoTongServer.ST_RoomList_Name.size(); a++){ // ���� ä�ù� ����Ʈ���� ���� ��û ä�ù��� �����ϴ��� Ȯ��
         if(roomName.equals(SoTongServer.ST_RoomList_Name.get(a))){
            check = 1; // ä�ù��� ������ ��� check�� 1�� ��ȯ
            SoTongServer.ST_RoomList_Name.remove(a); //�ش� ä�ù� ����
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
                     SoTongServer.ST_UserRoom.put(arr[i].toString(), "Lobby"); //�ؽ��� ��ü�� �ش� ���� �κ�� ������Ʈ
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