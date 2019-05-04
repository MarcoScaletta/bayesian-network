package logger;

public class Logger {

    private static final String header = "> BIF-BN-PARSER >";



    public static void log(String toLog){
//        System.out.println(header + " LOG: " + toLog);
    }

    public static void err(String toLog){
        System.err.println(header + " error: " + toLog);
    }
}
