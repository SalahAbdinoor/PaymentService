package se.lumera;

import se.lumera.utils.IOUtil;
import se.lumera.validation.Validate;

import java.nio.file.Path;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        System.err.println("New Process");
        run();
        System.err.println("Process Terminated");

    }

    public static void run() {

        // Object created from file
        HashMap<String, Object> objectFromFile;

        String[] fileData;

        // Folder to read files from
        final String folder = "src/se/lumera/files";

        // Holds scanner
        IOUtil util = new IOUtil();

        System.out.println("SCANNING FOR FILES...");

        // Find and retrieve files in folder named "files"
        String[] filesInFolder = util.findFilesInFolder(folder);

        for (String pathname : filesInFolder) {

            System.out.println("\n-----------------------------------------------------");

            Path currentPath = Path.of(folder + "/" + pathname);

            // Read data from file and build suitable object (Based on PostType)
            objectFromFile = util.buildObjectFromFile(currentPath);

            // Splits fileType and fileName
            fileData = util.getFileData(currentPath.getFileName().toString());


            // Compares if payments-amounts ($) match total-amount($$$)
            boolean paymentTotal = new Validate().comparePaymentsWithTotal(objectFromFile, fileData[1]);

            // Compares if amount of payments made are the same as in the Opening/Closing-post
            boolean amountOfPayments = new Validate().compareAmountOfPayments(objectFromFile, fileData[1]);

            // Logs to "/processed"
            util.logConfirmedPayment(objectFromFile, fileData, paymentTotal && amountOfPayments);

            System.out.println("-----------------------------------------------------");

        }


    }
}
