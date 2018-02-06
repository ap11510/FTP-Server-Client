import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connection
{

    private static int BUFFER_SIZE = 1024;
    private Socket controlSocket = null;
    private PrintWriter controlSocketWriter = null;
    private BufferedReader controlSocketReader = null;


    Connection(Socket controlSocket)
    {
        this.controlSocket = controlSocket;
    }

    /**
     * Starts a connection wth a remote host
     *
     * @throws IOException - Thrown when creating controlChannelSocket to a remote host
     */
    public void start() throws IOException
    {

        controlSocketWriter = new PrintWriter(controlSocket.getOutputStream(), true);
        controlSocketReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

        Utils.writeOutput("Connected to: \n" + ((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getHostName() +
                "\nPort: " + controlSocket.getPort(), null);
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
        }
        finally
        {
            controlSocketWriter = null;
            controlSocketReader = null;
            controlSocket = null;
        }
    }

    /**
     * Send a message to a remote host
     *
     * @param message - Message to be sent to a remote host
     */
    public void sendMessage(String message)
    {
        controlSocketWriter.println(message);
    }

    /**
     * Receive a message from a remote host
     *
     * @return - Message received from a remote host
     * @throws IOException - Thrown when receiving a message from remote host
     */
    public String receiveMessage() throws IOException
    {
        StringBuffer response = new StringBuffer();

        while (true)
        {
            String s = controlSocketReader.readLine().trim();
            if (s.equals(""))
            {
                break;
            }
            response.append(s);
            response.append("\n");
        }
        return response.toString();
    }

    public String getRemoteHostName()
    {
        return ((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getHostName();
    }

    public int getPort()
    {
        return controlSocket.getPort();
    }

    public Socket getDatSocket() throws IOException
    {
        Socket dataSocket = new Socket(
                ((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getHostName(), controlSocket.getPort() - 1);
        return dataSocket;
    }

    public void receiveData(OutputStream os) throws IOException
    {

        Socket dataSocket = new Socket(
                ((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getHostName(), controlSocket.getPort() - 1);

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

    public void sendData(long size, InputStream is) throws IOException
    {

        Socket dataSocket = new Socket(
                ((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getHostName(), controlSocket.getPort() - 1);

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
}
