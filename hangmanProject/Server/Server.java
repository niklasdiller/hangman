import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.lang.Character.toUpperCase;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    private Game game;

    private BinarySemaphore cs1;
    private BinarySemaphore cs2;

    public Server() {
        connections = new ArrayList<>();
        done = false;       // default not done
        cs1 = new BinarySemaphore(true);
        cs2 = new BinarySemaphore(true);
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);    // port number
            pool = Executors.newCachedThreadPool();
            while (!done) {                             // checking if no shut down
                Socket client = server.accept();        // waits for client contact
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);   // adds to connection Array list
                pool.execute(handler); // adds handler to threadpool
                System.out.println("Connection established!");
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {    // message all clients
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            pool.shutdown();
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;

        String message;
        public ConnectionHandler(Socket client) { // Constructor
            this.client = client;   //
        }

        @Override
        public void run() {
            try {

                out = new PrintWriter(client.getOutputStream(), true);  //output

                in = new BufferedReader(new InputStreamReader(client.getInputStream()));    //input from client


                if(game == null)

                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/start") && game == null) {
                        broadcast("/start");
                        System.out.println("Game starts ..");
                        cs1.P();
                        if(game == null) game = new Game(); // critical section 1
                        cs1.V();

                        String stateMessage = game.getWord() + "," + game.getBlank() + "," + game.getFalseLetters();
                        broadcast(stateMessage);
                        System.out.println(game.getBlank());

                    } else if (message.startsWith("/quit")) {

                        shutdown();
                    } else if (game != null) {
                        if (!(game.getBlank().equals(game.getWord()) && (game.getErrors() >= 0))) { // criteria for ending loop
                            if (message.length() == 1) {
                                char guess = message.charAt(0);
                                cs2.P();
                                game.guess(guess);  // critical section 2
                                cs2.V();
                            }

                            if (game.getBlank().equals(game.getWord())) {
                                String stateMessage = game.getWord() + "," + game.getBlank() + "," + game.getFalseLetters();
                                broadcast(stateMessage);
                                System.out.println(game.getBlank());

                                game = null;    // race condition, but same outcome

                            } else if ((game.getErrors() < 0)) {
                                String stateMessage = game.getWord() + "," + game.getBlank() + "," + game.getFalseLetters();
                                broadcast(stateMessage);
                                System.out.println(game.getBlank());

                                game = null;
                            }
                            else {

                                System.out.println("Remaining Errors: " + game.getErrors());
                                System.out.println("False Letters: " + game.getFalseLetters());
                                System.out.println("Word: " + game.getBlank());
                                String stateMessage = game.getWord() + "," + game.getBlank() + "," + game.getFalseLetters();
                                broadcast(stateMessage);
                            }
                        }
                    } /*else {
                        broadcast(nickname + ": " + message); // TODO: msg != empty
                    }*/
                }
            } catch (IOException e) {
                shutdown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    class Game {
        private char[] word;
        private char[] blank;
        private int errors;
        private ArrayList<Character> falseLetters;

        public Game() {
            try {
                this.word = getRandomWord();//new char[]{'H', 'A', 'N', 'G', 'M', 'A', 'N'};
                //this.word = new char[]{'T','E','S','T'};
            } catch (IOException e) {
                shutdown();
            }
            System.out.println(word[0] + word.length);
            this.blank = new char[this.word.length];    //blank fitting to word
            System.out.println("TEst 2");
            for (int i = 0; i < this.blank.length; i++) {
                this.blank[i] = '_';
            }
            System.out.println("TEst 3");
            this.errors = 7;    // 7 lines of hangman
            System.out.println("TEst 4");
            this.falseLetters = new ArrayList<>();

        }

        private char[] getRandomWord() throws FileNotFoundException {

            ArrayList<String> allWords = new ArrayList<>();
            Scanner scanner = new Scanner(new File("wordfile.txt"));
            scanner.useDelimiter(", ");
            while (scanner.hasNext()) {
                allWords.add(scanner.next());
            }
            scanner.close();
            int randomIndex = (int) (allWords.size() * Math.random());
            return allWords.get(randomIndex).toUpperCase().toCharArray();
        }

        public void guess(char c) {
            char upperC = toUpperCase(c);
            boolean inWord = false;
            for (int i = 0; i < word.length; i++) {
                if (word[i] == upperC) {
                    inWord = true;
                    blank[i] = upperC;
                }
            }
            if (inWord == false && !(falseLetters.contains(upperC))) {
                errors--;
                falseLetters.add(upperC);
            }
        }

        public int getErrors() {
            return errors;
        }

        public String getBlank() {  // getter as String instead of Array
            String blankString = "";
            for (int i = 0; i < blank.length; i++) {
                blankString += blank[i];
            }
            return blankString;
        }

        public String getWord() {
            String blankWord = "";
            for (int i = 0; i < word.length; i++) {
                blankWord += word[i];
            }
            return blankWord;
        }

        public String getFalseLetters() {
            if (falseLetters.size() != 0) {
                String falseLettersWord = "";
                for (int i = 0; i < falseLetters.size() - 1; i++) {
                    falseLettersWord += falseLetters.get(i);
                    falseLettersWord += ",";
                }
                falseLettersWord += falseLetters.get(falseLetters.size() - 1);
                return falseLettersWord;
            } else return "";
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
