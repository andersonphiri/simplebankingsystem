package banking;

import java.util.List;

public interface IAccountDataStore {
    int initializeApplication(String dbUrl);

    int insertCard(Card card);

    List<Card> getCards();

    Card getCard(String cardNumber, String pinNumber);
    Card getCard(String cardNumber);
    boolean cardExists(Card card);
    boolean cardExists(String password, String cardNumber);
    boolean transfer(String fromCardNumber, String toCardNumber, int transferAmount);
    boolean deposit(String cardNumber, String pinNumber, int newMoney);
    boolean withdraw(String cardNumber, String pinNumber, int withdrawalAmount);
    int getBalance(String cardNumber, String pinNumber);
    boolean deleteAccount(Card card);
    void close();
    String getUrl();
}
