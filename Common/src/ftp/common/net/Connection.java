package ftp.common.net;

import ftp.common.util.Utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class Connection
{

    private static int BUFFER_SIZE = 1024;

    protected Socket controlSocket;
    private String remoteHostName;
    private int remoteHostCommandPort;
    private String localHostName;
    private int localHostCommandPort;
    private PrintWriter controlSocketWriter ;
    private BufferedReader controlSocketReader ;


    protected Connection(Socket controlSocket) throws IOException
    {
        this.controlSocket = controlSocket;

        InetSocketAddress remoteControlSocketAddress = (InetSocketAddress) controlSocket.getRemoteSocketAddress();
        InetSocketAddress localControlSocketAddress = (InetSocketAddress) controlSocket.getLocalSocketAddress();

        this.remoteHostName = remoteControlSocketAddress.getHostName();
        this.remoteHostCommandPort = remoteControlSocketAddress.getPort();

        this.localHostName = localControlSocketAddress.getHostName();
        this.localHostCommandPort = localControlSocketAddress.getPort();

        controlSocketWriter = new PrintWriter(controlSocket.getOutputStream(), true);
        controlSocketReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
    }

    final public void close() throws IOException
    {
        controlSocketWriter.close();
        controlSocketReader.close();

        controlSocketWriter = null;
        controlSocketReader = null;
        controlSocket = null;
    }

    public final void sendMessage(String message)
    {
        Utils.writeMessage(new String[]{"Message Sent to remote host: " + getRemoteHostName() + ":" + controlSocket.getPort() , message});
        if (message.endsWith("\n"))
        {
            controlSocketWriter.println(message);
        }
        else
        {
            controlSocketWriter.println((new StringBuffer(message)).append(" \n").toString());
        }
    }

    final public String receiveMessage() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        while (true)
        {
            String s = controlSocketReader.readLine().trim();
            if (s.equals(""))
            {
                break;
            }
            sb.append(s);
            sb.append("\n");
        }
        String response = sb.toString();

        Utils.writeMessage(new String[]{"Message received from remote host: " + getRemoteHostName() + ":" + controlSocket.getPort() , response});
        return response;
    }

    final public void receiveData(OutputStream os) throws IOException
    {
        Socket dataSocket = openDataConnection();

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

    final public void sendData(long size, InputStream is) throws IOException
    {
        Socket dataSocket = openDataConnection();

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

    final public String getRemoteHostName()
    {
        return remoteHostName;
    }

    final public int getRemoteHostCommandPort()
    {
        return remoteHostCommandPort;
    }

    public String getLocalHostName()
    {
        return localHostName;
    }

    public int getLocalHostCommandPort()
    {
        return localHostCommandPort;
    }

    protected abstract Socket openDataConnection() throws IOException;
}
