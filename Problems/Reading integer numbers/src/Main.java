import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        // put your code here
        Scanner scanner = new Scanner(System.in);
        String[] nums = scanner.nextLine().split("\\s+");
        for (String num:
             nums) {
            System.out.println(num);
        }
    }
}