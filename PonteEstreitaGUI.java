import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class PonteEstreitaGUI extends JFrame {

    private List<Carro> carros; // Lista para armazenar os carros criados

    private JPanel panelCarros; // Painel para exibir informações sobre os carros
    private JButton btnNovoCarro; // Botão para criar um novo carro
    private JTextField txtIdentificador; // Campo de texto para inserir o identificador do carro
    private JTextField txtTempoTravessia; // Campo de texto para inserir o tempo de travessia
    private JTextField txtTempoPermanencia; // Campo de texto para inserir o tempo de permanência
    private JComboBox<String> cmbSentido; // ComboBox para selecionar o sentido de travessia do carro
    private JComboBox<String> cmbCor; // ComboBox para selecionar a cor do carro
    private PontePanel pontePanel; // Painel para desenhar a ponte e os carros

    // Filas de carros aguardando em cada lado da ponte
    private Queue<Carro> filaLeste;
    private Queue<Carro> filaOeste;

    // Semáforos para controlar o acesso dos carros à ponte
    public static Semaphore semaforoLeste;
    public static Semaphore semaforoOeste;

    public PonteEstreitaGUI() {
        super("Problema da Ponte Estreita");
        carros = new ArrayList<>(); // Inicializa a lista de carros

        filaLeste = new LinkedList<>();
        filaOeste = new LinkedList<>();

        panelCarros = new JPanel(); // Inicializa o painel para exibir informações sobre os carros
        panelCarros.setLayout(new BoxLayout(panelCarros, BoxLayout.Y_AXIS)); // Define o layout do painel

        btnNovoCarro = new JButton("Novo Carro"); // Cria um botão para criar um novo carro
        btnNovoCarro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarNovoCarro(); // Adiciona um ActionListener ao botão para criar um novo carro quando clicado
            }
        });

        /*  Painel para inserir o identificador do carro, o tempo de travessia, o tempo
        de permanência, selecionar o sentido de travessia e a cor do carro */
        JPanel panelEntradas = new JPanel(new GridLayout(5, 2));
        panelEntradas.add(new JLabel("Identificador do Carro:"));
        txtIdentificador = new JTextField(5);
        panelEntradas.add(txtIdentificador);
        panelEntradas.add(new JLabel("Tempo de Travessia (s):"));
        txtTempoTravessia = new JTextField(5);
        panelEntradas.add(txtTempoTravessia);
        panelEntradas.add(new JLabel("Tempo de Permanência (s):"));
        txtTempoPermanencia = new JTextField(5);
        panelEntradas.add(txtTempoPermanencia);
        panelEntradas.add(new JLabel("Sentido:"));
        cmbSentido = new JComboBox<>(new String[]{"Leste para Oeste", "Oeste para Leste"});
        panelEntradas.add(cmbSentido);
        panelEntradas.add(new JLabel("Cor do Carro:"));
        cmbCor = new JComboBox<>(new String[]{"Vermelho", "Azul", "Verde", "Amarelo", "Preto"});
        panelEntradas.add(cmbCor);

        JPanel panelBotoes = new JPanel();
        panelBotoes.add(panelEntradas);
        panelBotoes.add(btnNovoCarro);

        pontePanel = new PontePanel(); // Inicializa o painel para desenhar a ponte e os carros

        getContentPane().setLayout(new BorderLayout()); // Define o layout da janela
        getContentPane().add(panelBotoes, BorderLayout.NORTH); // Adiciona o painel de botões à parte superior da janela
        getContentPane().add(pontePanel, BorderLayout.CENTER); // Adiciona o painel de desenho ao centro da janela
        getContentPane().add(panelCarros, BorderLayout.SOUTH); // Adiciona o painel de informações sobre os carros ao
        // sul da janela

        setSize(800, 500); // Define o tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Define o comportamento padrão ao fechar a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setVisible(true); // Torna a janela visível

        // Inicializa os semáforos com permissão para apenas um carro atravessar a ponte ao mesmo tempo
        semaforoLeste = new Semaphore(1);
        semaforoOeste = new Semaphore(1);
    }

    private void criarNovoCarro() {
        try {
            // Obtém os valores inseridos nos campos de texto e no ComboBox
            int identificador = Integer.parseInt(txtIdentificador.getText());
            int tempoTravessia = Integer.parseInt(txtTempoTravessia.getText());
            int tempoPermanencia = Integer.parseInt(txtTempoPermanencia.getText());
            int sentidoIndex = cmbSentido.getSelectedIndex();
            Sentido sentido = (sentidoIndex == 0) ? Sentido.LESTE_PARA_OESTE : Sentido.OESTE_PARA_LESTE;
            Color cor = obterCorSelecionada();

            // Cria um novo carro com os valores obtidos
            Carro carro = new Carro(identificador, tempoTravessia, tempoPermanencia, sentido, cor);
            carros.add(carro); // Adiciona o carro à lista de carros
            // Adiciona uma label ao painel de carros para exibir as informações do novo
            // carro
            panelCarros.add(new JLabel("Carro " + carro.getIdentificador() + ": Tempo de Travessia - " + tempoTravessia
                    +
                    "s, Tempo de Permanência - " + tempoPermanencia + "s, Sentido - " + sentido));

            pontePanel.adicionarCarro(carro); // Adiciona o carro ao painel de desenho

            // Adiciona o carro à fila correspondente
            if (sentido == Sentido.LESTE_PARA_OESTE) {
                filaLeste.add(carro);
            } else {
                filaOeste.add(carro);
            }

            carro.start(); // Inicia a thread do carro
        } catch (NumberFormatException ex) {
            // Exibe uma mensagem de erro se os valores inseridos não forem numéricos
            JOptionPane.showMessageDialog(this,
                    "Por favor, insira valores numéricos válidos para identificador, tempo de travessia e tempo de permanência.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Color obterCorSelecionada() {
        switch (cmbCor.getSelectedIndex()) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.YELLOW;
            default:
                return Color.BLACK;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PonteEstreitaGUI::new); // Cria e exibe a janela Swing
    }

    class Carro extends Thread {
        private int identificador;
        private int tempoTravessia;
        private int tempoPermanencia;
        private Sentido sentido;
        private Color cor;
        private int xPosition; // Adiciona a posição x do carro

        public Carro(int identificador, int tempoTravessia, int tempoPermanencia, Sentido sentido, Color cor) {
            this.identificador = identificador;
            this.tempoTravessia = tempoTravessia;
            this.tempoPermanencia = tempoPermanencia;
            this.sentido = sentido;
            this.cor = cor;
            this.xPosition = 0; // Inicializa a posição x do carro como 0
        }

        public int getIdentificador() {
            return identificador;
        }

        public Sentido getSentido() {
            return sentido;
        }

        @Override
        public void run() {
            try {
                Semaphore semaforoAtual = (sentido == Sentido.LESTE_PARA_OESTE) ? semaforoLeste : semaforoOeste;

                // Carro aguarda na fila até ser sua vez de atravessar
                semaforoAtual.acquire();

                // Verifica se há carros atravessando no sentido oposto
                Semaphore semaforoOutro = (sentido == Sentido.LESTE_PARA_OESTE) ? semaforoOeste : semaforoLeste;
                if (semaforoOutro.availablePermits() == 0) {
                    // Se houver carros atravessando no sentido oposto, o carro espera
                    semaforoOutro.acquire();
                    semaforoOutro.release();
                }

                // Calcula o número de passos para atravessar a ponte com base no tempo de travessia
                int steps = (pontePanel.getWidth() - 200) / (tempoTravessia * 10);

                // Move o carro para a ponte gradualmente
                for (int i = 0; i < pontePanel.getWidth() - 200; i += steps) {
                    for(int k = 0; k<1000; k++){
                        for(int j = 0; j<2000; j++){
                            double soma = 0;
                            soma = soma + Math.sin(i) + tempoPermanencia;
                        }
                    }
                    pontePanel.moveCarro(this, i);
                }

                // Aguarda o tempo de permanência na ponte
                for(int k = 0; k<1000; k++){
                    for(int j = 0; j<2000; j++){
                        double soma = 0;
                        soma = soma + Math.sin(k) + Math.sin(k);
                    }
                }

                semaforoAtual.release(); // Libera a permissão para o próximo carro

                pontePanel.removeCarro(this); // Remove o carro do painel de desenho

                // Remove o carro da fila correspondente
                if (sentido == Sentido.LESTE_PARA_OESTE) {
                    filaLeste.remove(this);
                } else {
                    filaOeste.remove(this);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    enum Sentido {
        LESTE_PARA_OESTE,
        OESTE_PARA_LESTE
    }

    class PontePanel extends JPanel {
        private List<Carro> carrosNaPonte;

        public PontePanel() {
            carrosNaPonte = new ArrayList<>();
        }

        public void adicionarCarro(Carro carro) {
            carrosNaPonte.add(carro);
            repaint(); // Redesenha o painel para exibir o novo carro
        }

        public void removeCarro(Carro carro) {
            carrosNaPonte.remove(carro);
            repaint(); // Redesenha o painel para remover o carro
        }

        public void moveCarro(Carro carro, int xPosition) {
            carro.xPosition = xPosition; // Atualiza a posição do carro
            repaint(); // Redesenha o painel para atualizar a posição do carro
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Desenha a ponte
            g.setColor(Color.GRAY);
            g.fillRect(50, getHeight() / 2 - 20, getWidth() - 100, 40);
            // Desenha os carros na ponte
            for (Carro carro : carrosNaPonte) {
                g.setColor(carro.cor);
                int y = getHeight() / 2 - 15;
                int x;
                if (carro.sentido == Sentido.LESTE_PARA_OESTE) {
                    x = 60 - (getWidth() - 200) / 100 + carro.xPosition; // Posição horizontal do carro na ponte
                } else {
                    x = getWidth() - 60 - 20 + (getWidth() - 200) / 100 - carro.xPosition; // Posição horizontal do
                    // carro na ponte
                }
                g.fillRect(x, y, 20, 30);
            }

            // Desenha os carros aguardando em uma fila em cima da ponte
            int xFilaLeste = 50;
            for (Carro carro : filaLeste) {
                g.setColor(carro.cor);
                g.fillRect(xFilaLeste, getHeight() / 2 - 60, 20, 30);
                xFilaLeste += 30;
            }

            int xFilaOeste = getWidth() - 70;
            for (Carro carro : filaOeste) {
                g.setColor(carro.cor);
                g.fillRect(xFilaOeste, getHeight() / 2 + 30, 20, 30);
                xFilaOeste -= 30;
            }
        }
    }
}
