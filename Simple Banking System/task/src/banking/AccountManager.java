package banking;

import java.io.File;
import java.util.Scanner;

public class AccountManager {
    private  Account account;
    private IAccountDataStore accountDataStore;
    private IAccountValidator accountValidator;
    private static final Scanner scanner = new Scanner(System.in);
    private static final String jdbcConnectionStringProtocol = "jdbc:sqlite:."+ File.separator;

    public AccountManager(String databasePath) {
        accountDataStore = new AccountDataStore(generateConnectionString(databasePath));
        accountValidator = new AccountValidator(accountDataStore);
        account = new Account(scanner, accountValidator, accountDataStore);
    }

    public void manage() {
        this.account.logInMenu();
        this.account.processLogInMenuInput();
    }

    private static String generateConnectionString(String databaseFileName) {
        return jdbcConnectionStringProtocol + databaseFileName;
    }

}
