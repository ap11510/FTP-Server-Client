package ftp.client.net;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientConnection extends ftp.common.net.Connection
{
    public ClientConnection(Socket controlSocket) throws IOException
    {
        super(controlSocket);
    }

    protected Socket openDataConnection() throws IOException
    {
        Socket dataSocket = new Socket(
                ((InetSocketAddress) controlSocket.getRemoteSocketAddress()).getHostName(), controlSocket.getPort() - 1);

        return dataSocket;
    }
}
