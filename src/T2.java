public class T2 {

	public static void main(String[] args) 
	{	
		int a,b,c,d,i;
		i=0; // i : [0,0]
		a=1; // a : [1,1]
		b=2; // b : [2,2]
		c=3; // c : [3,3]
		d=4; // d : [4,4]
		
		if( i < 100) 
		{			     	
			a = i+3;  // a : [3,3]
			b= a*2;   // b : [6,6]
			c= b/3;   // c : [2,2]
			d = d-4;   // d : [0,0]
		}
		else 
		{
			a= a*2;    // a : [2,2]
			b = d-2;   // b : [2,2]
			c = b + 2; // c : [4, 4]
			d = a/2;   // d : [1,1] 	
		}
		// a : [2, 3]
		// b : [2, 6]
		// c : [2,4]			
		// d : [0, 1]
		i = i+1;
		//  i : [1,1]
	}
}


