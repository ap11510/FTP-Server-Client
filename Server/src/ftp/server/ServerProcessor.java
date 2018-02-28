package ftp.server;

import java.io.*;
import java.nio.file.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import ftp.common.Processor;
import ftp.common.util.Utils;
import ftp.common.Commands;
import ftp.common.Codes;
import ftp.server.net.ServerConnection;

public class ServerProcessor extends Processor implements Runnable
{

    ServerProcessor(ServerConnection connection) throws IOException, ClassNotFoundException, IllegalAccessException
    {
        super(connection);
    }

    /**
     *
     */
    public void run()
    {
        String request;

        try
        {
            boolean running =  true;

            do
            {
                request = connection.receiveMessage();

                if (request == null)
                {
                    running = false;
                }
                else
                {
                    request = request.trim();
                    if (!request.equals(""))
                    {
                        String[] requestArguments = Utils.parseRequest(request);

                        String command = requestArguments[0];

                        String argument = requestArguments[1];

                        if (!commandList.contains(command))
                        {
                            connection.sendMessage(Codes.R_500 + "Invalid Command");
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
                                    execute_RETR(argument);
                                    break;
                                case Commands.STOR:
                                    execute_STOR(argument);
                                    break;

                                case Commands.QUIT:
                                    execute_QUIT();
                                    running = false;
                                    break;
                                default:
                                    connection.sendMessage(Codes.R_500 + "Command not Implemented");
                            }
                        }
                    }
                }
            }
            while (running);

            connection.close();
        }
        catch (IOException exception)
        {
            Utils.writeError("Communication Error. Connection with remote host terminated.", exception);
        }
    }

    /**
     * Send the Directory/File name list in the working directory
     *
     */
    private void execute_LIST()
    {
        File[] fileList = workingDirectory.listFiles();

        StringBuffer response = new StringBuffer();

        response.append(Codes.R_200);

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
        connection.sendMessage(response.toString());
    }


    /**
     * Get the name of the working directory
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_PWD() throws IOException
    {
        connection.sendMessage(
                    (new StringBuffer(Codes.R_200)
                    .append("Remote Host Working Directory: ")
                    .append(workingDirectory.getCanonicalPath())
                    .toString()));
    }

    /**
     * Change working directory
     *
     * @param argument - directory name
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_CWD(String argument) throws IOException
    {
        String response;
        if(argument.equals(""))
        {
            response = (Codes.R_500 + "Improper Usage: Need either <Directory> or <..>");
        }
        else
        {
            String newDirectoryName = null;

            if(argument.startsWith("/"))
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
            {
                newDirectoryName = (new StringBuffer()).append(workingDirectory.getCanonicalPath()).append(workingDirectory.toPath().getFileSystem().getSeparator()).append(argument).toString();
            }

            File newDirectory = new File(newDirectoryName);
            if (newDirectory.exists())
            {
                if (newDirectory.isDirectory())
                {
                    this.workingDirectory = newDirectory;
                    response =
                            (new StringBuffer(Codes.R_200))
                            .append("Remote Host Working Directory changed to: ")
                            .append(workingDirectory.getCanonicalPath())
                            .toString();
                }
                else
                {
                    response =
                            (new StringBuffer(Codes.R_500))
                            .append(newDirectoryName.toString())
                            .append(" - This is a file not a Directory")
                            .toString();
                }
            }
            else
            {
                response =
                        (new StringBuffer(Codes.R_500))
                        .append("Directory doesn't exist")
                        .toString();
             }
        }
        connection.sendMessage(response);
    }

    /**
     * Create Directory
     *
     * @param argument - Name of Directory to be created
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_MKD(String argument) throws IOException {
        String response;

        if (argument.equals(""))
        {
            response =
                    (new StringBuffer(Codes.R_500))
                            .append("Invalid argument, missing directory name")
                            .toString();
        }
        else if (argument.startsWith(".") || argument.startsWith("/"))
        {
            response =
                    (new StringBuffer(Codes.R_500))
                            .append("Invalid argument: ")
                            .append(argument)
                            .toString();
        } else
            {

            String name = new StringBuffer(workingDirectory.getCanonicalPath()).append(workingDirectory.toPath().getFileSystem().getSeparator()).append(argument).toString();

            File file = new File(name);


            if (file.exists()) {
                if (file.isDirectory())
                {
                    response =
                            (new StringBuffer(Codes.R_500))
                            .append("Directory already exists")
                            .append(name)
                            .toString();
                }
                else
                {
                    response =
                            (new StringBuffer(Codes.R_500))
                            .append("File with the same name already exists")
                            .append(name)
                            .toString();
                }
            }
            else
            {
                try
                {
                    if (file.mkdir())
                    {
                        response =
                                (new StringBuffer(Codes.R_200))
                                .append("Directory was created successfully : ")
                                .append(name)
                                .toString();
                    }
                    else
                    {
                        response =
                                (new StringBuffer(Codes.R_500))
                                .append("Directory was not created successfully : ")
                                .append(name)
                                .toString();
                    }

                } catch (SecurityException exception) {
                    response =
                            (new StringBuffer(Codes.R_500))
                                    .append("You don't have permissions to create directory : ")
                                    .append(name)
                                    .toString();
                }
            }
        }
        connection.sendMessage(response);
    }

    /**
     * Delete file
     *
     * @param argument - file name
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_DELE(String argument) throws IOException
    {
        String response;
        if(argument.equals(""))
        {
            response =
                    (new StringBuffer(Codes.R_500))
                            .append("Improper Usage: Command is delete <remote_file_name>")
                            .toString();
        }
        else
        {
            String name = (new StringBuffer(workingDirectory.getCanonicalPath())).append(workingDirectory.toPath().getFileSystem().getSeparator()).append(argument).toString();

            File file = new File(name);

            if(!file.isDirectory())
            {
                if(file.exists())
                {
                    try
                    {
                        if (file.delete())
                        {
                            response =
                                    (new StringBuffer(Codes.R_200))
                                            .append("File was deleted successfully: ")
                                            .append(name)
                                            .toString();

                        }
                        else
                        {
                            response =
                            (new StringBuffer(Codes.R_500))
                                    .append("File was not deleted successfully: ")
                                    .append(name)
                                    .toString();
                        }

                    }
                    catch (SecurityException exception)
                    {
                        response =
                                (new StringBuffer(Codes.R_500))
                                        .append("You don't have permissions to delete File: ")
                                        .append(name)
                                        .toString();
                    }
                }
                else
                {
                    response =
                            (new StringBuffer(Codes.R_500))
                                    .append("File does not exist: ")
                                    .append(name)
                                    .toString();
                }
            }
            else
            {
                response =
                        (new StringBuffer(Codes.R_500))
                                .append("Cannot delete a directory : ")
                                .append(name)
                                .append("\n Use command: ")
                                .append(Commands.RMD)
                                .toString();
            }
        }
        connection.sendMessage(response);
    }

    /**
     * Get the name of the working directory
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_RETR(String argument) throws IOException
    {
        String response;
        String name = (new StringBuffer(workingDirectory.getCanonicalPath())).append(workingDirectory.toPath().getFileSystem().getSeparator()).append(argument).toString();

        File file = new File(name);

        if (!file.exists())
        {
            response =
                    (new StringBuffer(Codes.R_400))
                            .append("File does not exist on remote host: ")
                            .append(name)
                            .toString();
            connection.sendMessage(response);
        }
        else
        {
            response = Codes.R_100;
            connection.sendMessage(response);

            FileInputStream is = null;

            try
            {
                is = new FileInputStream(file);

                connection.sendData(file.length(), is);

                is.close();
            }
            finally
            {
                is = null;
            }
            response = Codes.R_200 + "File transfer successfull";
            connection.sendMessage(response);

        }
    }

    /**
     * Get the name of the working directory
     *
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_STOR(String argument) throws IOException
    {
        String response;
        String name = (new StringBuffer(workingDirectory.getCanonicalPath())).append(workingDirectory.toPath().getFileSystem().getSeparator()).append(argument).toString();

        File file = new File(name);

        if (file.exists())
        {
            response =
                    (new StringBuffer(Codes.R_400))
                            .append("File already exists on remote host: ")
                            .append(name)
                            .toString();
            connection.sendMessage(response);
        }
        else
        {
            response = Codes.R_100;
            connection.sendMessage(response);

            FileOutputStream os = null;

            try
            {
                os = new FileOutputStream(file);

                connection.receiveData(os);

                os.close();
            }
            finally
            {
                os = null;
            }
            response = Codes.R_200 + "File transfer successfull";
            connection.sendMessage(response);
        }
    }



    /**
     * Terminates a client session
     * @throws IOException - thrown when sending to remote host
     */
    private void execute_QUIT() throws IOException
    {
        connection.close();
        connection = null;
    }
}

