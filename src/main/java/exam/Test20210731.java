package exam;

import java.util.Arrays;
import java.util.Scanner;

public class Test20210731 {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String nm = scanner.nextLine();
		int n = Integer.parseInt(nm.split(" ")[0]);
		int m = Integer.parseInt(nm.split(" ")[1]);
		String[] aa = scanner.nextLine().split(" ");
		String[] bb = scanner.nextLine().split(" ");
		//
		long[] bsort = new long[m];
		for (int i = 0; i < m; i++) {
			bsort[i] = Long.parseLong(bb[i]);
		}
		Arrays.sort(bsort);
		//
		int num = 0;
		for (int i = 0; i < n; i++) {
			long a = Long.parseLong(aa[i]);
			//
			int fu = 0;
			for (int j = 0; j < m; j++) {
				if (a > bsort[j])
					fu++;
				else if (a == bsort[j]) {
					fu = 1;
					break;
				} else
					break;
			}
			if (fu % 2 == 0)
				num++;
			//
		}
		System.out.println(num);
	}

}
