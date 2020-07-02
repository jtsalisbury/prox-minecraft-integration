package com.cbot.integration;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;
import org.bukkit.Bukkit;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import java.net.URISyntaxException;

import org.apache.commons.codec.digest.DigestUtils;

import javax.net.ssl.*;

public class MessageInterface {
    private static String CONNECTION_ADDRESS = "https://damp-coast-58076.herokuapp.com/";
    private boolean connected = false;
    private boolean reconnect = true;
    private Socket conn = null;
    private Logger logger = null;
    private Queue<String[]> outgoingMessages = new ArrayDeque<String[]>();

    public MessageInterface(Logger logger) {
        this.logger = logger;
    }

    private void log(String message) {
        logger.info(message);
    }

    public String hash(String str) {
        DigestUtils digest = new DigestUtils("SHA-1");
        String result = digest.sha1Hex(str);

        return result;
    }

    // Try and connect to
    public void connect(String token) {
        try {
            // Setup a new ssl context
            SSLContext mySSLContext = SSLContext.getInstance("TLS");
            X509TrustManager trustManager =  new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
            };

            mySSLContext.init(null, new TrustManager[] { trustManager }, null);

            // Setup a new hostname verifier
            HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Setup a new http client based off our ssl setup
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(myHostnameVerifier)
                    .sslSocketFactory(mySSLContext.getSocketFactory(), trustManager)
                    .build();

            // Set the options for socketio
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;
            opts.upgrade = false;
            opts.transports = new String[] {"websocket"};

            log("Attempting to connect...");
            conn = IO.socket(CONNECTION_ADDRESS);

            // Connection event, here we should try and authorize
            conn.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    JSONObject obj = new JSONObject();

                    try {
                        obj.put("signature", hash(token));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    log("Connected! Attempting authorization...");
                    conn.emit("authentication", obj);
                }
            });

            // Server error, send a notificaiton and attempt to reconnect
            conn.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    log("Server error - unable to connect, will attempt to reconnect in 30 seconds...");
                    conn.disconnect();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            conn.connect();
                        }
                    }, 30 * 1000);
                }
            });

            // Attempted connection but not authorized
            conn.on("unauthorized", new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    log("Failed to authorize! Are you sure the token is correct and the integration exists?");
                    log("Authorization error: " + objects[0]);
                }
            });

            // Post connection and authentication
            conn.on("authenticated", new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    log("Authenticated!");

                    // Dequeue all our standing messages
                    while (!outgoingMessages.isEmpty()) {
                        String[] messageData = outgoingMessages.peek();

                        doSendMessage(messageData[0], messageData[1]);
                        outgoingMessages.poll();
                    }

                    connected = true;
                }
            });

            // Disconnect event
            conn.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    log("Disconnected");

                    connected = false;

                    // Only reconnect when we expect to connect (ie not during plugin shutdown)
                    if (reconnect) {
                        log("Attempting reconnect in 30 seconds...");
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                conn.connect();
                            }
                        }, 30 * 1000);
                    }
                }
            });

            conn.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    JSONObject obj = (JSONObject) objects[0];

                    // Attempt to read the sender and content
                    String sender = null;
                    String content = null;

                    // Attempt to get the sender and content from the message we received
                    try {
                        sender = obj.get("sender").toString();
                        content = obj.get("content").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Invalid payload
                    if (sender == null || content == null) {
                        return;
                    }

                    // Send a message to all players
                    Bukkit.broadcastMessage("[Discord] " + sender + ": " + content);
                }
            });

            conn.connect();

        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        this.reconnect = false;

        conn.disconnect();
    }

    // Helper method to actually senda  message
    private void doSendMessage(String sender, String message) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("sender", sender);
            obj.put("content", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        conn.emit("message", obj);
    }

    // Send a message to discord
    public void sendMessage(String sender, String message) {
        // If we aren't connected, throw it in the queue to send
        if (!this.getConnected()) {

            outgoingMessages.offer(new String[] {sender, message});
            return;
        }

        doSendMessage(sender, message);
    }

    // Returns the connection state
    public boolean getConnected() {
        return connected;
    }
}
