package com.finalproject;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.util.Map;
import java.util.HashMap;
import android.util.Log;
import java.net.*;
import java.io.*;
import java.net.Inet4Address;
import java.util.Arrays;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;





public class Client extends ReactContextBaseJavaModule {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    InputHandler inHandler;

    OutputHandler outputHandler;

    String inMessage;

    String outMessage;

    BinarySemaphore waithere;

    BinarySemaphore waithere2;

    ReactApplicationContext reactContext;


    Client(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
   }

    @Override
    public String getName() {
   return "Client";
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public int connectToServer(){
        try{
            System.out.println("Trying to connect");
          // String addr = InetAddress.getByName("hangmanserver.herokuapp.com").getHostAddress();
           // System.out.println("Hallo ich bins" + addr);
           // client = new Socket("hangmanserver.herokuapp.com", 9999);
           client = new Socket("157.245.17.95", 9999);
            System.out.println("Got past connect");
            out = new PrintWriter(client.getOutputStream(), true);  //output to Server
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            inHandler = new InputHandler();
            outputHandler = new OutputHandler();
            done = false;
            waithere = new BinarySemaphore(false);
            waithere2 = new BinarySemaphore(false);
            Thread tUb = new Thread(inHandler);
            Thread tOut = new Thread(outputHandler);
            tOut.start();
            tUb.start();
            return 1;
        } catch(Exception e){
            System.out.println("Could not connect");
            System.out.println(e);
            return -1;
        }
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void sendMessage(String message) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(50);
        this.inMessage = message;
        System.out.println("Got here (A), message: " + message);
        waithere.V();
    }



    class OutputHandler implements Runnable {


    @Override
    public void run() {
        String serverMessage;
        try{
        while((serverMessage = in.readLine()) != null){
            System.out.println(serverMessage);
            outMessage = serverMessage;
            //ReactContext reactContext = getReactApplicationContext();;
            //TODO: outmessage to App.getJS
            DeviceEventManagerModule.RCTDeviceEventEmitter emitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
            emitter.emit("myEvent", outMessage );

        }
        } catch(Exception e){
            shutdown();
        }
    }
    }

    class InputHandler implements Runnable{

        String oldMessage;

        @Override
        public void run() {
            while(!done){
                try{
                    waithere.P();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                oldMessage = inMessage;
                String message = inMessage;
                System.out.println("Got here (C), message: " + inMessage);
                if (message.equals("/quit")) {
                    out.println(message);
                    //   inReader.close();
                    shutdown();
                } else {
                    out.println(message);
                }
            }
        }
    }


    public void shutdown(){
        done = true;
        try {
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        } catch(IOException e){
            //ignore
        }
    }

    class BinarySemaphore {
        boolean value;  // init value to be true or false

        public BinarySemaphore(boolean value) {
            this.value = value;
        }   //depends on need

        public synchronized void P() throws InterruptedException { // atomic operation // blocking
            while (value == false) {
                wait(); // add process to the queue of blocked processes
            }
            value = false;
        }

        public synchronized void V() { // atomic operation // non-blocking
            value = true;
            notify(); // wake up a process from the queue }
        }
    }
}




