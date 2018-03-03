package ftp.common.process.transaction;

import ftp.common.Codes;
import ftp.common.application.Config;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.util.MessageWriter;

public abstract class Transaction implements Runnable
{
    protected String id;
    protected ControlConnection controlConnection;
    protected DataConnection dataConnection;
    protected String fileName;

    protected boolean success = false;

    protected TransactionManager transactionManager = TransactionManager.getInstance();

    //------------------------------------------------------------------------------------------------------------------
    public Transaction(String id, String fileName, ControlConnection controlConnection, DataConnection dataConnection)
    {
        this.id = id;
        this.fileName = fileName;
        this.controlConnection = controlConnection;
        this.dataConnection = dataConnection;
    }

    //------------------------------------------------------------------------------------------------------------------
    public String getFileName()
    {
        return fileName;
    }

    //------------------------------------------------------------------------------------------------------------------
    public String getId()
    {
        return id;
    }

    final public void run()
    {
        if (beforeTransfer())
        {
            executeTransfer();
            afterTransfer();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    protected boolean beforeTransfer()
    {
        return transactionManager.addTransaction(this);
    }

    //------------------------------------------------------------------------------------------------------------------
    abstract protected void executeTransfer();

    //------------------------------------------------------------------------------------------------------------------
    protected void afterTransfer()
    {
        String response = null;
        if (success)
        {
            if (Config.getEnvironmentType().equals(Config.Environment.SERVER))
            {
                response = Codes.R_200 + "File transfer completed successfully - " + fileName;
                controlConnection.sendMessage(response.toString());
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
        else
        {
            if (Config.getEnvironmentType().equals(Config.Environment.SERVER))
            {
                response = Codes.R_400 + "File transfer not completed successfully - " + fileName;
                controlConnection.sendMessage(response.toString());
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
        transactionManager.removeTransaction(this);

    }

    //------------------------------------------------------------------------------------------------------------------
    public void stop()
    {
        dataConnection.stopDataTransfer();
        transactionManager.removeTransaction(this);
    }
}
