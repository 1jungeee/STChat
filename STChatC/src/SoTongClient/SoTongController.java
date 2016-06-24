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

   private final String ST_Join_Req = "#Join";                        // 21 ȸ������ ��û
   private final String ST_Login_Req = "#Login";                     // 23 �α��� ��û
   private final String ST_Logout_Req = "#Logout";                     // 25 �α׾ƿ� ��û
   private final String ST_RoomList_Req = "#RoomList";                  // ä�ù� ��� ��û
   private final String ST_UserList_Req = "#UserList";                  // ����� ��� ��û
   private final String ST_CreateRoom_Req = "#CreateRoom";               // ä�ù� ���� ��û
   private final String ST_EnterRoom_Req = "#EnterRoom";               // ä�ù� ���� ��û
   private final String ST_DeleteRoom_Req = "45";                     // ä�ù� ���� ��û
   private final String ST_KickOutRoom_Req = "47";                     // ä�ù� �߹� ��û
   private final String ST_EnterLobby_Req = "49";                     // �κ� ���� ��û
   private final String ST_MailList_Req = "#MailList";                  // ���� ��� ��û
   private final String ST_ReadMail_Req = "#MailRead";                  // ���� �б� ��û
   private final String ST_WriteMail_Req = "#MailWrite";               // ���� ���� ��û
   private final String ST_DeleteMail_Req = "#MailDelete";               // ���� ���� ��û
   private final String ST_SendMessage_Req = "61";                     // �޽��� ���� ��û
   private final String ST_WebSearch_Req = "#ST-nowWeather";            // [����] ���˻� ���� ��û(���糯��)
   private final String ST_SendFile_Req = "73";                     // ���� ���� ��û
   private final String ST_DownFile_Req = "75";                     // ���� �ٿ� ��û
   private final String ST_SendEmoticon_Req = "#ST-*";                  // [����] �̸�Ƽ�� ���� ��û
   private final String ST_SendWhisper_Req = "#ST-/w";                  // [����] �ӼӸ� ���� ��û
   private final String ST_SendEncryMessage_Req = "#ST-/s";            // [����] ��ȣȭ �޽��� ���� ��û(AES�˰��� ���, Ŭ���̾�Ʈ �ܿ��� ��/��ȣȭ)
   private final String ST_AdminMessage_Req = "$Ntf";                  // [������] ������ �޽��� ���� ��û
   private final String ST_AdminDeleteRoom_Req = "$DeleteRoom";         // [������] ä�ù� ���� ���� ��û
   private final String ST_AdminUpdateBlackListe_Req = "$UpdateBlackList";   // [������] ������Ʈ ������Ʈ ���� ��û
   private final int ST_Hello_Ntf = 12;                           // ���Ӽ��� ȯ�� �޽��� ��ȯ
   private final int ST_Join_Res = 22;                              // ȸ������ ��� ��ȯ
   private final int ST_Login_Res = 24;                           // �α��� ��� ��ȯ
   private final int ST_Logout_Res = 26;                           // �α׾ƿ� ��� ��ȯ
   private final int ST_RoomList_Res = 32;                           // ä�ù� ��� ��ȯ
   private final int ST_UserList_Res = 34;                           // ����� ��� ��ȯ
   private final int ST_CreateRoom_Res = 42;                        // ä�ù� ���� ��� ��ȯ
   private final int ST_EnterRoom_Res = 44;                        // ä�ù� ���� ��� ��ȯ
   private final int ST_DeleteRoom_Res = 46;                        // ä�ù� ���� ��� ��ȯ
   private final int ST_KickOutRoom_Res = 48;                        // ä�ù� �߹� ��� ��ȯ
   private final int ST_EnterLobby_Res = 50;                        // �κ� ���� ��� ��ȯ
   private final int ST_MailList_Res = 52;                           // ���� ��� ��� ��ȯ
   private final int ST_ReadMail_Res = 54;                           // ���� �б� ��� ��ȯ
   private final int ST_WriteMail_Res = 56;                        // ���� ���� ��� ��ȯ
   private final int ST_DeleteMail_Res = 58;                        // ���� ���� ��� ��ȯ
   private final int ST_SendMessage_Res = 62;                        // �޽��� ���۰�� ��ȯ
   private final int ST_WebSearch_Res = 72;                        // [����] ���˻� ���۰�� ��ȯ(���糯��)
   private final int ST_SendFile_Res = 74;                           // ���� ���۰�� ��ȯ
   private final int ST_DownFile_Res = 76;                           // ���� �ٿ��� ��ȯ
   private final int ST_SendEmoticon_Res = 78;                        // [����] �̸�Ƽ�� ���۰�� ��ȯ
   private final int ST_SendWhisper_Res = 80;                        // [����] �ӼӸ� ���۰�� ��ȯ
   private final int ST_SendEncryMessage_Res = 82;                     // [����] ��ȣȭ �޽��� ���۰�� ��ȯ
   private final int ST_AdminMessage_Res = 102;                     // [������] ������ �޽��� ���۰�� ��ȯ
   private final int ST_AdminDeleteRoom_Res = 104;                     // [������] ä�ù� ���� ���۰�� ��ȯ
   private final int ST_AdminUpdateBlackListe_Res = 106;               // [������] ������Ʈ ������Ʈ ���۰�� ��ȯ
   
   private Socket ST_C_Socket = null;
   private static PrintWriter ST_C_PW = null; //[C->S ��� ��Ʈ��]
   private static String ST_C_Location = "guest"; //����� ��ġ guest=��α��� 
   private static String ST_C_Id = null; //����� �α��� ���̵�
   SoTongController(){
      
   }
   SoTongController(Socket ST_C_Socket) throws IOException{
      this.ST_C_Socket = ST_C_Socket;
      ST_C_PW = new PrintWriter(new OutputStreamWriter(this.ST_C_Socket.getOutputStream()));
   }
   
   public void controllerReq(String msg) throws Exception{
      String[] ArrayMsg = msg.split(" ");
      switch (ArrayMsg[0]) {
         case ST_Join_Req:             //21 ȸ������ ��û
            if(ArrayMsg.length==3){
               ST_C_PW.println("21|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ȸ������ ��ɾ� ������ Ȯ�����ּ���. \"#Join [JoinID] [JoinPwd]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_Login_Req:            //23 �α��� ��û
            if(ST_C_Id != null){ //�̹� �α��� �� ���¿��� �α����� ��û�Ͽ��� ���
               System.out.println("[error]: �α׾ƿ��� ���� ���ּ���. \"#Logout\"");
            }else if(ArrayMsg.length==3){
               ST_C_PW.println("23|"+ArrayMsg[1]+"|"+ArrayMsg[2]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: �α��� ��ɾ� ������ Ȯ�����ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_Logout_Req:            //25 �α׾ƿ� ��û
            if(ST_C_Id != null){
               ST_C_PW.println("25|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_RoomList_Req:         //31 ä�ù� ��� ��û
            if(ST_C_Id != null){
               ST_C_PW.println("31|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_UserList_Req:         //33 ����� ��� ��û
            if(ST_C_Id == null){ 
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("33|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ����� ��� ��û ��ɾ� ������ Ȯ�����ּ���. \"#UserList [RoomName]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_CreateRoom_Req:         //41ä�ù� ���� ��û
            if(ST_C_Id == null){ 
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("41|"+ArrayMsg[1]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else if(ArrayMsg.length==3){
               ST_C_PW.println("41|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ä�ù� ���� ��ɾ� ������ Ȯ�����ּ���. \"#CreateRoom [RoomName]\" ('[',']'�� ����)");
               System.out.println("[error]: ����� ä�ù��� ���  \"#CreateRoom [RoomName] [RoomPwd]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_EnterRoom_Req:         //43 ä�ù� ���� ��û
            if(ST_C_Id == null){ 
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("43|"+ArrayMsg[1]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ä�ù� ���� ��ɾ� ������ Ȯ�����ּ���. \"#EnterRoom [RoomName]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_DeleteRoom_Req:         //45 ä�ù� ���� ��û
            break;
         case ST_KickOutRoom_Req:      //47 ä�ù� �߹� ��û
            break;
         case ST_EnterLobby_Req:         //49 �κ� ���� ��û
            break;
         case ST_MailList_Req:         //51 ���� ��� ��û
            ST_C_PW.println("51|");
            ST_C_PW.flush();
            break;
         case ST_ReadMail_Req:         //53 ���� �б� ��û
            if(ArrayMsg.length==2){
               ST_C_PW.println("53|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ���� �б� ��ɾ� ������ Ȯ�����ּ���. \"#ReaMail [MailName]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_WriteMail_Req:         //55 ���� ���� ��û
            if(ArrayMsg.length==4){
               ST_C_PW.println("55|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ArrayMsg[3]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ���� �ۼ� ��ɾ� ������ Ȯ�����ּ���. \"#WriteMail [DestinationUserIP] [MailTitle] [MailContent]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_DeleteMail_Req:         //57 ���� ���� ��û
            if(ArrayMsg.length==2){
               ST_C_PW.println("57|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ���� ���� ��ɾ� ������ Ȯ�����ּ���. \"#MailDelete [MailName]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_WebSearch_Req:         //71 [����] ���˻� ���� ��û(���糯��)
            controllerRes("72|");
            break;
         case ST_SendFile_Req:         //73 ���� ���� ��û
            break;
         case ST_DownFile_Req:         //75 ���� �ٿ� ��û
            break;
         case ST_SendEmoticon_Req:      //77 [����] �̸�Ƽ�� ���� ��û
            if(ST_C_Id == null){ //��ȸ�� ���ӿ��� �Ұ����� ��ɾ� ��
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("77|"+ArrayMsg[1]+"|"+ST_C_Location+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: �̸�Ƽ�� ���� ��ɾ� ������ Ȯ�����ּ���. \"#ST-* [EmoticonType]\" ('[',']'�� ����, ���� ��밡�� �̸�Ƽ��: �����, ����)");
               getState();
            }
            break;
         case ST_SendWhisper_Req:      //79 [����] �ӼӸ� ���� ��û
            if(ST_C_Id == null){ //��ȸ�� ���ӿ��� �Ұ����� ��ɾ� ��
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }else if(ArrayMsg.length==3){
               ST_C_PW.println("79|"+ArrayMsg[1]+"|"+ArrayMsg[2]+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: �ӼӸ� ���� ��ɾ� ������ Ȯ�����ּ���. \"#ST-/w [DesUserId] [Msg]\" ('[',']'�� ����, ���� ��밡�� �̸�Ƽ��: �����, ����)");
               getState();
            }
            break;
         case ST_SendEncryMessage_Req:   //81 [����] ��ȣȭ �޽��� ���� ��û
            if(ST_C_Id == null){ //��ȸ�� ���ӿ��� �Ұ����� ��ɾ� ��
               System.out.println("[error]: �α����� ���� ���ּ���. \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               getState();
            }else if(ArrayMsg.length==2){
               ST_C_PW.println("81|"+getEncryption(ArrayMsg[1])+"|"+ST_C_Location+"|"+ST_C_Id);
               ST_C_PW.flush();
            }else{
               System.out.println("[error]: ��ȣȭ �޽��� ���� ��ɾ� ������ Ȯ�����ּ���. \"#ST-/s [Msg]\" ('[',']'�� ����)");
               getState();
            }
            break;
         case ST_AdminMessage_Req:      //101 [������] ������ �޽��� ���� ��û
            if(ST_C_Id.equals("admin")){ //������ ID�� ���
               ST_C_PW.println("101|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{  //������ ID�� �ƴ� ���
               System.out.println("[error]: ������ �����ϴ�. ������ ��ɾ� �Դϴ�.");
               getState();
            }
            break;
         case ST_AdminDeleteRoom_Req:   //103 [������] ä�ù� ���� ���� ��û
            if(ST_C_Id.equals("admin")){ //������ ID�� ���
               ST_C_PW.println("103|"+ArrayMsg[1]);
               ST_C_PW.flush();
            }else{  //������ ID�� �ƴ� ���
               System.out.println("[error]: ������ �����ϴ�. ������ ��ɾ� �Դϴ�.");
               getState();
            }
            break;
         case ST_AdminUpdateBlackListe_Req:   //105 [������] ������Ʈ ������Ʈ ���� ��û
            if(ST_C_Id.equals("admin")){ //������ ID�� ���
               if(ArrayMsg[1].equals("into")){ //������Ʈ �߰��� ���
                  ST_C_PW.println("105|1|"+ArrayMsg[2]);
                  ST_C_PW.flush();
               }else if(ArrayMsg[1].equals("delete")){ //������Ʈ ������ ���
                  ST_C_PW.println("105|2|"+ArrayMsg[2]);
                  ST_C_PW.flush();
               }else{
                  System.out.println("[error]: ������Ʈ ������Ʈ ��ɾ� ������ Ȯ�����ּ���. \"$UpdateBlackList [type] [userId]\" ('[',']'�� ����, �߰�:into, ����:delete)");
                  getState();
               }
            }else{  //������ ID�� �ƴ� ���
               System.out.println("[error]: ������ �����ϴ�. ������ ��ɾ� �Դϴ�.");
               getState();
            }
            break;
         default:                  //61 �⺻ �޽��� ���� [ST_SendMessage_Req] ���� ��ġ������ �⺻ ä�� ��û(��ϵ� Ư�� ��ɾ �ش����� ���� ���)
            if(ST_C_Id == null){ //��ȸ�� ���ӿ��� ��ϵ��� ���� ��ɾ� �޽����� ������ ���
               System.out.println("[error]: ���� ����(������)���� �������� �ʴ� ��ɾ� �Դϴ�. �α��� �Ǵ� ȸ�������� ���� ���ּ���.");
               System.out.println("[error]: �α��� ��ɾ� : \"#Login [LoginID] [LoginPwd]\" ('[',']'�� ����)");
               System.out.println("[error]: ȸ������ ��ɾ� :\"#Join [JoinID] [JoinPwd]\" ('[',']'�� ����)");
               getState();
            }else{ 
               //[ST_SendMessage_Req] ���� ��ġ���� �⺻ ä��(��ε�ĳ��Ʈ) ��û(61|�޽���|��û��ġ|��û���̵�)
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
         case ST_Hello_Ntf: //���Ӽ��� ȯ�� �޽��� ��ȯ
            System.out.println("[Server�� ���� ���޵� �޽���]: "+ArrayMsg[1]);
            getState();
            break;
         case ST_Join_Res:   // ȸ������ ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� ȯ���մϴ�. ȸ�����Կ� �����ϼ̽��ϴ�. �α��� ���ּ���. ");
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� �̹� ��ϵ� ���̵��Դϴ�. �ٸ� ���̵�� ȸ������ ���ּ���. ");
                     getState();
                     break;
               case 3: System.out.println("[Server�� ���� ���޵� �޽���]: ���� ���� ���Դϴ�. ��� �Ŀ� �ٽ� �õ��Ͽ� �ּ���. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_Login_Res:   // �α��� ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� ȯ���մϴ�. "+ArrayMsg[3]+"���� ���� �ϼ̽��ϴ�.");
                     System.out.println("[Server�� ���� ���޵� �޽���]: \"Lobby\"�� �̵��մϴ�.");
                     ST_C_Location = "Lobby"; //���� ��ġ �κ�� ����
                     ST_C_Id = ArrayMsg[2];
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: ���� ���� ���Դϴ�. ��� �Ŀ� �ٽ� �õ��Ͽ� �ּ���. ");
                     getState();
                     break;
               case 3: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� ��ϵ��� ���� ���̵��Դϴ�. ���̵� Ȯ���� �ּ���. ");
                     getState();
                     break;
               case 4: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� ���� �н����尡 ��ġ���� �ʽ��ϴ�. �н����带 Ȯ���� �ּ���. ");
                     getState();
                     break;
               case 5: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"���� ���� ������Ʈ�� ��ϵ� �����Դϴ�. �����ڿ��� �����Ͻñ� �ٶ��ϴ�. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_Logout_Res:   // �α׾ƿ� ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� �α׾ƿ� �Ǽ̽��ϴ�.");
                     System.out.println("[Server�� ���� ���޵� �޽���]: \"��ȸ������\"���� �̵��մϴ�.");
                     ST_C_Location = "guest"; //���� ��ġ �Խ�Ʈ�� ����
                     ST_C_Id = null;
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: ���� ���� ���Դϴ�. ��� �Ŀ� �ٽ� �õ��Ͽ� �ּ���. ");
                     getState();
                     break;
               case 3: System.out.println("[error]: �α��� ���°� �ƴմϴ�. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_RoomList_Res:   // ä�ù� ��� ��ȯ
            System.out.println("[Server�� ���� ���޵� �޽���]: ���� �����Ǿ� �ִ� ä�ù� ����Ʈ = "+ArrayMsg[1]);
            getState();
            break;
         case ST_UserList_Res:   // ����� ��� ��ȯ
            if(ArrayMsg[1].equals("2")){
               System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� �������� �ʴ� ä�ù� �Դϴ�. Ȯ�� �� �ٽ� ��û���ּ���.");
            }else{
               if(ArrayMsg.length==3){
                  System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� ���ӵǾ� �ִ� ����ڰ� �����ϴ�.");
               }else{
                  System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� ���ӵǾ� �ִ� ����� ����Ʈ = "+ArrayMsg[3]);
               }
            }
            getState();
            break;
         case ST_CreateRoom_Res:   // ä�ù� ���� ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\" ä�ù��� �����Ǿ����ϴ�.");
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� �̹� �����ϴ� ä�ù� �Դϴ�. �ٸ� �̸����� ä�ù��� ������ �ּ���.");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_EnterRoom_Res:   // ä�ù� ���� ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\" ä�ù濡 ���� �ϼ̽��ϴ�.");
                     ST_C_Location = ArrayMsg[2]; //���� ��ġ ����
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[2]+"\"�� �����ϴ� �ʴ� ä�ù� �Դϴ�. ä�ù� �̸��� Ȯ�����ּ���.");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_DeleteRoom_Res:   // ä�ù� ���� ��� ��ȯ
            break;
         case ST_KickOutRoom_Res:   // ä�ù� �߹� ��� ��ȯ
            break;
         case ST_EnterLobby_Res:   // �κ� ���� ��� ��ȯ
            break;
         case ST_MailList_Res:   // ���� ��� ��� ��ȯ
            System.out.println("[Server�� ���� ���޵� �޽���]: \""+ArrayMsg[1]+"\" ���� ��� = "+ArrayMsg[2]);
            getState();
            break;
         case ST_ReadMail_Res:   // ���� �б� ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: ���� �б⿡ �����Ͽ����ϴ�. [DesAddress]"+ArrayMsg[2]+" [Title]"+ArrayMsg[3]+" [Content]"+ArrayMsg[4]);
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: �ش� ������ �������� �ʽ��ϴ�. Ȯ�� �� �ٽ� ��û���ּ���. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_WriteMail_Res:   // ���� ���� ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: ���� ���⿡ �����Ͽ����ϴ�. [DesAddress]"+ArrayMsg[2]+" [Title]"+ArrayMsg[3]+" [Content]"+ArrayMsg[4]);
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: ���� ���� ���Դϴ�. ��� �Ŀ� �ٽ� �õ��Ͽ� �ּ���. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_DeleteMail_Res:   // ���� ���� ��� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: System.out.println("[Server�� ���� ���޵� �޽���]: ���ϻ����� �����Ͽ����ϴ�. [DesAddress]"+ArrayMsg[2]+" [Title]"+ArrayMsg[3]);
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: �ش� ������ �������� �ʽ��ϴ�. Ȯ�� �� �ٽ� ��û���ּ���. ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_SendMessage_Res:   // �޽��� ���۹���
            if(ST_C_Id.equals(ArrayMsg[2])){ // �ڽ��� ���� �޽����� ������� ����
            }else{ // �ٸ� Ŭ���̾�Ʈ�κ��� �� �޽����� ���
               System.out.println("");
               System.out.println(ArrayMsg[1]);
               getState();
            }
            break;
         case ST_WebSearch_Res:   // [����] ���˻� ���۰�� ��ȯ(���糯��)
            try {
               String nowWeather = getWeather();
               String[] ArrayNowWeather = nowWeather.split("\\|");
               System.out.println("[ST-���糯��]: #���ӵ���:"+ArrayNowWeather[0]+"#--#���糯��:"+ArrayNowWeather[1]+"#--#��������:���û ���׳��� #");
            } catch (IOException e) {
               e.printStackTrace();
            }
            getState();
            break;
         case ST_SendFile_Res:   // ���� ���۰�� ��ȯ
            break;
         case ST_DownFile_Res:   // ���� �ٿ��� ��ȯ
            break;
         case ST_SendEmoticon_Res:   // [����] �̸�Ƽ�� ���۰�� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: getState();System.out.println(ArrayMsg[2]);
                     getState();
                     break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: ���� ������ �̸�Ƽ���� �ƴմϴ�. #�������� �̸�Ƽ��:�����,����# ");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_SendWhisper_Res:   // [����] �ӼӸ� ���۰�� ��ȯ (Ŭ���̾�Ʈ �ܿ����� ��ȣȭ ����)
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: getState();break;
               case 2: System.out.println("[Server�� ���� ���޵� �޽���]: ���� �������� ���� ����� �Դϴ�. Ȯ�� �� �ٽ� �ӼӸ��� ��û���ּ���.");
                     getState();
                     break;
               default:
                     break;
            }
            break;
         case ST_SendEncryMessage_Res:   // [����] ��ȣȭ �޽��� ���۰�� ��ȯ (Ŭ���̾�Ʈ �ܿ����� ��ȣȭ ����)
            if(ArrayMsg[3].equals(ST_C_Id)){ //�ڽ��� ���� ��ȣȭ �޽����� ���
               System.out.println("[ST-Chat]["+ArrayMsg[2]+"]["+ArrayMsg[3]+"]: "+getDecryption(ArrayMsg[1]));
               getState();
               break;
            }else{
               System.out.println();
               System.out.println("[ST-Chat]["+ArrayMsg[2]+"]["+ArrayMsg[3]+"]: "+getDecryption(ArrayMsg[1]));
               getState();
               break;
            }
         case ST_AdminMessage_Res:      // [������] ������ �޽��� ���۰�� ��ȯ
            System.out.println();
            System.out.println("[�����ڷ� ���� ���޵� ��ü �޽���]: "+ArrayMsg[1]);
            getState();
            break;
         case ST_AdminDeleteRoom_Res:   // [������] ä�ù� ���� ���۰�� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: 
                  System.out.println();
                  System.out.println("[Server�� ���� ���޵� �޽���]: �����ڴԲ��� ���� ������Ű�̽��ϴ�. \"Lobby\"�� �̵��մϴ�.");
                  ST_C_Location = "Lobby"; //���� ��ġ �κ�� ����
                  getState();
                  break;
               case 2: 
                  System.out.println();
                  System.out.println("[Server�� ���� ���޵� �޽���]: �������� �ʴ� ���Դϴ�. Ȯ�� �� �ٽ� ��û�� ���ּ���.");
                  getState();
                  break;
               case 3: 
                  System.out.println();
                  System.out.println("[Server�� ���� ���޵� �޽���]: ���������� \""+ArrayMsg[2]+"\"���� �����Ǿ����ϴ�.");
                  getState();
                  break;
               default:
                     break;
            }
            break;
         case ST_AdminUpdateBlackListe_Res:   // [������] ������Ʈ ������Ʈ ���۰�� ��ȯ
            switch (Integer.parseInt(ArrayMsg[1])) {
               case 1: //������Ʈ �߰� ����
                  System.out.println("[Server�� ���� ���޵� �޽���]: ���������� ������Ʈ�� \""+ArrayMsg[2]+"\"���� �߰��Ͽ����ϴ�.");
                  System.out.println("[Server�� ���� ���޵� �޽���]: ���� ������Ʈ: "+ArrayMsg[3]);
                  ST_C_Location = "Lobby"; //���� ��ġ �κ�� ����
                  getState();
                  break;
               case 2: //������Ʈ �߰� ����
                  System.out.println("[Server�� ���� ���޵� �޽���]: ������Ʈ�� \""+ArrayMsg[2]+"\"���� �̹� �����մϴ�.");
                  System.out.println("[Server�� ���� ���޵� �޽���]: ���� ������Ʈ: "+ArrayMsg[3]);
                  getState();
                  break;
               case 3: //������Ʈ ���� ����
                  System.out.println("[Server�� ���� ���޵� �޽���]: ���������� ������Ʈ���� \""+ArrayMsg[2]+"\"���� �����Ͽ����ϴ�.");
                  System.out.println("[Server�� ���� ���޵� �޽���]: ���� ������Ʈ: "+ArrayMsg[3]);
                  getState();
                  break;
               case 4: //������Ʈ ���� ����
                  System.out.println("[Server�� ���� ���޵� �޽���]: ������Ʈ�� \""+ArrayMsg[2]+"\"���� �������� �ʽ��ϴ�.");
                  System.out.println("[Server�� ���� ���޵� �޽���]: ���� ������Ʈ: "+ArrayMsg[3]);
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
   
   //Ŭ���̾�Ʈ �������(��ġ) ��ȯ
   private String getLocation(){
      return ST_C_Location;
   }
   
   //Ŭ���̾�Ʈ ������� �ܼ����
   private void getState(){
      if(ST_C_Id==null) System.out.print("[ST-Chat][��ȸ������]: ");
      else {
         System.out.print("[ST-Chat]["+getLocation()+"]["+ST_C_Id+"]: ");
      }
   }
   
   //ST-���ó���-Ŭ���̾�Ʈ������ġ�� ������� ���û ���׳��� ������ ���� http �Ľ�
   private String getWeather() throws IOException{
         URLConnection urlConn = null;
         URL url = new URL("http://www.kma.go.kr/weather/forecast/timeseries.jsp"); //���û ���׳��� �ּ�
         String temp = null;
         String tempAll = "";
         String result = "";
         String location = ""; //�����̸�
         String nowWeather = ""; //���糯��
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
         location = location.replaceAll("&nbsp;", " "); //�����̸�
         nowWeather = tempAll.substring(tempAll.indexOf("<dd class=\"now_weather1_right temp1 MB10\">")+42,
               + tempAll.indexOf("<dd class=\"now_weather1_right temp1 MB10\">")+45);
         return location+"|"+nowWeather;
   }
   
   //ST-�޽��� ��ȣȭ(RSA)
   private String getEncryption(String Encryption) throws Exception{
      String temp = Encryption;
      String result = StringCrypto.encrypt("GEOSERVICE", temp);
      return result;
   }
   //ST-�޽��� ��ȣȭ(RSA)
   private String getDecryption(String Decryption) throws Exception{
      String temp = Decryption;
      String result2 = StringCrypto.decrypt("GEOSERVICE", temp);
      return result2;
   }
   
}