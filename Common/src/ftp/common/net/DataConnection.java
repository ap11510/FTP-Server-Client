package ftp.common.net;

import ftp.common.util.MessageWriter;

import java.io.*;
import java.net.Socket;

public class DataConnection
{

    private static int BUFFER_SIZE = 8192;
    protected boolean success = false;
    private Socket dataSocket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private boolean stopped = false;

    //------------------------------------------------------------------------------------------------------------------
    public DataConnection(Socket dataSocket) throws IOException
    {
        this.dataSocket = dataSocket;
        dataInputStream = new DataInputStream(dataSocket.getInputStream());
        dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());
    }

    //------------------------------------------------------------------------------------------------------------------
    final public void receiveData(File file)
    {
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(file);

            long size = dataInputStream.readLong();

            byte[] buffer = new byte[BUFFER_SIZE];

            int read = 0;
            long remaining = size;


            while (remaining > 0 && !stopped)
            {
                read = dataInputStream.read(buffer);
                remaining = remaining - read;
                if (!stopped)
                {
                    fileOutputStream.write(buffer, 0, read);
                }
            }
            fileOutputStream.flush();
            fileOutputStream.close();

        }
        catch (Exception exception)
        {
            MessageWriter.writeError("Error in DataConnection.receiveData()", exception);
        }



        if (!stopped)
        {
            success = true;
        }
        else
        {
            file.delete();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    final public void sendData(File file)
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(file);

            long size = file.length();

            dataOutputStream.writeLong(size);

            byte[] buffer = new byte[BUFFER_SIZE];

            while (fileInputStream.read(buffer) > -1 && !stopped)
            {
                dataOutputStream.write(buffer);
            }
            dataOutputStream.flush();

            fileInputStream.close();

        }
        catch (Exception exception)
        {
            MessageWriter.writeError("Error in DataConnection.sendData()", exception);
        }

        if (!stopped)
        {
            success = true;
        }

    }

    //------------------------------------------------------------------------------------------------------------------
    final public void close()
    {
        try
        {
            dataInputStream.close();
            dataOutputStream.close();
            dataSocket.close();
        }
        catch (IOException exception)
        {
            MessageWriter.writeError("Error in DataConnection.close()", exception);

        }
    }

    public void stopDataTransfer()
    {
        stopped = true;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public boolean isStopped()
    {
        return stopped;
    }
}
