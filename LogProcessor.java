import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class LogProcessor {
    // Created a logger for error handling
    private static final Logger logger = Logger.getLogger(LogProcessor.class.getName());
    // A class to represent each log entry
    static class LogEntry {
        String timestamp;
        String logLevel;
        String message;
        //This is the constructor that creates a log entry which creates a timestamp a log level and a message
        public LogEntry(String timestamp, String logLevel, String message) {
            this.timestamp = timestamp;
            this.logLevel = logLevel;
            this.message = message;
        }
        //This is the toString method that returns the log entry in the format of timestamp logLevel message
        @Override
        public String toString() {
            return "[" + timestamp + "] " + logLevel + " " + message;
        }
    }

    public static void main(String[] args) {
        // Check if file path is provided as a command line argument
        if (args.length != 1) {
            System.out.println("Usage: java LogProcessor <path_to_log_data_csv>");
            return;
        }
        String filePath = args[0];  // grabs the CSV file path from command line input
        Queue<LogEntry> logQueue = new LinkedList<>();
        Stack<LogEntry> errorStack = new Stack<>();
        // Variables to count the number of logs for each type
        int infoCount = 0;
        int warnCount = 0;
        int errorCount = 0;
        int memoryWarnCount = 0;
        // This is used to store the last 100 errors
        List<LogEntry> recentErrors = new ArrayList<>();
        try {
            // This reads the current log file using BufferedReader
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                // This assumes log entries follow the format: [timestamp] logLevel message
                String[] parts = line.split(" ", 3);
                if (parts.length < 3) continue;
                // Combine the first two parts as the timestamp (date + time)
                String timestamp = parts[0] + " " + parts[1];
                // Extracts the log level (first word of the third part)
                String logLevel = parts[2].substring(0, parts[2].indexOf(" "));
                // Extracts the message (the rest of the third part)
                String message = parts[2].substring(parts[2].indexOf(" ") + 1);
                // Creates a new log entry object with the timestamp, log level,
                LogEntry logEntry = new LogEntry(timestamp, logLevel, message);
                logQueue.offer(logEntry);
            }
            // This while loop processes each log entry in the queue
            while (!logQueue.isEmpty()) {
                LogEntry logEntry = logQueue.poll();
                String logLevel = logEntry.logLevel;

                // Check the log level and increase the respective counter
                switch (logLevel) {
                    case "INFO":
                        infoCount++; // Increment INFO counter
                        break;
                    case "WARN":
                        warnCount++; // Increment WARN counter
                        if (logEntry.message.contains("Memory")) {
                            memoryWarnCount++;
                        }
                        break;
                    case "ERROR":
                        errorCount++;
                        errorStack.push(logEntry);
                        // Stores the last 100 errors
                        if (recentErrors.size() == 100) {
                            recentErrors.remove(0);
                        }
                        recentErrors.add(logEntry);
                        break;
                }
            }
            // Displays the analysis of each log level
            System.out.println("Log Level Counts:");
            System.out.println("INFO: " + infoCount);
            System.out.println("WARN: " + warnCount);
            System.out.println("ERROR: " + errorCount);
            System.out.println("\nLast 100 Errors:");
            for (LogEntry error : recentErrors) {
                System.out.println(error);
            }
            System.out.println("\nMemory Warnings: " + memoryWarnCount);
            // Print the error stack (in LIFO order)
            System.out.println("\nErrors stored in Stack (in reverse order):");
            while (!errorStack.isEmpty()) {
                System.out.println(errorStack.pop());
            }
        } catch (IOException e) {
            logger.severe("Error reading the file: " + e.getMessage());
        }
    }
}