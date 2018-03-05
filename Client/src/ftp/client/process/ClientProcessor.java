package ftp.client.process;

import ftp.client.net.ClientConnectionFactory;
import ftp.common.Codes;
import ftp.common.Commands;
import ftp.common.process.Processor;
import ftp.common.process.transaction.FileTransaction;
import ftp.common.process.transaction.ReceiveFileTransaction;
import ftp.common.process.transaction.SendFileTransaction;
import ftp.common.process.transaction.TransactionManager;
import ftp.common.util.InputParser;
import ftp.common.util.MessageWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientProcessor extends Processor
{
    //------------------------------------------------------------------------------------------------------------------
    public ClientProcessor() throws IOException, ClassNotFoundException, IllegalAccessException
    {
        super();

        controlConnection = ClientConnectionFactory.getInstance().getControlConnection();

        sessionId = controlConnection.receiveCode();

        MessageWriter.writeMessage("Connected to: " + controlConnection.getRemoteHostName() + ":" + controlConnection.getRemoteHostControlPort());
    }

    //------------------------------------------------------------------------------------------------------------------
    public void run()
    {
        String request;

        try
        {
            running = true;
            do
            {
                request = readInput("Command > ").trim();

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
                                execute_RETR(argument, suffix);
                                break;
                            case Commands.STOR:
                                execute_STOR(argument, suffix);
                                break;
                            case Commands.TERM:
                                execute_TERM(argument);
                                break;
                            case Commands.QUIT:
                                controlConnection.sendMessage(Commands.QUIT);
                                running = false;
                                break;
                            default:
                                execute_ServerCommand(request);
                        }
                    }
                }
            }
            while (running);
            controlConnection.close();
        }
        catch (IOException exception)
        {
            MessageWriter.writeError("ClientControlConnection terminated by remote host : " + controlConnection.getRemoteHostName() + ":" + controlConnection.getRemoteHostControlPort(), exception);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_RETR(String fileName, String suffix) throws IOException
    {

        if (fileName.equals(""))
        {
            MessageWriter.writeMessage("Please specify File Name");
        }
        else if (fileName.startsWith(".") || fileName.startsWith("/") || fileName.startsWith("\\"))
        {
            MessageWriter.writeMessage("Invalid File Name: " + fileName);
        }
        else
        {
            File file = new File(fileName);

            if (file.exists())
            {
                MessageWriter.writeMessage("File already exists: " + fileName);
            }
            else
            {
                controlConnection.sendMessage(Commands.RETR + " " + fileName + suffix);
                String response = controlConnection.receiveMessage();

                if (response.startsWith(Codes.R_100))
                {
                    String transactionID = controlConnection.receiveCode();

                    FileTransaction fileTransaction = new ReceiveFileTransaction(sessionId, transactionID, Commands.RETR, fileName, suffix.equals(Commands.COMMAND_SUFFIX), controlConnection, ClientConnectionFactory.getInstance());
                    TransactionManager.getInstance().execute(fileTransaction);
                }
                else
                {
                    MessageWriter.writeMessage(response);
                }

            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_STOR(String fileName, String suffix) throws IOException
    {
        if (fileName.equals(""))
        {
            MessageWriter.writeMessage("Please specify File Name");
        }
        else if (fileName.startsWith(".") || fileName.startsWith("/"))
        {
            MessageWriter.writeMessage("Invalid File Name: " + fileName);
        }
        else
        {
            File file = new File(fileName);

            if (!file.exists())
            {
                MessageWriter.writeMessage("File does not exists: " + fileName);
            }
            else
            {
                controlConnection.sendMessage(Commands.STOR + " " + fileName + suffix);
                String response = controlConnection.receiveMessage();

                if (response.startsWith(Codes.R_100))
                {
                    String transactionID = controlConnection.receiveCode();

                    FileTransaction fileTransaction = new SendFileTransaction(sessionId, transactionID, Commands.STOR, fileName, suffix.equals(Commands.COMMAND_SUFFIX), controlConnection, ClientConnectionFactory.getInstance());
                    TransactionManager.getInstance().execute(fileTransaction);
                }
                else
                {
                    MessageWriter.writeMessage(response);
                }
            }
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    private void execute_TERM(String argument)
    {
        FileTransaction fileTransaction = TransactionManager.getInstance().getTransactionByID(argument);
        if (fileTransaction != null)
        {
            controlConnection.sendMessage(Commands.TERM + " " + argument);
            fileTransaction.stop();
        }
        else
        {
            MessageWriter.writeMessage("No fileTransaction in progress with the specified id: " + argument);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_ServerCommand(String request) throws IOException
    {
        controlConnection.sendMessage(request);
        String response = controlConnection.receiveMessage();
        MessageWriter.writeMessage(new String[]{response});
    }

    //------------------------------------------------------------------------------------------------------------------
    private String readInput(String promptMessage) throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(promptMessage);
        return in.readLine();
    }

}
