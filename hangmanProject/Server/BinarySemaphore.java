public class BinarySemaphore {
    boolean value;  // init value to be true or false

    public BinarySemaphore(boolean value ) {
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
