package ftp.common.process.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TransactionManager
{
    private static TransactionManager instance;

    private Map transactionById = new HashMap();
    private Map transactionByFileName = new HashMap();

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

    //------------------------------------------------------------------------------------------------------------------
    synchronized public boolean addTransaction(FileTransaction fileTransaction)
    {
        boolean added = false;

        String id = fileTransaction.getTransactionId();
        String fileName = fileTransaction.getFileName();

        if (!transactionByFileName.containsKey(fileName))
        {
            transactionByFileName.put(fileName, fileTransaction);
            transactionById.put(id, fileTransaction);
            added = true;
        }
        return added;
    }

    //------------------------------------------------------------------------------------------------------------------
    synchronized public void removeTransaction(FileTransaction fileTransaction)
    {
        transactionById.remove(fileTransaction.getTransactionId());
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
