import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class testing extends JFrame implements ActionListener {

    private final JButton[][] boardButtons = new JButton[3][3];
    private JTextField nameField;

    JMenuBar menuBar;
    JMenu controlMenu, helpMenu;
    JMenuItem exitItem, helpItem;

    private void initPanel() {
        getContentPane().setLayout(new BorderLayout());

        JPanel messagePanel = new JPanel();
        messagePanel.add(new JLabel("Enter your player name..."));
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
        scorePanel.add(new JLabel("Player X: 0"));
        scorePanel.add(new JLabel("Player O: 0"));

        gamePanel.add(boardPanel, BorderLayout.CENTER);
        gamePanel.add(scorePanel, BorderLayout.EAST);
        add(gamePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        inputPanel.add(new JLabel("Player name:"));
        nameField = new JTextField(12);
        inputPanel.add(nameField);

        JLabel timeLabel = new JLabel("Time: --:--");
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
    }

    public testing() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initMenu();
        initPanel();
        setSize(500, 500);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (JButton[] row : boardButtons) {
            for (JButton cell : row) {
                if (e.getSource() == cell) {
                    if (cell.getText().isEmpty()) {
                        cell.setText("X");
                        cell.setEnabled(false);
                    }
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new testing());
    }
}