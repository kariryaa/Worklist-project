public class T6 {

	public static void main(String[] args) {
	
		int x = 1;
		// X: [1,20]
		while ( x < 1000) {
			// X: [1,20]=> Widening + Narrowing
			// X: [1, 999] => Widening
			x = x + 1;
			if ( x > 20 ) break ;
		}		
		//X: [21,21]
	}
}


