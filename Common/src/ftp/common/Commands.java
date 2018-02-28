package ftp.common;


public interface Commands
{
    String CWD = "CD";         //	RFC 697	Change working directory.
    String DELE = "DELETE";     //		Delete file.
    String LIST = "LS";         //		Returns information of a file or directory if specified, else information of the current working directory is returned.
    String MKD = "MKDIR";      //		Make directory.
    String PWD = "PWD";        //		Print working directory. Returns the current directory of the host.
    String QUIT = "QUIT";       //		Disconnect.
    String RETR = "GET";        //		Retrieve a copy of the file
    String RMD = "RMD";        //		Remove a directory.
    String STOR = "PUT";        //		Accept the data and to store the data as a file at the server site

    // Commands not implemented
    String ABOR = "ABOT";    //		Abort an active file transfer.
    String ACCT = "ACCT";    //		Account information.
    String ADAT = "ADAT";    //	RFC 2228	Authentication/Security Data
    String ALLO = "ALLO";    //		Allocate sufficient disk space to receive a file.
    String APPE = "APPE";    //		Append (with create)
    String AUTH = "AUTH";    //	RFC 2228	Authentication/Security Mechanism
    String CCC = "CCC";     //	RFC 2228	Clear Command Channel
    String CDUP = "CDUP";    //		Change to Parent Directory.
    String CONF = "CONF";    //	RFC 2228	Confidentiality Protection Command
    String ENC = "ENC";     //	RFC 2228	Privacy Protected Channel
    String EPRT = "EPRT";    //	RFC 2428	Specifies an extended address and port to which the server should connect.
    String EPSV = "EPSV";    //	RFC 2428	Enter extended passive mode.
    String FEAT = "FEAT";    //	RFC 2389	Get the feature list implemented by the server.
    String HELP = "HELP";    //		Returns usage documentation on a command if specified, else a general help document is returned.
    String HOST = "HOST";    //	RFC 7151	Identify desired virtual host on server, by name.
    String LANG = "LANG";    //	RFC 2640	Language Negotiation
    String LPRT = "LPRT";    //	RFC 1639	Specifies a long address and port to which the server should connect.
    String LPSV = "LPSV";    //	RFC 1639	Enter long passive mode.
    String MDTM = "MDTM";    //	RFC 3659	Return the last-modified time of a specified file.
    String MFCT = "MFCT";    //	The 'MFMT', 'MFCT', and 'MFF' Command Extensions for FTP	Modify the creation time of a file.
    String MFF = "MFF";     //	The 'MFMT', 'MFCT', and 'MFF' Command Extensions for FTP	Modify fact (the last modification time, creation time, UNIX group/owner/mode of a file).
    String MFMT = "MFMT";    //	The 'MFMT', 'MFCT', and 'MFF' Command Extensions for FTP	Modify the last modification time of a file.
    String MIC = "MIC";     //	RFC 2228	Integrity Protected Command
    String MLSD = "MLSD";    //	RFC 3659	Lists the contents of a directory if a directory is named.
    String MLST = "MLST";    //	RFC 3659	Provides data about exactly the object named on its command line, and no others.
    String MODE = "MODE";    //		Sets the transfer mode (Stream, Block, or Compressed).
    String NLST = "NLST";    //		Returns a list of file names in a specified directory.
    String NOOP = "NOOP";    //		No operation (dummy packet; used mostly on keepalives).
    String OPTS = "OPTS";    //	RFC 2389	Select options for a feature (for example OPTS UTF8 ON).
    String PASS = "PASS";    //		Authentication password.
    String PASV = "PASV";    //		Enter passive mode.
    String PBSZ = "PBSZ";    //	RFC 2228	Protection Buffer Size
    String PORT = "PORT";    //		Specifies an address and port to which the server should connect.
    String PROT = "PROT";    //	RFC 2228	Data Channel Protection Level.
    String REIN = "REIN";    //		Re initializes the connection.
    String REST = "REST";    //	RFC 3659	Restart transfer from the specified point.
    String RNFR = "RNFR";    //		Rename from.
    String RNTO = "RNTO";    //		Rename to.
    String SITE = "SITE";    //		Sends site specific commands to remote server (like SITE IDLE 60 or SITE UMASK 002). Inspect SITE HELP output for complete list of supported commands.
    String SIZE = "SIZE";    //	RFC 3659	Return the size of a file.
    String SMNT = "SMNT";    //		Mount file structure.
    String SPSV = "SPSV";    //	FTP Extension Allowing IP Forwarding (NATs)	Use single port passive mode (only one TCP port number for both control connections and passive-mode data connections)
    String STAT = "STAT";    //		Returns the current status.
    String STOU = "STOU";    //		Store file uniquely.
    String STRU = "STRU";    //		Set file transfer structure.
    String SYST = "SYST";    //		Return system type.
    String TYPE = "TYPE";    //		Sets the transfer mode (ASCII/Binary).
    String USER = "USER";    //		Authentication username.
    String XCUP = "XCUP";    //	RFC 775	Change to the parent of the current working directory
    String XMKD = "XMKD";    //	RFC 775	Make a directory
    String XPWD = "XPWD";    //	RFC 775	Print the current working directory
    String XRCP = "XRCP";    //	RFC 743
    String XRMD = "XRMD";    //	RFC 775	Remove the directory
    String XRSQ = "XRSQ";    //	RFC 743
    String XSEM = "XSEM";    //	RFC 737	Send, mail if cannot
    String XSEN = "XSEN";    //	RFC 737	Send to terminal

    String TERM = "TERMINATE";  //special comnmand
}
