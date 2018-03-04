package ftp.common.process.transaction;

import ftp.common.Codes;
import ftp.common.application.Config;
import ftp.common.net.ConnectionFactory;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.util.MessageWriter;

import java.io.IOException;

public abstract class FileTransaction implements Runnable
{
    protected String sessionId;
    protected String transactionId;
    protected ControlConnection controlConnection;
    protected DataConnection dataConnection;
    protected String fileName;

    protected TransactionManager transactionManager = TransactionManager.getInstance();

    //------------------------------------------------------------------------------------------------------------------
    protected FileTransaction(String sessionId, String transactionId, String fileName, ControlConnection controlConnection, ConnectionFactory connectionFactory) throws IOException
    {
        this.sessionId = sessionId;
        this.transactionId = transactionId;
        this.fileName = fileName;
        this.controlConnection = controlConnection;
        this.dataConnection = connectionFactory.getDataConnection();
    }


    //------------------------------------------------------------------------------------------------------------------
    public String getFileName()
    {
        return fileName;
    }

    //------------------------------------------------------------------------------------------------------------------
    public String getTransactionId()
    {
        return transactionId;
    }

    final public void run()
    {
        if (beforeTransfer())
        {
            executeTransfer();
            afterTransfer();
        }
        else
        {
            MessageWriter.writeMessage("FileTransaction already in progess for file: " + fileName);
        }
        transactionManager.removeTransaction(this);
        dataConnection.close();
    }

    //------------------------------------------------------------------------------------------------------------------
    private boolean beforeTransfer()
    {
        return transactionManager.addTransaction(this);
    }

    //------------------------------------------------------------------------------------------------------------------
    abstract protected void executeTransfer();

    //------------------------------------------------------------------------------------------------------------------
    private void afterTransfer()
    {
        String response = null;

        if (Config.getEnvironmentType().equals(Config.Environment.SERVER))
        {
            if (dataConnection.isStopped())
            {
                controlConnection.sendMessage(Codes.R_200 + "Successfully terminated Transfer of file: " + fileName);
            }
            else
            {
                if (dataConnection.isSuccess())
                {
                    controlConnection.sendMessage(Codes.R_200 + "File transfer completed successfully - " + fileName);
                }
                else
                {
                    controlConnection.sendMessage(Codes.R_400 + "File transfer not completed successfully - " + fileName);
                }
            }
        }
        else
        {
            try
            {
                response = controlConnection.receiveMessage();
                MessageWriter.writeMessage(new String[]{response});
            }
            catch (Exception exception)
            {
                MessageWriter.writeError("Error While receiving message from server", exception);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public void stop()
    {
        dataConnection.stopDataTransfer();
    }
}
