package ftp.server.process;

import ftp.common.Codes;
import ftp.common.Commands;
import ftp.common.process.Processor;
import ftp.common.process.transaction.FileTransaction;
import ftp.common.process.transaction.ReceiveFileTransaction;
import ftp.common.process.transaction.SendFileTransaction;
import ftp.common.process.transaction.TransactionManager;
import ftp.common.util.InputParser;
import ftp.common.util.MessageWriter;
import ftp.server.net.ServerConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.UUID;


public class ServerProcessor extends Processor
{

    //------------------------------------------------------------------------------------------------------------------
    public ServerProcessor() throws Exception
    {
        super();
        controlConnection = ServerConnectionFactory.getInstance().getControlConnection();

        sessionId = UUID.randomUUID().toString();

        controlConnection.sendMessage(sessionId);

        MessageWriter.writeMessage("Connection accepted from: " + controlConnection.getRemoteHostName() + ":" + controlConnection.getLocalHostControlPort());
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
                request = controlConnection.receiveMessage();

                if (request.equals(""))
                {
                    running = false;
                }
                else
                {
                    request = request.trim();
                    if (!request.equals(""))
                    {
                        String[] requestArguments = InputParser.parseRequest(request);

                        String command = requestArguments[0];
                        String argument = requestArguments[1];
                        String suffix = requestArguments[2];

                        if (!commandList.contains(command))
                        {
                            controlConnection.sendMessage(Codes.R_500 + "Invalid Command");
                        }
                        else
                        {

                            switch (command)
                            {
                                case Commands.PWD:
                                    execute_PWD();
                                    break;
                                case Commands.LIST:
                                    execute_LIST();
                                    break;
                                case Commands.CWD:
                                    execute_CWD(argument);
                                    break;
                                case Commands.MKD:
                                    execute_MKD(argument);
                                    break;
                                case Commands.DELE:
                                    execute_DELE(argument);
                                    break;
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
                                    MessageWriter.writeMessage("Connection closed by: " + controlConnection.getRemoteHostName() + ":" + controlConnection.getLocalHostControlPort());
                                    running = false;
                                    break;
                                default:
                                    controlConnection.sendMessage(Codes.R_500 + "Command not Implemented");
                            }
                        }
                    }
                }
            }
            while (running);

