import java.util.Iterator;
import java.util.List;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;


public class Test 
{
	public Test(UnitGraph ug)
	{
		Iterator it = ug.iterator();
		System.out.println(ug.getBody().toString()+"\n\n");
		
		while(it.hasNext())
		{
			Unit unit = (Unit) it.next();
			System.out.println("\n"+unit.toString());
			/*
			System.out.println("Predecessors :-");
			List<Unit> pred = ug.getPredsOf(unit);
			Iterator<Unit> itu = pred.iterator();
			while (itu.hasNext()) 
			{
				System.out.println("\t"+itu.next().toString());
			}
			System.out.println("Successor :-");
			List<Unit> su = ug.getSuccsOf(unit);
			itu = su.iterator();
			while (itu.hasNext()) 
			{
				System.out.println("\t"+itu.next().toString());
			}
			*/
		}
	}
}
