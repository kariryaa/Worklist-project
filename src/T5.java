import java.util.Scanner;

public class T5 {

	public static void main(String[] args) {
	
		Scanner scan = new Scanner(System.in);
		int j = scan.nextInt();
		System.out.println("j is " + j);		
		
		// j : [-INF, INF]		
		while( true ) {
			// j : [-INF, INF]
			if (j >  0) {
				j = j+10;
			}
			else {
				j = j-10;
			}
		}
	}
}

