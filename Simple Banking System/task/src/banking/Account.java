package banking;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Account {
    private String pinNumber;
    private boolean loggedIn;
    private  final  int[] issuerIdentificationNumberArray = new int[] {4, 0, 0, 0, 0, 0};
    private static final int accountNumberSize = 9;
    private static final int cardNumberSize = 16;
    private final Scanner scanner;
    private final  IAccountValidator accountValidator;
    private Random random;
    private int balance;
    private final int[] cardNumberArray;
    private final int[] accountNumberArray;
    private Card card;
    private Card currentLoggedInCard;
    private final IAccountDataStore accountDataStore;
    private final String databaseUrl;
    public Account(Scanner scanner, IAccountValidator accountValidator, IAccountDataStore accountDataStore) {
        this.scanner = scanner;
        this.accountValidator = accountValidator;
        this.accountDataStore = accountDataStore;
        databaseUrl = this.accountDataStore.getUrl();
        loggedIn = false;
        cardNumberArray = new int[cardNumberSize];
        accountNumberArray = new int[accountNumberSize];
        setIssuerIdentificationNumber();
    }
    public void createAccount() {
        this.pinNumber = generatePinNumber();
        //this.accountNumber = generateAccountNumber();
        generateCardNumber();
        String number = this.getCardNumber();
        String pinNumber = this.getPinNumber();
        int balance = this.getBalance();
        this.card = new Card(number, pinNumber, balance);
        accountDataStore.insertCard(this.card);
        this.balance = 0;

    }
    private void setIssuerIdentificationNumber( ) {
        System.arraycopy(this.issuerIdentificationNumberArray, 0, this.cardNumberArray, 0, this.issuerIdentificationNumberArray.length);
    }
    private void setAccountNumber() {
        System.arraycopy(this.accountNumberArray, 0, this.cardNumberArray,
                this.issuerIdentificationNumberArray.length, this.accountNumberArray.length);
    }

    private void setCheckSum(int checkSum) {
        this.cardNumberArray[cardNumberSize - 1] = checkSum;
    }

    private boolean signIn(String password, String cardNumber){
        return  accountValidator.validateCardCredentials(password,cardNumber);
    }

    private void printDetailsOnAccountCreation() {
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(getPinNumber());
    }
    public void processLogInMenuInput() {
        accountDataStore.initializeApplication(databaseUrl);
        var userInput = scanner.nextLine();
        int option = Integer.parseInt(userInput);

        switch (option) {
            case 1:
                createAccount();
                printDetailsOnAccountCreation();
                logInMenu();
                processLogInMenuInput();
                break;
            case 2:
                logIn();
                break;

            case 0:
                printExitMessage();
                System.exit(0);
                accountDataStore.close();
                break;

        }

    }
    private void logIn(){
        System.out.println("Enter your card number:");
        String cardNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        String pin = scanner.nextLine();
        boolean signed = signIn(pin, cardNumber);
        if (signed) {
            currentLoggedInCard = new Card(cardNumber,pin);
            printLoginMessage();
            logOutMenu();
            processLogOutMenuInput();
        }
        else {
            System.out.println("Wrong card number or PIN!");
            logInMenu();
            processLogInMenuInput();
        }
    }

    public void processLogOutMenuInput() {
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1:
                printBalance();
                logOutMenu();
                processLogOutMenuInput();
                break;
            case 2:
                //TODO: add income
                addIncome();
                logOutMenu();
                processLogOutMenuInput();
                break;
            case 3:
                //TODO: Do transfer
                doTransfer();
                logOutMenu();
                processLogOutMenuInput();
                break;
            case 4:
                //TODO: Close account
                closeAccount();
                logInMenu();
                processLogInMenuInput();
                break;
            case 5:
                loggedIn = false;
                currentLoggedInCard = null;
                accountDataStore.close();
                printLogOutMessage();
                logInMenu();
                processLogInMenuInput();
                break;

            case 0:
                printExitMessage();
                System.exit(0);
                accountDataStore.close();
                break;
            default:
                logOutMenu();
                processLogOutMenuInput();
                        break;

        }

    }
    private void addIncome(){
        System.out.println("Enter Income:");
        int newIncome = scanner.nextInt();// Integer.parseInt(scanner.nextLine()) ;
        scanner.nextLine();
        var insert = accountDataStore.deposit(this.currentLoggedInCard.getNumber(),
                this.currentLoggedInCard.getPin(), newIncome);
        System.out.println("Income was added!");
    }

    private void doTransfer() {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String receivingCardNumber = this.scanner.nextLine();
        boolean isCardNumberLuhnValid = AccountUtils.checkLuhnValidity(receivingCardNumber);
        if (!isCardNumberLuhnValid) {
            System.out.println("Probably you made mistake in the card number. Please try again!");
            return;
        }
        Card dbCard = accountDataStore.getCard(receivingCardNumber);
       // boolean cardNumberExists = accountDataStore.cardExists(currentLoggedInCard);
        if (null == dbCard
               // || !cardNumberExists
        ) {
            System.out.println("Such a card does not exist.");
            return;
        }
        int currentBalance = accountDataStore.getBalance(currentLoggedInCard.getNumber(),
                currentLoggedInCard.getPin());
        System.out.println("Enter how much money you want to transfer:");
        int transferAmount = Integer.parseInt(scanner.nextLine());
        if (transferAmount > currentBalance) {
            System.out.println("Not enough money!");
            return;
        }
        boolean success = accountDataStore.transfer(currentLoggedInCard.getNumber(),
                receivingCardNumber, transferAmount);
        if (success) {
           // System.out.println("Success!");
        }
        System.out.println("Success!");

    }
    public void closeAccount() {
            boolean accountClosed = accountDataStore.deleteAccount(currentLoggedInCard);
            if (accountClosed) {
                System.out.println("The account has been closed!");
            }
    }

    public void logInMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }
    private void  logOutMenu(){
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }
    private void printBalance(){
        int balance = accountDataStore.getBalance(currentLoggedInCard.getNumber(), currentLoggedInCard.getPin());
        System.out.println("Balance: " + balance);
    }
    private void printLogOutMessage() {
        System.out.println("You have successfully logged out!");
    }
    private void printLoginMessage() {
        System.out.println("You have successfully logged in!");
    }
    private void printExitMessage(){
        System.out.println("Bye!");
    }
    public int getBalance() {
        return balance;
    }

    public String getPinNumber() {
        return pinNumber;
    }

    public String getCardNumber() {
        return AccountUtils.arrayToString(this.cardNumberArray);
    }

    private int[] generatePinArray(){
        int[] pin = new int[4];
        for (int i = 0; i < 4; i++) {
            random = new Random();
            pin[i] = random.nextInt(10);
        }
        return  pin;
    }
    private int[] generateAccountNumberArray() {
        int[] accountNum = new int[9];
        for (int i = 0; i < 9; i++) {
            random = new Random();
            accountNum[i] = random.nextInt(10);
        }
        return accountNum;
    }
    public void generateCardNumber() {
        int[] accountNumberArrayTemp = generateAccountNumberArray();
        System.arraycopy(accountNumberArrayTemp, 0, this.accountNumberArray, 0, accountNumberArrayTemp.length);
        setAccountNumber();
        int checkSum = AccountUtils.generateCheckSum(this.cardNumberArray);
        setCheckSum(checkSum);
    }





    private String generatePinNumber() {
        int[] pin = generatePinArray();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            result.append(pin[i]);
        }
        return  result.toString();
    }



}