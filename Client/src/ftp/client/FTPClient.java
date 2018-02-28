package ftp.client;

import ftp.client.net.ClientConnection;
import ftp.common.application.Config;
import ftp.common.util.Utils;

import java.net.Socket;
import java.util.logging.Level;


public class FTPClient
{
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            Utils.writeMessage("Usage: java FTPClient <host name> <port number> ");
            System.exit(1);
        }

        Config.initialize(Config.Environment.CLIENT);
        Config.getInstance();

        String message = null;
        try
        {
            message = "Invalid Port number: " + args[1];
            int port = Integer.parseInt(args[1]);

            message = "Cannot establish connection with remote host.";
            Socket controlSocket = new Socket(args[0], port);

            ClientConnection connection;
            ClientProcessor processor;

            message = "Communication Error";

            connection = new ClientConnection(controlSocket);

            Utils.writeMessage("Connected to: " + connection.getRemoteHostName() + ":" + connection.getRemoteHostCommandPort());

            processor = new ClientProcessor(connection);

            message = "ClientConnection Terminated by remote host";

            processor.run();

            message = "Communication Error";
            connection.close();

            message = "ClientConnection not closed correctly";
            controlSocket.close();

        }
        catch (Exception exception)
        {
            Utils.writeError(message, exception);
            System.exit(1);
        }

        System.exit(0);
    }
}
