package banking;

public class Card {
    private int id = -1;
    private String number;
    private String pin;
    private int balance;

    public Card(String number, String pin, int balance) {

        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }
    public Card(String number, String pin) {
        this.number = number;
        this.pin = pin;
    }
    public void setNumber(String cardNumber) {

    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void addBalance(int additionalBalance) {
        this.balance += additionalBalance;
    }

    public Card(int id, String number, String pin, int balance) {
        this.id = id;
        this.number = number;
        this.pin = pin;
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public String getNumber() {
        return number;
    }

    public String getPin() {
        return pin;
    }

    public int getId() {
        return id;
    }
}
