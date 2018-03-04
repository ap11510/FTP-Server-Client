package ftp.client.process;

import ftp.client.net.ClientConnectionFactory;
import ftp.common.*;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.process.transaction.FileReceiveTransaction;
import ftp.common.process.transaction.FileSendTransaction;
import ftp.common.process.transaction.Transaction;
import ftp.common.process.Processor;
import ftp.common.util.InputParser;
import ftp.common.util.MessageWriter;

import java.io.*;

public class ClientProcessor extends Processor
{
    //------------------------------------------------------------------------------------------------------------------
    public ClientProcessor(ControlConnection controlConnection) throws IOException, ClassNotFoundException, IllegalAccessException
    {
        super(controlConnection);
    }

    //------------------------------------------------------------------------------------------------------------------
    public void run()
    {
        String request;
        String response;

        try
        {
            running = true;
            do
            {
                request = readInput("Command > ").trim();

                response = null;
                if (!request.equals(""))
                {
                    String[] requestArguments = InputParser.parseRequest(request);

                    String command = requestArguments[0];
                    String argument = requestArguments[1];
                    String suffix = requestArguments[2];

                    if (!commandList.contains(command))
                    {
                        MessageWriter.writeMessage(new String[]{
                                "Invalid Command",
                                command});
                    }
                    else
                    {

                        switch (command)
                        {
                            case Commands.RETR:
                                response = execute_RETR(argument, suffix);
                                break;
                            case Commands.STOR:
                                response = execute_STOR(argument, suffix);
                                break;
                            case Commands.QUIT:
                                controlConnection.sendMessage(Commands.QUIT);
                                running = false;
                                break;
                            default:
                                response = execute_ServerCommand(request);
                        }
                        if (response != null)
                        {
                            MessageWriter.writeMessage(new String[]{response});
                        }
                    }
                }
            }
            while (running);
        }
        catch (IOException exception)
        {
            MessageWriter.writeError("ClientControlConnection terminated by remote host : " + controlConnection.getRemoteHostName() + ":" + controlConnection.getRemoteHostCommandPort(), exception);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_RETR(String fileName, String suffix) throws IOException
    {
        String response = null;

        if (fileName.equals(""))
        {
            response = "Please specify File Name";
        }
        else if (fileName.startsWith(".") || fileName.startsWith("/") || fileName.startsWith("\\"))
        {
            response = "Invalid File Name: " + fileName;
        }
        else
        {
            File file = new File(fileName);

            //if (file.exists())
            //{
            //    response = "File already exists: " +fileName;
            //}

            if(true)
            {
                response = execute_ServerCommand(Commands.RETR + " " + fileName + suffix);

                if (response.startsWith(Codes.R_100))
                {
                    String transactionID = controlConnection.receiveMessage();

                    DataConnection dataConnection = ClientConnectionFactory.getInstance().getDataConnection();

                    Transaction transaction = new FileReceiveTransaction(transactionID, fileName, controlConnection, dataConnection);
                    transferFile(transaction, suffix.equals(Commands.COMMAND_SUFFIX));
                    response = null;
                }
            }
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_STOR(String fileName, String suffix) throws IOException
    {
        String response = null;

        if (fileName.equals(""))
        {
            response = "Please specify File Name";
        }
        else if (fileName.startsWith(".") || fileName.startsWith("/"))
        {
            response = "Invalid File Name: " + fileName;
        }
        else
        {
            File file = new File(fileName);

            if (!file.exists())
            {
                response = "File does not exists: " +fileName;
            }
            else
            {
                response = execute_ServerCommand(Commands.STOR + " " + fileName + suffix);
                if (response.startsWith(Codes.R_100))
                {
                    DataConnection dataConnection = ClientConnectionFactory.getInstance().getDataConnection();


                    String transactionID = controlConnection.receiveMessage();


                    Transaction transaction = new FileSendTransaction(transactionID, fileName, controlConnection, dataConnection);
                    transferFile(transaction, suffix.equals(Commands.COMMAND_SUFFIX));
                    response = null;
                }

            }
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_ServerCommand(String request) throws IOException
    {
        controlConnection.sendMessage(request);
        return controlConnection.receiveMessage();
    }

    //------------------------------------------------------------------------------------------------------------------
    private String readInput(String promptMessage) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(promptMessage);
        return in.readLine();
    }

    //------------------------------------------------------------------------------------------------------------------
    private void transferFile(Transaction transaction, boolean runInBackground)
    {
        String response = null;
        if (runInBackground)
        {
            MessageWriter.writeMessage("To terminate transferring file: " + transaction.getFileName() + "\nUse command : " + Commands.TERM + " " + transaction.getId());
            Thread thread = new Thread(transaction);
            thread.start();
        }
        else
        {
            transaction.run();
        }
    }

}