            controlConnection.close();
        }
        catch (IOException exception)
        {
            MessageWriter.writeError("Communication Error. ControlConnection with remote host terminated.", exception);
        }
        running = false;
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_LIST()
    {
        File[] fileList = workingDirectory.listFiles();

        StringBuilder response = new StringBuilder();

        response.append(Codes.R_200);

        DecimalFormat formatter = new DecimalFormat();

        assert fileList != null;
        for (File file : fileList)
        {
            if (file.isFile())
            {
                response.append("File     : ");
            }
            else if (file.isDirectory())
            {
                response.append("Directory: ");
            }

            response.append(file.getName());

            response.append("\t\t");

            if (file.isFile())
            {
                response.append(formatter.format(file.length()));
                response.append(" bytes");
            }

            response.append("\n");

        }

        controlConnection.sendMessage(response.toString());
    }


    //------------------------------------------------------------------------------------------------------------------
    private void execute_PWD() throws IOException
    {
        controlConnection.sendMessage(Codes.R_200 + "Remote Host Working Directory: " + workingDirectory.getCanonicalPath());
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_CWD(String argument) throws IOException
    {
        if (argument.equals(""))
        {
            controlConnection.sendMessage(Codes.R_500 + "Improper Usage: Need either <Directory> or <..>");
        }
        else
        {
            String newDirectoryName;

            if (argument.startsWith("/") || argument.startsWith("\\"))
            {
                newDirectoryName = new File(argument).getCanonicalPath();
            }
            else if (argument.equals(".."))
            {
                Path parentPath = workingDirectory.toPath().getParent();

                if (parentPath != null)
                {
                    newDirectoryName = workingDirectory.toPath().getParent().toFile().getCanonicalPath();
                }
                else
                {
                    newDirectoryName = workingDirectory.getCanonicalPath();
                }

            }
            else if (argument.charAt(1) == ':' && System.getenv("OS").toUpperCase().startsWith("WINDOWS"))
            {
                newDirectoryName = new File(argument).getCanonicalPath();

            }
            else
            {
                newDirectoryName = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;
            }

            File newDirectory = new File(newDirectoryName);
            if (newDirectory.exists())
            {
                if (newDirectory.isDirectory())
                {
                    this.workingDirectory = newDirectory;
                    controlConnection.sendMessage(Codes.R_200 + " Remote Host Working Directory changed to: " + workingDirectory.getCanonicalPath());
                }
                else
                {
                    controlConnection.sendMessage(Codes.R_500 + " This is a file not a Directory: " + newDirectoryName);
                }
            }
            else
            {
                controlConnection.sendMessage(Codes.R_500 + " Directory doesn't exist");
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_MKD(String argument) throws IOException
    {
        if (argument.equals(""))
        {
            controlConnection.sendMessage(Codes.R_500 + " Invalid argument, missing directory name");
        }
        else if (argument.startsWith(".") || argument.startsWith("/"))
        {
            controlConnection.sendMessage(Codes.R_500 + " Invalid argument: " + argument);
        }
        else
        {

            String name = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;

            File file = new File(name);


            if (file.exists())
            {
                if (file.isDirectory())
                {
                    controlConnection.sendMessage(Codes.R_500 + " Directory already exists" + name);
                }
                else
                {
                    controlConnection.sendMessage(Codes.R_500 + " File with the same name already exists" + name);
                }
            }
            else
            {
                try
                {
                    if (file.mkdir())
                    {
                        controlConnection.sendMessage(Codes.R_200 + " Directory was created successfully : " + name);
                    }
                    else
                    {
                        controlConnection.sendMessage(Codes.R_500 + " Directory was not created successfully : " + name);
                    }

                }
                catch (SecurityException exception)
                {
                    controlConnection.sendMessage(Codes.R_500 + " You don't have permissions to create directory : " + name);
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_DELE(String argument) throws IOException
    {
        if (argument.equals(""))
        {
            controlConnection.sendMessage(Codes.R_500 + " Improper Usage: Command is delete <remote_file_name>");
        }
        else
        {
            String name = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;

            File file = new File(name);

            if (!file.isDirectory())
            {
                if (file.exists())
                {
                    try
                    {
                        if (file.delete())
                        {
                            controlConnection.sendMessage(Codes.R_200 + " File was deleted successfully: " + name);
                        }
                        else
                        {
                            controlConnection.sendMessage(Codes.R_500 + " File was not deleted successfully: " + name);
                        }

                    }
                    catch (SecurityException exception)
                    {
                        controlConnection.sendMessage(Codes.R_500 + " You don't have permissions to delete File: " + name);
                    }
                }
                else
                {
                    controlConnection.sendMessage(Codes.R_500 + " File does not exist: " + name);
                }
            }
            else
            {
                controlConnection.sendMessage(Codes.R_500 + " Cannot delete a directory : " + name + "\n Use command: " + Commands.RMD);
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_RETR(String argument, String suffix) throws IOException
    {
        String fileName = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;

        File file = new File(fileName);

        if (!file.exists())
        {
            controlConnection.sendMessage(Codes.R_400 + " File does not exist on remote host: " + fileName);
        }
        else
        {
            controlConnection.sendMessage(Codes.R_100);

            String transactionId = TransactionManager.getInstance().generateId();
            controlConnection.sendMessage(transactionId);

            FileTransaction fileTransaction = new SendFileTransaction(sessionId, transactionId, Commands.RETR, fileName, suffix.equals(Commands.COMMAND_SUFFIX), controlConnection, ServerConnectionFactory.getInstance());
            TransactionManager.getInstance().execute(fileTransaction);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_STOR(String argument, String suffix) throws IOException
    {
        String fileName = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;

        File file = new File(fileName);

        if (file.exists())
        {
            controlConnection.sendMessage(Codes.R_400 + "File already exists on remote host: " + fileName);
        }
        else
        {
            controlConnection.sendMessage(Codes.R_100);

            String transactionId = TransactionManager.getInstance().generateId();
            controlConnection.sendMessage(transactionId);

            FileTransaction fileTransaction = new ReceiveFileTransaction(sessionId, transactionId, Commands.STOR, fileName, suffix.equals(Commands.COMMAND_SUFFIX), controlConnection, ServerConnectionFactory.getInstance());
            TransactionManager.getInstance().execute(fileTransaction);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void execute_TERM(String argument)
    {
        FileTransaction fileTransaction = TransactionManager.getInstance().getTransactionByID(argument);
        if (fileTransaction != null)
        {
            fileTransaction.stop();
        }
        else
        {
            controlConnection.sendMessage("No fileTransaction in progress with the specified id: " + argument);
        }
    }
}

