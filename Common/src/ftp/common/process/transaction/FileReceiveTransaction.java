package ftp.common.process.transaction;

import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.util.MessageWriter;

import java.io.File;
import java.io.FileOutputStream;

public class FileReceiveTransaction extends Transaction
{
    //------------------------------------------------------------------------------------------------------------------
    public FileReceiveTransaction(String transactionID, String fileName, ControlConnection controlConnection, DataConnection dataConnection)
    {
        super(transactionID, fileName, controlConnection, dataConnection);
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void executeTransfer()
    {
        File file = new File(fileName);
        FileOutputStream fileOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(file);

            dataConnection.receiveData(fileOutputStream);

            fileOutputStream.close();
            success = true;
        }
        catch (Exception exception)
        {
            MessageWriter.writeError("Could not successfully receive file: " + fileName, exception);
        }
        afterTransfer();
    }
}
