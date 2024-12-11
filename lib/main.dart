import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'package:platform_via_flutter/user_model/user_model.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: CounterScreen(),
    );
  }
}

class NativeService {
  static const platform = MethodChannel('com.ab.services/native');

  // Call to start the native service
  Future<void> startService() async {
    try {
      await platform.invokeMethod('startService');
      print("service start ho gai");
    } on PlatformException catch (e) {
      print("Failed to start service: ${e.message}");
    }
  }

  // Listen for updates from native Android code
  void listenToNativeUpdates(Function(int) onUpdate, Function(String) respp) {
    platform.setMethodCallHandler((call) async {
      if (call.method == 'updateCount') {
        final int count = call.arguments as int;
        onUpdate(count); // Pass the count to your Flutter UI
      }
      if (call.method == 'updateResponse') {
        final String res = call.arguments as String;
        respp(res); // Pass the count to your Flutter UI
      }
    });
  }

  void sendApiResponseToFlutter(String addUrl) {
    platform.invokeMethod("addingApiUrl", addUrl);
  }
}

class CounterScreen extends StatefulWidget {
  @override
  _CounterScreenState createState() => _CounterScreenState();
}

class _CounterScreenState extends State<CounterScreen> {
  int counter = 0;
  String? response;
  final NativeService _nativeService = NativeService();

  List<Map<String, dynamic>> decodedData = [];
  List<UserModel> userModelList = [];

  @override
  void initState() {
    super.initState();
    // Start the native service and listen for updates
    _nativeService.listenToNativeUpdates(
      (int count) {
        setState(() {
          counter = count;
        });
      },
      (String resss) {
        setState(() {
          response = resss;
          try {
            userModelList = userModelFromJson(response!);
            print("<<<============ user model list ai hai : $userModelList");

            //   List<dynamic> list = jsonDecode(response!);
            //   decodedData =
            //       list.map((e) => Map<String, dynamic>.from(e)).toList();
          } catch (e) {
            print("<<<============ exception ai hai : $e");
          }
        });
      },
    );

    _nativeService.startService();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Native Service Counter')),
      body: Column(
        children: [
          Text('Counter: $counter', style: const TextStyle(fontSize: 30)),
          Expanded(
            child: ListView.builder(
              itemCount: userModelList.length,
              itemBuilder: (context, index) {
                return ListTile(
                  title: Text(userModelList[index].name),
                  subtitle: Text(userModelList[index].phone),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
