import 'dart:async';

import 'package:flutter/services.dart';

class FlutterMssql {
  static const MethodChannel _channel = const MethodChannel('flutter_mssql');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 初始化数据库
  static Future<Map<String, dynamic>> initDataBase(String server, String port,
      String dbName, String userName, String userPwd) async {
    final Map<String, dynamic>? result =
        await _channel.invokeMapMethod('initDataBase', {
      "server": server,
      "port": port,
      "dbName": dbName,
      "userName": userName,
      "userPwd": userPwd
    });

    return result!;
  }

  /// 执行数据库操作
  static Future<Map<String, dynamic>> executeSql(
      String sql, List params) async {
    //print('mssql主入口文件executeSql：${params.toString()}');
    final Map<String, dynamic>? result = await _channel
        .invokeMapMethod('executeSql', {"sql": sql, "params": params});
    return result!;
  }

  /// 执行数据库操作(开启事务，执行两条sql)
  static Future<Map<String, dynamic>> executeSqlTwo(String sql1, String sql2,
      List params1, List params2, bool isBatch) async {
    //print('mssql主入口文件executeSqlTwo：${params1.toString()}');
    final Map<String, dynamic>? result =
        await _channel.invokeMapMethod('executeSqlTwo', {
      "sql1": sql1,
      "sql2": sql2,
      "params1": params1,
      "params2": params2,
      "isBatch": isBatch
    });
    return result!;
  }

  /// Statement方式
  static Future<Map<String, dynamic>> ExecuteInsertData(String sql) async {
    final Map<String, dynamic>? result =
        await _channel.invokeMapMethod('ExecuteInsertData', {"sql": sql});
    return result!;
  }
}
