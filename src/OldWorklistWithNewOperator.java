import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;


public class OldWorklistWithNewOperator 
{
	//int phase;	//other-Nothing,	1-Widening,		2-narrowing
	
	Map<Integer, Integer> visitCount;
	Map<Integer, Integer> indexMap;
	Map<Integer, Integer> hashCodeMap;
	Map<Integer, Unit> unitMap;
	Map<Integer, List <SimpleInterval>> inMap;
	Map<Integer, List <SimpleInterval>> out1Map;
	Map<Integer, List <SimpleInterval>> out2Map;
	
	Set<Integer> worklist = new HashSet<Integer>();
	
	UnitGraph ug;
	
	public OldWorklistWithNewOperator(UnitGraph inUg)
	{
		init(inUg);
		//phase = 1;
		doAnalysis(ug);
		
		/*System.out.println("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!NARROWING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n\n");
		initNarrowing(ug);
		phase = 2;
		doAnalysis(ug);
		*/
	}
	
	void init(UnitGraph inUg)
	{
		/*
		// initialization
		for every n: in(n) = INIT
		for every n: insert(n, worklist)
		*/
		Integer index = 0;
		ug = inUg;
			
		visitCount = new HashMap<>();
		indexMap = new HashMap<>();
		hashCodeMap = new HashMap<>();
		unitMap = new HashMap<>();
		inMap = new HashMap<>();
		out1Map = new HashMap<>();
		out2Map = new HashMap<>();
		
		FlowSet localVariablesFlowSet = new ArraySparseSet();
		Iterator<Local> localsIterator = ug.getBody().getLocals().iterator();
		while(localsIterator.hasNext()) 
			localVariablesFlowSet.add(localsIterator.next());
		List<String> localVariablesList;
		localVariablesList=removeDuplicate(localVariablesFlowSet);
		
		Iterator<Unit> unitIterator = ug.iterator();
		while(unitIterator.hasNext())
		{
			Unit currentUnit = unitIterator.next();
			Integer hashCode = currentUnit.hashCode();
			worklist.add(index);
			//tempWorklist.push(hashCode);
			
			visitCount.put(hashCode, 0);
			hashCodeMap.put(index,hashCode);
			indexMap.put(hashCode, index++);
			unitMap.put(hashCode, currentUnit);
			
			List <SimpleInterval> intervalSetIN = new ArrayList <SimpleInterval> (0);
			List <SimpleInterval> intervalSetOut1 = new ArrayList <SimpleInterval> (0);
			List <SimpleInterval> intervalSetOut2 = new ArrayList <SimpleInterval> (0);
			Iterator<String> localVariableListIterator = localVariablesList.iterator();
			while(localVariableListIterator.hasNext())								
			{	
				String var = localVariableListIterator.next().toString();
				SimpleInterval siIN = new SimpleInterval(var);
				SimpleInterval siOut1 = new SimpleInterval(var);
				SimpleInterval siOut2 = new SimpleInterval(var);
				intervalSetIN.add(siIN);
				intervalSetOut1.add(siOut1);
				intervalSetOut2.add(siOut2);
			}
			inMap.put(hashCode, intervalSetIN);
			out1Map.put(hashCode, intervalSetOut1);
			out2Map.put(hashCode, intervalSetOut2);
		}
	}
	
	void initNarrowing(UnitGraph inUg)
	{
		/*
		// initialization
		for every n: in(n) = INIT
		for every n: insert(n, worklist)
		*/
		Integer index = 0;
			
		visitCount = new HashMap<>();
		
		Iterator<Unit> unitIterator = ug.iterator();
		while(unitIterator.hasNext())
		{
			Unit currentUnit = unitIterator.next();
			Integer hashCode = currentUnit.hashCode();
			worklist.add(index++);
			visitCount.put(hashCode, 0);
		}
	}
	
