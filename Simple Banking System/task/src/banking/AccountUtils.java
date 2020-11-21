package banking;

import java.util.Arrays;

public  class AccountUtils {
    public static int sum(int[] array) {
        return Arrays.stream(array).sum();
    }
    public static int[] doubleOdds(int[] array) {
        int length = array.length;
        int[] result = new int[length];
        System.arraycopy(array, 0, result, 0, length);
        int temp;
        for (int i = 0; i < length - 1  ; i += 2) {
            temp = array[i];
            result[i] = 2 * temp;
        }
        return result;
    }
    public static int[] normalizeOverNine(int[] array) {
        int length = array.length;
        int[] result = new int[length];
        int temp;
        for (int i = 0 ; i < length - 1; i++) {
            temp = array[i];
            result[i] = temp > 9 ? temp - 9 : temp;
        }
        return result;
    }

    public static  int[] stringToIntArray(String inputString) {
        int length = inputString.length();
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = Integer.parseInt(Character.toString(inputString.charAt(i)));
        }
        return result;
    }

    public static String arrayToString(int[] array) {
        StringBuilder result = new StringBuilder();
        for (int num :
                array) {
            result.append(num);
        }
        return  result.toString();
    }

    public static int generateCheckSum(int[] inputArray) {
        int[] doubleOdds = doubleOdds(inputArray);
        int[] toSum = normalizeOverNine(doubleOdds);
        int sum = sum(toSum) - toSum[toSum.length - 1];
        int mod = sum % 10;
        return mod == 0 ? 0 : 10 - mod;
    }
    public static  boolean checkLuhnValidity(String inputString) {
        if (inputString.length() < 1) {
            return false;
        }
            int[] cardArray = stringToIntArray(inputString);
            int checkSum = generateCheckSum(cardArray);
            return cardArray[inputString.length() - 1] == checkSum;
    }
}
