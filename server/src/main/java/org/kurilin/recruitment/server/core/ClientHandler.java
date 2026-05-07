package org.kurilin.recruitment.server.core;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dispatcher.RequestDispatcher;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Request;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final RequestDispatcher dispatcher;
    private final Gson gson = GsonFactory.getGson();

    public ClientHandler(Socket socket, RequestDispatcher requestDispatcher) {
        clientSocket = socket;
        dispatcher = requestDispatcher;
    }

    @Override
    public void run() {
        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            logger.info("Starting client connection");

            String message;

            while ((message = in.readLine()) != null) {
                logger.info("Received from client: {}", message);
                try {
                    Request request = gson.fromJson(message, Request.class);
                    logger.info("Request type: {}", request.getType());

                    Response response = dispatcher.dispatch(request);
                    out.println(gson.toJson(response));
                    //out.println("{\"success\": true, \"message\": \"Message received!\"}");
                } catch (RecruitmentBusinessException e){
                    logger.warn("Business error: {}", e.getMessage());
                    Response response = new Response(false, e.getMessage(), null);
                    out.println(gson.toJson(response));
                } catch (Exception e) {
                    logger.error("Error while processing request: {}",message, e);
                    Response response = new Response(false, "Error on server. ", null);
                    out.println(gson.toJson(response));
                }
            }
            logger.info("Connection closed");
        } catch (Exception e) {
            logger.error("Error handling client connection", e);
        }
    }
}