	void doAnalysis(UnitGraph ug)
	{
		
		while(!worklist.isEmpty())
		{
			Iterator<Integer> worklistIterator = worklist.iterator();
			Integer currentHashCode = worklistIterator.next();
			worklist.remove(currentHashCode);
			currentHashCode = hashCodeMap.get(currentHashCode);
			
			visitCount.put(currentHashCode, visitCount.get(currentHashCode)+1);
			Unit currentUnit = unitMap.get(currentHashCode);
			List <SimpleInterval> intervalSetIN;
			List <SimpleInterval> intervalSetOut1;
			List <SimpleInterval> intervalSetOut2;
			List <SimpleInterval> tempIntervalSet;
			List <SimpleInterval> oldInteralSetIN = new ArrayList <SimpleInterval> (0);
			
			intervalSetIN = inMap.get(currentHashCode);
			intervalSetOut1 = out1Map.get(currentHashCode);
			intervalSetOut2 = out2Map.get(currentHashCode);
			
			copy(intervalSetIN,oldInteralSetIN);
			
			
			tempIntervalSet = computeInSet(currentUnit,intervalSetIN);			
			
			//printAll(oldInteralSetIN);
			
			copy(tempIntervalSet,intervalSetIN);
			
			//printAll(intervalSetIN);
			
			inMap.put(currentHashCode, intervalSetIN);
			
			if(!isEqual(intervalSetIN,oldInteralSetIN))
			{
				//System.out.println("Old and new!!! of "+currentUnit.toString());
				//printAll(oldInteralSetIN);
				//printAll(intervalSetIN);
				
				List<Unit> children = ug.getSuccsOf(currentUnit);
				Iterator<Unit> childrenIterator = children.iterator();
				while(childrenIterator.hasNext())
				{
					worklist.add(indexMap.get(childrenIterator.next().hashCode()));
				}
				//put children of currentUnit in the worklist stack
			}
			
			//printAll(intervalSetIN);
			copy(intervalSetIN, oldInteralSetIN);
			
			flowThrough(intervalSetIN, currentUnit, intervalSetOut1, intervalSetOut2);
			inMap.put(currentHashCode, oldInteralSetIN);
			out1Map.put(currentHashCode, intervalSetOut1);
			out2Map.put(currentHashCode, intervalSetOut2);
		}
		
		/*
		// main cycle
		while not empty(worklist) do
		     x = extract(worklist)
		     old = in(x)
		     if FORWARD
		          new = MEET(all p in fathers(n)) (TRASF(in(p))) 
		          if old <> new 
		               for every f in children(n): insert(f, worklist); 
		               in(n) = new
		     else 
		          new = MEET(all p in children(n)) (TRASF(in(p))) 
		          if old <> new 
		               for every f in fathers(n): insert(f, worklist); 
		               in(n) = new   
		enddo
		*/
	}
	
