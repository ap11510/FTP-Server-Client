public class Utils
{

    public static String[] parseRequest(String request)
    {

        String[] requestArguments = new String[2];

        request = request.trim();
        if (!request.equals(""))
        {
            String command;

            String argument = "";

            if (request.indexOf(" ") != -1)
            {
                command = request.substring(0, request.indexOf(" ")).toUpperCase();
                argument = request.substring(request.indexOf(" ") + 1);
            } else
            {
                command = request.toUpperCase();
            }

            requestArguments[0] = command;
            requestArguments[1] = argument;

        }

        return requestArguments;
    }

    /**
     *
     * Write message to console
     *
     * @param header  - header
     * @param message - message
     */
    public static void writeOutput(String header, String message) {
        if (header != null) {
            System.out.println(header);
        }
        if (message != null) {
            System.out.println(message);
        }
        System.out.println("\n");
    }
}
