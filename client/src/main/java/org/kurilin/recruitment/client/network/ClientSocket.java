package org.kurilin.recruitment.client.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientSocket {
    private static final Logger logger = LoggerFactory.getLogger(ClientSocket.class);
    private static final int PORT = 1024;
    private static ClientSocket instance;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    private ClientSocket() {
        try {
            socket = new Socket("localhost", PORT);
            writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            logger.info("Socket created");
        } catch (Exception e) {
            logger.error("Error creating socket", e);
        }
    }

    public static synchronized ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public String sendMessage(String jsonRequest) {
        if (socket == null || socket.isClosed()) {
            logger.info("Socket is not connected");
            return "{\"success\":false, \"message\":\"No connection\"}";
        }
        try {
            writer.println(jsonRequest);
            logger.info("Sent to server: {}", jsonRequest);
            return reader.readLine();
        } catch (IOException e)
        {
            logger.error("Error sending message", e);
            return "{\"success\":false, \"message\":\"Error sending message\"}";
        }
    }

    public void closeConnection() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
            logger.info("Connection closed");
        } catch (IOException e) {
            logger.error("Error closing connection", e);
        }
    }
}
