public interface FTPResponseCodes
{
    String R_100 = "100\n";  //Series	The requested action is being initiated, expect another reply before proceeding with a new command.
    String R_200 = "200\n";  //Series	The requested action has been successfully completed.
    String R_300 = "300\n";  //Series	The command has been accepted, but the requested action is on hold, pending receipt of further information.
    String R_400 = "400\n";  //Series	The command was not accepted and the requested action did not take place, but the error condition is temporary and the action may be requested again.
    String R_500 = "500\n";  //Syntax error, command unrecognized and the requested action did not take place. This may include errors such as command line too long.
    String R_600 = "600\n";  //Series	Replies regarding confidentiality and integrity
}
