package exam;

import java.util.Scanner;

public class JIajia {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int t = Integer.parseInt(scanner.nextLine());
        int[] abs=new int[t];
        int max=0;
        for (int i = 0; i < t; i++) {
            String ab = scanner.nextLine();
            int a = Integer.parseInt(ab.split(" ")[0]);
            int b = Integer.parseInt(ab.split(" ")[1]);
            abs[i]=Math.abs(a-b);
            if(abs[i]>max){
                max=abs[i];
            }
        }
        int[] result = minMove(abs,max);
        for(int x:result){
            System.out.println(x);
        }
    }
    private static int[] minMove(int[] abs, int max){
        int[] result =new int[abs.length];
        for(int i=0;i< abs.length;i++){
            double a=(Math.sqrt((double)abs[i]*8+1)-1)/2;
            int b=(int)a;
            result[i]=b+(abs[i]-b*(b+1)/2)*2;
            int c=(b+1)*(b+2)/2-abs[i];
            if(c%2==0 && c/2<=b+1)
                result[i]=b+1;
        }
        return result;
    }

    private static int[] minMove2(int[] abs, int max) {
        int[] result =new int[abs.length];
        for(int i=0, add=1;i<max;i+=add,add++){
            for(int j=0;j<abs.length;j++){
                if(abs[j]>=i &&abs[j]<i+add){
                    result[j]=add-1+(abs[j]-i)*2;
                }
            }
        }
        return result;
    }
}
