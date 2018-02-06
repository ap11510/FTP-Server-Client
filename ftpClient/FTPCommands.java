interface FTPCommands
{
    String CWD  = "CD";         //	RFC 697	Change working directory.
    String DELE = "DELETE";     //		Delete file.
    String LIST = "LS";         //		Returns information of a file or directory if specified, else information of the current working directory is returned.
    String MKD  = "MKDIR";      //		Make directory.
    String PWD  = "PWD";        //		Print working directory. Returns the current directory of the host.
    String QUIT = "QUIT";       //		Disconnect.
    String RETR = "GET";        //		Retrieve a copy of the file
    String RMD  = "RMD";        //		Remove a directory.
    String STOR = "PUT";        //		Accept the data and to store the data as a file at the server site
}
