package htl.steyr.wechselgeldapp.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A service class that handles reading from and writing to a BluetoothSocket.
 * Used for communication between two paired Bluetooth devices (e.g., seller and customer).
 */
public class BluetoothDataService {
    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Handler handler; // Optional UI handler for message updates

    /**
     * Constructs a BluetoothDataService that manages communication over a given socket.
     *
     * @param socket The connected BluetoothSocket.
     * @param handler A handler to post received messages to the main thread (can be null).
     * @throws IOException if input/output streams cannot be created from the socket.
     */
    public BluetoothDataService(BluetoothSocket socket, Handler handler) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.handler = handler;
    }

    /**
     * Sends a string message through the Bluetooth connection.
     * A newline character is automatically added to mark the end of the message.
     *
     * @param data The data string to send.
     */
    public void write(String data) {
        try {
            outputStream.write((data + "\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts a background thread to listen for incoming messages.
     * Each message is read line-by-line and sent to the provided handler (if not null).
     */
    public void listenForMessages() {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (handler != null) {
                        Message msg = Message.obtain();
                        msg.obj = line;
                        handler.sendMessage(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(); // Optionally add logging or callback
            }
        }).start();
    }

    /**
     * Closes the Bluetooth socket and releases all resources.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
