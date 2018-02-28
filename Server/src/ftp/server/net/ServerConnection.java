package ftp.server.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import ftp.common.util.Utils;

public class ServerConnection extends ftp.common.net.Connection
{
    private ServerSocket dataServerSocket ;

    public ServerConnection(Socket controlSocket, ServerSocket dataServerSocket) throws IOException
    {
        super(controlSocket);
        this.dataServerSocket = dataServerSocket;
    }

    protected Socket openDataConnection() throws IOException
    {
        Utils.writeMessage("Server Waiting for Client Data Connection on port: " + dataServerSocket.getLocalPort());
        Socket socket = dataServerSocket.accept();
        Utils.writeMessage("Client Data Connection established on port: " + dataServerSocket.getLocalPort());
        return socket;
    }
}
