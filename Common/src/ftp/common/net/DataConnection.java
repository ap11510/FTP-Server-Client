package ftp.common.net;

import java.io.*;
import java.net.Socket;

public class DataConnection
{

    private static int BUFFER_SIZE = 16384;
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
    final public void receiveData(File file) throws IOException
    {
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        long size = dataInputStream.readLong();

        byte[] buffer = new byte[BUFFER_SIZE];

        int read = 0;
        long remaining = size;


        while (remaining > 0 && !stopped)
        {
            read = dataInputStream.read(buffer);
            remaining = remaining - read;
            fileOutputStream.write(buffer, 0, read);
        }
        fileOutputStream.flush();
        fileOutputStream.close();

        if (stopped)
        {
            file.delete();
        }
        else
        {
            success = true;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    final public void sendData(File file) throws IOException
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
        if (stopped)
        {
            file.delete();
        }
        else
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
