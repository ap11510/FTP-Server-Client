package ftp.common.process.transaction;

import ftp.common.net.ConnectionFactory;
import ftp.common.net.ControlConnection;
import ftp.common.util.MessageWriter;

import java.io.File;
import java.io.IOException;

public class ReceiveFileTransaction extends FileTransaction
{
    //------------------------------------------------------------------------------------------------------------------
    public ReceiveFileTransaction(String sessionId, String transactionID, String transactionCommand, String fileName, boolean runInBackground, ControlConnection controlConnection, ConnectionFactory connectionFactory) throws IOException
    {
        super(sessionId, transactionID, transactionCommand, fileName, runInBackground, controlConnection, connectionFactory);
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void executeTransfer()
    {
        File file = new File(fileName);
        dataConnection.receiveData(file);
    }
}
