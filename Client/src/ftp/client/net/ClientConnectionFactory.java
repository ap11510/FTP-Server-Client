package ftp.client.net;

import ftp.common.net.ConnectionFactory;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;

import java.io.IOException;
import java.net.Socket;

public class ClientConnectionFactory extends ConnectionFactory
{
    private static ClientConnectionFactory instance;

    private String remoteHost;

    public static ClientConnectionFactory getInstance()
    {
        if (instance == null)
        {
            instance = new ClientConnectionFactory();
        }
        return instance;

    }

    //------------------------------------------------------------------------------------------------------------------
    public void initialize(String remoteHost, int controlSocketPortNumber, int dataSocketPortNumber) throws IOException
    {
        if (!initialized)
        {
            if (controlSocketPortNumber == dataSocketPortNumber)
            {
                throw new IOException("Control port number must be different than data port number ");
            }

            this.remoteHost = remoteHost;
            this.controlSocketPortNumber = controlSocketPortNumber;
            this.dataSocketPortNumber = dataSocketPortNumber;

            initialized = true;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public ControlConnection getControlConnection() throws IOException
    {
        Socket socket = new Socket(remoteHost, controlSocketPortNumber);

        return new ControlConnection(socket);

    }

    //------------------------------------------------------------------------------------------------------------------
    public DataConnection getDataConnection() throws IOException
    {
        Socket socket = new Socket(remoteHost, dataSocketPortNumber);

        return new DataConnection(socket);

    }
}

