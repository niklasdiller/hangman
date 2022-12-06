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

    public Server() {
        connections = new ArrayList<>();
        done = false;       // default not done
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
        private String nickname;

        private Game game;

        public ConnectionHandler(Socket client) { // Constructor
            this.client = client;   //
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);  //output
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));    //input from client
                out.println("Please enter a nickname: ");       // out to every client
                nickname = in.readLine();                   // reads in text from client
                System.out.println(nickname + " connected!");
                broadcast(nickname + " joined the game!"); // to all
                broadcast("Type /start to play a game.");
                String message;     // null String
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick ")) {  // option nickname change
                        String[] messageSplit = message.split(" ", 2);  // checks input
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Succesfully changed nickname to " + nickname);
                        } else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/start")) {
                        broadcast("Game starts ..");
                        game = new Game();
                        broadcast("Remaining Errors: " + game.getErrors());
                        broadcast("Word: " + game.getBlank());

                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat.");
                        shutdown();
                    } else if (game != null) {
                        if (!(game.getBlank().equals(game.getWord()) && (game.getErrors() >= 0))) { // criteria for ending loop
                            if (message.length() == 1) {
                                char guess = message.charAt(0);
                                game.guess(guess);
                            }

                            if (game.getBlank().equals(game.getWord())) {
                                broadcast("You won, you guessed the word " + game.getWord());
                                broadcast("Type /start for a new game.");
                                game = null;
                                return;
                            } else if ((game.getErrors() < 0)) {
                                broadcast("You lost, the word was " + game.getWord());
                                broadcast("Type /start for a new game.");
                                game = null;
                                return;
                            }
                            broadcast("Remaining Errors: " + game.getErrors());
                            broadcast("False Letters: " + game.getFalseLetters());
                            broadcast("Word: " + game.getBlank());
                        }
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
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

        public Game() throws FileNotFoundException {
            this.word = getRandomWord();//new char[]{'H', 'A', 'N', 'G', 'M', 'A', 'N'};  //TODO: randomize possible words
            this.blank = new char[this.word.length];    //blank fitting to word
            for (int i = 0; i < this.blank.length; i++) {
                this.blank[i] = '_';
            }
            this.errors = 7;    // 7 lines of hangman
            this.falseLetters = new ArrayList<>();
        }

        private char[] getRandomWord() throws FileNotFoundException {

            ArrayList<String> allWords = new ArrayList<>();
            Scanner scanner = new Scanner(new File("words.csv"));
            scanner.useDelimiter(", ");
            while (scanner.hasNext()) {
                allWords.add(scanner.next());
            }
            scanner.close();
            int randomIndex = (int) (allWords.size() * Math.random());
            return allWords.get(randomIndex).toUpperCase().toCharArray();
        }

        public void guess(char c) {      // critical section
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
                    falseLettersWord += ", ";
                }
                falseLettersWord += falseLetters.get(falseLetters.size() - 1);
                return falseLettersWord;
            } else return "none yet";
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}