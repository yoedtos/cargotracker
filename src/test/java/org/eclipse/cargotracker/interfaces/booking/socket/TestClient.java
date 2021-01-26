package org.eclipse.cargotracker.interfaces.booking.socket;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class TestClient {
  private static final Logger LOGGER = Logger.getLogger(TestClient.class.getName());

  public static CountDownLatch latch;

  public static String response;

  private ClientEndpointConfig clientConfig;
  private String user;

  @OnOpen
  public void connected(Session session, EndpointConfig clientConfig) {
    LOGGER.log(Level.INFO, "connected: {0}", session.getId());
  }

  @OnMessage
  public void onMessage(String msg) {
    LOGGER.info("Message from server: " + msg);
    response = msg;
    latch.countDown();
  }

  @OnClose
  public void disconnected(Session session, CloseReason reason) {
    LOGGER.log(
        Level.INFO,
        "disconnected id: {0}, reason: {1} ",
        new Object[] {session.getId(), reason.getReasonPhrase()});
  }

  @OnError
  public void disconnected(Session session, Throwable error) {
    error.printStackTrace();
    LOGGER.info("Error communicating with server: " + error.getMessage());
  }
}
