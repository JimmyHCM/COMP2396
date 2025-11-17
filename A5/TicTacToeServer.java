import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * TCP server that coordinates a two-player online Tic-Tac-Toe match.
 */
public class TicTacToeServer {

    private static final int DEFAULT_PORT = 5000;

    private final int port;
    private final ClientHandler[] clients = new ClientHandler[2];
    private final char[][] board = new char[3][3];
    private final String[] names = new String[2];
    private final int[] wins = new int[2];
    private final Boolean[] restartVotes = new Boolean[2];
    private int draws;
    private int currentPlayerIndex;
    private boolean roundActive;
    private boolean awaitingRestart;

    /**
     * Creates a server that listens for Tic-Tac-Toe clients on the given port.
     *
     * @param port listening port for incoming client connections
     */
    public TicTacToeServer(int port) {
        this.port = port;
    }

    /**
     * Entry point for the Tic-Tac-Toe server.
     *
     * @param args optional single argument specifying the listening port
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.err.println("Port must be an integer. Using default: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        TicTacToeServer server = new TicTacToeServer(port);
        server.start();
    }

    /**
     * Starts accepting incoming connections and managing matches.
     */
    public void start() {
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("TicTacToeServer listening on port " + port);
            while (true) {
                Socket clientSocket = socket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }
        } catch (IOException ex) {
            System.err.println("Server stopped: " + ex.getMessage());
        }
    }

    private synchronized boolean registerClient(ClientHandler handler) {
        if (clients[0] == null && clients[1] == null) {
            resetMatchState();
        }

        int slot = clients[0] == null ? 0 : (clients[1] == null ? 1 : -1);
        if (slot == -1) {
            handler.sendMessage("STATUS|Server is currently full.");
            return false;
        }

        handler.playerIndex = slot;
        clients[slot] = handler;
        handler.sendMessage("ASSIGN|" + slot);

        int other = 1 - slot;
        if (clients[other] != null && names[other] != null) {
            handler.sendMessage("NAME_CONFIRMED|" + other + "|" + names[other]);
        }
        if (names[slot] != null) {
            handler.sendMessage("NAME_CONFIRMED|" + slot + "|" + names[slot]);
        }

        System.out.println("Player " + slot + " connected from " + handler.getRemoteAddress());
        return true;
    }

    private synchronized void handleNameSubmission(ClientHandler handler, String rawName) {
        int idx = handler.playerIndex;
        if (idx < 0) {
            handler.sendMessage("INVALID|Server has not assigned you a slot yet.");
            return;
        }
        if (names[idx] != null) {
            handler.sendMessage("INVALID|Name already submitted.");
            return;
        }
        String name = rawName == null ? "" : rawName.trim().replace('|', ' ');
        if (name.isEmpty()) {
            handler.sendMessage("INVALID|Name cannot be empty.");
            return;
        }
        names[idx] = name;
        handler.sendMessage("NAME_CONFIRMED|" + idx + "|" + name);
        int other = 1 - idx;
        if (clients[other] != null) {
            clients[other].sendMessage("NAME_CONFIRMED|" + idx + "|" + name);
            if (names[other] != null) {
                handler.sendMessage("NAME_CONFIRMED|" + other + "|" + names[other]);
            }
        }
        maybeStartRound();
    }

    private synchronized void maybeStartRound() {
        if (clients[0] != null && clients[1] != null
                && names[0] != null && names[1] != null
                && !roundActive && !awaitingRestart) {
            startNewRound();
        }
    }

    private synchronized void startNewRound() {
        resetBoard();
        currentPlayerIndex = 0;
        roundActive = true;
        awaitingRestart = false;
        restartVotes[0] = null;
        restartVotes[1] = null;
        broadcast("ROUND_START|" + currentPlayerIndex);
        System.out.println("Round started: " + names[0] + " vs " + names[1]);
    }

    private synchronized void handleMove(ClientHandler handler, int row, int col) {
        if (!roundActive) {
            handler.sendMessage("INVALID|Round not active.");
            return;
        }
        if (handler.playerIndex != currentPlayerIndex) {
            handler.sendMessage("INVALID|Not your turn.");
            return;
        }
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            handler.sendMessage("INVALID|Move outside the board.");
            return;
        }
        if (board[row][col] != '\0') {
            handler.sendMessage("INVALID|Cell already occupied.");
            return;
        }

        char mark = markFor(handler.playerIndex);
        board[row][col] = mark;
        broadcast("MARK|" + row + "|" + col + "|" + mark);

        if (hasWinner(mark)) {
            roundActive = false;
            awaitingRestart = true;
            wins[handler.playerIndex]++;
            broadcast("ROUND_END|WIN|" + handler.playerIndex + "|" + wins[0] + "|" + wins[1] + "|" + draws);
            restartVotes[0] = null;
            restartVotes[1] = null;
            System.out.println("Player " + handler.playerIndex + " won the round.");
            return;
        }

        if (isBoardFull()) {
            roundActive = false;
            awaitingRestart = true;
            draws++;
            broadcast("ROUND_END|DRAW|" + wins[0] + "|" + wins[1] + "|" + draws);
            restartVotes[0] = null;
            restartVotes[1] = null;
            System.out.println("Round ended in draw.");
            return;
        }

        currentPlayerIndex = 1 - currentPlayerIndex;
        broadcast("TURN|" + currentPlayerIndex);
    }

    private synchronized void handleRestartVote(ClientHandler handler, boolean restart) {
        if (!awaitingRestart || handler.playerIndex < 0) {
            return;
        }
        restartVotes[handler.playerIndex] = restart;
        if (!restart) {
            handler.closeSilently();
            return;
        }

        if (Boolean.TRUE.equals(restartVotes[0]) && Boolean.TRUE.equals(restartVotes[1])) {
            awaitingRestart = false;
            startNewRound();
        }
    }

    private synchronized void handlePlayerExit(ClientHandler handler) {
        handler.closeSilently();
    }

    private synchronized void onClientDisconnected(ClientHandler handler) {
        int idx = handler.playerIndex;
        if (idx < 0) {
            return;
        }
        if (clients[idx] == handler) {
            clients[idx] = null;
        }
        names[idx] = null;
        restartVotes[idx] = null;
        roundActive = false;
        awaitingRestart = false;
        int other = 1 - idx;
        ClientHandler opponent = clients[other];
        if (opponent != null) {
            opponent.sendMessage("OPPONENT_LEFT");
        }
        if (clients[0] == null && clients[1] == null) {
            resetMatchState();
        }
        System.out.println("Player " + idx + " disconnected.");
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            if (client != null) {
                client.sendMessage(message);
            }
        }
    }

    private void resetMatchState() {
        Arrays.fill(wins, 0);
        Arrays.fill(names, null);
        Arrays.fill(restartVotes, null);
        draws = 0;
        roundActive = false;
        awaitingRestart = false;
        resetBoard();
        System.out.println("Server reset for a new match.");
    }

    private void resetBoard() {
        for (int row = 0; row < 3; row++) {
            Arrays.fill(board[row], '\0');
        }
    }

    private boolean hasWinner(char mark) {
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == mark && board[row][1] == mark && board[row][2] == mark) {
                return true;
            }
        }
        for (int col = 0; col < 3; col++) {
            if (board[0][col] == mark && board[1][col] == mark && board[2][col] == mark) {
                return true;
            }
        }
        return (board[0][0] == mark && board[1][1] == mark && board[2][2] == mark)
                || (board[0][2] == mark && board[1][1] == mark && board[2][0] == mark);
    }

    private boolean isBoardFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col] == '\0') {
                    return false;
                }
            }
        }
        return true;
    }

    private char markFor(int playerIndex) {
        return playerIndex == 0 ? 'X' : 'O';
    }

    private final class ClientHandler extends Thread {

        private final Socket socket;
        private final BufferedReader input;
        private final PrintWriter output;
        private int playerIndex = -1;
        private volatile boolean closing;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }

        String getRemoteAddress() {
            return socket.getRemoteSocketAddress().toString();
        }

        @Override
        public void run() {
            boolean accepted = registerClient(this);
            if (!accepted) {
                closeSilently();
                return;
            }
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    handleMessage(line);
                }
            } catch (IOException ex) {
                // client disconnected
            } finally {
                closeSilently();
                onClientDisconnected(this);
            }
        }

        private void handleMessage(String line) {
            String[] parts = line.split("\\|", -1);
            if (parts.length == 0) {
                return;
            }
            String type = parts[0];
            switch (type) {
                case "NAME" -> {
                    if (parts.length >= 2) {
                        handleNameSubmission(this, parts[1]);
                    }
                }
                case "MOVE" -> {
                    if (parts.length >= 3) {
                        try {
                            int row = Integer.parseInt(parts[1]);
                            int col = Integer.parseInt(parts[2]);
                            handleMove(this, row, col);
                        } catch (NumberFormatException ex) {
                            sendMessage("INVALID|Invalid move coordinates.");
                        }
                    }
                }
                case "RESTART" -> {
                    if (parts.length >= 2) {
                        boolean restart = "YES".equalsIgnoreCase(parts[1]);
                        handleRestartVote(this, restart);
                    }
                }
                case "EXIT" -> handlePlayerExit(this);
                default -> {
                    // ignore unsupported message types
                }
            }
        }

        void sendMessage(String message) {
            synchronized (output) {
                if (!closing) {
                    output.println(message);
                }
            }
        }

        void closeSilently() {
            if (closing) {
                return;
            }
            closing = true;
            try {
                socket.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }
}
