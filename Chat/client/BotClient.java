package Chat.client;

import Chat.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BotClient extends Client {

    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hello, there. I'm a bot. I understand the following commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {

            ConsoleHelper.writeMessage(message);
            if (message.contains(": ")) {
                String name = message.split(": ")[0];
                String content = message.split(": ")[1];

                String reply;
                Calendar calendar = Calendar.getInstance();
                switch (content) {
                    case "date": {
                        reply = new SimpleDateFormat("d.MM.YYYY", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    case "day": {
                        reply = new SimpleDateFormat("d", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    case "month": {
                        reply = new SimpleDateFormat("MMMM", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    case "year": {
                        reply = new SimpleDateFormat("YYYY", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    case "time": {
                        reply = new SimpleDateFormat("H:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    case "hour": {
                        reply = new SimpleDateFormat("H", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    case "minutes": {
                        reply = new SimpleDateFormat("m", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    case "seconds": {
                        reply = new SimpleDateFormat("s", Locale.ENGLISH).format(calendar.getTime());
                        break;
                    }
                    default: {
                        return;
                    }
                }
                sendTextMessage(String.format("Information for %s: %s", name, reply));
            }
        }
    }



    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%s", (int) (Math.random() * 100));
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }


}