	protected void flowThrough(List<SimpleInterval> intervalSet, Unit unit,
			List<SimpleInterval> out1, List<SimpleInterval> out2) 
	{	
		Iterator<ValueBox> defBox = unit.getDefBoxes().iterator();
		Iterator<ValueBox> useBox = unit.getUseBoxes().iterator();
		
		List<SimpleInterval> tempout1=new ArrayList <SimpleInterval> (0);
		List<SimpleInterval> tempout2=new ArrayList <SimpleInterval> (0);
		
		copy(intervalSet,tempout1);
		copy(intervalSet,tempout2);
		
		copy(intervalSet,out1);
		copy(intervalSet,out2);
		
		
		if(unit instanceof JIfStmt)
		{
			ValueBox usbox;
			Value use=null;
			String operand[] = new String [2];
			String expr=null;
			for(int i=0;i<3;i++)
			{
				usbox = (ValueBox) useBox.next();
				use = usbox.getValue();
				if(i==2)
					expr=use.toString();
				else
					operand[i]=use.toString();
			}
			String op = getRelOperator(expr);
			
			SimpleInterval a,b;
			if(isNum(operand[0]))
			{
				a = new SimpleInterval(num(operand[0]));
			}
			else
			{
				a = intervalSet.get(getIndex(intervalSet, operand[0]));
			}
			if(isNum(operand[1]))
			{
				b = new SimpleInterval(num(operand[1]));
			}
			else
			{
				b = intervalSet.get(getIndex(intervalSet, operand[1]));
			}
			SimpleInterval [] SIarr = SplitSI(a,b,op);
			
			//copy the values
			for(int i=0;i<2;i++)
			{
				replaceInterval1(SIarr[i],out2);
			}
			for(int i=2;i<4;i++)
			{
				replaceInterval1(SIarr[i],out1);
			}
			//System.out.println("out1");
			//printAll(out1);
			//System.out.println("out2");
			//printAll(out2);
		}
		
		if(unit instanceof AssignStmt)
		{
			while (defBox.hasNext())
			{
				ValueBox dfbox = (ValueBox) defBox.next();
				Value def = dfbox.getValue();
				
				ValueBox usbox;
				Value use=null;
				while(useBox.hasNext())
				{
					usbox = (ValueBox) useBox.next();
					use = usbox.getValue();
				}
				//System.out.println(unit.toString());
				//printAll(intervalSet);
				int lhsIndex = getIndex(intervalSet, def.toString());
				//System.out.println(def.toString());
				SimpleInterval lhsSI = intervalSet.get(lhsIndex);
				SimpleInterval lhsSiCopy = new SimpleInterval(lhsSI);
				String unitString = use.toString();
				int numop = numOfOperands(unitString);
				String[] operands = getOperands(unitString,numop);
				SimpleInterval temp;
				
				//lhsSI.print();
				
				
				switch(numop)
				{
				case 0:
					if(isNum(operands[0]))
					{
						temp = new SimpleInterval(num(operands[0]));	
					}
					else
					{
						temp = intervalSet.get(getIndex(intervalSet, operands[0]));
					}
					temp.copyValues(lhsSI);
					break;
				case 1:
					temp = intervalSet.get(getIndex(intervalSet, operands[0]));
					temp.copyValues(lhsSI);
					lhsSI.negate();
					break;
				case 2:
					SimpleInterval operand1SI;
					SimpleInterval operand2SI;
					if(isNum(operands[0]))
					{
						operand1SI = new SimpleInterval(num(operands[0]));
						
					}
					else
					{
						operand1SI = intervalSet.get(getIndex(intervalSet, operands[0]));
					}
					if(isNum(operands[1]))
					{
						operand2SI = new SimpleInterval(num(operands[1]));
						
					}
					else
					{
						operand2SI = intervalSet.get(getIndex(intervalSet, operands[1]));
					}
					char op = operator(unitString);
					switch(op)
					{
					case '*':
						lhsSI.multiply(operand1SI, operand2SI);
						break;
					case '/':
						lhsSI.divide(operand1SI, operand2SI);
						break;
					case '+':
						lhsSI.add(operand1SI, operand2SI);
						break;
					case '-':
						lhsSI.subtract(operand1SI, operand2SI);
						break;
					}
					break;
				}
				
				Integer currentVisitCount = visitCount.get(unit.hashCode());
				if(currentVisitCount > 3)
				{
					//lhsSiCopy.print();
					//lhsSI.print();
					if(!lhsSiCopy.contains(lhsSI))
					{
						//System.out.println("Applying widening :-");
						lhsSI.widening(lhsSiCopy);
					}
					else
					{
						//System.out.println("Applying narrowing :-");
						lhsSI.narrowing(lhsSiCopy);
					}
					//lhsSI.print();
				}
				
				
				/*
				if(phase==1)
				{
					if(currentVisitCount==3)
					{
						lhsSI.widening(lhsSiCopy);
					}
				}
				else if(phase==2)
				{
					if(currentVisitCount==1)
					{
						System.out.println("Applying narrowing...........");
						lhsSI.print();
						lhsSI.narrowing(lhsSiCopy);
						lhsSI.print();
						System.out.println("Narrowing applied...........");
					}
				}
				*/
			}
			copy(intervalSet,out1);
			copy(intervalSet,out2);
		}
		
		System.out.println("\nResult of Unit : "+unit.toString());
		printAll(intervalSet);
		System.out.println("$###############################$");
	}
	
	protected void merge(List<SimpleInterval> in1, List<SimpleInterval> in2,
			List<SimpleInterval> out) 
	{
		Iterator<SimpleInterval> it1 = in1.iterator();
		Iterator<SimpleInterval> it2 = in2.iterator();
		Iterator<SimpleInterval> ito = out.iterator();
 			
		while(it1.hasNext() && it2.hasNext() && ito.hasNext())
		{
			((SimpleInterval)ito.next()).union((SimpleInterval)it1.next(), (SimpleInterval)it2.next());
		}
	}
	
	protected void copy(List<SimpleInterval> sourceSet, List<SimpleInterval> destSet) 
	{
		destSet.clear();
		
		Iterator<SimpleInterval> it = sourceSet.iterator();
		while(it.hasNext())
		{
			destSet.add(new SimpleInterval((SimpleInterval)it.next()));
		}	
	}
	
