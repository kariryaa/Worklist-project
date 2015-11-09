
public class SimpleInterval 
{
	String name="temp";
	long l=1;
	long r=-1;
	//boolean change = true;
	boolean inf = true;
	boolean ninf = true;
	boolean bottom = false;
	
	public SimpleInterval(SimpleInterval s)
	{
		name=s.name;
		l=s.l;
		r=s.r;
		inf=s.inf;
		ninf=s.ninf;
		bottom=s.bottom;
	}
	
	public SimpleInterval(long n)
	{
		name="temp";
		l=n;
		r=n;
		inf=false;
		ninf=false;
		bottom=false;
	}
	
	public SimpleInterval(String n)
	{
		name = n;
		init();
	}
	
	String getName()
	{
		return name;
	}
	
	void copy(SimpleInterval s)
	{
		s.name=name;
		this.copyValues(s);
	}
	
	void copyValues(SimpleInterval s)
	{
		s.l=l;
		s.r=r;
		s.inf=inf;
		s.ninf=ninf;
		s.bottom=bottom;
	}
	
	void negate()
	{
		long temp=l;
		l=-r;
		r=-temp;
	}
	
	void print()
	{
		System.out.print(name + ":- ");
		
		if(bottom)
		{
			System.out.println("PHI");
			return;
		}
		if(ninf)
			System.out.print("( -INF");
		else
			System.out.print("[ "+ this.l);
		
		System.out.print(" , ");
		
		if(inf)
			System.out.print("INF )");
		else
			System.out.print(this.r +" ]");
		System.out.println();
	}
	
	boolean	contains(SimpleInterval s)
	{
		//modify this to include cases for inf and ninf
		boolean flag = true;
		if(s.bottom)
			return true;
		if(bottom)
			return false;
		if(!inf)
		{
			if(s.inf)
				flag = false;
			else if(r < s.r)
				flag = false;
		}
		if(!ninf)
		{
			if(s.ninf)
				flag = false;
			else if(l>s.l)
				flag = false;
		}
		return flag;		
	}
	
	boolean	isEqualTo(SimpleInterval s) 
	{
		if(this.bottom)
		{
			if(s.bottom)
				return true;
			else
				return false;
		}
		else if(s.bottom)
				return false;
		
		return (this.l == s.l && this.r == s.r);
	}
	
	boolean containElement(long ele)
	{
		if(bottom)
			return false;
		if(l <= ele && ele <= r)
			return true;
		return false;
	}
	
	private long min(long a, long b)
	{
		return a<b?a:b;
	}
	private long max(long a, long b) 
	{
		return a>b?a:b;
	}
	
	void equal(long n)
	{
		l=n;
		r=n;
		inf=ninf=bottom=false;
	}
	
	void intersection(SimpleInterval s1, SimpleInterval s2) 
	{
		if(s1.bottom || s2.bottom)
		{
			bottom=true;
			return;
		}
		if(!s1.ninf&&!s2.ninf)
		{
			l = max(s1.l,s2.l);
			ninf=false;
		}
		else
		{
			ninf=true;
		}
		if(!s1.inf&&!s2.inf)
		{
			r = min(s1.r,s2.r);
			inf=false;
		}
		else
		{
			inf=true;
		}
		bottom = false;
		this.check();
	}
	
	void LTEQ(SimpleInterval b)
	{
		if(b.inf)
			return;
		if(inf)
		{
			r = b.r;
			inf=false;
		}
		else
			r=min(b.r,r);
		this.check();
	}
	void GTEQ(SimpleInterval b)
	{
		if(b.ninf)
			return;
		if(ninf)
		{
			l = b.l;
			ninf=false;
		}
		else
			l=max(l,b.l);
		this.check();
	}
	void GT(SimpleInterval b)
	{
		if(b.ninf)
			return;
		if(ninf)
		{
			l = b.l+1;
			ninf=false;
		}
		else
			l=max(l,b.l+1);
		this.check();
	}
	void LT(SimpleInterval b)
	{
		//System.out.println("\t\t\tIn LT!!!!!!!!!!!!!");
		//print();
		//b.print();
		if(b.inf)
			return;
		if(inf)
		{
			r = b.r-1;
			inf=false;
		}
		else
			r=min(b.r-1,r);
		this.check();
	}
	void EQ(SimpleInterval b)
	{
		this.copyValues(b);
	}
	void NQ(SimpleInterval b)
	{
		//no change
	}
	
	void check()
	{
		if(!inf && !ninf && l>r)
		{
			bottom=true;
			l=1;
			r=-1;
			inf=false;
			ninf=false;
		}
	}
	
	void init()
	{
		bottom=false;
		l=1;
		r=-1;
		inf=true;
		ninf=true;
	}
	void union(SimpleInterval s1, SimpleInterval s2) 
	{
		if(s1.bottom && s2.bottom)
		{
			bottom=true;
			return;
		}
		if(s1.bottom)
		{
			s2.copyValues(this);
			return;
		}
		if(s2.bottom)
		{
			s1.copyValues(this);
			return;
		}
		if(s1.ninf||s2.ninf)
		{
			ninf=true;
		}
		else
		{
			l = min(s1.l,s2.l);
			ninf=false;
		}
		if(s1.inf||s2.inf)
		{
			inf=true;
		}
		else
		{
			r = max(s1.r,s2.r);
			inf=false;
		}
		bottom = false;
	}
	
