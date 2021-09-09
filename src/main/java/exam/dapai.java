package exam;

import java.util.Arrays;
import java.util.Scanner;

public class dapai {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String[] s1 = scanner.nextLine().split(" ");
        int[] he=new int[s1.length];
        for(int i=0;i< s1.length;i++){
            he[i]=Integer.parseInt(s1[i]);
        }
        String[] s2 = scanner.nextLine().split(" ");
        int[] my=new int[s2.length];
        for(int i=0;i< s2.length;i++){
            my[i]=Integer.parseInt(s2[i]);
        }
    }
}
