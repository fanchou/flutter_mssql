package cn.freshfans.flutter_mssql;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.freshfans.flutter_mssql.utils.SqlHelper;
import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** FlutterMssqlPlugin */
public class FlutterMssqlPlugin implements FlutterPlugin, MethodCallHandler {

  private MethodChannel channel;
  private Handler uiThreadHandler = new Handler(Looper.getMainLooper());

  /**
   * 数据库操作相关参数
  */

  private String server;     // 数据库地址
  private String port;       // 数据库端口号
  private String dbName;     // 数据库名称
  private String userName;   // 用户名
  private String userPwd;    // 密码
  private SqlHelper sh;
  private String sql;
  private List<Object> params;

  //处理两条
  private String sql1;
  private String sql2;
  private List<Object> params1;
  private List<Object> params2;
  //是否批量处理
  private boolean isBatch;



  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_mssql");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if(call.method.equals("initDataBase")){
      server = call.argument("server");
      port = call.argument("port");
      dbName = call.argument("dbName");
      userName = call.argument("userName");
      userPwd = call.argument("userPwd");
      sh = new SqlHelper(server,port,dbName,userName,userPwd);
      initDataBase(result);
    }else if(call.method.equals("executeSql")){
      sql = call.argument("sql");
      params = call.argument("params");
      ExecuteSql(sql, params, result);
    }else if(call.method.equals("executeSqlTwo")){
      sql1 = call.argument("sql1");
      params1 = call.argument("params1");
      sql2 = call.argument("sql2");
      params2 = call.argument("params2");
      isBatch = call.argument("isBatch") ? true : false; 
      ExecuteSqlTwo(sql1,sql2,params1,params2,result);
    } else {
      result.notImplemented();
    }
  }

  /**
   * 初始化数据库
   */

  private void initDataBase(@NonNull Result result){
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("message", "初始化成功");
    params.put("code", "200");
    Log.d("initDataBase","初始化数据库成功！");
    result.success(params);
  }

  /**
   * 执行数据库操作
   */
  public void ExecuteSql(final String sql, final List<Object> params, @NonNull final Result result) {
      // 单独开启一个线程
     new Thread(new Runnable() {
        @Override
        public void run() {
          String b = Execute(sql, params, result);
          Log.i("数据执行","数据库执行返回结果"+ b);
        }
      }).start();
  }
  /**
   * 执行数据库操作 -事务处理两条sql
   */
  public void ExecuteSqlTwo(final String sql1, final String sql2, final List<Object> params1,final List<Object> params2, @NonNull final Result result) {
    // 单独开启一个线程
    new Thread(new Runnable() {
      @Override
      public void run() {
        String b = ExecuteTwo(sql1, sql2, params1, params2, result);
        Log.i("数据执行","数据库执行返回结果"+ b);
      }
    }).start();
  }

  /// ExecuteSql
  public String Execute(String sql, final List<Object> params, @NonNull final Result result) {
    Log.d("执行数据库操作",sql);
    try {
      int count = sh.ExecuteNonQuery(sql, params);
      if (count > 0) {
        final Map<String, Object> callParams = new HashMap<String, Object>();
        callParams.put("message", "操作成功");
        callParams.put("code", "200");
        // 需要在主线程中返回
        uiThreadHandler.post(new Runnable(){
          @Override
          public void run() {
            result.success(callParams);
          }
        });
        return "操作成功";
      } else {
        uiThreadHandler.post(new Runnable(){
          @Override
          public void run() {
            result.error("500","操作失败",null);
          }
        });
        return "操作失败！";
      }
    } catch (final Exception e) {
      System.out.println(e.getMessage());
      uiThreadHandler.post(new Runnable(){
        @Override
        public void run() {
          result.error("500",e.toString(),null);
        }
      });
      return "操作失败！";
    }
  }
  /// ExecuteSql
  public String ExecuteTwo(String sql1, String sql2, final List<Object> params1, final List<Object> params2, @NonNull final Result result) {
    //Log.d("TAG","执行数据库操作sql1:" + sql1);
    //Log.d("TAG","执行数据库操作sql2:"+ sql2);
    //Log.d("TAG","数据params1:"+ params1.toString());
    //Log.d("TAG","数据params2:"+params2.toString());
    Log.d("TAG","是否批量处理isBatch:"+isBatch);
    //System.out.println("isBatch:" + isBatch);
    try {
      int count;
      if(isBatch) { //批量处理
        count = sh.ExecuteNonQueryTwoBatch(sql1, sql2, params1, params2);
      }else {
        count = sh.ExecuteNonQueryTwo(sql1, sql2, params1, params2);
      }
      if (count > 0) {
        final Map<String, Object> callParams = new HashMap<String, Object>();
        callParams.put("message", "操作成功");
        callParams.put("code", "200");
        // 需要在主线程中返回
        uiThreadHandler.post(new Runnable(){
          @Override
          public void run() {
            result.success(callParams);
          }
        });
        return "操作成功";
      } else {
        uiThreadHandler.post(new Runnable(){
          @Override
          public void run() {
            result.error("500","操作失败",null);
          }
        });
        return "操作失败！";
      }
    } catch (final Exception e) {
      System.out.println(e.getMessage());
      uiThreadHandler.post(new Runnable(){
        @Override
        public void run() {
          result.error("500",e.toString(),null);
        }
      });
      return "操作失败！";
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

}
