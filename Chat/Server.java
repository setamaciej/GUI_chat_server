package Chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Chat.Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Chat.ConsoleHelper.writeMessage("Input server port: ");
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            Chat.ConsoleHelper.writeMessage("Server is UP!");

            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Something wrong, Server socket closed.");
        }

    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {

            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("error while sending message to " + pair.getKey() + ".");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("New connection established with" + socket.getRemoteSocketAddress() + ".");
            String clientName = null;

            try (Connection connection = new Connection(socket)) {
                clientName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, clientName));
                notifyUsers(connection, clientName);
                serverMainLoop(connection, clientName); // loops here until dc

                connectionMap.remove(clientName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, clientName));

            } catch (IOException | ClassNotFoundException ignored) {
                connectionMap.remove(clientName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, clientName));
                sendBroadcastMessage(new Message(MessageType.TEXT,"---User " + clientName + " disconnected---"));
                ConsoleHelper.writeMessage("User " + clientName + " disconnected");
            }


        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message response = connection.receive();
                if (response.getType().equals(MessageType.USER_NAME)) {
                    if (!response.getData().isEmpty()) {
                        if (!connectionMap.containsKey(response.getData())) {
                            name = response.getData();
                            break;
                        }
                    }
                }
            }
            connectionMap.put(name, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            return name;
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message received = connection.receive();
                if (received.getType().equals(MessageType.TEXT)) {
                    String m = userName + ": " + received.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, m));
                } else {
                    ConsoleHelper.writeMessage("Error: not a message");
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                String name = pair.getKey();
                if (name.equals(userName)) continue;
                connection.send(new Message(MessageType.USER_ADDED, name));

            }
        }
    }


}
