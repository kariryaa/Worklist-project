import java.util.Scanner;

public class T3 {

	public static void main(String[] args) {
		
		int i,c,b;
		// i: [UNKNOWN, UNKNOWN]
		i=200;
		// i: [200,200]
		while( i > 100) 
		{
			// i: [0,99]	
			i = i+2;
			// i: [2,101]
			if (i < 20) 
			{	
				// i: [2,19]
				c=1;
			}
			
			i=i+1;
			// i: [3,102]
		}
		b=0;
		// i: [100,102]
	}
}
