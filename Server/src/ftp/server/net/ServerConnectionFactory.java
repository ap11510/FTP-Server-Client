package ftp.server.net;

import ftp.common.net.ConnectionFactory;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConnectionFactory extends ConnectionFactory
{
    private static ServerConnectionFactory instance;

    private boolean initialized = false;

    private ServerSocket controlSocketServer;
    private ServerSocket dataSocketServer;


    public static ServerConnectionFactory getInstance()
    {
        if (instance == null)
        {
            instance = new ServerConnectionFactory();
        }
        return instance;
    }

    //------------------------------------------------------------------------------------------------------------------
    public void initialize(int controlSocketPortNumber, int dataSocketPortNumber) throws IOException
    {
        if (!initialized)
        {
            if (controlSocketPortNumber == dataSocketPortNumber)
            {
                throw new IOException("Control port number must be different than data port number ");
            }

            String message= null;

            try
            {
                message = "Server cannot listen on specified control port: " + controlSocketPortNumber;
                controlSocketServer = new ServerSocket(controlSocketPortNumber);

                message = "Server cannot listen on specified data port: " + controlSocketPortNumber;
                dataSocketServer = new ServerSocket(dataSocketPortNumber);

            }
            catch (IOException exception)
            {
                throw new IOException(message, exception);
            }

            this.controlSocketPortNumber = controlSocketPortNumber;
            this.dataSocketPortNumber = dataSocketPortNumber;

            initialized = true;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public ControlConnection getControlConnection() throws IOException
    {
        if (!initialized)
        {
            throw new IOException("ServerConnection Factory not initialzed");
        }
        Socket socket = controlSocketServer.accept();
        return new ControlConnection(socket);
    }

    //------------------------------------------------------------------------------------------------------------------
    public DataConnection getDataConnection() throws IOException
    {
        if (!initialized)
        {
            throw new IOException("ServerConnection Factory not initialzed");
        }
        Socket socket = dataSocketServer.accept();
        return new DataConnection(socket);
    }
}

