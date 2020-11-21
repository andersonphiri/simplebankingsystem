package banking;

import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDataStore implements IAccountDataStore {
    private String url;
    private SQLiteDataSource sqLiteDataSource;
    private Connection connection;
    private boolean connectionValid;
    private static final  int connectionTimeout = 10;
    private static final String dbFileName = "card.s3db";
    public AccountDataStore(String databaseUrl) {

        initializeApplication(databaseUrl);
    }
    private boolean isConnectionValid() {
        return  connectionValid;
    }
    private void setConnection() {
        try {
            this.connection = sqLiteDataSource.getConnection();
            this.connectionValid = this.connection.isValid(connectionTimeout);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public int initializeApplication(String databaseUrl) {
        File dbFile = new File( "."+File.separator + dbFileName);
        if (dbFile.exists()) {
            sqLiteDataSource = new SQLiteDataSource();
            url = databaseUrl;
            sqLiteDataSource.setUrl(this.url);
            setConnection();
            return 1;
        }
        sqLiteDataSource = new SQLiteDataSource();
        url = databaseUrl;
        sqLiteDataSource.setUrl(this.url);
        setConnection();

        String query = "CREATE TABLE IF NOT EXISTS card("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "number TEXT NOT NULL, "
                + "pin TEXT NOT NULL, "
                + "balance INTEGER DEFAULT 0 )";
        return  executeUpdateQueryUtil(query);
    }

    private int executeUpdateQueryUtil(String query) {
        int result = -1;
        try {
            Statement statement = connection.createStatement();
            result = statement.executeUpdate(query);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return  result;
    }
    private int executeCountQuery(String query) {
        int result = -1;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return  result;
    }

    @Override
    public int insertCard(Card card) {
        int result = -1;
        String query = "insert into card(number, pin, balance) values (?, ?, ?)";
        String query2 = "insert into card(number, pin, balance) values ('"+card.getNumber()
                +"', '"+card.getPin() + "' , "+card.getBalance() + ")";

        turnOnAutoCommit(false);

        try (

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                //Statement statement = connection.createStatement();
        ) {
            //Savepoint savepoint = connection.setSavepoint();
            preparedStatement.setString(1, card.getNumber());
            preparedStatement.setString(2, card.getPin());
            preparedStatement.setInt(3, card.getBalance());
            result = preparedStatement.executeUpdate();



            //result = statement.executeUpdate(query2);
            connection.commit();
        } catch(SQLException e) {
            if (this.connection != null){
                try {
                    System.err.println("Transaction is being rolled back");
                    connection.rollback();
                    e.printStackTrace();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // turnOnAutoCommit(true);
        return  result;
    }
    private void turnOnAutoCommit(boolean on) {
        try {
            if (!this.connection.isValid(connectionTimeout)){
                sqLiteDataSource = new SQLiteDataSource();
                sqLiteDataSource.setUrl(url);
                connection = sqLiteDataSource.getConnection();
            }
            this.connection.setAutoCommit(on);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public List<Card> getCards() {
        List<Card> results = new ArrayList<>();
        Card card = null;
        String query = "SELECT * FROM card";
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {

                card = new Card(resultSet.getInt("id"),
                        resultSet.getString("number"), resultSet.getString("pin"),
                        resultSet.getInt("balance"));
                results.add(card);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return  results;
    }

    @Override
    public Card getCard(String cardNumber, String pinNumber) {
        String query = "SELECT * FROM card WHERE number = ? AND PIN = ?";
        Card card = null;
        try (
             PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pinNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                card = new Card(resultSet.getInt("id"),
                        resultSet.getString("number"),
                        resultSet.getString("pin"),
                        resultSet.getInt("balance"));

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return  card;
    }
    @Override
    public Card getCard(String cardNumber) {
        String query = "SELECT * FROM card WHERE number = ?";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setString(1, cardNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {

               return new Card(resultSet.getInt("id"),
                        resultSet.getString("number"),
                        resultSet.getString("pin"),
                        resultSet.getInt("balance"));

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return  null;
    }

    @Override
    public boolean cardExists(Card card) {
        return cardExists(card.getPin(), card.getNumber());
    }

    @Override
    public boolean cardExists(String password, String cardNumber) {
//        String query = "SELECT * FROM card WHERE number = '"+ cardNumber
//                + "' AND pin = '" + password + "'";
        String query = "SELECT * FROM card WHERE number = ? AND pin = ?";
        boolean result = false;
        try ( PreparedStatement preparedStatement = connection.prepareStatement(query);

        ) {
            preparedStatement.setString(1,cardNumber );
            preparedStatement.setString(2,password );
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                result = true;
                break;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return  result;
    }

    @Override
    public boolean transfer(String fromCardNumber, String toCardNumber, int transferAmount) {
        String queryFrom = "UPDATE card SET balance = balance - ? WHERE number = ?";
        String queryTo = "UPDATE card SET balance = balance + ? WHERE number = ?";
        turnOnAutoCommit(false);
        try (
                PreparedStatement preparedStatementFrom = connection.prepareStatement(queryFrom);
                PreparedStatement preparedStatementTo = connection.prepareStatement(queryTo);

        ) {
            preparedStatementFrom.setInt(1, transferAmount);
            preparedStatementFrom.setString(2, fromCardNumber);
            preparedStatementFrom.executeUpdate();

            preparedStatementTo.setInt(1, transferAmount);
            preparedStatementTo.setString(2, toCardNumber);
            preparedStatementTo.executeUpdate();
            connection.commit();
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException se) {
                se.printStackTrace();
            }
            return false;
        }
    }

    private boolean depositWithdrawalUtil(String cardNumber, String pinNumber,
                                          int newMoney, String operator) {
        boolean result = false;
        boolean cardExists = cardExists(pinNumber, cardNumber);
        if (!cardExists) {
            return  false;
        }
        String query = "UPDATE card SET balance = balance " + operator
                + " ? WHERE number = ? AND pin = ?";
        turnOnAutoCommit(false);

        try (

                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setInt(1, newMoney);
            preparedStatement.setString(2, cardNumber);
            preparedStatement.setString(3, pinNumber);
            int cnt = preparedStatement.executeUpdate();
            connection.commit();
            result = cnt > 0;
        } catch(SQLException e) {
            if (this.connection != null){
                try {
                    System.err.println("Transaction is being rolled back");
                    connection.rollback();

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            result = false;
        }
        return result;
    }

    @Override
    public boolean  deposit(String cardNumber, String pinNumber, int newMoney) {
        if (newMoney < 0){
            return false;
        }
        return depositWithdrawalUtil(cardNumber, pinNumber, newMoney, "+");
    }

    @Override
    public boolean withdraw(String cardNumber, String pinNumber, int withdrawalAmount) {
        if (withdrawalAmount < 0){
            return false;
        }
        return depositWithdrawalUtil(cardNumber, pinNumber, withdrawalAmount, "-");
    }

    @Override
    public int getBalance(String cardNumber, String pinNumber) {
        int balance = 0;
        String query = "SELECT balance from card WHERE number = ? AND pin = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.setString(1, cardNumber);
            preparedStatement.setString(2, pinNumber);
            ResultSet   resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                balance = resultSet.getInt("balance");
                break;
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return balance;
    }

    @Override
    public boolean deleteAccount(Card card) {
        boolean result = false;
        boolean cardExists = cardExists(card.getPin(), card.getNumber());
        if (!cardExists) {
            return  false;
        }
        String query = "DELETE from card where number = ? AND pin = ?";
        turnOnAutoCommit(false);

        try (PreparedStatement preparedStatement = connection.prepareStatement(query);)
        {
            preparedStatement.setString(1, card.getNumber());
            preparedStatement.setString(2, card.getPin());
            int cnt = preparedStatement.executeUpdate();
            connection.commit();
            result = cnt > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getUrl() {
        return url;
    }
}
