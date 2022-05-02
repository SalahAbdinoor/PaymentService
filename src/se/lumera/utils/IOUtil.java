package se.lumera.utils;

import se.lumera.model.BetalningsService;
import se.lumera.model.InbetalningsTjansten;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IOUtil {

    /**
     * Builds a map based on fileType,
     * --- Must contain one Opening-post
     * --- Could contain multiple Payment-posts
     * --- Could contain up to one Closing-post
     * example format:
     * <p>
     * Opening-post: inbetalningstjansten { postTypeOpening='00', reserved_O1='00000000', clearingNumber=1234567897, accountNumber=1234, reserved_O2='00000000000000000000000000000000000000000000000000000000' }
     * Payment-post_1: inbetalningstjansten { postTypePayment='30', amountPerPayment=400000.0, reserved_P1='000000000000000000', reference='9876543210' }
     * Payment-post_2: inbetalningstjansten { postTypePayment='30', amountPerPayment=100000.0, reserved_P1='000000000000000000', reference='9876543210' }
     * Payment-post_3: inbetalningstjansten { postTypePayment='30', amountPerPayment=1030000.0, reserved_P1='000000000000000000', reference='9876543210' }
     * Closing-post: inbetalningstjansten { postTypeClosing='99', totalAmount=1530000.0, reserved_C1='00000000', amountOfPayments='3', reserved_C2='000000000000000000000000000000000000000000' }
     *
     * @param path - path to Payment-file
     * @return fullFile - Hashmap holding Posts
     */
    public HashMap<String, Object> buildObjectFromFile(Path path) {

        // Map holds full file as object
        HashMap<String, Object> fullFile = new HashMap<>();

        // Index for paymentPost
        int counter = 1;

        // Turns false once Opening-post has been created
        boolean isOpening = true;
        boolean isClosing;

        // This splits the name of the file into: t.ex name: Exempelfil & type: betalningsservice.txt
        String[] file = getFileData(path.getFileName().toString());
        String fileName = file[0];
        String fileType = file[1];

        System.out.println("File Found! Name: " + fileName + " Type: " + fileType);

        // Try/catch with resources || No need to close Scanner
        try (Scanner fileScanner = new Scanner(path)) {

            System.out.println("READING FILE... \n");

            while (fileScanner.hasNext()) {

                String currentLine = fileScanner.nextLine();

                // TODO: This is where you add the new fileType
                if (fileType.equals("betalningsservice.txt")) {

                    BetalningsService post = new BetalningsService();

                    // Creates post based on currentLine
                    post = post.createPost(currentLine, isOpening);

                    // Map - Opening-post
                    if (isOpening) {
                        fullFile.put("Opening-post", post);
                        System.out.println("");
                        isOpening = false;
                        //Map - Payment-post
                    } else {
                        fullFile.put("Payment-post_" + counter, post);
                        counter++;
                    }

                } else if (fileType.equals("inbetalningstjansten.txt")) {

                    InbetalningsTjansten post = new InbetalningsTjansten();

                    // If last line -> Closing-post
                    isClosing = !fileScanner.hasNext();

                    // Creates post based on currentLine
                    post = post.createPost(currentLine, isOpening, isClosing);

                    // Map - Opening-post
                    if (isOpening) {
                        fullFile.put("Opening-post", post);
                        isOpening = false;

                        //Map - Closing-post
                    } else if (isClosing) {
                        fullFile.put("Closing-post", post);

                        //Map - Payment-post
                    } else {
                        fullFile.put("Payment-post_" + counter, post);
                        counter++;
                    }

                } else {
                    System.err.println("File Type is not recognized\nTerminating Process");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file");
            e.printStackTrace();
            System.exit(0);

        } catch (StringIndexOutOfBoundsException e) {
            System.err.println("File is not properly formatted: Usually happens when file can't be sub-stringed correctly due to:" +
                    "not enough characters in row \nTerminating Process");

            e.printStackTrace();
            System.exit(0);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.err.println("\nCouldn't validate post\nTerminating Process");
            System.exit(0);
        }

        return fullFile;
    }

    /**
     * Logs Payment-posts to "/processed"
     *
     * @param file          - confirmed & validated payment-file
     * @param fileData      - [fileName, fileType]
     * @param validPayment- total payment = individual payments
     */
    public void logConfirmedPayment(HashMap<String, Object> file, String[] fileData, boolean validPayment) {

        String confirmedPayments = "src/se/lumera/processed/";

        if (validPayment) {
            System.out.println("Payment processed without any issues: See '/processed/confirmed_payment'");
            confirmedPayments += "/confirmed_payments";
        } else {
            confirmedPayments += "/failed_payments";
        }

        try (PrintWriter writer = new PrintWriter(
                new FileWriter(confirmedPayments, true))) {

            String fileName = fileData[0];
            String fileType = fileData[1];

            // Current day & time
            String dateOfConfirmation = LocalDate.now() + " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // File data
            if (validPayment) {
                writer.write("\n-------------- Payment Confirmed: --------------\n");
            } else {
                writer.write("\n-------------- Payment Declined: --------------\n");
            }

            writer.write("\nFile-name: " + fileName + "\nFile-type: " + fileType);
            writer.write("\nPayment Processed: " + dateOfConfirmation);
            writer.write("\n\nPayment file:");

            // For each post
            for (Map.Entry<String, Object> post : file.entrySet()) {
                if (post.getValue() != null) {

                    // Print Post-type: Post-object
                    writer.append("\n       ").append(post.getKey()).append(": ").append(String.valueOf(post.getValue()));

                }
            }
            writer.write("\n");
            writer.flush();

        } catch (IOException e) {
            System.err.println("An error occurred while writing to file");
            e.printStackTrace();
            System.exit(0);

        }
    }

    /**
     * this helper method find all files in "/files"
     * --> returns a list of the pathnames
     *
     * @return pathnames - paths to files in folder
     */
    public String[] findFilesInFolder(String folder) {

        // Creates an array in which we will store the names of files
        String[] pathnames;

        File files = new File(folder);

        // Populates the array with names of files
        pathnames = files.list();

        assert pathnames != null;
        if (pathnames.length == 0) {
            System.err.println("No files in folder, Terminating process");
            System.exit(0);
        }

        // Return the array with names of files
        return pathnames;
    }

    /**
     * this helper method splits the file into:
     * --- fileName (could be anything before the underscore (_) t.ex. "Exempelfil")
     * --- fileType (t.ex. beställningsservice.txt)
     *
     * @param file full name of file
     * @return String[fileName, fileType]
     */
    public String[] getFileData(String file) {

        // Splits name into name (before "_") and type (after "_")
        String fileName = file.split("_")[0];
        String fileType = file.split("_")[1];

        return new String[]{fileName, fileType};
    }

}
