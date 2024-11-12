import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A minimalistic chat client that connects to a server to send and receive messages.
 *
 *
 * This client establishes a connection to a specified host and port, allowing for real-time
 * text communication with a server. It listens for incoming messages from the server
 * on a separate thread while simultaneously handling user input.
 *
 *
 * The client uses the following commands:
 * - "exit": Closes the connection and terminates the chat session.
 * Usage:
 * - Run with default host and port, or specify a host and port as command-line arguments.
 */
public class Client {
    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Initializes a new Client instance, connecting to the specified host and port.
     * Sets up input and output streams and starts the chat.
     *
     * @param host the host address the client should connect to.
     * @param port the port number the client should connect to.
     */
    public Client(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1), true);
            startChat();
        } catch (IOException e) {
            System.err.println("Error: Unable to connect to server.");
        }
    }

    /**
     * Listens for incoming messages from the server and prints them to the console.
     * This method runs in a separate thread to allow simultaneous input handling.
     */
    private void startListening() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles user input from the console and sends messages to the server.
     * If the user types "exit", the connection will be closed.
     */
    private void handleUserInput() {
        try {
            String message;
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter message: ");
            while ((message = userInput.readLine()) != null) {
                // this is not needed but maybe nice to have.
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(message);
            }
        } catch (IOException e) {
            System.err.println("Error sending message.");
        } finally {
            close();
        }
    }

    /**
     * Closes the client connection, including input and output streams and the socket.
     * Prints a message to the console indicating the connection is closed.
     */
    private void close() {
        try {
            out.close();
            in.close();
            socket.close();
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection.");
        }
    }

    /**
     * Starts the chat by creating a new thread to listen for server messages,
     * while simultaneously handling user input in the main thread.
     */
    private void startChat() {
        Thread recieveThread = new Thread(this::startListening);
        recieveThread.start();
        handleUserInput();
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
