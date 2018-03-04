package ftp.common.process.transaction;

import ftp.common.net.ConnectionFactory;
import ftp.common.net.ControlConnection;
import ftp.common.util.MessageWriter;

import java.io.File;
import java.io.IOException;

public class SendFileTransaction extends FileTransaction
{
    //------------------------------------------------------------------------------------------------------------------
    public SendFileTransaction(String sessionId, String transactionID, String fileName, ControlConnection controlConnection, ConnectionFactory connectionFactory) throws IOException
    {
        super(sessionId, transactionID, fileName, controlConnection, connectionFactory);
    }

    //------------------------------------------------------------------------------------------------------------------
    public void executeTransfer()
    {
        try
        {
            File file = new File(fileName);
            dataConnection.sendData(file);
        }
        catch (Exception exception)
        {
            MessageWriter.writeError("Could not successfully send file: " + fileName, exception);
        }
    }
}
