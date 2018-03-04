package ftp.common.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ControlConnection
{
    protected Socket controlSocket;
    private String remoteHostName;
    private int remoteHostControlPort;
    private String localHostName;
    private int localHostControlPort;
    private PrintWriter controlSocketWriter;
    private BufferedReader controlSocketReader;


    //------------------------------------------------------------------------------------------------------------------
    public ControlConnection(Socket controlSocket) throws IOException
    {
        this.controlSocket = controlSocket;

        InetSocketAddress remoteControlSocketAddress = (InetSocketAddress) controlSocket.getRemoteSocketAddress();
        InetSocketAddress localControlSocketAddress = (InetSocketAddress) controlSocket.getLocalSocketAddress();

        this.remoteHostName = remoteControlSocketAddress.getHostName();
        this.remoteHostControlPort = remoteControlSocketAddress.getPort();

        this.localHostName = localControlSocketAddress.getHostName();
        this.localHostControlPort = localControlSocketAddress.getPort();

        controlSocketWriter = new PrintWriter(controlSocket.getOutputStream(), true);
        controlSocketReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
    }

    //------------------------------------------------------------------------------------------------------------------
    final public void close() throws IOException
    {
        controlSocketWriter.close();
        controlSocketReader.close();

        controlSocket.close();
    }

    //------------------------------------------------------------------------------------------------------------------
    public final void sendMessage(String message)
    {
        if (message.endsWith("\n"))
        {
            controlSocketWriter.println(message);
        }
        else
        {
            controlSocketWriter.println(message + " \n");
        }
    }

    //------------------------------------------------------------------------------------------------------------------
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
        return sb.toString();
    }

    //------------------------------------------------------------------------------------------------------------------
    final public String receiveCode() throws IOException
    {
        String response = receiveMessage();

        return response.substring(0, response.length() - 1);
    }

    //------------------------------------------------------------------------------------------------------------------
    final public String getRemoteHostName()
    {
        return remoteHostName;
    }

    //------------------------------------------------------------------------------------------------------------------
    final public int getRemoteHostControlPort()
    {
        return remoteHostControlPort;
    }

    //------------------------------------------------------------------------------------------------------------------
    public String getLocalHostName()
    {
        return localHostName;
    }

    //------------------------------------------------------------------------------------------------------------------
    public int getLocalHostControlPort()
    {
        return localHostControlPort;
    }

}
