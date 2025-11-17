import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Swing client for the online Tic-Tac-Toe game.
 */
public class tttOnline extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final String TITLE_PREFIX = "Tic Tac Toe";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    private final JButton[][] boardButtons = new JButton[3][3];
    private JTextField nameField;
    private JButton submitNameButton;
    private JLabel messageLabel;
    private JLabel playerXScoreLabel;
    private JLabel playerOScoreLabel;
    private JLabel drawScoreLabel;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread listenerThread;

    private volatile int playerIndex = -1;
    private volatile boolean myTurn;
    private volatile boolean awaitingRestart;
    private volatile boolean waitingForServerAck;
    private volatile boolean connected;
    private volatile boolean nameSubmitted;
    private volatile boolean exitSent;

    private String playerXName = "";
    private String playerOName = "";
    private int xWins;
    private int oWins;
    private int drawCount;

    private JMenuItem exitItem;
    private JMenuItem helpItem;

    private void initPanel() {
        getContentPane().setLayout(new BorderLayout());

        JPanel messagePanel = new JPanel();
        messageLabel = new JLabel("Connecting to server...");
        messagePanel.add(messageLabel);
        add(messagePanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 2, 2));
        Font cellFont = new Font(Font.SANS_SERIF, Font.BOLD, 36);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton button = new JButton();
                button.setFont(cellFont);
                button.setActionCommand("CELL_" + row + "_" + col);
                button.addActionListener(this);
                button.setEnabled(false);
                boardButtons[row][col] = button;
                boardPanel.add(button);
            }
        }

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.add(new JLabel("Scoreboard"));
        scorePanel.add(Box.createVerticalStrut(8));
        playerXScoreLabel = new JLabel("Player X: 0");
        playerOScoreLabel = new JLabel("Player O: 0");
        drawScoreLabel = new JLabel("Draws: 0");
        scorePanel.add(playerXScoreLabel);
        scorePanel.add(Box.createVerticalStrut(4));
        scorePanel.add(playerOScoreLabel);
        scorePanel.add(Box.createVerticalStrut(4));
        scorePanel.add(drawScoreLabel);

        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(scorePanel, BorderLayout.EAST);
        add(gamePanel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Player name:"));
        nameField = new JTextField(12);
        nameField.setEnabled(false);
        submitNameButton = new JButton("Submit");
        submitNameButton.setEnabled(false);
        submitNameButton.addActionListener(evt -> submitName());
        nameField.addActionListener(evt -> submitName());
        inputPanel.add(nameField);
        inputPanel.add(submitNameButton);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu controlMenu = new JMenu("Control");
        JMenu helpMenu = new JMenu("Help");

        exitItem = new JMenuItem("Exit");
        helpItem = new JMenuItem("Instruction");

        exitItem.addActionListener(evt -> handleExit());
        helpItem.addActionListener(evt -> showInstructions());

        controlMenu.add(exitItem);
        helpMenu.add(helpItem);
        menuBar.add(controlMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Creates the client UI and connects to the Tic-Tac-Toe server.
     *
     * @param host server host name
     * @param port server port
     */
    public tttOnline(String host, int port) {
        super(TITLE_PREFIX);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        initMenu();
        initPanel();
        setSize(520, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        setVisible(true);
        connectToServer(host, port);
    }

    /**
     * Handles board button clicks.
     *
     * @param event the action event raised by a button press
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command == null || !command.startsWith("CELL_")) {
            return;
        }

        if (!connected || awaitingRestart || waitingForServerAck || !myTurn) {
            return;
        }

        String[] parts = command.split("_");
        if (parts.length != 3) {
            return;
        }

        int row = Integer.parseInt(parts[1]);
        int col = Integer.parseInt(parts[2]);
        waitingForServerAck = true;
        sendMessage("MOVE|" + row + "|" + col);
    }

    /**
     * Launches the Tic-Tac-Toe client.
     *
     * @param args optional command line arguments: host [port]
     */
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                System.err.println("Port must be an integer. Using default: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        String finalHost = host;
        int finalPort = port;
        SwingUtilities.invokeLater(() -> new tttOnline(finalHost, finalPort));
    }

    private void submitName() {
        if (!connected || nameSubmitted) {
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            messageLabel.setText("Name cannot be empty.");
            return;
        }
        name = name.replace('|', ' ');
        submitNameButton.setEnabled(false);
        nameField.setEnabled(false);
        sendMessage("NAME|" + name);
    }

    private void connectToServer(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                connected = true;
                SwingUtilities.invokeLater(() -> {
                    messageLabel.setText("Enter your player name...");
                    nameField.setEnabled(true);
                    submitNameButton.setEnabled(true);
                });
                listenerThread = new Thread(new ServerListener(), "ttt-listener");
                listenerThread.setDaemon(true);
                listenerThread.start();
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(tttOnline.this,
                            "Unable to connect to server: " + ex.getMessage(),
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                    messageLabel.setText("Unable to connect. Please close the window.");
                });
            }
        }, "ttt-connector").start();
    }

    private void onAssigned(int index) {
        playerIndex = index;
        updateScoreLabels();
    }

    private void onNameConfirmed(int index, String name) {
        if (index == 0) {
            playerXName = name;
        } else {
            playerOName = name;
        }

        if (index == playerIndex) {
            nameSubmitted = true;
            nameField.setText(name);
            nameField.setEnabled(false);
            submitNameButton.setEnabled(false);
            messageLabel.setText("WELCOME " + name);
            setTitle(TITLE_PREFIX + "-Player: " + name);
        }
        updateScoreLabels();
    }

    private void onRoundStart(int currentPlayer) {
        awaitingRestart = false;
        waitingForServerAck = false;
        resetBoard();
        myTurn = playerIndex == currentPlayer;
        if (myTurn) {
            messageLabel.setText("Your turn. Make your move.");
            setBoardEnabled(true);
        } else {
            messageLabel.setText("Waiting for your opponent to make the first move.");
            setBoardEnabled(false);
        }
    }

    private void onMarkPlaced(int row, int col, char mark) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            return;
        }
        JButton button = boardButtons[row][col];
        button.setText(String.valueOf(mark));
        button.setEnabled(false);
    }

    private void onTurnChanged(int currentPlayer) {
        myTurn = playerIndex == currentPlayer;
        waitingForServerAck = false;
        if (myTurn) {
            messageLabel.setText("Your opponent has moved, now is your turn.");
            setBoardEnabled(true);
        } else {
            messageLabel.setText("Valid move, wait for your opponent.");
            setBoardEnabled(false);
        }
    }

    private void onInvalidAction(String reason) {
        waitingForServerAck = false;
        messageLabel.setText(reason);
        if (!nameSubmitted) {
            nameField.setEnabled(true);
            submitNameButton.setEnabled(true);
        } else if (myTurn && !awaitingRestart) {
            setBoardEnabled(true);
        }
    }

    private void onRoundEndWin(int winnerIndex, int newXWins, int newOWins, int newDraws) {
        awaitingRestart = true;
        myTurn = false;
        waitingForServerAck = false;
        setBoardEnabled(false);
        xWins = newXWins;
        oWins = newOWins;
        drawCount = newDraws;
        boolean winner = winnerIndex == playerIndex;
        messageLabel.setText(winner ? "You win!" : "You lose.");
        String dialogText = winner ? "You win! Start a new round?" : "You lose. Start a new round?";
        int option = JOptionPane.showConfirmDialog(this, dialogText, "Round Complete",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            sendMessage("RESTART|YES");
            updateScoreLabels();
        } else {
            sendMessage("RESTART|NO");
            handleExit();
        }
    }

    private void onRoundEndDraw(int newXWins, int newOWins, int newDraws) {
        awaitingRestart = true;
        myTurn = false;
        waitingForServerAck = false;
        setBoardEnabled(false);
        xWins = newXWins;
        oWins = newOWins;
        drawCount = newDraws;
        messageLabel.setText("Round ends in a draw.");
        int option = JOptionPane.showConfirmDialog(this, "Round ends in a draw. Start a new round?",
                "Round Complete", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            sendMessage("RESTART|YES");
            updateScoreLabels();
        } else {
            sendMessage("RESTART|NO");
            handleExit();
        }
    }

    private void onOpponentLeft() {
        awaitingRestart = false;
        myTurn = false;
        waitingForServerAck = false;
        setBoardEnabled(false);
        JOptionPane.showMessageDialog(this, "Game Ends. One of the players left.", "Game Ends",
                JOptionPane.INFORMATION_MESSAGE);
        handleExit();
    }

    private void resetBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton button = boardButtons[row][col];
                button.setText("");
                button.setEnabled(false);
            }
        }
    }

    private void setBoardEnabled(boolean enabled) {
        for (JButton[] row : boardButtons) {
            for (JButton button : row) {
                if (enabled) {
                    button.setEnabled(button.getText().isEmpty());
                } else {
                    button.setEnabled(false);
                }
            }
        }
    }

    private void updateScoreLabels() {
        String xLabelName = playerXName == null || playerXName.isBlank()
                ? "Player X"
                : "Player X (" + playerXName + ")";
        String oLabelName = playerOName == null || playerOName.isBlank()
                ? "Player O"
                : "Player O (" + playerOName + ")";
        playerXScoreLabel.setText(xLabelName + ": " + xWins);
        playerOScoreLabel.setText(oLabelName + ": " + oWins);
        drawScoreLabel.setText("Draws: " + drawCount);
    }

    private void handleExit() {
        if (connected) {
            sendExitMessage();
        }
        disconnect();
        dispose();
        System.exit(0);
    }

    private void showInstructions() {
        StringBuilder instructions = new StringBuilder();
        instructions.append("Tic-Tac-Toe Online Instructions:\n\n");
        instructions.append("1. Enter your player name before making a move.\n");
        instructions.append("2. Player X always starts first in each round.\n");
        instructions.append("3. You may only play on your turn and on empty cells.\n");
        instructions.append("4. A row, column, or diagonal of your mark wins the round.\n");
        instructions.append("5. Select \"Yes\" after a round to start the next round, or \"No\" to exit.\n");
        instructions.append("6. Use Control > Exit or close the window to leave the match.");
        JOptionPane.showMessageDialog(this, instructions.toString(), "Instruction",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void disconnect() {
        connected = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
                // no-op
            }
        }
        if (writer != null) {
            writer.close();
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // no-op
            }
        }
    }

    private void sendExitMessage() {
        if (!exitSent) {
            sendMessage("EXIT");
            exitSent = true;
        }
    }

    private void sendMessage(String message) {
        if (writer == null) {
            return;
        }
        writer.println(message);
    }

    private void handleServerMessage(String line) {
        if (line == null || line.isBlank()) {
            return;
        }
        String[] parts = line.split("\\|", -1);
        String type = parts[0];
        switch (type) {
            case "ASSIGN" -> {
                if (parts.length >= 2) {
                    int index = Integer.parseInt(parts[1]);
                    SwingUtilities.invokeLater(() -> onAssigned(index));
                }
            }
            case "NAME_CONFIRMED" -> {
                if (parts.length >= 3) {
                    int index = Integer.parseInt(parts[1]);
                    String name = parts[2];
                    SwingUtilities.invokeLater(() -> onNameConfirmed(index, name));
                }
            }
            case "ROUND_START" -> {
                if (parts.length >= 2) {
                    int current = Integer.parseInt(parts[1]);
                    SwingUtilities.invokeLater(() -> onRoundStart(current));
                }
            }
            case "MARK" -> {
                if (parts.length >= 4) {
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    char mark = parts[3].charAt(0);
                    SwingUtilities.invokeLater(() -> onMarkPlaced(row, col, mark));
                }
            }
            case "TURN" -> {
                if (parts.length >= 2) {
                    int current = Integer.parseInt(parts[1]);
                    SwingUtilities.invokeLater(() -> onTurnChanged(current));
                }
            }
            case "INVALID" -> {
                String reason = parts.length >= 2 ? parts[1] : "Invalid action.";
                SwingUtilities.invokeLater(() -> onInvalidAction(reason));
            }
            case "ROUND_END" -> handleRoundEnd(parts);
            case "OPPONENT_LEFT" -> SwingUtilities.invokeLater(this::onOpponentLeft);
            default -> {
                // ignore unsupported messages
            }
        }
    }

    private void handleRoundEnd(String[] parts) {
        if (parts.length < 2) {
            return;
        }
        String outcome = parts[1];
        if ("WIN".equals(outcome) && parts.length >= 6) {
            int winner = Integer.parseInt(parts[2]);
            int newXWins = Integer.parseInt(parts[3]);
            int newOWins = Integer.parseInt(parts[4]);
            int newDraws = Integer.parseInt(parts[5]);
            SwingUtilities.invokeLater(() -> onRoundEndWin(winner, newXWins, newOWins, newDraws));
        } else if ("DRAW".equals(outcome) && parts.length >= 5) {
            int newXWins = Integer.parseInt(parts[2]);
            int newOWins = Integer.parseInt(parts[3]);
            int newDraws = Integer.parseInt(parts[4]);
            SwingUtilities.invokeLater(() -> onRoundEndDraw(newXWins, newOWins, newDraws));
        }
    }

    private void handleServerDisconnect() {
        if (connected) {
            connected = false;
            SwingUtilities.invokeLater(() -> {
                setBoardEnabled(false);
                JOptionPane.showMessageDialog(this, "Disconnected from server.", "Connection Closed",
                        JOptionPane.WARNING_MESSAGE);
                dispose();
                System.exit(0);
            });
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    handleServerMessage(line);
                }
            } catch (IOException ex) {
                // connection lost
            } finally {
                handleServerDisconnect();
            }
        }
    }
}