import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection
{

    private static int BUFFER_SIZE = 1024;

    private Socket controlSocket = null;

    private PrintWriter controlSocketWriter = null;
    private BufferedReader controlSocketReader = null;
    private ServerSocket dataServerSocket = null;

            Connection (Socket controlSocket)
    {
        this.controlSocket = controlSocket;
    }

    /**
     * Starts a connection wth a remote host
     *
     * @throws IOException - Thrown when creating controlSocket to a remote host
     */
    public void start() throws IOException
    {

        dataServerSocket = new ServerSocket(controlSocket.getLocalPort() - 1);

        controlSocketWriter = new PrintWriter(this.controlSocket.getOutputStream(), true);
        controlSocketReader = new BufferedReader(new InputStreamReader(this.controlSocket.getInputStream()));

        Utils.writeOutput("Connection accepted from: \n" + ((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getHostName() +
                "\nPort: " + controlSocket.getLocalPort(), null);


    }

    /**
     * Stops the connection to a remote host
     *
     * @throws IOException - Thrown when closing connection to a remote host
     */
    public void stop() throws IOException
    {
        try
        {
            if (controlSocketWriter != null)
            {
                controlSocketWriter.close();
            }

            if (controlSocketReader != null)
            {
                controlSocketReader.close();
            }

            if (dataServerSocket != null)
            {
                dataServerSocket.close();
            }
        }
        finally
        {
            controlSocketWriter = null;
            controlSocketReader = null;
            controlSocket = null;
            dataServerSocket = null;
        }
    }

    /**
     * Send a message to a remote host
     *
     * @param message - Message to be sent to a remote host
     */
    public void sendMessage(String message)
    {
        if (message.endsWith("\n"))
        {
            controlSocketWriter.println(message);
        }
        else
        {
            controlSocketWriter.println((new StringBuffer(message)).append(" \n").toString());
        }
        Utils.writeOutput("Message Sent: ", message);
    }

    /**
     * Receive a message from a remote host
     *
     * @return - Message received from a remote host
     * @throws IOException - Thrown when receiving a message from remote host
     */
    public String receiveMessage() throws IOException
    {
        return controlSocketReader.readLine();
    }

    public void sendData(long size, InputStream is) throws IOException
    {
        Socket dataSocket = dataServerSocket.accept();

        DataOutputStream dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());

        dataOutputStream.writeLong(size);

        byte[] buffer = new byte[BUFFER_SIZE];

        while (is.read(buffer) > -1)
        {
            dataOutputStream.write(buffer);
        }
        dataOutputStream.flush();

        dataOutputStream.close();

        dataSocket.close();
    }

    public void receiveData(OutputStream os) throws IOException
    {

        Socket dataSocket = dataServerSocket.accept();

        DataInputStream dataInputStream = new DataInputStream(dataSocket.getInputStream());

        long size = dataInputStream.readLong();

        byte[] buffer = new byte[BUFFER_SIZE];

        int read = 0;
        int totalRead = 0;
        int remaining = (int) size;

        while ((read = dataInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0)
        {
            totalRead += read;
            remaining -= read;

            os.write(buffer, 0, read);
        }
        dataInputStream.close();
        dataSocket.close();
    }
}
