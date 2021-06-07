import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_mssql/flutter_mssql.dart';
import 'package:flutter_mssql_example/Sqlserver.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    initDataBase();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterMssql.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> initDataBase() async {
    try {
      await FlutterMssql.initDataBase(
          "192.168.2.65", "1433", "CDE", "xfdev", "Qwer1234");
    } catch (e) {
      print("数据库初始化失败：" + e.toString());
    }
  }

  Future<void> insert() async {
    List params = [];
    params.add(Sqlserver.randomBit(24));
    params.add("456734");
    params.add(104);
    params.add("201812050443005");
    params.add("红丝绒瑞士卷1");
    params.add(35);
    params.add(1);
    params.add(35);
    params.add(2);
    params.add(35);
    try {
      await FlutterMssql.executeSql(
          "INSERT INTO [t_port085_d]([SaleNo],[ItemNo],[SerialNo],[BarCode],[ItemName],[Price],[ItemQty],[ymoney],[zmoney],[SaleMount]) VALUES (?,?,?,?,?,?,?,?,?,?)",
          params);
    } catch (e) {
      print("数据库操作失败：" + e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Text('Running on: $_platformVersion\n'),
            ElevatedButton(
              onPressed: () {
                 var db = new Sqlserver("192.168.2.65", "1433", "CDE", "xfdev", "Qwer1234");
                 db.initDataBase();
                 print('数据库连接状态：${Sqlserver.dbStatus}');
              },
              child: Text("数据连接"),
            ),
            ElevatedButton(
              onPressed: () {
                /// 插入数据
                Sqlserver.insert(0);
              },
              child: Text("插入数据"),
            ),
            ElevatedButton(
              onPressed: () {
                /// 插入数据
                Sqlserver.insert(1);
              },
              child: Text("事务插入两条数据"),
            ),
            ElevatedButton(
              onPressed: () {
                /// 插入数据
                Sqlserver.insert(2);
              },
              child: Text("批量插入两个表数据"),
            ),
            ElevatedButton(
              onPressed: () {
                /// 插入数据
                Sqlserver.insertSqlData();
              },
              child: Text("Statement方式插入表数据"),
            )
          ],
        ),
      ),
    );
  }
}
