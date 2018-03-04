package ftp.client;

import ftp.client.net.ClientConnectionFactory;
import ftp.client.process.ClientProcessor;
import ftp.common.application.Config;
import ftp.common.util.MessageWriter;


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

        String message = null;
        try
        {
            message = "Invalid Control Port number: " + args[1];
            int controlSocketPortNumber = Integer.parseInt(args[1]);

            message = "Invalid Data Port number: " + args[2];
            int dataSocketPortNumber = Integer.parseInt(args[2]);

            message = "Cannot establish controlConnection with remote host.";

            ClientConnectionFactory.getInstance().initialize(args[0], controlSocketPortNumber, dataSocketPortNumber);

            message = "Communication Error";

            ClientProcessor processor = new ClientProcessor();

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
        }
        catch (Exception exception)
        {
            MessageWriter.writeError(message, exception);
            System.exit(1);
        }

        System.exit(0);
    }
}
