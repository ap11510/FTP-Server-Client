package ftp.common.process.transaction;

import ftp.common.net.ConnectionFactory;
import ftp.common.net.ControlConnection;
import ftp.common.util.MessageWriter;

import java.io.File;
import java.io.IOException;

public class ReceiveFileTransaction extends FileTransaction
{
    //------------------------------------------------------------------------------------------------------------------
    public ReceiveFileTransaction(String sessionId, String transactionID, String fileName, ControlConnection controlConnection, ConnectionFactory connectionFactory) throws IOException
    {
        super(sessionId, transactionID, fileName, controlConnection, connectionFactory);
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void executeTransfer()
    {
        File file = new File(fileName);
        try
        {
            dataConnection.receiveData(file);
        }
        catch (Exception exception)
        {
            MessageWriter.writeError("Could not successfully receive file: " + fileName, exception);
        }
    }
}
