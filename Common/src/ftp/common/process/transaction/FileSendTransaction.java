package ftp.common.process.transaction;

import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.util.MessageWriter;

import java.io.File;
import java.io.FileInputStream;

public class FileSendTransaction extends Transaction
{
    //------------------------------------------------------------------------------------------------------------------
    public FileSendTransaction(String transactionID, String fileName, ControlConnection controlConnection, DataConnection dataConnection)
    {
        super(transactionID, fileName, controlConnection, dataConnection);
    }

    //------------------------------------------------------------------------------------------------------------------
    public void executeTransfer()
    {
        try
        {
            File file = new File(fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            dataConnection.sendData(file.length(), fileInputStream);

            fileInputStream.close();

            success = true;
        }
        catch (Exception exception)
        {
            MessageWriter.writeError("Could not successfully send file: " + fileName, exception);
        }
        afterTransfer();
        transactionManager.removeTransaction(this);
    }
}
