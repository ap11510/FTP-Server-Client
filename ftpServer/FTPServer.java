import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class FTPServer
{
    private FTPServer()
    {
    }

    public static void main(String[] args)
    {
        int portNumber = Integer.parseInt(args[0]);

        FTPServer ftpServer = new FTPServer();

        while (true)
        {
            ftpServer.run(portNumber);
        }
    }

    private void run(int port)
    {
        ServerSocket controlServerSocket = null;
        Socket controlSocket = null;

        Connection connection = null;

        Processor processor = null;

        try
        {
            controlServerSocket = new ServerSocket(port);

            try
            {
                controlSocket = controlServerSocket.accept();

                connection = new Connection(controlSocket);

                connection.start();

                processor = new Processor(connection);

                processor.run();

            }
            catch (IOException exception)
            {
                Utils.writeOutput("Cannot Establish Communication with remote host", null);
                exception.printStackTrace();
            }
            catch (Exception exception)
            {
                Utils.writeOutput("Runtime Error. Cannot Execute", null);
                exception.printStackTrace();
            }

            try
            {
                connection.stop();

                if (controlServerSocket != null)
                {
                    controlServerSocket.close();
                }

                if (controlSocket != null)
                {
                    controlSocket.close();
                }
            }
            catch (Exception exception)
            {
                Utils.writeOutput("Runtime Error. Connections not closed correctly", null);
                exception.printStackTrace();
            }

        }
        catch (Exception exception)
        {
            Utils.writeOutput("Server cannot listen on specified port: " + port, null);
            exception.printStackTrace();
        }
        finally
        {
            processor = null;
            connection = null;
            controlServerSocket = null;
            controlSocket = null;
        }
    }
}

