import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

/**
 * Swing UI for a tic-tac-toe game played by a human against a computer.
 */
public class testing extends JFrame implements ActionListener {

    private static final String WELCOME_MESSAGE = "WELCOME";
    private static final String WAITING_MESSAGE = "Valid move, waiting for your opponent.";
    private static final String PLAYER_TURN_MESSAGE = "Your opponent has moved, now is your turn.";

    private final JButton[][] boardButtons = new JButton[3][3];
    private final char[][] boardState = new char[3][3];
    private final Random random = new Random();
    private JTextField nameField;
    private JLabel messageLabel;
    private JLabel playerScoreLabel;
    private JLabel computerScoreLabel;
    private JLabel drawScoreLabel;
    private JLabel timeLabel;
    private boolean playerTurn = true;
    private boolean gameActive = false;
    private boolean awaitingRestart = false;
    private Timer computerMoveTimer;
    private Timer roundTimer;
    private long roundStartMillis;
    private int playerWins;
    private int computerWins;
    private int draws;

    JMenuBar menuBar;
    JMenu controlMenu, helpMenu;
    JMenuItem exitItem, helpItem;

    private void initPanel() {
        getContentPane().setLayout(new BorderLayout());

        JPanel messagePanel = new JPanel();
        messageLabel = new JLabel("Enter your player name...");
        messagePanel.add(messageLabel);
        add(messagePanel, BorderLayout.NORTH);

        JPanel gamePanel = new JPanel(new BorderLayout());
        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));

        Font cellFont = new Font(Font.SANS_SERIF, Font.BOLD, 36);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                JButton cellButton = new JButton();
                cellButton.setFont(cellFont);
                cellButton.addActionListener(this);
                boardButtons[row][col] = cellButton;
                boardPanel.add(cellButton);
            }
        }

        scorePanel.add(new JLabel("Scoreboard"));
        scorePanel.add(Box.createVerticalStrut(8));
        playerScoreLabel = new JLabel("Player X: 0");
        computerScoreLabel = new JLabel("Player O: 0");
        drawScoreLabel = new JLabel("Draws: 0");
        scorePanel.add(playerScoreLabel);
        scorePanel.add(computerScoreLabel);
        scorePanel.add(drawScoreLabel);

        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(scorePanel, BorderLayout.EAST);
        add(gamePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        inputPanel.add(new JLabel("Player name:"));
        nameField = new JTextField(12);
        inputPanel.add(nameField);
        nameField.addActionListener(e -> startGameIfReady());

        timeLabel = new JLabel("Time: --:--");
        timePanel.add(timeLabel);

        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(timePanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        controlMenu = new JMenu("Control");
        helpMenu = new JMenu("Help");

        exitItem = new JMenuItem("Exit");
        helpItem = new JMenuItem("Instructions");

        controlMenu.add(exitItem);
        helpMenu.add(helpItem);
        menuBar.add(controlMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        exitItem.addActionListener(evt -> handleExit());
        helpItem.addActionListener(evt -> showInstructions());
    }

    /**
     * Constructs the main game window and initialises the UI components.
     */
    public testing() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMenu();
        initPanel();
        setSize(500, 500);
        setVisible(true);
    }

    /**
     * Handles menu selections and board clicks.
     *
     * @param e the event raised by the UI component
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == exitItem) {
            handleExit();
            return;
        }

        if (source == helpItem) {
            showInstructions();
            return;
        }

        if (!(source instanceof JButton)) {
            return;
        }

        JButton cell = (JButton) source;
        Point location = locateCell(cell);
        if (location == null) {
            return;
        }

        if (!gameActive) {
            startGameIfReady();
        }

        if (!gameActive || awaitingRestart || !playerTurn) {
            return;
        }

        if (!cell.getText().isEmpty()) {
            return;
        }

        handlePlayerMove(cell, location);
    }

    /**
     * Launches the application.
     *
     * @param args ignored command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new testing());
    }

    private void startGameIfReady() {
        if (gameActive) {
            return;
        }

        if (nameField.getText().trim().isEmpty()) {
            return;
        }

        if (computerMoveTimer != null && computerMoveTimer.isRunning()) {
            computerMoveTimer.stop();
        }

        resetBoard();
        gameActive = true;
        playerTurn = true;
        awaitingRestart = false;
        String playerName = nameField.getText().trim();
        messageLabel.setText(WELCOME_MESSAGE + " " + playerName);
        setBoardEnabled(true);
        startRoundTimer();
    }

    private void handlePlayerMove(JButton cell, Point location) {
        boardState[location.x][location.y] = 'X';
        cell.setText("X");
        cell.setEnabled(false);

        if (checkWinner('X')) {
            finishRound("Congratulations, you win!", RoundOutcome.PLAYER);
            return;
        }

        if (isBoardFull()) {
            finishRound("Round ends in a draw.", RoundOutcome.DRAW);
            return;
        }

        playerTurn = false;
        messageLabel.setText(WAITING_MESSAGE);
        scheduleComputerMove();
    }

    private void scheduleComputerMove() {
        if (computerMoveTimer != null && computerMoveTimer.isRunning()) {
            computerMoveTimer.stop();
        }

        computerMoveTimer = new Timer(2000, evt -> {
            computerMoveTimer.stop();
            executeComputerMove();
        });
        computerMoveTimer.setRepeats(false);
        computerMoveTimer.start();
    }

    private void executeComputerMove() {
        List<Point> emptyCells = collectEmptyCells();
        if (emptyCells.isEmpty()) {
            finishRound("Round ends in a draw.", RoundOutcome.DRAW);
            return;
        }

        Point choice = emptyCells.get(random.nextInt(emptyCells.size()));
        JButton cell = boardButtons[choice.x][choice.y];
        boardState[choice.x][choice.y] = 'O';
        cell.setText("O");
        cell.setEnabled(false);

        if (checkWinner('O')) {
            finishRound("Your opponent wins this round.", RoundOutcome.COMPUTER);
            return;
        }

        if (isBoardFull()) {
            finishRound("Round ends in a draw.", RoundOutcome.DRAW);
            return;
        }

        playerTurn = true;
        messageLabel.setText(PLAYER_TURN_MESSAGE);
    }

    private void finishRound(String message, RoundOutcome outcome) {
        gameActive = false;
        playerTurn = false;
        awaitingRestart = true;
        messageLabel.setText(message);
        if (computerMoveTimer != null && computerMoveTimer.isRunning()) {
            computerMoveTimer.stop();
        }
        stopRoundTimer();
        setBoardEnabled(false);
        applyOutcome(outcome);

        Object[] options = {"Yes"};
        int choice = JOptionPane.showOptionDialog(this, message + "\nPlay another round?", "Round Complete",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            awaitingRestart = false;
            startGameIfReady();
        }
    }

    private boolean checkWinner(char mark) {
        for (int row = 0; row < 3; row++) {
            if (boardState[row][0] == mark && boardState[row][1] == mark && boardState[row][2] == mark) {
                return true;
            }
        }

        for (int col = 0; col < 3; col++) {
            if (boardState[0][col] == mark && boardState[1][col] == mark && boardState[2][col] == mark) {
                return true;
            }
        }

        return (boardState[0][0] == mark && boardState[1][1] == mark && boardState[2][2] == mark)
            || (boardState[0][2] == mark && boardState[1][1] == mark && boardState[2][0] == mark);
    }

    private boolean isBoardFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (boardState[row][col] == '\0') {
                    return false;
                }
            }
        }
        return true;
    }

    private List<Point> collectEmptyCells() {
        List<Point> empty = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (boardState[row][col] == '\0') {
                    empty.add(new Point(row, col));
                }
            }
        }
        return empty;
    }

    private Point locateCell(JButton cell) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (boardButtons[row][col] == cell) {
                    return new Point(row, col);
                }
            }
        }
        return null;
    }

    private void resetBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                boardState[row][col] = '\0';
                boardButtons[row][col].setText("");
                boardButtons[row][col].setEnabled(true);
            }
        }
    }

    private void setBoardEnabled(boolean enabled) {
        for (JButton[] row : boardButtons) {
            for (JButton cell : row) {
                if (enabled) {
                    cell.setEnabled(cell.getText().isEmpty());
                } else {
                    cell.setEnabled(false);
                }
            }
        }
    }

    private void applyOutcome(RoundOutcome outcome) {
        switch (outcome) {
            case PLAYER -> playerWins++;
            case COMPUTER -> computerWins++;
            case DRAW -> draws++;
        }
        updateScoreLabels();
    }

    private void updateScoreLabels() {
        playerScoreLabel.setText("Player X: " + playerWins);
        computerScoreLabel.setText("Player O: " + computerWins);
        drawScoreLabel.setText("Draws: " + draws);
    }

    private void handleExit() {
        stopRoundTimer();
        if (computerMoveTimer != null && computerMoveTimer.isRunning()) {
            computerMoveTimer.stop();
        }
        dispose();
        System.exit(0);
    }

    private void showInstructions() {
        StringBuilder instructions = new StringBuilder();
        instructions.append("Tic-Tac-Toe Rules:\n\n");
        instructions.append("1. Enter your name to begin.\n");
        instructions.append("2. You always play first as 'X'.\n");
        instructions.append("3. Click any empty cell on your turn.\n");
        instructions.append("4. The computer moves as 'O' after a 2-second delay.\n");
        instructions.append("5. Complete a row, column, or diagonal to win.\n");
        instructions.append("6. Filling the board without a winner results in a draw.\n");
        instructions.append("7. Use Control > Exit to close the game.");

        JOptionPane.showMessageDialog(this, instructions.toString(), "Instructions",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void startRoundTimer() {
        roundStartMillis = System.currentTimeMillis();
        if (roundTimer == null) {
            roundTimer = new Timer(1000, evt -> updateTimeLabel());
            roundTimer.setInitialDelay(1000);
        } else if (roundTimer.isRunning()) {
            roundTimer.stop();
        }
        updateTimeLabel();
        roundTimer.start();
    }

    private void stopRoundTimer() {
        if (roundTimer != null && roundTimer.isRunning()) {
            roundTimer.stop();
        }
    }

    private void updateTimeLabel() {
        long elapsedSeconds = Math.max(0, (System.currentTimeMillis() - roundStartMillis) / 1000);
        long minutes = elapsedSeconds / 60;
        long seconds = elapsedSeconds % 60;
        timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    private enum RoundOutcome {
        PLAYER,
        COMPUTER,
        DRAW
    }
}