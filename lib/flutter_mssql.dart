
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterMssql {
  static const MethodChannel _channel = const MethodChannel('flutter_mssql');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// 初始化数据库
  static Future<Map<String, dynamic>> initDataBase(
      String server,
      String port,
      String dbName,
      String userName,
      String userPwd) async{
    final Map<String, dynamic> result = await _channel.invokeMapMethod('initDataBase', {
      "server": server,
      "port": port,
      "dbName": dbName,
      "userName": userName,
      "userPwd": userPwd
    });

    return result;
  }

  /// 执行数据库操作
  static Future<Map<String, dynamic>> executeSql(String sql, List params) async{
    final Map<String, dynamic> result = await _channel.invokeMapMethod('executeSql', {
      "sql": sql,
      "params": params
    });
    return result;
  }

}
