import java.io.*;
import java.net.Socket;


public class myftp
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage: java FTPClient <host name> <port number>");
            System.exit(1);
        }

        int port = 0;
        try
        {
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException exception)
        {
            System.out.println("Invalid Port number");
            System.exit(1);
        }

        Socket controlSocket = null;
        Connection connection = null;
        Processor processor = null;

        try
        {
            controlSocket = new Socket(args[0], port);

            connection = new Connection(controlSocket);

            connection.start();

            processor = new Processor(connection);
        }
        catch (IOException exception)
        {
            System.out.println("Cannot establish connection with remote host.");
            controlSocket = null;
            System.exit(1);
        }

        try
        {
            processor.run();
        }
        catch (IOException exception)
        {
            System.out.println("Connection Terminated by remote host");
            controlSocket = null;
            System.exit(1);
        }

        try
        {
            connection.stop();

            controlSocket.close();
        }
        catch (IOException exception)
        {
            System.out.println("Connection not closed correctly");
            controlSocket = null;
            System.exit(1);
        }

        System.exit(0);
    }
}
