package cn.freshfans.flutter_mssql.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class SqlHelper {
  private String drive = "net.sourceforge.jtds.jdbc.Driver";
  private String connStr;
  private String server;     // 数据库地址
  private String port;       // 数据库端口号
  private String dbName;     // 数据库名称
  private String userName;   // 用户名
  private String userPwd;    // 密码
  private Connection con;
  private PreparedStatement pstm;
  private PreparedStatement pstm1;
  private PreparedStatement pstm2;

  public SqlHelper(String server, String port, String dbName, String userName, String userPwd) {
    this.server = server;
    this.port = port;
    this.dbName = dbName;
    this.connStr = "jdbc:jtds:sqlserver://" + this.server + ":" + this.port + "/" + this.dbName;
    this.userName = userName;
    this.userPwd = userPwd;

    try {
      Class.forName(drive);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  //处理一条sql
  public int ExecuteNonQuery(String sql, List<Object> params) {
    try {
      con = DriverManager.getConnection(this.connStr, this.userName, this.userPwd);
      pstm = con.prepareStatement(sql);
      if (params != null && !params.equals("")) {
        for (int i = 0; i < params.size(); i++) {
          pstm.setObject(i + 1, params.get(i));
        }
      }
      return pstm.executeUpdate();
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      return -1;
    } finally {
      try {
        pstm.close();
        con.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  //处理两条sql -->事务处理
  public int ExecuteNonQueryTwo(String sql1, String sql2, List<Object> params1, List<Object> params2) {

    try {
      con = DriverManager.getConnection(this.connStr, this.userName, this.userPwd);
      con.setAutoCommit(false);//将自动提交关闭
      pstm1 = con.prepareStatement(sql1);
      pstm2 = con.prepareStatement(sql2);

      if (params1 != null && !params1.equals("")) { //第一条sql
        for (int i = 0; i < params1.size(); i++) {
          pstm1.setObject(i + 1, params1.get(i));
        }
        pstm1.addBatch();
      }
      if (params2 != null && !params2.equals("")) { //第二条sql
        for (int i = 0; i < params2.size(); i++) {
          pstm2.setObject(i + 1, params2.get(i));
        }
        pstm2.addBatch();
      }
      pstm1.executeUpdate();
      pstm2.executeUpdate();
      con.commit();  //提交事务
      return 1; //
      //return pstm.executeUpdate();
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      try {
        con.rollback();
      }catch (SQLException err) {
        err.printStackTrace();
      }
      return -1;
    } finally {
      try {
        pstm1.close();
        pstm2.close();
        con.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  //处理两条sql -->事务处理->批量处理
  public int ExecuteNonQueryTwoBatch(String sql1, String sql2, List<Object> params1, List<Object> params2) {

    try {
      con = DriverManager.getConnection(this.connStr, this.userName, this.userPwd);
      con.setAutoCommit(false);//将自动提交关闭
      pstm1 = con.prepareStatement(sql1);
      pstm2 = con.prepareStatement(sql2);
      long startTime = System.currentTimeMillis();
      //params1，params2为二维数组
      if (params1 != null && params1.size() > 0) { //第一条sql赋值
        int len1 = params1.size();
        for (int i = 0; i < len1; i++) {
          List clist = (List) params1.get(i);
         // System.out.println("p1写入数据key：" + i + "值：" + clist);
          int clen1 = clist.size();
          for(int j = 0; j < clen1; j++) {
            pstm1.setObject(j + 1, clist.get(j));
          }
          pstm1.addBatch();
        }
      }
      if (params2 != null && params2.size() > 0) { //第二条sql赋值
        int len2 = params2.size();
        for (int i = 0; i < len2; i++) {
          List clist2 = (List) params2.get(i);
          int clen2 = clist2.size();
          for(int j = 0; j < clen2; j++) {
            pstm2.setObject(j + 1, clist2.get(j));
          }
         pstm2.addBatch(); 
        }
      }
      pstm1.executeBatch();
      pstm2.executeBatch();
      con.commit();  //提交事务
      con.setAutoCommit(true);//将自动提交
      long endTime = System.currentTimeMillis();
      System.out.println(params1.size() + "调数据，批量事务插入时间:"+(endTime-startTime)+" ms");
      return 1; //
      //return pstm.executeUpdate();
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
      try {
        con.rollback();
      }catch (SQLException err) {
        err.printStackTrace();
      }
      return -1;
    } finally {
      try {
        pstm1.close();
        pstm2.close();
        con.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public String ExecuteQuery(String sql, List<Object> params) {
    // TODO Auto-generated method stub
    JSONArray jsonArray = new JSONArray();
    try {
      con = DriverManager.getConnection(this.connStr, this.userName, this.userPwd);
      pstm = con.prepareStatement(sql);
      if (params != null && !params.equals("")) {
        for (int i = 0; i < params.size(); i++) {
          pstm.setObject(i + 1, params.get(i));
        }
      }
      ResultSet rs = pstm.executeQuery();
      ResultSetMetaData rsMetaData = rs.getMetaData();
      while (rs.next()) {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
          String columnName = rsMetaData.getColumnLabel(i + 1);
          String value = rs.getString(columnName);
          jsonObject.put(columnName, value);
        }
        jsonArray.put(jsonObject);
      }
      return jsonArray.toString();
    } catch (Exception e) {
      // TODO: handle exception
      return null;
    } finally {
      try {
        pstm.close();
        con.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}

