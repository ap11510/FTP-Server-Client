package ftp.common.process.transaction;

import ftp.common.Codes;
import ftp.common.Commands;
import ftp.common.application.Config;
import ftp.common.util.MessageWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TransactionManager
{
    private static TransactionManager instance;

    private Map <String, FileTransaction> transactionById = new HashMap() ;
    private Map <String, FileTransaction> transactionByFileName = new HashMap();

    private Random generator = new Random();

    //------------------------------------------------------------------------------------------------------------------
    public static TransactionManager getInstance()
    {
        if (instance == null)
        {
            instance = new TransactionManager();
        }
        return instance;
    }

    public void execute(FileTransaction fileTransaction)
    {
        if (addTransaction(fileTransaction))
        {
            if (fileTransaction.isRunInBackground())
            {
                if (Config.getEnvironmentType().equals(Config.Environment.CLIENT))
                {
                    MessageWriter.writeMessage("To terminate transferring file: " + fileTransaction.getFileName() + "\nUse command : " + Commands.TERM + " " + fileTransaction.getId());
                }
                Thread thread = new Thread(fileTransaction);
                thread.start();
            }
            else
            {
                fileTransaction.run();
            }
        }
        else
        {
            fileTransaction.getDataConnection().close();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    synchronized public boolean addTransaction(FileTransaction fileTransaction)
    {
        boolean added = false;

        String id = fileTransaction.getId();
        String fileName = fileTransaction.getFileName();

        FileTransaction runningFileTransaction = transactionByFileName.get(fileName);

        String message = null;
        if (runningFileTransaction == null)
        {
            transactionByFileName.put(fileName, fileTransaction);
            transactionById.put(id, fileTransaction);
            added = true;
        }
        else
        {
            if (fileTransaction.command.equals(Commands.STOR))
            {
//  another transaction either PUT or GET is in progress on the same file; that is not allowed
                message = "Operation not allowed. Another " + Commands.STOR + "/" + Commands.RETR + " transaction is in progress for file: " + fileTransaction.getFileName();
            }
            else
            {
                if (runningFileTransaction.command.equals(Commands.STOR))
                {
// another transaction is writing the file; until that is finished any read attempt should be blocked since it would produce unpredictable results
                    message = "Operation not allowed. Another " + Commands.STOR + " transaction is in progress for file: " + fileTransaction.getFileName();
                }
                else
                if (runningFileTransaction.id.equals(fileTransaction.id))
                {
// there is already a GET operation on that file from the same client;
                    message = "Operation not allowed. Another " + Commands.RETR + " transaction is in progress from the same client for file: " + fileTransaction.getFileName();
                }
                else
                {
                    transactionByFileName.put(fileName, fileTransaction);
                    transactionById.put(id, fileTransaction);
                    added = true;
                }
            }
        }
        if (!added)
        {
            if (Config.getEnvironmentType().equals(Config.Environment.SERVER))
            {
                fileTransaction.getControlConnection().sendMessage(Codes.R_400 + message);
            }
            else
            {
                MessageWriter.writeMessage(message);
            }
        }
        return added;
    }

    //------------------------------------------------------------------------------------------------------------------
    synchronized public void removeTransaction(FileTransaction fileTransaction)
    {
        transactionById.remove(fileTransaction.getId());
        transactionByFileName.remove(fileTransaction.getFileName());
    }

    //------------------------------------------------------------------------------------------------------------------
    public FileTransaction getTransactionByFileName(String fileName)
    {
        return (FileTransaction) transactionByFileName.get(fileName);
    }

    //------------------------------------------------------------------------------------------------------------------
    public FileTransaction getTransactionByID(String id)
    {
        return (FileTransaction) transactionById.get(id);
    }

    //------------------------------------------------------------------------------------------------------------------
    synchronized public String generateId()
    {
        int number = 0;
        String id = null;
        do
        {
            number = generator.nextInt(16384);
            id = Integer.toString(number);
        }
        while (transactionById.containsKey(id));

        return id;

    }

}
