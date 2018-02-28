package ftp.server;

import java.net.ServerSocket;
import java.net.Socket;


import ftp.common.application.Config;
import ftp.common.util.Utils;
import ftp.server.net.ServerConnection;

public class FTPServer
{
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            Utils.writeMessage("Usage: java FTPServer <command control port number> <command terminate port number>" );
        }
        else
        {

            Config.initialize(Config.Environment.SERVER);
            Config.getInstance();

            String message = null;
            int commandControlPortNumber = 0;
            try
            {
                message = "Invalid command control port number: " + args[0];
                commandControlPortNumber = Integer.parseInt(args[0]);

            }
            catch(NumberFormatException exception)
            {
                Utils.writeError(message, exception);
                System.exit(1);
            }

            int commandTerminatePortNumber = 0;
            try
            {
                message = "Invalid command terminate port number: " + args[1];
                commandTerminatePortNumber = Integer.parseInt(args[1]);

            }
            catch(NumberFormatException exception)
            {
                Utils.writeError(message, exception);
                System.exit(1);
            }

            if (commandControlPortNumber == commandTerminatePortNumber)
            {
                Utils.writeMessage("Command terminate port number must be different than Command control port number ");
                System.exit(1);
            }
            else if (commandTerminatePortNumber == commandControlPortNumber -1)
            {
                Utils.writeMessage(new String[]{"By convention Data connections are establised on Command Control port -1 ","Command terminate port number must be different than Data port number "});
                System.exit(1);
            }

            try
            {

                message = "Server cannot listen on specified control port: " + commandControlPortNumber;
                ServerSocket controlServerSocket = new ServerSocket(commandControlPortNumber);

                message = "Server cannot listen on specified terminate port: " + commandTerminatePortNumber;
                ServerSocket terminateServerSocket = new ServerSocket(commandTerminatePortNumber);


                message = "Server cannot listen on data port: " + (commandControlPortNumber -1);
                ServerSocket dataServerSocket = new ServerSocket(commandControlPortNumber - 1);

                while (true)
                {
                    Utils.writeMessage("Server Waiting for Client ServerConnection on port: " + controlServerSocket.getLocalPort());
                    Socket controlSocket = controlServerSocket.accept();

                    createWorkerThread(controlSocket, dataServerSocket);
                }

            }
            catch (Exception exception)
            {
                Utils.writeError(message, exception);
            }
        }
    }


    private static void createWorkerThread(Socket controlSocket, ServerSocket dataServerSocket)
    {
        String message = null;

        try
        {
            message = "Cannot Establish Communication with remote host";

            ServerConnection connection = new ServerConnection(controlSocket, dataServerSocket);

            Utils.writeMessage("ServerConnection accepted from: " + connection.getRemoteHostName() + ":" + connection.getLocalHostCommandPort());

            message = "Runtime Error. Cannot Execute";

            new Thread(new ServerProcessor(connection)).start();

            message = "Runtime Error. Connections not closed correctly";

        }
        catch (Exception exception)
        {
            Utils.writeError(message, exception);
        }
    }

}

