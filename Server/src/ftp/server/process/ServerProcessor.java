package ftp.server.process;

import ftp.common.Codes;
import ftp.common.Commands;
import ftp.common.net.ControlConnection;
import ftp.common.net.DataConnection;
import ftp.common.process.transaction.FileReceiveTransaction;
import ftp.common.process.transaction.FileSendTransaction;
import ftp.common.process.transaction.Transaction;
import ftp.common.process.Processor;
import ftp.common.process.transaction.TransactionManager;
import ftp.common.util.InputParser;
import ftp.common.util.MessageWriter;
import ftp.server.net.ServerConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public class ServerProcessor extends Processor
{

    //------------------------------------------------------------------------------------------------------------------
    public ServerProcessor(ControlConnection controlConnection) throws Exception
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
            running =  true;

            do
            {
                response = null;
                request = controlConnection.receiveMessage();

                if (request == null)
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
                                    response = execute_PWD();
                                    break;
                                case Commands.LIST:
                                    response = execute_LIST();
                                    break;
                                case Commands.CWD:
                                    response = execute_CWD(argument);
                                    break;
                                case Commands.MKD:
                                    response = execute_MKD(argument);
                                    break;
                                case Commands.DELE:
                                    response = execute_DELE(argument);
                                    break;
                                case Commands.RETR:
                                    response = execute_RETR(argument, suffix);
                                    break;
                                case Commands.STOR:
                                    response = execute_STOR(argument, suffix);
                                    break;
                                case Commands.QUIT:
                                    MessageWriter.writeMessage("Connection closed by: " + controlConnection.getRemoteHostName() + ":" + controlConnection.getLocalHostCommandPort());
                                    running = false;
                                    break;
                                default:
                                    response = Codes.R_500 + "Command not Implemented";
                            }
                            if (response != null)
                            {
                                controlConnection.sendMessage(response.toString());
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
    private String execute_LIST()
    {
        File[] fileList = workingDirectory.listFiles();

        StringBuilder response = new StringBuilder();

        response.append(Codes.R_200);

        assert fileList != null;
        for(File file: fileList)
        {
            if(file.isFile())
            {
                response.append("File     : ");
            }
            else if(file.isDirectory())
            {
                response.append("Directory: ");
            }

            response.append(file.getName());

            response.append("\n");

        }
        return response.toString();
    }


    //------------------------------------------------------------------------------------------------------------------
    private String execute_PWD() throws IOException
    {
        return (Codes.R_200 + "Remote Host Working Directory: " + workingDirectory.getCanonicalPath());
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_CWD(String argument) throws IOException
    {
        String response;
        if(argument.equals(""))
        {
            response = (Codes.R_500 + "Improper Usage: Need either <Directory> or <..>");
        }
        else
        {
            String newDirectoryName ;

            if(argument.startsWith("/") || argument.startsWith("\\"))
            {
                newDirectoryName = new File(argument).getCanonicalPath();
            }
            else
            if(argument.equals(".."))
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
            else
            if(argument.charAt(1) == ':' && System.getenv("OS").toUpperCase().startsWith("WINDOWS"))
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
                    response = Codes.R_200 + " Remote Host Working Directory changed to: " + workingDirectory.getCanonicalPath();
                }
                else
                {
                    response = Codes.R_500 + " This is a file not a Directory: " + newDirectoryName;
                }
            }
            else
            {
                response = Codes.R_500 + " Directory doesn't exist";
             }
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_MKD(String argument) throws IOException {
        String response;

        if (argument.equals(""))
        {
            response = Codes.R_500 + " Invalid argument, missing directory name";
        }
        else if (argument.startsWith(".") || argument.startsWith("/"))
        {
            response = Codes.R_500 + " Invalid argument: " + argument;
        } else
            {

            String name = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;

            File file = new File(name);


            if (file.exists()) {
                if (file.isDirectory())
                {
                    response = Codes.R_500 + " Directory already exists" + name ;
                }
                else
                {
                    response = Codes.R_500 + " File with the same name already exists" + name;
                }
            }
            else
            {
                try
                {
                    if (file.mkdir())
                    {
                        response = Codes.R_200 + " Directory was created successfully : " + name;
                    }
                    else
                    {
                        response = Codes.R_500 + " Directory was not created successfully : " + name;
                    }

                } catch (SecurityException exception) {
                    response = Codes.R_500 + " You don't have permissions to create directory : " + name;
                }
            }
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_DELE(String argument) throws IOException
    {
        String response;
        if(argument.equals(""))
        {
            response = Codes.R_500 + " Improper Usage: Command is delete <remote_file_name>";
        }
        else
        {
            String name = workingDirectory.getCanonicalPath()+ workingDirectory.toPath().getFileSystem().getSeparator() + argument;

            File file = new File(name);

            if(!file.isDirectory())
            {
                if(file.exists())
                {
                    try
                    {
                        if (file.delete())
                        {
                            response = Codes.R_200 + " File was deleted successfully: " + name;
                        }
                        else
                        {
                            response = Codes.R_500 + " File was not deleted successfully: " + name;
                        }

                    }
                    catch (SecurityException exception)
                    {
                        response = Codes.R_500 + " You don't have permissions to delete File: " + name;
                    }
                }
                else
                {
                    response = Codes.R_500 + " File does not exist: " + name;
                }
            }
            else
            {
                response = Codes.R_500 + " Cannot delete a directory : " + name + "\n Use command: " + Commands.RMD;
            }
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_RETR(String argument, String suffix) throws IOException
    {
        String response = null;
        String fileName = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;

        File file = new File(fileName);

        if (!file.exists())
        {
            response = Codes.R_400 + " File does not exist on remote host: " +fileName;
        }
        else
        {
            controlConnection.sendMessage(Codes.R_100);

            String id = TransactionManager.getInstance().generateId();

            controlConnection.sendMessage(id);

            DataConnection dataConnection = ServerConnectionFactory.getInstance().getDataConnection();

            Transaction transaction = new FileSendTransaction(id, fileName, controlConnection, dataConnection);


            transferFile(transaction, suffix.equals(Commands.COMMAND_SUFFIX));

            response = null;
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    private String execute_STOR(String argument, String suffix) throws IOException
    {
        String response = null;
        String fileName = workingDirectory.getCanonicalPath() + workingDirectory.toPath().getFileSystem().getSeparator() + argument;

        File file = new File(fileName);

        if (file.exists())
        {
            response = Codes.R_400 + "File already exists on remote host: " + fileName;
        }
        else
        {
            response = Codes.R_100;
            controlConnection.sendMessage(Codes.R_100);

            String id = TransactionManager.getInstance().generateId();

            DataConnection dataConnection = ServerConnectionFactory.getInstance().getDataConnection();

            Transaction transaction = new FileReceiveTransaction(id, fileName, controlConnection, dataConnection);

            controlConnection.sendMessage(id);

            transferFile(transaction, suffix.equals(Commands.COMMAND_SUFFIX));

            response = null;
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    private void transferFile(Transaction transaction, boolean runInBackground)
    {
        if (runInBackground)
        {
            Thread thread = new Thread(transaction);
            thread.start();
        }
        else
        {
            transaction.run();
        }
    }
}

