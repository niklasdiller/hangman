import java.io.*;
import java.net.*;

public class Client implements Runnable {
    //2 threads, one for receiving messages from server, 1 for reveive console input

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            done = false;
            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);  //because inputHandler is only one, not many
            t.start(); //starts a new thread

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println(inMessage); //prints incoming messages
            }
        } catch (IOException e) {
            //TODO: hanlde
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    out.println(message); //sends in to the server
                }
            } catch (IOException e) {
                //TODO: handle
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
