import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Optional;

public class Main extends Application {
    public static void main(String[] args) {
        File receiptsDirectory = new File("receipts");
        if(!receiptsDirectory.exists()) {
            receiptsDirectory.mkdir();
        }

        File resourcesDirectory = new File("res");
        if(!resourcesDirectory.exists()) {
            resourcesDirectory.mkdir();
        }

        File PLUFile = new File("res/plu.txt");
        if(!PLUFile.exists()) {
            try {
                URL githubPLUSource = new URL("https://raw.githubusercontent.com/CodeTimesTen/java-mockup-pos/master/res/plu.txt");

                FileOutputStream fileOutputStream = new FileOutputStream(PLUFile);
                ReadableByteChannel readableByteChannel = Channels.newChannel(githubPLUSource.openStream());

                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                fileOutputStream.close();
                readableByteChannel.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        launch(args);
    }

    @Override
    public void start(Stage rootStage) throws Exception {
        final Color ROOT_BACKGROUND_COLOR = Color.WHITESMOKE;

        final Ticket[] customerTicket = {new Ticket()};
        final Item[] selectedItem = {null};

        BorderPane rootPane = new BorderPane();

        //This is the main section of the software; it is where all of the scanned items will be put in a list
        final Insets SCANNED_ITEMS_PANE_PADDING = new Insets(3, 3, 3, 3);
        VBox scannedItemsPane = new VBox();
        scannedItemsPane.setPadding(SCANNED_ITEMS_PANE_PADDING);
        scannedItemsPane.setAlignment(Pos.CENTER_LEFT);

        final Insets SCANNED_ITEMS_LABEL_PADDING = new Insets(1, 0, 1, 0);
        final Font SCANNED_ITEMS_LABEL_FONT = new Font("Arial", 13);

        final ArrayList<Label> scannedItemsLabels = new ArrayList<>();

        final Border SCROLL_PANE_BORDER = new Border(new BorderStroke(Color.DIMGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

        ScrollPane scannedItemsScrollPane = new ScrollPane();
        scannedItemsScrollPane.setBorder(SCROLL_PANE_BORDER);
        scannedItemsScrollPane.setBackground(new Background(new BackgroundFill(ROOT_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        scannedItemsScrollPane.setContent(scannedItemsPane);

        //This will give some basic order info taken from an instance of the Ticket class
        VBox orderInfoPane = new VBox();
        orderInfoPane.setAlignment(Pos.CENTER);

        final Font TICKET_TOTAL_LABEL_FONT = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 20);
        final Insets TICKET_TOTAL_LABEL_PADDING = new Insets(15, 0, 5, 0);

        Label ticketTotalLabel = new Label();
        ticketTotalLabel.setFont(TICKET_TOTAL_LABEL_FONT);
        ticketTotalLabel.setPadding(TICKET_TOTAL_LABEL_PADDING);
        if(customerTicket[0].getTotal() != null) {
            ticketTotalLabel.setText("Total: " + customerTicket[0].getTotal());
        } else {
            ticketTotalLabel.setText("Total: $0.00"); //This is simply a default value for the total
        }

        AnimationTimer updateTotal = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(customerTicket[0].getTotal() != null) {
                    ticketTotalLabel.setText("Total: $" + customerTicket[0].getTotal());
                }
            }
        };
        updateTotal.start();

        orderInfoPane.getChildren().addAll(ticketTotalLabel);

        final Font BUTTON_FONT = new Font("Arial", 15);
        final double BUTTON_SPACING = 5;

        final Font UTILITY_LABEL_FONT = new Font("Arial", 15);

        //Finishing cash payments will be handled from this pane
        VBox finishCashPaymentPane = new VBox();
        finishCashPaymentPane.setAlignment(Pos.CENTER);

        //All cash payments will be handled from this pane
        VBox cashPaymentPane = new VBox();
        cashPaymentPane.setSpacing(BUTTON_SPACING);
        cashPaymentPane.setAlignment(Pos.CENTER);

        Label cashAmountLabel = new Label("Enter Cash Amount (in dollars): ");
        cashAmountLabel.setFont(UTILITY_LABEL_FONT);

        TextField cashAmountTextField = new TextField();

        Button enterCashAmountButton = new Button("Enter");
        enterCashAmountButton.setFont(BUTTON_FONT);
        enterCashAmountButton.setOnAction(event -> {
            try {
                customerTicket[0].enterPaymentMode();
                BigDecimal cashAmountGiven = new BigDecimal(cashAmountTextField.getText());
                customerTicket[0].payTowardsAmountOwed(cashAmountGiven);

                if (customerTicket[0].getChangeOwed() != null) {
                    final Insets CHANGE_LABEL_PADDING = new Insets(0, 0, 5, 0);

                    Label changeLabel = new Label("Change: $" + customerTicket[0].getChangeOwed().setScale(2, RoundingMode.HALF_UP));
                    changeLabel.setPadding(CHANGE_LABEL_PADDING);
                    changeLabel.setFont(TICKET_TOTAL_LABEL_FONT);
                    orderInfoPane.getChildren().add(changeLabel);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Alert incorrectMoneyFormat = new Alert(Alert.AlertType.ERROR);
                incorrectMoneyFormat.setHeaderText("Please enter a valid dollar format. Do not include a dollar sign and use a decimal point if neccesary.");
                incorrectMoneyFormat.showAndWait();
            }

            if(!customerTicket[0].isAmountStillOwed()) {
                rootPane.setRight(finishCashPaymentPane);
            } else {
                orderInfoPane.getChildren().clear();
                Label remainingBalanceLabel = new Label("Total: $" + customerTicket[0].getAmountOwed());
                remainingBalanceLabel.setFont(TICKET_TOTAL_LABEL_FONT);
                orderInfoPane.getChildren().add(remainingBalanceLabel);
            }
            cashAmountTextField.clear();
        });

        cashPaymentPane.getChildren().addAll(cashAmountLabel, cashAmountTextField, enterCashAmountButton);

        //All EFT payments will be handled from this pane
        VBox electronicFundsTransferPane = new VBox();
        electronicFundsTransferPane.setSpacing(BUTTON_SPACING);
        electronicFundsTransferPane.setAlignment(Pos.CENTER);

        Button finishEFTButton = new Button("Finish EFT Payment");
        finishEFTButton.setFont(BUTTON_FONT);
        finishEFTButton.setOnAction(event -> {
            customerTicket[0].enterPaymentMode();
            customerTicket[0].payTowardsAmountOwed(customerTicket[0].getAmountOwed());

            customerTicket[0].setTicketEnded(true);
        });

        electronicFundsTransferPane.getChildren().addAll(finishEFTButton);

        //This pane will only appear when the cashier indicates the order is over and it is time to pay
        VBox paymentPane = new VBox();
        paymentPane.setSpacing(BUTTON_SPACING);
        paymentPane.setAlignment(Pos.CENTER);

        Label paymentTypeLabel = new Label("Select Payment Type:");
        paymentTypeLabel.setFont(UTILITY_LABEL_FONT);

        Button payCashButton = new Button("Cash");
        payCashButton.setFont(BUTTON_FONT);
        payCashButton.setOnAction(event -> rootPane.setRight(cashPaymentPane));

        Button payElectronicButton = new Button("Electronic Funds Transfer");
        payElectronicButton.setFont(BUTTON_FONT);
        payElectronicButton.setOnAction(event -> rootPane.setRight(electronicFundsTransferPane));

        paymentPane.getChildren().addAll(paymentTypeLabel, payCashButton, payElectronicButton);

        //This pane is where all the functional buttons/widgets will be stored
        VBox cashierUtilityPane = new VBox();
        cashierUtilityPane.setSpacing(BUTTON_SPACING);
        cashierUtilityPane.setAlignment(Pos.CENTER);

        Label selectedItemOptionsLabel = new Label("Selected Item Options:");
        selectedItemOptionsLabel.setFont(UTILITY_LABEL_FONT);

        Button deselectButton = new Button("Deselect");
        deselectButton.setFont(BUTTON_FONT);
        deselectButton.setOnAction(event -> {
            selectedItem[0] = null;
            //Clearing the formatting of the previous selection
            for(Label label : scannedItemsLabels) {
                label.setBackground(new Background(new BackgroundFill(ROOT_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        });

        Button voidItemButton = new Button("Void Selected Item");
        voidItemButton.setFont(BUTTON_FONT);
        voidItemButton.setOnAction(event -> {
            if(selectedItem[0] != null) {
                int indexOfItemToVoid = customerTicket[0].getItems().indexOf(selectedItem[0]);

                scannedItemsLabels.remove(indexOfItemToVoid);

                scannedItemsPane.getChildren().clear();
                scannedItemsPane.getChildren().addAll(scannedItemsLabels);

                customerTicket[0].voidItem(selectedItem[0]);
            }
        });

        Button changeItemCostButton = new Button("Change Item Cost");
        changeItemCostButton.setFont(BUTTON_FONT);
        changeItemCostButton.setOnAction(event -> {
            TextInputDialog newPriceDialog = new TextInputDialog();
            newPriceDialog.setTitle("Set New Price");
            newPriceDialog.setHeaderText("Please enter the price you would like to change the selected item to. Do not include a dollar sign and use a decimal point if neccesary.");
            newPriceDialog.setContentText("Price (in dollars):");

            Optional<String> inputedNewPrice = newPriceDialog.showAndWait();
            try {
                BigDecimal newCost = new BigDecimal(inputedNewPrice.get());//.isPresent() check not neccesary because this try/catch block is meant to catch ANY mistakes a user might make when entering a number
                int indexOfItemToVoid = customerTicket[0].getItems().indexOf(selectedItem[0]);

                String itemName = selectedItem[0].getName();
                BigDecimal itemPricePerPound = selectedItem[0].getPricePerPound();
                int itemPLU = selectedItem[0].getPLU();
                double itemWeight = selectedItem[0].getWeight();

                scannedItemsLabels.remove(indexOfItemToVoid);

                customerTicket[0].voidItem(selectedItem[0]);

                Item itemWithNewCost = new Item(itemName, itemPricePerPound, newCost, itemPLU, itemWeight);
                customerTicket[0].addItem(itemWithNewCost);

                Label itemWithNewCostLabel = new Label(itemName.toUpperCase() + " : $" + newCost.setScale(2, RoundingMode.HALF_UP));
                itemWithNewCostLabel.setFont(SCANNED_ITEMS_LABEL_FONT);
                itemWithNewCostLabel.setPadding(SCANNED_ITEMS_LABEL_PADDING);
                itemWithNewCostLabel.setOnMouseClicked(event1 -> {
                    //Clearing the formatting of the previous selection
                    for(Label label : scannedItemsLabels) {
                        label.setBackground(new Background(new BackgroundFill(ROOT_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
                    }

                    selectedItem[0] = itemWithNewCost;
                    itemWithNewCostLabel.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                });
                scannedItemsLabels.add(itemWithNewCostLabel);

                scannedItemsPane.getChildren().clear();
                scannedItemsPane.getChildren().addAll(scannedItemsLabels);
            } catch (Exception e){
                Alert incorrectMoneyFormat = new Alert(Alert.AlertType.ERROR);
                incorrectMoneyFormat.setHeaderText("Please enter a correctly formatted dollar amount. Do not include a dollar sign and use a decimal point if neccesary.");
                incorrectMoneyFormat.showAndWait();
            }
        });

        Label weightTextFieldLabel = new Label("Enter the items weight (in lbs): ");
        weightTextFieldLabel.setFont(UTILITY_LABEL_FONT);

        TextField weightTextField = new TextField();

        Label PLUCodeTextFieldLabel = new Label("Search for PLU Code:");
        PLUCodeTextFieldLabel.setFont(UTILITY_LABEL_FONT);

        TextField PLUCodeTextField = new TextField();

        Button PLUSearchButton = new Button("Search for PLU");
        PLUSearchButton.setFont(BUTTON_FONT);
        PLUSearchButton.setOnAction(event -> {
            String enteredPLU = PLUCodeTextField.getText();
            String enteredWeight = weightTextField.getText();

            try {
                Integer.parseInt(enteredPLU);
                Double.parseDouble(enteredWeight);
            } catch (Exception e) {
                Alert emptyTextField = new Alert(Alert.AlertType.ERROR);
                emptyTextField.setHeaderText("Both the PLU code and the weight of the item must be must have valid inputs before searching!");
                emptyTextField.showAndWait();
                return;
            }

            int enteredPLUAsInteger = Integer.parseInt(enteredPLU);
            double enteredWeightAsInteger = Double.parseDouble(enteredWeight);

            Item item = new Item(enteredPLUAsInteger, enteredWeightAsInteger);
            if(item.getName() == null || item.getPricePerPound() == null) {
                Alert invalidPLU = new Alert(Alert.AlertType.ERROR);
                invalidPLU.setHeaderText("Invalid PLU Code!");
                invalidPLU.showAndWait();
            } else {
                weightTextField.setText("");
                PLUCodeTextField.setText("");

                customerTicket[0].addItem(item);

                Label newItemLabel = new Label(item.getName().toUpperCase() + " : " + item.getWeight() + " lbs : $" + item.getCost().setScale(2, RoundingMode.HALF_UP) + " @ $" + item.getPricePerPound() + "/lbs");
                newItemLabel.setFont(SCANNED_ITEMS_LABEL_FONT);
                newItemLabel.setPadding(SCANNED_ITEMS_LABEL_PADDING);
                newItemLabel.setOnMouseClicked(event1 -> {
                    //Clearing the formatting of the previous selection
                    for(Label label : scannedItemsLabels) {
                        label.setBackground(new Background(new BackgroundFill(ROOT_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
                    }

                    selectedItem[0] = item;
                    newItemLabel.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                });

                scannedItemsLabels.add(newItemLabel);

                scannedItemsPane.getChildren().clear();
                scannedItemsPane.getChildren().addAll(scannedItemsLabels);

            }
        });

        Button finishAndPayButton = new Button("Finish and Pay");
        finishAndPayButton.setFont(BUTTON_FONT);
        finishAndPayButton.setOnAction(event -> rootPane.setRight(paymentPane));

        cashierUtilityPane.getChildren().addAll(selectedItemOptionsLabel, deselectButton, voidItemButton, changeItemCostButton, weightTextFieldLabel, weightTextField, PLUCodeTextFieldLabel, PLUCodeTextField, PLUSearchButton, finishAndPayButton);

        //This timer is used to reset everything once the ticket is over
        AnimationTimer checkEndOfTicket = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(customerTicket[0].isTicketEnded()) {
                    orderInfoPane.getChildren().clear();//This is used to get rid of the change label
                    orderInfoPane.getChildren().add(ticketTotalLabel);

                    rootPane.setRight(cashierUtilityPane);

                    scannedItemsLabels.clear();
                    scannedItemsPane.getChildren().clear();

                    customerTicket[0] = new Ticket();

                    weightTextField.setDisable(false);
                    PLUCodeTextField.setDisable(false);
                    PLUSearchButton.setDisable(false);
                    voidItemButton.setDisable(false);
                    deselectButton.setDisable(false);
                    changeItemCostButton.setDisable(false);
                }
            }
        };
        checkEndOfTicket.start();

        //All the cancels buttons are defined here because they must reference panes that are not necessarily defined at the time their parent panes are defined
        Button cancelPaymentButton = new Button("Cancel Finish and Pay");
        cancelPaymentButton.setFont(BUTTON_FONT);
        cancelPaymentButton.setOnAction(event -> rootPane.setRight(cashierUtilityPane));
        paymentPane.getChildren().add(cancelPaymentButton);

        Button cancelEFTButton = new Button("Cancel EFT");
        cancelEFTButton.setFont(BUTTON_FONT);
        cancelEFTButton.setOnAction(event -> rootPane.setRight(paymentPane));
        electronicFundsTransferPane.getChildren().add(cancelEFTButton);

        Button cancelCashPaymentButton = new Button("Back to Finish and Pay");
        cancelCashPaymentButton.setFont(BUTTON_FONT);
        cancelCashPaymentButton.setOnAction(event -> {
            rootPane.setRight(paymentPane);
            if(customerTicket[0].isAmountStillOwed()) {
                weightTextField.setDisable(true);
                PLUCodeTextField.setDisable(true);
                PLUSearchButton.setDisable(true);
                voidItemButton.setDisable(true);
                deselectButton.setDisable(true);
                changeItemCostButton.setDisable(true);
            }
        });
        cashPaymentPane.getChildren().add(cancelCashPaymentButton);

        Button finishCashPaymentButton = new Button("Finish Cash Payment");
        finishCashPaymentButton.setFont(BUTTON_FONT);
        finishCashPaymentButton.setOnAction(event -> {
            customerTicket[0].setTicketEnded(true);
        });
        finishCashPaymentPane.getChildren().addAll(finishCashPaymentButton);

        final int WINDOW_WIDTH = 800;
        final int WINDOW_HEIGHT = 650;

        rootPane.setCenter(scannedItemsScrollPane);
        rootPane.setBottom(orderInfoPane);
        rootPane.setRight(cashierUtilityPane);
        rootPane.setBackground(new Background(new BackgroundFill(ROOT_BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        rootStage.setTitle("POS");
        rootStage.setScene(new Scene(rootPane, WINDOW_WIDTH, WINDOW_HEIGHT));
        rootStage.show();
    }
}
