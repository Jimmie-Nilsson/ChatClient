import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * A minimalistic chat client that connects to a server.
 */
public class ChatClient {

    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ChatClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ChatClient(String host) {
        this(host, DEFAULT_PORT);
    }

    public ChatClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1), true);
            startChat();
        }catch (IOException e) {
            System.err.println("Error: Unable to connect to server.");
        }
    }
    private void startListening(){
            try {
                String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
            }catch (IOException e){
                System.err.println(e.getMessage());
            }
    }
    private void handleUserInput(){
    try{
        String message;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter message: ");
        while ((message = userInput.readLine()) != null) {
           // this is not needed but maybe nice to have.
            if (message.equalsIgnoreCase("exit")){
                break;
            }
            out.println(message);
        }
    }catch (IOException e){
        System.err.println("Error sending message.");
    }finally {
        close();
    }
    }
    private void close(){
        try {
            out.close();
            in.close();
            socket.close();
            System.out.println("Connection closed.");
        }catch (IOException e) {
            System.err.println("Error closing connection.");
        }
    }
    private void startChat(){
        Thread recieveThread = new Thread(this::startListening);
        recieveThread.start();
        handleUserInput();
    }
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

    new ChatClient(host, port);
    System.out.println("Chat client started.");
    System.out.println("Connected to " + host + " on port: " + port);
    }
}
