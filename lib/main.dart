import 'package:flutter/material.dart';
import 'package:flutter_nfc_kit/flutter_nfc_kit.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Crachá Virtual',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.purple),
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _status = 'Aguardando...';
  // O código que será registrado é o AID definido no apduservice.xml
  final String _codigoEnviado = "F0 A1 B2 C3 D4 E5 F6";

  @override
  void initState() {
    super.initState();
    _verificarNfc();
  }

  // Apenas verificamos se o NFC está ligado. A emulação é automática.
  void _verificarNfc() async {
    try {
      NFCAvailability availability = await FlutterNfcKit.nfcAvailability;
      if (availability == NFCAvailability.available) {
        setState(() {
          _status =
              'Crachá virtual ativado!\nMantenha o app aberto e aproxime da fechadura.';
        });
      } else {
        setState(() {
          _status = 'NFC não disponível ou desativado.';
        });
      }
    } catch (e) {
      setState(() {
        _status = 'Erro ao verificar NFC: ${e.toString()}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Meu Crachá de Acesso')),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.badge, size: 150, color: Colors.purple),
              const SizedBox(height: 30),
              Text(_status,
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 20),
              Text(
                'Código Enviado: $_codigoEnviado',
                style: Theme.of(context)
                    .textTheme
                    .bodyLarge
                    ?.copyWith(fontWeight: FontWeight.bold),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
