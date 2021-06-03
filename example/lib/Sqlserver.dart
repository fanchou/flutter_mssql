
import 'dart:math';
import 'package:flustars/flustars.dart';
import 'package:flutter_mssql/flutter_mssql.dart';
import 'package:intl/intl.dart';

class Sqlserver {
  String server;
  String port;
  String dbName;
  String userName;
  String userPwd;
  static bool dbStatus;
  Sqlserver(this.server, this.port, this.dbName, this.userName, this.userPwd);
  //初始化数据库连接
  Future<bool> initDataBase() async {
    try {
      // await FlutterMssql.initDataBase(server, port, dbName, userName, userPwd);
      Sqlserver.dbStatus = true;
    } catch (e) {
      Sqlserver.dbStatus = false;
      print('数据库初始化失败' + e.toString());
    }
  }

  //插入入数据
  static Future<void> insert(int type) async {
    List params1 = [];
    List params2 = [];
    String SaleNo = Sqlserver.getStr24();
    params1 = Sqlserver.getTableD(SaleNo);
    params2 = Sqlserver.getTabeH(SaleNo);
    if(type == 1) {//开启事务插入
      insert_t_port085_h_d(params1,params2,false);
    }else if(type == 2)  { //事务批量插入
      insertBatch();
    }else{
      await insert_t_port085_d(params1);
      await insert_t_port085_h(params2);
    }
  }

  //批量插入数据
  static Future<void> insertBatch() async {
      List params1 = [];
      List params2 = [];
      const len = 100;
      for(int i = 0; i < len; i++) {
        List p1 = [];
        List p2 = [];
        String SaleNo = Sqlserver.getStr24();
        p1 = Sqlserver.getTableD(SaleNo);
        p2 = Sqlserver.getTabeH(SaleNo);
        params1.add(p1);
        params2.add(p2);
      }
      //print('数据params1:${params1.toString()}');
      //print('数据params2:${params2.toString()}');
      insert_t_port085_h_d(params1, params2, true);
  }

  //通过事务一次插入两条不同的sql
  static Future<void> insert_t_port085_h_d(List params1,List params2,bool isBatch) async {
    String sql1 = "INSERT INTO [t_port085_d]([SaleNo],[ItemNo],[SerialNo],[BarCode],[ItemName],[Price],[ItemQty],[ymoney],[zmoney],[SaleMount]) VALUES (?,?,?,?,?,?,?,?,?,?)";
    String sql2 = "INSERT INTO [t_port085_h]([SaleNo],[SaleDate],[LesCode],[BandCode],[SaleQty],[ymoney],[smoney],[zmoney],[SaleDay],[saleyear],[salemonth],[curperiod],[ReMark]) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      int start = DateUtil.getNowDateMs();
      await FlutterMssql.executeSqlTwo(sql1, sql2,params1,params2,isBatch);
      int end = DateUtil.getNowDateMs();
      print('${params1.length}条数据执行时间：${end - start} ms');
    }catch(e) {
      print("数据库操作失败insert_t_port085_h_d：" + e.toString());
   }
  }

  //插入一条数据（表：t_port085_d）
  static Future<void> insert_t_port085_d(List params) async {
    try {
      await FlutterMssql.executeSql(
          "INSERT INTO [t_port085_d]([SaleNo],[ItemNo],[SerialNo],[BarCode],[ItemName],[Price],[ItemQty],[ymoney],[zmoney],[SaleMount]) VALUES (?,?,?,?,?,?,?,?,?,?)",
          params);
    } catch (e) {
      print("数据库操作失败：" + e.toString());
    }
  }


  //插入一条数据（表：t_port085_h）
  static Future<void> insert_t_port085_h(List params) async {
    try {
      await FlutterMssql.executeSql(
          "INSERT INTO [t_port085_h]([SaleNo],[SaleDate],[LesCode],[BandCode],[SaleQty],[ymoney],[smoney],[zmoney],[SaleDay],[saleyear],[salemonth],[curperiod],[ReMark]) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
          params);
    } catch (e) {
      print("数据库操作失败：" + e.toString());
    }
  }

  static List getTableD(String SaleNo) {
    List params = [];
    params.add(SaleNo);
    params.add("456734");
    params.add(104);
    params.add("201812050443005");
    params.add("随机数:" + Sqlserver.randomBit(4));
    params.add(35);
    params.add(1);
    params.add(35);
    params.add(2);
    params.add(35);
    return params;
  }

  static List getTabeH(String SaleNo) {
    List params = [];
    var now = new DateTime.now();
    //print('日期：${DateUtil.getNowDateStr()}');
    //print(DateTime.now().toIso8601String().split(".").first.split("T").join(" "));
    //销售时间（精确到时分秒）
    String SaleDate = DateFormat("yyyy-MM-dd HH:mm:ss").format(now); //DateFormat("yyyy-MM-dd HH:mm:ss").format(now);
    //--商户编码 (19015901)(必填)
    String LesCode = '19015901';
    String BandCode = '201812050003'; //--品牌编码 (201812050003)(必填)
    int SaleQty = 12; //  --销售数量
    double ymoney = 12.4; //  --应收金额
    double smoney = 22; //  --实收金额
    double zmoney = 3.44; //  --折让金额
    String SaleDay =
    DateFormat('yyyy-MM-dd').format(now); //  --销售日期(具体到天，不需要时分秒)
    String saleyear = DateFormat('yyyy').format(now); //--年(yyyy)
    String salemonth = DateFormat('MM').format(now); //      --月(mm)
    String curperiod = DateFormat('yyyyMM').format(now); //--会计期间年月(yyyymm)
    String ReMark = '插入订单'; //VARCHAR(100)  NULL,      --备注

    params.add(SaleNo);
    params.add(SaleDate);
    params.add(LesCode);
    params.add(BandCode);
    params.add(SaleQty);
    params.add(ymoney);
    params.add(smoney);
    params.add(zmoney);
    params.add(SaleDay);
    params.add(saleyear);
    params.add(salemonth);
    params.add(curperiod);
    params.add(ReMark);
    //print('插入数据：${params}');
    return params;
  }
  //返回随机数
  static String randomBit(int len) {
    String scopeF = '123456789'; //首位
    String scopeC = '0123456789'; //中间
    String result = '';
    for (int i = 0; i < len; i++) {
      if (i == 0) {
        result = scopeF[Random().nextInt(scopeF.length)];
      } else {
        result = result + scopeC[Random().nextInt(scopeC.length)];
      }
    }
    return result;
  }
  //返回24位数字
  static String getStr24() {
    int timeStr =
    (new DateTime.now().millisecondsSinceEpoch ~/ 1000).toInt(); //10位时间戳
    String SaleNo = Sqlserver.randomBit(14).toString() + timeStr.toString();
    return SaleNo;
  }
}
