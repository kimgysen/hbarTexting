package bloomfilter.polynomialhash;

public class PolynomialHash {
	
	public int p;
	public int M;
	
	public PolynomialHash(int p_, int M_)
	{
		p = p_;
		M = M_;
	}
	
	public  int hash(String s)
	{
		int h = 0;
		
		for (byte b:s.getBytes())
		{
			h = (h*p+(int)b)%M;
		}
		
		return h;
	}
	
	public String toString()
	{
		return "Polynomial hash prime p="+p+"\n Modulus M="+M;
	}
	
	
	public static void main(String[] args)
	{
		PolynomialHash h = new PolynomialHash(97, 128);
		
		h.hash("coucou");
		
		System.out.println(""+h);
	}

}