	void add(SimpleInterval s1, SimpleInterval s2)
	{
		if(s1.bottom || s2.bottom)
		{
			bottom=true;
			return;
		}

		if(s1.ninf||s2.ninf)
		{
			ninf=true;
		}
		else
		{
			l = s1.l + s2.l;
			ninf=false;
		}
		if(s1.inf||s2.inf)
		{
			inf=true;
		}
		else
		{
			r = s1.r + s2.r;
			inf=false;
		}
		bottom = false;
	}
	
	void subtract(SimpleInterval s1, SimpleInterval s2)
	{
		if(s1.bottom || s2.bottom)
		{
			bottom=true;
			return;
		}
		
		if(s1.ninf||s2.ninf)
		{
			ninf=true;
		}
		else
		{
			l = s1.l - s2.r;
			ninf=false;
		}
		if(s1.inf||s2.inf)
		{
			inf=true;
		}
		else
		{
			r = s1.r - s2.l;
			inf=false;
		}
		bottom = false;
	}
	
	void multiply(SimpleInterval s1, SimpleInterval s2)
	{
		if(s1.bottom || s2.bottom)
		{
			bottom=true;
			return;
		}
		
		long s1l=s1.l,s2l=s2.l,s1r=s1.r,s2r=s2.r;
		if(s1.ninf||s2.ninf)
		{
			ninf=true;
		}
		else
		{
			l = min(s1l*s2l, min(s1l*s2r, min(s1r*s2l, s1r*s2r)));
			ninf=false;
		}
		if(s1.inf||s2.inf)
		{
			inf=true;
		}
		else
		{
			r = max(s1l*s2l, max(s1l*s2r, max(s1r*s2l, s1r*s2r)));
			inf=false;
		}
		bottom = false;
	}
	
	void divide(SimpleInterval s1, SimpleInterval s2)
	{
		if(s1.bottom || s2.bottom)
		{
			bottom=true;
			return;
		}
		// what about divide by zero?
		long s1l=s1.l,s2l=s2.l,s1r=s1.r,s2r=s2.r;
		if(s1.ninf||s2.ninf)
		{
			ninf=true;
		}
		else
		{
			l = min(s1l/s2l,min(s1l/s2r,min(s1r/s2l,s1r/s2r)));
			ninf=false;
		}
		if(s1.inf||s2.inf)
		{
			inf=true;
		}
		else
		{
			r = max(s1l/s2l,max(s1l/s2r,max(s1r/s2l,s1r/s2r)));
			inf=false;
		}	
		bottom = false;
	}
	
	// overrides the equal method
	
	public boolean equals(Object o) 
	{
		//boolean flag=true;
		if(o instanceof SimpleInterval)
		{
			SimpleInterval obj = (SimpleInterval)o;
			if(bottom && obj.bottom)
				return true;
			if(bottom)
				return false;
			if(obj.bottom)
				return false;
			//case for inf and ninf
			if(obj.inf && !inf)
			{
				return false;
			}
			if(!obj.inf && inf)
			{
				return false;
			}
			if(obj.ninf && !ninf)
			{
				return false;
			}
			if(!obj.ninf && ninf)
			{
				return false;
			}
			if(obj.inf&&inf)
			{
				if(ninf&&obj.ninf)
				{
					return true;
				}
				else
				{
					return obj.l==l;
				}
			}
			else if(r==obj.r)
			{
				if(ninf&&obj.ninf)
				{
					return true;
				}
				else
				{
					return obj.l==l;
				}
			}
			else
				return false;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return 1;
	}
	
	
	void widening(SimpleInterval s1)
	{
		if(s1.bottom||this.bottom)
			return;
		if(this.r>s1.r || this.inf || s1.inf)
			this.inf=true;
		else
		{
			this.r=max(this.r,s1.r);
		}
		if(this.l<s1.l || this.ninf || s1.ninf)
			this.ninf=true;
		else
		{
			this.l=min(s1.l,this.l);
		}
	}
	
	void narrowing(SimpleInterval s1)
	{
		if(s1.bottom||this.bottom)
			return;
		if(this.inf)
		{
			if(!s1.inf)
			{
				r=s1.r;
				inf=false;
			}
		}
		if(ninf)
		{
			if(!s1.ninf)
			{
				l=s1.l;
				ninf=false;
			}
		}
	}
	
	boolean isGreater(SimpleInterval s)
	{
		if(s.bottom)
			return true;
		if(bottom)
			return false;
		if(!inf)
		{
			if(s.inf)
				return false;
			if(r > s.r)
				return true;
		}
		if(!ninf)
		{
			if(s.ninf)
				return false;
			if(l < s.l)
				return true;
		}
		return true;
	}
	
}