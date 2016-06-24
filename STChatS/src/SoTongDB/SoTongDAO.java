package SoTongDB;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class SoTongDAO {
	private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
    private static final String USER = "scott";
    private static final String PASS = "1234";
 
    public Connection getConn() {
        Connection con = null;
        try {
            Class.forName(DRIVER);
            con = DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    } 
    
    public String getTime() {
		Date time = new java.util.Date(); //�ڹٹ������� �����ϴ� DateŬ������ �̿��Ͽ� ��ǻ�ͽð��� �޾ƿ�
    	return String.valueOf(time.getTime());
    }
    
	//[ȸ������] Join
    public String Join(String userId, String userPwd) {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        try {
            con = getConn();
            String sql = "insert into ST_Member"
                    + "(ST_Member_ID,ST_Member_PWD,ST_MEMBER_TIME) "
                    + "values(?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, userPwd);
            ps.setString(3, getTime());
            result = ps.executeUpdate();
            if(result != 1){ //���̵� �ߺ� �� ���
            	return "22|3|"+userId;
            }else{ //ȸ�����Կ� �������� ���
            	return "22|1|"+userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
		return "22|2|"+userId;
    } 
    
    
    //[�α���] Login
    public String Login(String id, String pwd) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String userId = null;
        String userTime = null;
        try {
            con = getConn();
            String sql = "select * from ST_MEMBER where ST_Member_ID=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (rs.next()) { //���̵� ������ ���
            	if(rs.getString("ST_Member_PWD").equals(pwd)){ //��й�ȣ�� ��ġ�� ���
                    userId = rs.getString(1);
                    //��������Ʈ üũ
                    if(BlackListCheck(userId)==1){ //�ش� ���̵� ��������Ʈ�� ���
                    	return "24|5|"+userId;
                    }else{ //��������Ʈ�� �ƴ� ���
                    	//���� �α��� �ð����� ���� ����� �ð�(����:��)
                        int totalSec = Integer.parseInt(String.valueOf(Long.valueOf(getTime().substring(0, 10)) - Long.valueOf(rs.getString(3).substring(0, 10)))); 
                		int day = totalSec / (60 * 60 * 24);
                		int hour = (totalSec - day * 60 * 60 * 24) / (60 * 60); 
                		int minute = (totalSec - day * 60 * 60 * 24 - hour * 3600) / 60; 
                		int second = totalSec % 60;
                		userTime = day + "�� " + hour + "�ð� " + minute + "�� " + second +"�� ";
                        //���� �α��� �ð� ������Ʈ �Լ� ȣ��
                        LoginTimeUpdate(id);
                    }
            		return "24|1|"+userId+"|"+userTime;
            	}else{ //��й�ȣ�� ����ġ�� ���
            		return "24|4|"+rs.getString(1);
            	}
            }else{ //���̵� ���� ���
            	return "24|3|"+id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {try {rs.close();} catch (SQLException e2) {e2.printStackTrace();}
            }
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
        return "24|2|"+id+"|";
    }
    
    
    //[���� �α��� �ð� ������Ʈ] LoginTime Update
    public void LoginTimeUpdate(String id) throws ClassNotFoundException, SQLException {
    	Class.forName("oracle.jdbc.driver.OracleDriver");
    	Connection con = DriverManager.getConnection(URL, USER, PASS);
    	Statement stmt = con.createStatement();
    	int result = 0;
    	
        try {
        	result = stmt.executeUpdate("update st_member set ST_MEMBER_TIME="+getTime()+" where st_member_id='"+id+"'");
        	System.out.println("[S-ST_Login_Req2]: �α��� ó���� �Ϸ��ϰ� ���� �α��� �ð��� ������Ʈ�մϴ�. ������Ʈ �� ���� �α��� �ð�: "+getTime());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {stmt.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
    }
    

    //[���Ͼ���] WriteMail
    public String WriteMail(String DesAddress, String SrcAddress, String Title, String Content) {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        try {
            con = getConn();
            String sql = "insert into ST_EMAIL"
                    + "(ST_EMAIL_INDEX, ST_EMAIL_DES, ST_EMAIL_SRC, ST_EMAIL_MSG, ST_EMAIL_TITLE) "
                    + "values(ST_EMAIL_INDEX.nextval,?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, DesAddress);
            ps.setString(2, SrcAddress);
            ps.setString(3, Content);
            ps.setString(4, Title);
            result = ps.executeUpdate();
            if(result != 1){ //�����������
            	return "56|2|"+DesAddress+"|"+SrcAddress+"|"+Title+"|"+Content;
            }else{ //ȸ�����Կ� �������� ���
            	return "56|1|"+DesAddress+"|"+SrcAddress+"|"+Title+"|"+Content;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
		return "56|2|"+DesAddress+"|"+SrcAddress+"|"+Title+"|"+Content;
    } 
    
    
    //[���ϸ��] MailList
    public String MailList(String DesAddress) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String temp = ""; //���� ����� ���� ����
        int result = 0;
        try {
            con = getConn();
            String sql = "select * from ST_EMAIL where ST_EMAIL_DES=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, DesAddress);
            rs = ps.executeQuery();
            while (rs.next()) { //������ ������ ���
            	temp += "["+rs.getString("ST_EMAIL_TITLE")+"]";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
		return "52|"+DesAddress+"|"+temp;
    } 
    
    
    //[�����б�] ReamMail
    public String ReadMail(String DesAddress, String Title) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String temp = ""; //���� ���� ������ ���� ����
        int result = 0;
        try {
            con = getConn();
            String sql = "select * from ST_EMAIL where ST_EMAIL_DES=? and ST_EMAIL_TITLE=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, DesAddress);
            ps.setString(2, Title);
            rs = ps.executeQuery();
            if (rs.next()) { //������ ������ ���
            	temp = rs.getString("ST_EMAIL_MSG");
            	return "54|1|"+DesAddress+"|"+Title+"|"+temp;
            }else{ //������ �������� ���� ���
            	return "54|2";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
    	return "54|2";
    } 
    
    
    //[���ϻ���] DeleteMail
    public String DeleteMail(String DesAddress, String Title) {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        try {
            con = getConn();
            String sql = "delete from ST_EMAIL where ST_EMAIL_DES=? and ST_EMAIL_TITLE=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, DesAddress);
            ps.setString(2, Title);
            result = ps.executeUpdate();
            if(result != 1){ //�����������
            	return "58|2|"+DesAddress+"|"+Title;
            }else{ //���ϻ����� �������� ���
            	return "58|1|"+DesAddress+"|"+Title;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
    	return "58|2|"+DesAddress+"|"+Title;
    } 
    
    //[��������Ʈ ������Ʈ(�߰�)] BlackListUpdateInto
    public String BlackListUpdateInto(String userId) {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        try {
            con = getConn();
            String sql = "insert into ST_BLACKLIST"
                    + "(ST_BLACKLIST_SEQ, ST_BLACKLIST_ID) "
                    + "values(ST_BLACKLIST_SEQ.nextval,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, userId);
            result = ps.executeUpdate();
            if(result != 1){ //�̹� ��ϵǾ��� ���
            	return "106|2|"+userId;
            }else{ //��������Ʈ �߰��� �������� ���
            	return "106|1|"+userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
		return "106|2|"+userId;
    } 

    //[��������Ʈ ������Ʈ(����)] BlackListUpdateDelete
    public String BlackListUpdateDelete(String userId) {
        Connection con = null;
        PreparedStatement ps = null;
        int result = 0;
        try {
            con = getConn();
            String sql = "delete from ST_BLACKLIST where ST_BLACKLIST_ID=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, userId);
            result = ps.executeUpdate();
            if(result != 1){ //�����������
            	return "106|4|"+userId;
            }else{ //��������Ʈ ������ �������� ���
            	return "106|3|"+userId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
    	return "106|4|"+userId;
    } 
    
    //[��������Ʈ ������Ʈ(����Ʈ)] BlackListUpdateList
    public String BlackListUpdateList() {
        Connection con = null;
        PreparedStatement ps = null;
        String ListTemp = "";
        ResultSet rs = null;
        try {
            con = getConn();
            String sql = "select * from ST_BLACKLIST";
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) { //��������Ʈ�� ������ ���
            	ListTemp += "["+rs.getString("ST_BLACKLIST_ID")+"]";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
    	return ListTemp;
    } 

    //[�α��� �� ��������Ʈ üũ)] BlackListCheck
    public int BlackListCheck(String userId) {
        Connection con = null;
        PreparedStatement ps = null;
        String ListTemp = "";
        ResultSet rs = null;
        int check = 0;
        try {
            con = getConn();
            String sql = "select * from ST_BLACKLIST where ST_BLACKLIST_ID=?";
            ps = con.prepareStatement(sql);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            if(rs.next()) { //��������Ʈ�� ������ ���
            	check = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {ps.close();} catch (SQLException e2) {e2.printStackTrace();} 
            }
            if (con != null) {
                try {con.close();} catch (SQLException e2) {e2.printStackTrace();}
            } 
        }
    	return check;
    } 
}