	List<SimpleInterval> computeInSet(Unit unit, List<SimpleInterval> intervalSetIN)
	{
		List<Unit> parents = ug.getPredsOf(unit);
		List<List<SimpleInterval>> outputs = new ArrayList <List<SimpleInterval>> (0);;
		
		if(parents.isEmpty())
		{
			List<SimpleInterval> temp = new ArrayList<SimpleInterval>(0);
			copy(intervalSetIN,temp);
			return temp;
		}
		if(unit instanceof JIfStmt)
		{
			if(!isIfUnit(unit))
			{
				if(visitCount.get(unit.hashCode())==1)
				{
					List<Unit> parentsList = ug.getPredsOf(unit);
					Integer minIndex = indexMap.get(parentsList.get(0).hashCode());
					Iterator<Unit> parentIterator = parentsList.iterator();
					Integer minParentHashCode=parentsList.get(0).hashCode();
					while(parentIterator.hasNext())
					{
						Integer tempHashCode = parentIterator.next().hashCode();
						Integer temp = indexMap.get(tempHashCode);
						if(temp<minIndex)
						{
							minIndex=temp;
							minParentHashCode = tempHashCode;
						}
					}
					return out2Map.get(minParentHashCode);
				}
			}
		}
		Iterator<Unit> parentsIterator = parents.iterator();
		while(parentsIterator.hasNext())
		{
			Unit parent = parentsIterator.next();
			outputs.add(getParentsOutput(parent,unit));
			//System.out.println("\nParent :- " + parent.toString());
			//System.out.println("Child :-  " + unit.toString());
			//printAll(getParentsOutput(parent,unit));
		}
		
		return mergeParentsOutputs(outputs);
	}
	
	List<SimpleInterval> getParentsOutput(Unit parent, Unit child)
	{
		Integer sibllingHashCode,childHashCode = child.hashCode();
		Integer parentHashCode = parent.hashCode();
		if(ug.getSuccsOf(parent).size()==1)
		{
			return out2Map.get(parent.hashCode());
		}
		else
		{
			//System.out.println("Yippie!!!");
			sibllingHashCode = getSibllingHashCode(parent,child);
			//System.out.println("\t\tSiblling unit :- "+unitMap.get(sibllingHashCode).toString());
			//System.out.println("\t\tSiblling of :- "+child.toString());
			//System.out.println("\t\tparent is :- "+parent.toString());
			if(isIfUnit(parent))
			{
				//System.out.println("Ka yeeee!!!");
				if(indexMap.get(childHashCode)>indexMap.get(sibllingHashCode))
				{
					//System.out.println("True!!!");
					return out2Map.get(parentHashCode);
				}
				else
				{
					//System.out.println("False!!!");
					return out1Map.get(parentHashCode);
				}
			}
			else
			{
				
				if(indexMap.get(childHashCode)>indexMap.get(sibllingHashCode))
				{
					return out1Map.get(parentHashCode);
				}
				else
				{
					return out2Map.get(parentHashCode);
				}
				/*
				else
				{
					List<Unit> parentsList = ug.getPredsOf(child);
					Integer minIndex = indexMap.get(parentsList.get(0).hashCode());
					Iterator<Unit> parentIterator = parentsList.iterator();
					Integer minParentHashCode=parentsList.get(0).hashCode();
					while(parentIterator.hasNext())
					{
						Integer tempHashCode = parentIterator.next().hashCode();
						Integer temp = indexMap.get(tempHashCode);
						if(temp<minIndex)
						{
							minIndex=temp;
							minParentHashCode = tempHashCode;
						}
					}
					if(indexMap.get(childHashCode)>indexMap.get(sibllingHashCode))
					{
						return out1Map.get(minParentHashCode);
					}
					else
					{
						return out2Map.get(minParentHashCode);
					}
				}
				*/
			}
		}
		//return null;
	}
	
	boolean isIfUnit(Unit unit)
	{
		if(!(unit instanceof JIfStmt))
		{
			System.out.println("ERROR IN idIfUnit()!!!!!!");
			return false;
		}
		String afterGoto = getAfterGoto(unit.toString());
		if(indexMap.get(unit.hashCode()) < indexMap.get(getStringHashCode(afterGoto,unit)))
		{
			//System.out.println("hmm....truly if!!");
			return true;
		}
		else
		{
			return false;
		}
	}
	
	Integer getStringHashCode(String afterGoto, Unit unit)
	{
		Iterator<Unit> unitIterator = ug.iterator();
		while(unitIterator.hasNext())
		{
			Unit tempUnit = unitIterator.next();
			String unitString = tempUnit.toString();
			if(unitString.contains(afterGoto))
			{
				if(tempUnit.hashCode()!=unit.hashCode())
					return tempUnit.hashCode();
			}
		}
		System.out.println("ERROR IN getStringHashCode()!!!!!!!!");
		return 0;
	}
	
	String getAfterGoto(String unitString)
	{
		int goTo = unitString.indexOf("goto");
		return unitString.substring(goTo+5);
	}
	
