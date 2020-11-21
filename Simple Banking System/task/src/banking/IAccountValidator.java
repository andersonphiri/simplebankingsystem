package banking;

public interface IAccountValidator {
    boolean validateCard(Card card);
    boolean validateCardNumber(Card card);
    boolean validatePinNumber(Card card);
    boolean validateCardNumber(String cardNumber);
    boolean validateCardCredentials(String pin, String cardNumber);
}
