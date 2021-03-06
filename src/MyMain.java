import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;


public class MyMain
{

	
	//Number of test classes in the project. Update it after adding a class.
	final static int TEST_CLASSES=7;
	final static int EXTRA_TEST_CLASSES=0;
	//----------------------------------------------------------------------
	
	private static boolean quit=false;
	private static List<String> list=new ArrayList<String>();
	private static String className;
	
	public static void main(String[] args) 
	{
		
		//Add the names of the test classes here. Don't forget to add them in the project folder.
		// e.g. list.add("YourClassName");
				
				
		for(int i=0;i<TEST_CLASSES;i++)
			list.add("T"+(i+1));
		
		
		//-------------------------------------------------------------------------------------
		
		while(!quit) // Quitters never win!! :P
		{
			
			//Initial menu
			print_menu();
			
			//Input from user
			int input=userInput();
			switch(input)
			{
				case -1:				//Wrong Input
					continue;
				case 0:					//User wants to quit
					continue;
				case 1:					//User chooses className correctly.
			}
			
			print_algo_menu();
			int secondInput = userInputAlgo();
			
			switch(secondInput)
			{
				case -1:				//Wrong Input
					continue;
				case 0:					//User wants to quit
					continue;
			}
			//Starting the analysis for all methods of the selected class.
			
			SootClass c = Scene.v().loadClassAndSupport(className);
			c.setApplicationClass();
			Options.v().setPhaseOption("jb", "use-original-names:true");     //Use same variable names as in test program
			Options.v().set_keep_line_number(true);
			
			List<SootMethod> m = c.getMethods();
			
			System.out.println("==========================================\n");
			System.out.println("Class Name : "+className);
			System.out.println("Number of methods in "+className+" : "+m.size()+"\n");
			
			for(int j=0;j<m.size();j++)
			{
				
				Body b = m.get(j).retrieveActiveBody();
				UnitGraph ug=new BriefUnitGraph(b);
				
				System.out.println("==========================================");
				System.out.println("#"+(j+1)+" Method Name : ");
				System.out.println(m.get(j)+"\n");				
				
				//This line does the complete Interval Domain analysis.
				//new Test(ug);
				
				switch(secondInput)
				{
					case 1:
						new OldRoundRobinWithOldOperator(ug);break;
					case 2:
						new OldRoundRobinWithNewOperator(ug);break;
					case 3:
						new StructuredRoundRobin(ug);break;
					case 4:
						new OldWorklistWithOldOperator(ug);break;
					case 5:
						new OldWorklistWithNewOperator(ug);break;
					case 6:
						new StructuredWorklist(ug);break;
					
				}
				
			}
			
		}			
		
		//Exit out of the program.
		System.out.println("------------------------------------------");
		System.out.println("Program Ends Successfully.");
		System.out.println("------------------------------------------");
		
		
	}
	
	static int userInput()
	{
		Scanner in = new Scanner(System.in);
		int choice=in.nextInt();
		//Setting "className" based on user choice
		
		if(choice<0||choice>(TEST_CLASSES+EXTRA_TEST_CLASSES))
		{
			System.out.println("Wrong Choice..Enter again\n\n");
			return -1;	
		}
		
		else if(choice==0)
		{
			quit=true;
			in.close();
			return 0;
		}
		
		else
		{
			className=list.get(choice-1);
			return 1;
		}
	}
	
	static int userInputAlgo()
	{
		Scanner in = new Scanner(System.in);
		int choice=in.nextInt();
		//Setting "Algo name" based on user choice
		
		if(choice<0||choice>6)
		{
			System.out.println("Wrong Choice..Enter again\n\n");
			return -1;	
		}
		
		else if(choice==0)
		{
			quit=true;
			in.close();
			return 0;
		}
		
		else
		{
			return choice;
		}
	}
		
	static void print_menu()
	{
		System.out.println("==========================================");
		System.out.println("Choose the name of the class : ");
		System.out.println("(Press '0' to exit)\n");
		
		for(int i=0;i<list.size();i++)
			System.out.println(i+1 + ". " + list.get(i));
		
	}
	
	static void print_algo_menu()
	{
		
		System.out.println("==========================================");
		System.out.println("Choose the name of the algorithm : ");
		System.out.println("(Press '0' to exit)\n");
		
		int i=1;
		System.out.println(i++ + ". " + "Standard Round Robin with 2 phase Widening and Narrowing");
		System.out.println(i++ + ". " + "Standard Round Robin with new operator");
		System.out.println(i++ + ". " + "Structured Round Robin with new operator");
		System.out.println(i++ + ". " + "Standard Worklist with 2 phase Widening and Narrowing");
		System.out.println(i++ + ". " + "Standard Worklist with new operator");
		System.out.println(i++ + ". " + "Structured Worklist with new operator");
		
		
	}
	
}