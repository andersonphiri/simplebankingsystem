package banking;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String dbPath = getFilePath(args);
        if(null == dbPath ){
            System.exit(0);
        }
        else {
            //System.out.println("Db file path value was " + dbPath);
            new AccountManager(dbPath).manage();
        }


    }

    private static String getFilePath(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-fileName".equals(args[i])) {
                return   args[i + 1];
            }
        }
        return null;
    }
}

