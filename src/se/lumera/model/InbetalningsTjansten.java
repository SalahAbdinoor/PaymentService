package se.lumera.model;

import se.lumera.validation.Validate;

import java.util.List;

public class InbetalningsTjansten {

    /*      Opening-post      */
    private final String postTypeOpening = "00";
    private String reserved_O1;
    private int clearingNumber;
    private int accountNumber;
    private String reserved_O2;

    public InbetalningsTjansten(String reserved_O1, int clearingNumber, int accountNumber, String reserved_O2) {
        this.reserved_O1 = reserved_O1;
        this.clearingNumber = clearingNumber;
        this.accountNumber = accountNumber;
        this.reserved_O2 = reserved_O2;
    }

    /*      Payment-post      */
    private final String postTypePayment = "30";
    private double amountPerPayment; // 2 decimals in file
    private String reserved_P1;
    private String reference;

    public InbetalningsTjansten(double amountPerPayment, String reserved_P1, String reference) {
        this.amountPerPayment = amountPerPayment;
        this.reserved_P1 = reserved_P1;
        this.reference = reference;
    }

    /*      Closing-post      */
    private final String postTypeClosing = "99";
    private double totalAmount; // Adding: amountPerPayment from Payment-post
    private String reserved_C1;
    private int amountOfPayments;
    private String reserved_C2;

    public InbetalningsTjansten(double totalAmount, String reserved_C1, int amountOfPayments, String reserved_C2) {
        this.totalAmount = totalAmount;
        this.reserved_C1 = reserved_C1;
        this.amountOfPayments = amountOfPayments;
        this.reserved_C2 = reserved_C2;
    }

    /*      Empty Constructor      */
    public InbetalningsTjansten() {
    }

    Validate validate = new Validate();


    public InbetalningsTjansten createPost(String currentLine, boolean isOpening, boolean isClosing) throws NumberFormatException {

        if (isOpening) {
            System.out.println("Validating: Opening-post...");
            // Build and validate Opening-post
            return createValidOpeningPost(currentLine);

        } else if (isClosing) {
            System.out.println("Validating: Closing-post...\n");

            // Build and validate Closing-post
            return createValidClosingPost(currentLine);

        } else {
            System.out.println("Validating: Payment-post...");

            // Build and validate Payment-post
            return createValidPaymentPost(currentLine);
        }
    }

    /**
     * Substrings row positions based on positions, validates params and returns Opening-post
     * <p>
     * -- For more information on positions - read Inbetalningstjänsten.doc
     */
    public InbetalningsTjansten createValidOpeningPost(String row) throws NumberFormatException {

        String reserved_1 = row.substring(2, 10).trim();
        String clearingNumber = row.substring(10, 14).trim();
        String accountNumber = row.substring(14, 24).trim();
        String reserved_2 = row.substring(24, 80).trim();

        validate.validReserved(new String[]{reserved_1, reserved_2});

        // output same order as input
        List<Double> validNumber = validate.validNumber(new String[]{accountNumber, clearingNumber});

        // .get(x): x -> depends on the order string values get validated
        int validAccountNumber = validNumber.get(0).intValue();
        int validClearingNumber = validNumber.get(1).intValue();

        return new InbetalningsTjansten(reserved_1, validClearingNumber, validAccountNumber, reserved_2);
    }

    /**
     * Substrings row positions based on positions, validates params and returns Payment-post
     * <p>
     * -- For more information on positions - read Inbetalningstjänsten.doc
     */
    public InbetalningsTjansten createValidPaymentPost(String line) throws NumberFormatException {

        String amountPerPayment = line.substring(2, 22).trim();
        String reserved_1 = line.substring(22, 40).trim();
        String reference = line.substring(40, 65).trim();

        // Throws if not valid
        validate.validReserved(new String[]{reserved_1});
        double validAmountPerPayment = validate.validNumber(new String[]{amountPerPayment}).get(0);

        validAmountPerPayment = moveDecimal(validAmountPerPayment, 2);

        return new InbetalningsTjansten(validAmountPerPayment, reserved_1, reference);
    }

    /**
     * Substrings row positions based on positions, validates params and returns Closing-post
     * <p>
     * -- For more information on positions - read Inbetalningstjänsten.doc
     */
    public InbetalningsTjansten createValidClosingPost(String line) throws NumberFormatException {

        String totalAmount = line.substring(2, 22).trim();
        String reserved_1 = line.substring(22, 30).trim();
        String amountOfPayments = line.substring(30, 38).trim();
        String reserved_2 = line.substring(38, 80).trim();

        // Throws if not valid

        validate.validReserved(new String[]{reserved_1, reserved_2});
        double validTotalAmount = validate.validNumber(new String[]{totalAmount}).get(0);
        validTotalAmount = moveDecimal(validTotalAmount, 2);

        return new InbetalningsTjansten(validTotalAmount, reserved_1, Integer.parseInt(amountOfPayments), reserved_2);
    }


    /**
     * helper method to move decimal by n-steps
     *
     * ---> Formula: X/10^n
     *
     * t.ex: from 400000.0 -> 400000.0/10^2 -> 4000.0
     * @param decimal - originalDecimal
     * @param stepsToMove - how many steps you want to move
     * @return - new decimal
     */
    private double moveDecimal(double decimal, int stepsToMove) {

        return decimal / Math.pow(10, stepsToMove);
    }

    public String toStringOpening() {
        return "reserved_O1='" + reserved_O1 + '\'' +
                ", clearingNumber=" + clearingNumber +
                ", accountNumber=" + accountNumber +
                ", reserved_O2='" + reserved_O2 + '\'';
    }

    public String toStringPayment() {
        return "amountPerPayment=" + amountPerPayment +
                ", reserved_P1='" + reserved_P1 + '\'' +
                ", reference='" + reference + '\'';
    }

    public String toStringClosing() {
        return "totalAmount=" + totalAmount +
                ", reserved_C1='" + reserved_C1 + '\'' +
                ", amountOfPayments='" + amountOfPayments + '\'' +
                ", reserved_C2='" + reserved_C2 + '\'';
    }

    // FIXME: Fulhack! Sköt de med postType
    @Override
    public String toString() {

        if (accountNumber != 0) {
            return "{ " + toStringOpening() + " }";
        } else if (totalAmount != 0) {
            return "{ " + toStringClosing() + " }";
        } else {
            return "{ " + toStringPayment() + " }";
        }
    }


    public double getTotalAmount() {
        return totalAmount;
    }

    public double getAmountPerPayment() {
        return amountPerPayment;
    }


    public int getAmountOfPayments() {
        return amountOfPayments;
    }
}
