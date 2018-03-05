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
    protected String id;
    protected ControlConnection controlConnection;
    protected DataConnection dataConnection;
    protected String fileName;
    protected String command;
    protected boolean runInBackground;

    //------------------------------------------------------------------------------------------------------------------
    protected FileTransaction(String sessionId, String id, String command, String fileName, boolean runInBackground, ControlConnection controlConnection, ConnectionFactory connectionFactory) throws IOException
    {
        this.sessionId = sessionId;
        this.id = id;
        this.command = command;
        this.fileName = fileName;
        this.controlConnection = controlConnection;
        this.dataConnection = connectionFactory.getDataConnection();
        this.runInBackground = runInBackground;
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
        executeTransfer();
        afterTransfer();
        TransactionManager.getInstance().removeTransaction(this);
        dataConnection.close();
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

    //------------------------------------------------------------------------------------------------------------------
    public boolean isRunInBackground()
    {
        return runInBackground;
    }

    //------------------------------------------------------------------------------------------------------------------
    public ControlConnection getControlConnection()
    {
        return controlConnection;
    }

    public DataConnection getDataConnection()
    {
        return dataConnection;
    }
}
