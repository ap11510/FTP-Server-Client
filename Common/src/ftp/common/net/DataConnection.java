package ftp.common.net;

import java.io.*;
import java.net.Socket;

public class DataConnection
{

    private static int BUFFER_SIZE = 4096;

    private Socket dataSocket;

    private DataInputStream dataInputStream ;
    private DataOutputStream dataOutputStream ;

    private boolean stopped = false;

    //------------------------------------------------------------------------------------------------------------------
    public DataConnection(Socket dataSocket) throws IOException
    {
        this.dataSocket = dataSocket;
        dataInputStream = new DataInputStream(dataSocket.getInputStream());
        dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());
    }

    //------------------------------------------------------------------------------------------------------------------
    final public void receiveData(OutputStream os) throws IOException
    {
        stopped = false;
        long size = dataInputStream.readLong();

        byte[] buffer = new byte[BUFFER_SIZE];

        int read = 0;
        int totalRead = 0;
        int remaining = (int) size;

        while
        (
            (read = dataInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0 &&
            !stopped
        )
        {
            totalRead += read;
            remaining -= read;

            os.write(buffer, 0, read);
            os.flush();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    final public void sendData(long size, InputStream is) throws IOException
    {
        stopped = false;

        dataOutputStream.writeLong(size);

        byte[] buffer = new byte[BUFFER_SIZE];

        while (is.read(buffer) > -1 && !stopped)
        {
            dataOutputStream.write(buffer);
        }
        dataOutputStream.flush();

    }

    //------------------------------------------------------------------------------------------------------------------
    final public void close() throws IOException
    {
        dataInputStream.close();
        dataOutputStream.close();
        dataSocket.close();
    }

    public void stopDataTransfer()
    {
        stopped = true;
    }
}
