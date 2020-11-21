package banking;

public class AccountValidator implements  IAccountValidator {

    private  Account account;
    private final  IAccountDataStore accountDataStore;
    public AccountValidator(IAccountDataStore accountDataStore) {
            this.accountDataStore = accountDataStore;
    }

    @Override
    public boolean validateCard(Card card) {
        return accountDataStore.cardExists(card) ;
    }

    @Override
    public boolean validateCardNumber(Card card) {
        int sum = 0;
        String cardNumber = card.getNumber();
        int length = cardNumber.length();
        int[] toSum = new int[length];

        int checkSum = Integer.parseInt(Character.toString(cardNumber.charAt(length - 1)));

        for (int i = 0; i < length; i++) {
            toSum[i] = Integer.parseInt(Character.toString(cardNumber.charAt(i)));
        }

        int[] doubled = AccountUtils.doubleOdds(toSum);
        int[] normalized = AccountUtils.normalizeOverNine(doubled);
        sum = AccountUtils.sum(normalized) - normalized[length - 1];

        return (sum + checkSum) % 10 == 0;
    }

    @Override
    public boolean validateCardNumber(String cardNumber) {
        int sum = 0;
        int length = cardNumber.length();
        int[] toSum = new int[length];

        int checkSum = Integer.parseInt(Character.toString(cardNumber.charAt(length - 1)));

        for (int i = 0; i < length; i++) {
            toSum[i] = Integer.parseInt(Character.toString(cardNumber.charAt(i)));
        }

        int[] doubled = AccountUtils.doubleOdds(toSum);
        int[] normalized = AccountUtils.normalizeOverNine(doubled);
        sum = AccountUtils.sum(normalized) - normalized[length - 1];

        return (sum + checkSum) % 10 == 0;
    }

    @Override
    public boolean validatePinNumber(Card card) {

        return accountDataStore.getCard(card.getNumber(), card.getPin()) != null;
    }
    @Override
    public boolean validateCardCredentials(String pin, String cardNumber) {
        return accountDataStore.getCard(cardNumber, pin) != null && validateCardNumber(cardNumber);
    }
}
