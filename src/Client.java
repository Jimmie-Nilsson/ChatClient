import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A minimalistic chat client that connects to a server to send and receive messages.
 * This client establishes a connection to a specified host and port.
 *
 * It listens for incoming messages from the server
 * on a separate thread while simultaneously handling user input.
 *
 * The client uses the following commands:
 * - "exit": Closes the connection and terminates the chat session.
 * Usage:
 * - Run with default host and port, or specify a host and port as command-line arguments.
 * - Default host is 127.0.0.1 and default port is 2000.
 */
public class Client {
    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;

    /**
     * Initializes a new Client instance, connecting to the specified host and port.
     * Sets up input and output streams, initialized GUI application and starts the chat.
     * @param host the host address the client should connect to.
     * @param port the port number the client should connect to.
     */
    public Client(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1), true);
            initializeGUI();
            startChat();
        } catch (IOException e) {
            // This uses System.err because if it cant connect to the server it won't start the application.
            System.err.println("Error: Unable to connect to server.");
        }
    }

    /**
     * Sets up the GUI for the chat client, including the message area and input field.
     * Adds event listeners for window closing and input field actions.
     */
    private void initializeGUI() {
        frame = new JFrame("Swing Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeConnection();
                System.exit(0); // Ensure the program terminates
            }
        });

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);


        inputField = new JTextField();
        inputField.setToolTipText("Type your message and press Enter to send.");
        inputField.setRequestFocusEnabled(true);
        inputField.addActionListener(e ->
                sendMessage()
        );

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputField, BorderLayout.SOUTH);
        frame.setVisible(true);
        inputField.requestFocusInWindow();
    }

    /**
     * Sends a message typed by the user in the input field to the server.
     * If the user types "exit", it closes the connection.
     * Displays an error message if the connection is closed.
     */
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            if (message.equalsIgnoreCase("exit")) {
                closeConnection();
                return;
            }
            if (out != null) { // Ensure the output stream is valid
                out.println(message);
            } else {
                showError("Cannot send message: Connection is closed.");
            }
            inputField.setText("");
        }
    }

    /**
     * Listens for incoming messages from the server in a separate thread.
     * Updates the message area of the GUI with received messages.
     * If the connection is lost, notifies the user in the message area.
     */
    private void startListening() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String finalMessage = message;
                SwingUtilities.invokeLater(() ->
                        messageArea.append(finalMessage + "\n")
                );
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                    messageArea.append("Connection lost.\n")
            );
        }
    }

    /**
     * Closes the client connection, including input and output streams and the socket.
     * Prints a message to the console indicating the connection is closed.
     */
    private void closeConnection() {
        try {
            out.close();
            in.close();
            socket.close();
            SwingUtilities.invokeLater(() -> {
                messageArea.append("Connection closed.\n");
                inputField.setEnabled(false);
            });
        } catch (IOException e) {
            showError("Error closing connection");
        }
    }

    /**
     * Starts the chat client by spawning a new thread to listen for incoming messages.
     */
    private void startChat() {
        Thread recieveThread = new Thread(this::startListening);
        recieveThread.start();
    }

    /**
     * Displays an error message in a popup dialog.
     * This method ensures the message is displayed on the Event Dispatch Thread.
     * @param message the error message to display.
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    /**
     * The main method for starting the client.
     * Connects to the default host and port if no arguments are provided.
     *
     * @param args optional command-line arguments for host and port.
     */
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        // Handle command-line arguments
        if (args.length == 1) {
            host = args[0];
        } else if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        new Client(host, port);
        System.out.println("Chat client started.");
        System.out.println("Connected to " + host + " on port: " + port);
    }
}
