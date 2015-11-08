public class T7 {

	public static void main(String[] args) {
		
		int x = 0; 
		// X: [0,0]
		int y = 0;
		// X: [0, 100]
		// y: [0,0]
		while( x <= 99 ) {
		       // X: [0,99]
		       // Y: [-INF, INF]
			if (x<=49) {
			      // X: [0,49]			      
				x = x+1;
      			y = y+1;
      			// X: [1,50]
			}
			else {
			     // X: [50,99]
      			x = x+1;
      			y = y-1;
      			// X: [51, 100]
      		}
      		// X: [1, 100]
		}
		// X: [100,100]
		if (y < 0) {
		     // Y: [-INF, -1]
			return;
		}
		// Y: [0, INF]
	}
}
