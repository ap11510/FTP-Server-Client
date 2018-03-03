package ftp.server;

import ftp.common.application.Config;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.util.MessageWriter;
import ftp.server.net.ServerConnectionFactory;
import ftp.server.process.ServerProcessor;

import java.util.UUID;

public class FTPServer
{
    //------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            MessageWriter.writeMessage("Usage: java FTPServer <command control port number> <data port number>");
        }
        else
        {

            Config.initialize(Config.Environment.SERVER);
            Config.getInstance();

            String message = null;
            int controlSocketPortNumber = 0;
            try
            {
                message = "Invalid command control port number: " + args[0];
                controlSocketPortNumber = Integer.parseInt(args[0]);

            }
            catch(NumberFormatException exception)
            {
                MessageWriter.writeError(message, exception);
                System.exit(1);
            }

            int dataSocketPortNumber = 0;
            try
            {
                message = "Invalid data port number: " + args[1];
                dataSocketPortNumber = Integer.parseInt(args[1]);

            }
            catch(NumberFormatException exception)
            {
                MessageWriter.writeError(message, exception);
                System.exit(1);
            }

            try
            {
                ServerConnectionFactory.getInstance().initialize(controlSocketPortNumber, dataSocketPortNumber);

                while (true)
                {
                    MessageWriter.writeMessage("Server Waiting for Client Connection on port: " + controlSocketPortNumber);

                    createThread();
                }

            }
            catch (Exception exception)
            {
                MessageWriter.writeError(message, exception);
            }
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    private static void createThread()
    {
        String message = null;


        try
        {
            message = "Cannot Establish Communication with remote host";

            ControlConnection controlConnection = ServerConnectionFactory.getInstance().getControlConnection();
            DataConnection dataConnection = ServerConnectionFactory.getInstance().getDataConnection();

            MessageWriter.writeMessage("Connection accepted from: " + controlConnection.getRemoteHostName() + ":" + controlConnection.getLocalHostCommandPort());

            message = "Runtime Error. Cannot Execute";

            new Thread(new ServerProcessor(UUID.randomUUID(), controlConnection, dataConnection)).start();

            message = "Runtime Error. Connections not closed correctly";

        }
        catch (Exception exception)
        {
            MessageWriter.writeError(message, exception);
        }
    }

}

