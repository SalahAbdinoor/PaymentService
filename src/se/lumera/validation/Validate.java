package se.lumera.validation;

import se.lumera.model.BetalningsService;
import se.lumera.model.InbetalningsTjansten;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * This class holds all validation-methods used within the models
 */
public class Validate {

    /**
     * Check to see if amount of payments are the same as the totalAmount in the Opening-post
     *
     * @param file     - Mapped file
     * @param fileType - t.ex. inbetalningstjansten.txt
     */
    public boolean comparePaymentsWithTotal(HashMap<String, Object> file, String fileType) {

        double totalAmount = 0;
        List<Double> listOfAmountPerPayment = new ArrayList<>();

        // adding the amounts from listOfAmountPerPayment
        double actualAmount = 0;

        // For each post in full file
        for (Map.Entry<String, Object> post : file.entrySet()) {

            boolean isOpeningPost = post.getKey().contains("Opening-post");
            boolean isPaymentPost = post.getKey().contains("Payment-post");
            boolean isClosingPost = post.getKey().contains("Closing-post");

            if (fileType.equals("inbetalningstjansten.txt")) {

                // Closing-post holds totalAmount
                if (isClosingPost) {
                    var tempPost = (InbetalningsTjansten) post.getValue();

                    totalAmount = tempPost.getTotalAmount();

                    // Payment-post holds individual payments
                } else if (isPaymentPost) {
                    var tempPost = (InbetalningsTjansten) post.getValue();

                    // add payment to list
                    listOfAmountPerPayment.add(tempPost.getAmountPerPayment());
                }
            } else if (fileType.equals("betalningsservice.txt")) {

                // Opening-post holds totalAmount
                if (isOpeningPost) {

                    var tempPost = (BetalningsService) post.getValue();

                    totalAmount = tempPost.getTotalAmount();

                    // Payment-post holds individual payments
                } else if (isPaymentPost) {
                    var tempPost = (BetalningsService) post.getValue();

                    listOfAmountPerPayment.add(tempPost.getAmountPerPayment());
                }
            }
        }

        // add each payment to actual payment
        for (Double aDouble : listOfAmountPerPayment) {
            actualAmount += aDouble;
        }

        // compare total amount and added amounts from individual payments
        if (actualAmount == totalAmount) {
            return true;
        } else {
            System.err.println("\nPayment failed!"
                    + "\nTotal Amount in variable: " + totalAmount
                    + "\nActual total amount: " + actualAmount
                    + "\nSee more: '/processed/failed_payments'");
            return false;
        }

    }


    public boolean compareAmountOfPayments(HashMap<String, Object> file, String fileType) {

        // from either Opening/Closing - Getters
        double AmountOfPayments = 0;

        // from counting "Payment-posts" in file
        double actualAmount = 0;

        // For each post in full file
        for (Map.Entry<String, Object> post : file.entrySet()) {

            boolean isOpeningPost = post.getKey().contains("Opening-post");
            boolean isClosingPost = post.getKey().contains("Closing-post");
            boolean isPaymentPost = post.getKey().contains("Payment-post");

            // counting Payment-posts
            if (isPaymentPost) {
                actualAmount++;
            }

            // Closing-post holds totalAmount
            if (fileType.equals("inbetalningstjansten.txt") && isClosingPost) {

                // File as object
                var tempPost = (InbetalningsTjansten) post.getValue();

                AmountOfPayments = tempPost.getAmountOfPayments();

                // Opening-post holds amountOfPayments in betalningsservice.txt
            } else if (fileType.equals("betalningsservice.txt") && isOpeningPost) {

                // File as object
                var tempPost = (BetalningsService) post.getValue();

                AmountOfPayments = tempPost.getAmountOfPayments();
            }
        }

        // compare total amount and added amounts from individual payments
        if (actualAmount == AmountOfPayments) {
            return true;
        } else {
            System.err.println("\nPayment failed!"
                    + "\nTotal amount of payment-posts in variable: " + AmountOfPayments
                    + "\nActual amount: " + actualAmount
                    + "\nSee more: '/processed/failed_payments'");
            return false;
        }
    }

    /**
     * This helper method replaces any ',' with '.' in the cases of decimals
     * and validates that String can be converted to double
     * <p>
     * returns in valid format
     *
     * @param listOfNumbers - [4711,17, 1564]
     * @return - [4711.17, 1564.00]
     * @throws NumberFormatException - If number can't be parsed
     */
    public List<Double> validNumber(String[] listOfNumbers) throws NumberFormatException {

        List<Double> validNumbers = new ArrayList<>();

        try {

            for (String number : listOfNumbers) {

                if (number.contains(","))
                    number = number.replace(',', '.');


                validNumbers.add(Double.parseDouble(number));
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Number is invalid! " + e.getMessage());
        }


        return validNumbers;
    }

    /**
     * Checks to see if accountNumber has space between clearing-number and account-number
     * and if accountNumber is a number
     *
     * @param accountNumber - Format: 5555 5555555555
     * @throws NumberFormatException - throws if accountNumber can't be parsed to double
     */
    public void validAccountNumber(String accountNumber) throws NumberFormatException {

        // checks if there is space between clearing-number and account-number
        if (accountNumber.contains(" ")) {

            // Remove space for parsing into double
            accountNumber = accountNumber.replace(" ", "");

            // If parsed -> accountNumber is valid || if caught -> accountNumber is not valid
            try {
                Double.parseDouble(accountNumber);

            } catch (NumberFormatException e) {
                throw new NumberFormatException("Invalid account number: " + accountNumber);
            }

            //
        } else {
            throw new NumberFormatException("Clearing number and account number is not seperated with ' ' (space).");
        }


    }

    /**
     * Method checks if date could be translated into localDate
     * <p>
     * Why not use new Date()?:
     * <p>
     * (Date class cannot create new Date() based on yyyyMMdd. only takes milliseconds or new Date())
     * ---> Does not throw exception if month = 99;
     *
     * @param paymentDate - yyyyMMdd
     * @return - true if date can be translated
     * @throws DateTimeParseException - t.ex. 2011-99-15
     */
    public Date validPaymentDate(String paymentDate) throws DateTimeParseException, ParseException {

        final String dateFormat = "yyyyMMdd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat, Locale.ENGLISH);

        // If parsed correctly -> Valid date else throws exception
        LocalDate.parse(paymentDate, formatter);

        // Creates simple date format yyMMdd
        SimpleDateFormat simpleDate = new SimpleDateFormat(dateFormat);

        // returns Date
        return simpleDate.parse(paymentDate);
    }

    /**
     * checks if every number in reserved = 0
     *
     * @param listOfReserved - [[0000000], [00000000000000000]]
     */
    public void validReserved(String[] listOfReserved) throws NumberFormatException {

        // [reserved_C1, reserved_C2]
        for (String reserved : listOfReserved) {

            // reserved_C1 = 00000000
            for (int i = 0; i < reserved.length(); i++) {

                // 00000000 -> [0,0,...,0]
                char currentNumber = reserved.charAt(i);

                if (currentNumber == '0') {

                    // If currentNumber == '0' --> Reserved is valid

                } else {
                    // If currentNumber != '0' --> Reserved is invalid
                    throw new NumberFormatException("Reserved is invalid! must be: '0'\n" +
                            "input: " + currentNumber + "\nreserved: " + reserved);
                }
            }
        }
    }
}
