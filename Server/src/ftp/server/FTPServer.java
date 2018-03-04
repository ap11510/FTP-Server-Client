package ftp.server;

import ftp.common.application.Config;
import ftp.common.util.MessageWriter;
import ftp.server.net.ServerConnectionFactory;
import ftp.server.process.ServerProcessor;


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
            catch (NumberFormatException exception)
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
            catch (NumberFormatException exception)
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

            new Thread(new ServerProcessor()).start();

            message = "Runtime Error. Connections not closed correctly";

        }
        catch (Exception exception)
        {
            MessageWriter.writeError(message, exception);
        }
    }

}

