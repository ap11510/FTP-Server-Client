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
    synchronized public boolean addTransaction(Transaction transaction)
    {
        boolean added = false;

        String id = transaction.getId();
        String fileName = transaction.getFileName();

        if (!transactionByFileName.containsKey(fileName))
        {
            transactionByFileName.put(fileName, transaction);
            transactionById.put(id, transaction);
            added = true;
        }
        return added;
    }

    //------------------------------------------------------------------------------------------------------------------
    synchronized public void removeTransaction(Transaction transaction)
    {
        transactionById.remove(transaction.getId());
        transactionByFileName.remove(transaction.getFileName());
    }

    //------------------------------------------------------------------------------------------------------------------
    public Transaction getTransactionByFileName(String fileName)
    {
        return (Transaction) transactionByFileName.get(fileName);
    }

    //------------------------------------------------------------------------------------------------------------------
    public Transaction getTransactionByID(String id)
    {
        return (Transaction) transactionById.get(id);
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
