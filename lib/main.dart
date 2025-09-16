// Importa o pacote principal do Flutter para construir a interface do usuário (widgets, temas, etc.).
import 'package:flutter/material.dart';
// Importa o pacote 'flutter_nfc_kit', que fornece as funcionalidades para interagir com o hardware NFC do dispositivo.
import 'package:flutter_nfc_kit/flutter_nfc_kit.dart';

// A função 'main' é o ponto de entrada de toda aplicação Flutter.
// Ela executa o aplicativo iniciando o widget raiz, que neste caso é o 'MyApp'.
void main() {
  runApp(const MyApp());
}

// 'MyApp' é o widget raiz da aplicação.
// É um 'StatelessWidget' porque seu conteúdo não muda ao longo do tempo.
// Ele configura a estrutura básica do app, como o título, o tema e a tela inicial.
class MyApp extends StatelessWidget {
  // Construtor do widget. 'key' é usado pelo Flutter para identificar widgets de forma única.
  const MyApp({super.key});

  // O método 'build' é responsável por descrever a parte da interface do usuário
  // representada por este widget. Ele é chamado pelo framework do Flutter.
  @override
  Widget build(BuildContext context) {
    // 'MaterialApp' é um widget que envolve vários outros widgets que são comumente
    // necessários para aplicações com Material Design.
    return MaterialApp(
      title: 'Crachá Virtual', // Título do app, usado pelo sistema operacional (ex: na lista de apps recentes).
      theme: ThemeData(
        // Define o tema visual do aplicativo.
        // 'ColorScheme.fromSeed' cria um esquema de cores a partir de uma cor principal (seedColor).
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.purple),
        // Habilita o uso do Material 3, a versão mais recente do design system do Google.
        useMaterial3: true,
      ),
      // 'home' define o widget que será exibido como a tela inicial do aplicativo.
      home: const HomePage(),
    );
  }
}

// 'HomePage' é a tela principal do nosso aplicativo.
// É um 'StatefulWidget' porque o seu conteúdo (o status do NFC) pode mudar
// durante a execução do app, exigindo que a tela seja redesenhada.
class HomePage extends StatefulWidget {
  const HomePage({super.key});

  // Cria o objeto de estado associado a este widget.
  @override
  State<HomePage> createState() => _HomePageState();
}

// '_HomePageState' é a classe que contém o estado e a lógica da 'HomePage'.
class _HomePageState extends State<HomePage> {
  // Variável para armazenar e exibir a mensagem de status do NFC na tela.
  // Inicia com 'Aguardando...'
  String _status = 'Aguardando...';

  // Esta é a string que o celular irá "anunciar" quando estiver em modo de emulação de cartão.
  // IMPORTANTE: Este valor (AID - Application ID) deve ser EXATAMENTE o mesmo
  // que está definido no arquivo de configuração nativo do Android (`apduservice.xml`).
  // O leitor NFC (a fechadura) é configurado para procurar por este AID específico.
  final String _codigoEnviado = "F0 A1 B2 C3 D4 E5 F6";

  // O método 'initState' é chamado uma única vez quando o widget é inserido na árvore de widgets.
  // É o lugar ideal para inicializar dados ou iniciar processos que precisam acontecer no início.
  @override
  void initState() {
    super.initState();
    // Chama a função para verificar o status do NFC assim que a tela é carregada.
    _verificarNfc();
  }

  // Função assíncrona para verificar a disponibilidade do NFC no dispositivo.
  // A emulação de cartão (HCE) é gerenciada pelo sistema operacional Android,
  // então o app Flutter só precisa garantir que o NFC esteja ligado.
  void _verificarNfc() async {
    try {
      // Usa o pacote 'flutter_nfc_kit' para obter o estado atual do NFC.
      // 'await' pausa a execução da função até que a verificação seja concluída.
      NFCAvailability availability = await FlutterNfcKit.nfcAvailability;

      // Verifica o resultado da consulta.
      if (availability == NFCAvailability.available) {
        // Se o NFC está disponível e ativado, atualiza o estado para informar ao usuário.
        // 'setState' notifica o Flutter que o estado mudou, fazendo com que o método 'build'
        // seja chamado novamente para redesenhar a tela com a nova mensagem.
        setState(() {
          _status =
              'Crachá virtual ativado!\nMantenha o app aberto e aproxime da fechadura.';
        });
      } else {
        // Se o NFC não está disponível (o hardware não existe) ou está desativado.
        setState(() {
          _status = 'NFC não disponível ou desativado.';
        });
      }
    } catch (e) {
      // Bloco 'catch' para capturar qualquer erro que possa ocorrer durante a verificação do NFC.
      setState(() {
        _status = 'Erro ao verificar NFC: ${e.toString()}';
      });
    }
  }

  // O método 'build' descreve a aparência da tela e é reconstruído toda vez que 'setState' é chamado.
  @override
  Widget build(BuildContext context) {
    // 'Scaffold' é um layout básico do Material Design. Ele fornece uma estrutura
    // para a tela, incluindo AppBar (barra de título), Body (corpo) e mais.
    return Scaffold(
      appBar: AppBar(title: const Text('Meu Crachá de Acesso')),
      // O corpo da tela.
      body: Center( // Centraliza o conteúdo filho horizontal e verticalmente.
        child: Padding( // Adiciona um espaçamento interno (margem) em volta do conteúdo.
          padding: const EdgeInsets.all(20.0),
          child: Column( // Organiza os widgets filhos em uma coluna vertical.
            mainAxisAlignment: MainAxisAlignment.center, // Centraliza a coluna no eixo vertical.
            children: [
              // Um ícone para representar visualmente o crachá.
              const Icon(Icons.badge, size: 150, color: Colors.purple),
              // Um espaço vertical de 30 pixels entre o ícone e o texto de status.
              const SizedBox(height: 30),
              // Widget de texto que exibe o conteúdo da variável '_status'.
              Text(
                _status,
                textAlign: TextAlign.center, // Centraliza o texto.
                style: Theme.of(context).textTheme.headlineSmall, // Aplica um estilo de texto pré-definido pelo tema.
              ),
              // Outro espaço vertical.
              const SizedBox(height: 20),
              // Widget de texto para mostrar o código (AID) que está sendo enviado.
              Text(
                'Código Enviado: $_codigoEnviado', // Interpola a variável na string.
                style: Theme.of(context)
                    .textTheme
                    .bodyLarge // Usa o estilo 'bodyLarge' do tema.
                    ?.copyWith(fontWeight: FontWeight.bold), // E o deixa em negrito.
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
