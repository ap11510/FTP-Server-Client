package ftp.client;

import ftp.client.net.ClientConnectionFactory;
import ftp.client.process.ClientProcessor;
import ftp.common.application.Config;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.util.MessageWriter;

import java.util.UUID;


public class FTPClient
{
    //------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            MessageWriter.writeMessage("Usage: java FTPClient <host name> <control port number> <data port numer>");
            System.exit(1);
        }

        Config.initialize(Config.Environment.CLIENT);
        Config.getInstance();

        ClientConnectionFactory connectionFactory = ClientConnectionFactory.getInstance();


        String message = null;
        try
        {
            message = "Invalid Control Port number: " + args[1];
            int controlSocketPortNumber = Integer.parseInt(args[1]);

            message = "Invalid Data Port number: " + args[2];
            int dataSocketPortNumber = Integer.parseInt(args[2]);

            message = "Cannot establish controlConnection with remote host.";


            connectionFactory.initialize(args[0], controlSocketPortNumber, dataSocketPortNumber);
            ClientProcessor processor;

            message = "Communication Error";

            ControlConnection controlConnection = connectionFactory.getControlConnection();
            DataConnection dataConnection = connectionFactory.getDataConnection();

            MessageWriter.writeMessage("Connected to: " + controlConnection.getRemoteHostName() + ":" + controlConnection.getRemoteHostCommandPort());

            processor = new ClientProcessor(UUID.randomUUID(), controlConnection, dataConnection);

            message = "ClientControlConnection Terminated by remote host";

            Thread thread = new Thread(processor);
            thread.start();

            while (true)
            {
                if (processor.isRunning())
                {
                    Thread.sleep(1000);
                }
                else
                {
                    break;
                }
            }

            message = "Communication Error";
            controlConnection.close();

            message = "ClientControlConnection not closed correctly";
            controlConnection.close();

        }
        catch (Exception exception)
        {
            MessageWriter.writeError(message, exception);
            System.exit(1);
        }

        System.exit(0);
    }
}
