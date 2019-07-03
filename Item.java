import java.io.*;
import java.math.BigDecimal;

public class Item {
    private String name;
    private BigDecimal pricePerPound; //Big decimal is preferable to float or double because it is more precise
    private BigDecimal cost; //Cost is what the item will actually cost when bought
    private int PLU;
    private double weight;

    //This constructor will auto-populate the values that can be found in the plu.txt file
    Item(int PLU, double weight) {
        this.PLU = PLU;
        this.weight = weight;

        File PLUFile = new File("res/plu.txt");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(PLUFile));
            String[] PLUs = bufferedReader.readLine().split(";");
            bufferedReader.close();

            final int ITEM_NAME_INDEX = 1;
            final int PRICE_PER_POUND_INDEX = 2;

            boolean isItemInPLUList = false;

            for(String itemData : PLUs) {
                String extractedPLUCode = itemData.substring(0,4);
                if(extractedPLUCode.equals(String.valueOf(this.PLU))) {
                    isItemInPLUList = true;

                    String[] PLUData = itemData.split(",");
                    name = PLUData[ITEM_NAME_INDEX];
                    pricePerPound = new BigDecimal(PLUData[PRICE_PER_POUND_INDEX]);

                    cost = pricePerPound.multiply(new BigDecimal(this.weight));

                    break;
                }
            }

            if(!isItemInPLUList) {
                this.name = null;
                this.pricePerPound = null;
                this.cost = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Item(String name, BigDecimal pricePerPound, BigDecimal cost, int PLU, double weight) {
        this.name = name;
        this.pricePerPound = pricePerPound;
        this.cost = cost;
        this.PLU = PLU;
        this.weight = weight;
    }


    BigDecimal getCost() {
        return this.cost;
    }

    String getName() {
        return name;
    }

    BigDecimal getPricePerPound() {
        return pricePerPound;
    }

    int getPLU() {
        return PLU;
    }

    void setPLU(int PLU) {
        this.PLU = PLU;
    }

    double getWeight() {
        return weight;
    }

    void setWeight(int weight) {
        this.weight = weight;
    }
}
