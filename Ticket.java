import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;

public class Ticket {
    private BigDecimal total;
    private boolean isTicketEnded;
    private boolean isAmountStillOwed;
    private BigDecimal amountOwed; //Big decimal is preferable to float or double because it is more precise
    private BigDecimal changeOwed;
    private ArrayList<Item> items;

    public Ticket() {
        this.amountOwed = new BigDecimal("0.00");
        this.changeOwed = null;
        this.items = new ArrayList<>();
        this.total = new BigDecimal("0.00");
        this.isTicketEnded = false;
        this.isAmountStillOwed = false;
    }

    void addItem(Item item) {
        total = total.add(item.getCost());
        items.add(item);
    }

    void voidItem(Item item) {
        items.remove(item);
        total = total.subtract(item.getCost());
    }

    private void finishTicket() {
        String dateTime = (java.time.LocalDate.now() + " " + java.time.LocalTime.now()).replace(":", "_");
        File receiptFile = new File("receipts/" + dateTime + ".txt");
        try {
            receiptFile.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(receiptFile));
            bufferedWriter.write("Ticket : " + dateTime + "\n");
            bufferedWriter.write("Total : " + getTotal() + "\n");
            bufferedWriter.write("Items : \n");
            for(Item item : items) {
                bufferedWriter.write(item.getName() + "\n");
            }
            if(getChangeOwed() != null) {
                bufferedWriter.write("Change : " + getChangeOwed());
            }
            bufferedWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        isTicketEnded = true;
    }

    void enterPaymentMode() {
        if(!isAmountStillOwed) {//If the customer has already paid some money, we don't want the amount owed to return back up to the total
            amountOwed = total;
        }
    }

    void setAmountOwed(BigDecimal amountOwed) {
        this.amountOwed = amountOwed;
    }

    void payTowardsAmountOwed(BigDecimal amount) {
        amountOwed = amountOwed.subtract(amount);

        if(amountOwed.compareTo(new BigDecimal("0.0")) == 0) {//If nothing is owed...
            finishTicket();
        } else if(amountOwed.compareTo(new BigDecimal("0.0")) == -1) {//If change needs to be given
            changeOwed = amount.subtract(total);
        } else {
            isAmountStillOwed = true;
        }
    }

    BigDecimal getTotal() {
        return total;
    }

    BigDecimal getAmountOwed() {
        return amountOwed;
    }

    BigDecimal getChangeOwed() {
        return changeOwed;
    }

    ArrayList<Item> getItems() {
        return items;
    }

    boolean isTicketEnded() {
        return isTicketEnded;
    }

    boolean isAmountStillOwed() {
        return isAmountStillOwed;
    }

    void setTicketEnded(boolean ticketEnded) {
        isTicketEnded = ticketEnded;
    }

    void setTotal(BigDecimal total) {
        this.total = total;
    }

    void setItems(ArrayList<Item> items) {
        this.items = items;
    }
}