	List<SimpleInterval> mergeParentsOutputs(List<List<SimpleInterval>> input)
	{
		if(input.size()==0)
			return null;
		
		List<SimpleInterval> output = new ArrayList <SimpleInterval> (0);
		copy(input.get(0),output);
		
		for(int i=1;i < input.size();i++)
		{
			merge(output,input.get(i),output);
			//Check that is it a problem if output is one of the inputs in merge
		}
		return output;
	}
	
	Integer getSibllingHashCode(Unit parent, Unit child)
	{
		Iterator<Unit> succsIterator = ug.getSuccsOf(parent).iterator();
		Integer childHashCode = child.hashCode(),sibllingHashCode;
		//System.out.println("all children");
		while(succsIterator.hasNext())
		{
			sibllingHashCode = succsIterator.next().hashCode();
			if(!sibllingHashCode.equals(childHashCode))
			{
				return sibllingHashCode;
			}
		}
		System.out.println("ERROR IN getSibllingHashCode()!!!!!!");
		return 0;
	}
	
	boolean isEqual(List<SimpleInterval> s1, List<SimpleInterval> s2)
	{
		Iterator<SimpleInterval> it1 = s1.iterator();
		Iterator<SimpleInterval> it2 = s2.iterator();
		
		while(it1.hasNext()&&it2.hasNext())
		{
			if(!it1.next().equals(it2.next()))
			{
				return false;
			}
		}
		return true;
	}
	
	private List<String> removeDuplicate(FlowSet localVariables) 
	{
		Iterator<?> it = localVariables.iterator();			//remove <?> if it doesn't work
		List <String> v = new ArrayList<String>(0);
		
		while(it.hasNext())
		{
			v.add(it.next().toString());
		}
		
		int len=v.size();
		
		for(int i=0;i<len;i++)
		{
			for(int j=i+1;j<len;j++)
			{
				if(v.get(i).equals(v.get(j)))
				{
					v.remove(j);
					len--;
				}
			}
		}
		return v;
	}
	
	void printWorklist(Stack<Integer> worklist)
	{
		System.out.println("\n\n");
		while(!worklist.empty())
			System.out.println(worklist.pop());
	}
	
	SimpleInterval [] SplitSI(SimpleInterval a,SimpleInterval b, String op)
	{
		SimpleInterval [] SIarr = new SimpleInterval [4];
		
		for(int i=0;i<4;i++)
		{
			SIarr[i]= new SimpleInterval("SIarr");
		}
		
		SimpleInterval t1=new SimpleInterval("a"),t2=new SimpleInterval("b");
		a.copy(t1);
		b.copy(t2);
		
		actuallySplitSI(a,b,op);
		a.copy(SIarr[0]);
		//b.copy(SIarr[1]);
		
		t1.copyValues(a);
		t2.copyValues(b);
		actuallySplitSI(a,b,operatorComp(op));
		a.copy(SIarr[2]);
		
		t1.copyValues(a);
		t2.copyValues(b);
		actuallySplitSI(b,a,operatorInverse(op));
		b.copy(SIarr[1]);
		
		t1.copyValues(a);
		t2.copyValues(b);
		actuallySplitSI(a,b,operatorComp(operatorInverse(op)));
		b.copy(SIarr[3]);
		
		
		return SIarr;
	}
		
	
	void actuallySplitSI(SimpleInterval a,SimpleInterval b, String op)
	{
		switch (op) 
		{
		case "<=":
			a.LTEQ(b);
			break;
		case ">=":
			a.GTEQ(b);
			break;
		case "<":
			a.LT(b);
			break;
		case ">":
			a.GT(b);
			break;
		case "==":
			a.EQ(b);
			break;
		case "!=":
			a.NQ(b);
			break;
		}
	}
	
	String getRelOperator(String str)
	{
	       
	        if(str.contains(">="))return ">=";
	        if(str.contains("<="))return "<=";
	        if(str.contains("!="))return "!=";
	        if(str.contains("=="))return "==";
	        if(str.contains(">"))return ">";
	        if(str.contains("<"))return "<";
	       
	        return "";
	}
	
	String operatorInverse(String str)
	{
	        if(str==">=")return "<=";
	        if(str=="<=")return ">=";
	        if(str==">")return "<";
	        if(str=="<")return ">";
	        if(str=="==")return "==";
	        if(str=="!=")return "!=";
	       
	        return "";
	}
	
