package Chat.client;

import Chat.Connection;
import Chat.ConsoleHelper;
import Chat.Message;
import Chat.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has joined the chat");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has left the chat");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                MessageType messageType = connection.receive().getType();
                if (messageType == null)
                    throw new IOException("Unexpected MessageType");
                switch (messageType) {
                    case NAME_REQUEST:
                        connection.send(new Message(MessageType.USER_NAME, getUserName()));
                        break;
                    case NAME_ACCEPTED:
                        notifyConnectionStatusChanged(true);
                        return;
                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() != MessageType.TEXT && message.getType() != MessageType.USER_REMOVED && message.getType() != MessageType.USER_ADDED) {
                    throw new IOException("Unexpected MessageType");
                }
                if (message.getType().equals(MessageType.TEXT)) {
                    processIncomingMessage(message.getData());
                    continue;
                }
                if (message.getType().equals(MessageType.USER_ADDED)) {
                    informAboutAddingNewUser(message.getData());
                    continue;
                }
                if (message.getType().equals(MessageType.USER_REMOVED)) {
                    informAboutDeletingNewUser(message.getData());
                }
            }
        }

        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();

            try {
                Socket socket = new Socket(address,port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }


        }
    }

    protected String getServerAddress(){
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            clientConnected = false;
            ConsoleHelper.writeMessage("Client disconnected");
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Error");
            System.exit(0);
        }

        if (clientConnected) {
            ConsoleHelper.writeMessage("Connection established. To exit, enter 'exit'.");
            while (clientConnected) {
                String message = ConsoleHelper.readString();
                if (message.equals("exit")) break;
                if (shouldSendTextFromConsole()) sendTextMessage(message);
            }
        } else ConsoleHelper.writeMessage("An error occurred while working with the client.");

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
