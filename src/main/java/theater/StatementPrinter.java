package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.setInvoice(invoice);
        this.setPlays(plays);
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final StringBuilder result =
                new StringBuilder("Statement for " + getInvoice().getCustomer() + System.lineSeparator());
        for (Performance singlePerformance : getInvoice().getPerformances()) {
            // print line for this order
            result.append(
                    String.format(
                            "  %s: %s (%s seats)%n", getPlay(singlePerformance).getName(), usd(
                                    getAmount(singlePerformance)
                            ), singlePerformance.getAudience()
                    )
            );
        }

        result.append(String.format("Amount owed is %s%n", usd(getTotalAmount())));
        result.append(String.format("You earned %s credits%n", getTotalVolumeCredits()));
        return result.toString();
    }

    private int getTotalAmount() {
        int totalAmount = 0;
        for (Performance singlePerformance : getInvoice().getPerformances()) {
            totalAmount += getAmount(singlePerformance);
        }
        return totalAmount;
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / Constants.PERCENT_FACTOR);
    }

    private int getTotalVolumeCredits() {
        int result = 0;
        for (Performance singlePerformance : getInvoice().getPerformances()) {
            result += getVolumeCredits(singlePerformance);
        }
        return result;
    }

    private int getVolumeCredits(Performance singlePerformance) {
        // add volume credits
        int result = 0;
        result += Math.max(singlePerformance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        // add extra credit for every five comedy attendees
        if ("comedy".equals(getPlay(singlePerformance).getType())) {
            result += singlePerformance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return result;
    }

    private Play getPlay(Performance singlePerformance) {
        return getPlays().get(singlePerformance.getPlayID());
    }

    private int getAmount(Performance performance) {
        int thisAmount;
        switch (getPlay(performance).getType()) {
            case "tragedy":
                thisAmount = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += getTragedyAmount(performance);
                }
                break;
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", getPlay(performance).getType()));
        }
        return thisAmount;
    }

    private static int getTragedyAmount(Performance performance) {
        return Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON
                * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    public void setPlays(Map<String, Play> plays) {
        this.plays = plays;
    }
}