	String operatorComp(String str)
	{
	        if(str==">=")return "<";
	        if(str=="<=")return ">";
	        if(str==">")return "<=";
	        if(str=="<")return ">=";
	        if(str=="==")return "!=";
	        if(str=="!=")return "==";
	       
	        return "";
	}
	
	void replaceInterval1(SimpleInterval a,List<SimpleInterval> out)
	{
		SimpleInterval temp;
		
		Iterator<SimpleInterval> tempit=out.iterator();
		while(tempit.hasNext())
		{
			temp=(SimpleInterval)tempit.next();
			if(temp.name.equals(a.name))
			{
				a.copyValues(temp);
			}
		}
	}
	
	void replaceInterval(SimpleInterval a,List<List<SimpleInterval>> out)
	{
		List<SimpleInterval> tempList;
		SimpleInterval temp;
		
		Iterator<List<SimpleInterval>> itif1 = out.iterator();
		while(itif1.hasNext())
		{
			tempList=(List<SimpleInterval>)itif1.next();
			Iterator<SimpleInterval> tempit=tempList.iterator();
			while(tempit.hasNext())
			{
				temp=(SimpleInterval)tempit.next();
				if(temp.name.equals(a.name))
				{
					a.copyValues(temp);
				}
			}
		}
	}
	
	protected char operator(String s)
	{
		int len=s.length();
		for(int i=0;i<len;i++)
		{
			if(s.charAt(i)=='*'||s.charAt(i)=='/'||s.charAt(i)=='+'||s.charAt(i)=='-')
			{
				return s.charAt(i);
			}
		}
		return '\0';
	}
	
	protected boolean isNum(String s) 
	{
		int len=s.length();
		int i=0;
		if(s.charAt(0)=='-')
			i=1;
		for(;i<len;i++)
		{
			if(s.charAt(i)<'0'||s.charAt(i)>'9')
				return false;
		}
		return true;
	}
	
	protected long num(String s)
	{
		int i=0;
		int neg=1;
		if(s.charAt(i)=='-')
		{
			i=1;
			neg=-1;
		}
		long l=0;
		int len=s.length();
		for(;i<len;i++)
		{
			l = l*10 + (s.charAt(i) - '0');
		}
		l=l*neg;
		return l;
	}
	
	int numOfOperands(String str)
	{
	       
	        int len=str.length();
	       
	        if(str.contains("neg"))return 1;
	 
	        if(str.charAt(0)=='-')
	        {
	                for(int i=1;i<len;i++)
	                {
	                        if(str.charAt(i)=='+')return 2;
	                else if(str.charAt(i)=='-')return 2;
	                else if(str.charAt(i)=='*')return 2;
	                else if(str.charAt(i)=='/')return 2;
	                }
	                return 0;
	        }
	 
	        for(int i=0;i<len;i++)
	        {
	                if(str.charAt(i)=='+')return 2;
	                else if(str.charAt(i)=='-')return 2;
	                else if(str.charAt(i)=='*')return 2;
	                else if(str.charAt(i)=='/')return 2;
	        }
	        return 0;
	}
	 
	String[] getOperands(String str,int num)
	{
	        String[] arr= new String[2];
	       
	        if(num==0)
	        {
	                arr[0]=str;
	                return arr;
	        }
	       
	        else if (num==1)
	        {
	                arr[0]=str.substring(4, str.length());
	                return arr;
	        }
	        else if(num==2)
	        {
	                int pos=0;
	               
	                for(int i=1;i<str.length();i++)
	                {
	                        if(str.charAt(i)=='+'||str.charAt(i)=='-'||str.charAt(i)=='*'||str.charAt(i)=='/')
	                        {
	                                pos=i;
	                                break;
	                        }
	                }
	               
	                String left=str.substring(0,pos-1);
	                String right=str.substring(pos+2,str.length());
	               
	                arr[0]=left;
	                arr[1]=right;
	                return arr;
	        }
	       
	        return arr;
	}
	
	protected int getIndex(List<SimpleInterval> v,String s)
	{
		Iterator<SimpleInterval> it = v.iterator();
		int i=0;
		while(it.hasNext())
		{
			SimpleInterval temp = (SimpleInterval)it.next();
			if(temp.getName().equals(s))
			{
				return i;
			}
			i++;
		}
		return -1;
	}

	void printAll(List<SimpleInterval> s)
	{
		Iterator<SimpleInterval> it = s.iterator();
		while(it.hasNext())
		{
			((SimpleInterval)it.next()).print();
		}
		System.out.println();
	}
}
