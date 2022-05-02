package se.lumera.model;

import se.lumera.validation.Validate;

import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

public class BetalningsService {

    /*      Opening-post      */
    private final String postTypeOpening = "O";
    private String accountNumber;
    private double TotalAmount; // Adding: AmountOfPayments from Payment-post
    private int AmountOfPayments;
    private Date paymentDate;
    private String currency;

    public BetalningsService(String accountNumber, double TotalAmount, int totalAmount, Date paymentDate, String currency) {
        this.accountNumber = accountNumber;
        this.TotalAmount = TotalAmount;
        this.AmountOfPayments = totalAmount;
        this.paymentDate = paymentDate;
        this.currency = currency;
    }

    /*      Payment-post      */
    private final String postTypePayment = "B";
    private double amountPerPayment;
    private String reference;

    public BetalningsService(double amountPerPayment, String reference) {
        this.amountPerPayment = amountPerPayment;
        this.reference = reference;
    }

    /*      Empty Constructor      */
    public BetalningsService() {
    }

    // Validation
    Validate validate = new Validate();

    /**
     * if Opening-post -> Create valid createValidOpening()
     * if Payment-post -> Create valid createValidPayment()
     */
    public BetalningsService createPost(String currentLine, boolean isOpening) {

        if (isOpening) {

            System.out.println("Validating: Opening-post...");

            // Build and validate Opening-post
            return createValidOpeningPost(currentLine);
        } else {

            System.out.println("Validating: Payment-post...");

            // Build and validate Payment-post
            return createValidPaymentPost(currentLine);
        }
    }

    /**
     * Builds validated Opening-post
     * <p>
     * -- For more information on positions - read Betalningsservice.doc
     *
     * @param row - current line being read
     * @return - Opening-post
     */
    public BetalningsService createValidOpeningPost(String row) {

        /*  Positions in row for generic dataType */
        String accountNumber = row.substring(1, 16).trim();
        String sum = row.substring(16, 30).trim();
        String totalAmount = row.substring(30, 40).trim();
        String paymentDate = row.substring(40, 48).trim();
        String currency = row.substring(48, 51).trim();     // Not getting validated...

        double validSum = 0;
        int validTotalAmount = 0;
        Date validPaymentDate = null;

        // Validation and Parsing to correct format
        try {

            validate.validAccountNumber(accountNumber);

            // output same order as input
            List<Double> validNumber = validate.validNumber(new String[]{sum, totalAmount});

            // .get(x): x -> depends on the order string values get validated
            validSum = validNumber.get(0);
            validTotalAmount = validNumber.get(1).intValue();
            validPaymentDate = validate.validPaymentDate(paymentDate);

        } catch (NumberFormatException | DateTimeParseException | ParseException e) {
            e.printStackTrace();
            System.err.println("\nCouldn't validate Opening-post\nTerminating Process");
            System.exit(0);

        }

        // Return Opening-post as object
        return new BetalningsService(accountNumber, validSum, validTotalAmount, validPaymentDate, currency);
    }

    /**
     * Builds validated Payment-post
     * <p>
     * -- For more information on positions - read Betalningsservice.doc
     *
     * @param row - current line being read
     * @return - Payment-post
     */
    public BetalningsService createValidPaymentPost(String row) {

        // This map holds data for Payment-post
        String amountPerPayment = row.substring(1, 15).trim();
        String reference = row.substring(15, 25).trim();

        double validAmountPerPayment = 0;
        try {
            // If parsed --> validated
            validAmountPerPayment = validate.validNumber(new String[]{amountPerPayment}).get(0);


            // if caught --> format is incorrect
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.err.println("\nCouldn't validate Payment-post\nTerminating Process");
            System.exit(0);
        }

        // Return Payment-post as object
        return new BetalningsService(validAmountPerPayment, reference);
    }

    public String toStringOpening() {
        return "accountNumber='" + accountNumber + '\'' +
                ", TotalAmount=" + TotalAmount +
                ", AmountOfPayments=" + AmountOfPayments +
                ", paymentDate=" + paymentDate +
                ", currency='" + currency + '\'';
    }

    public String toStringPayment() {
        return "amountPerPayment=" + amountPerPayment +
                ", reference='" + reference + '\'';
    }

    // FIXME: Fulhack! Sköt de med postType
    @Override
    public String toString() {

        if (accountNumber == null) {
            return "{ " + toStringPayment() + " }";
        } else {
            return "{ " + toStringOpening() + " }";
        }
    }

    public int getAmountOfPayments() {
        return AmountOfPayments;
    }

    public double getTotalAmount() {
        return TotalAmount;
    }

    public double getAmountPerPayment() {
        return amountPerPayment;
    }

}
