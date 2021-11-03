package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    static boolean not_done;
    static boolean boat_is_on_oahu;
    static Lock lock;
    static int children_on_boat;            
    static int total_children;
    static int total_adults;
    static Condition child_on_Oahu;
    static Condition child_on_Molokai;
    static Condition adult_on_Oahu;
    static Condition adult_on_Molokai;
    static int MolokaiAdults;
    static int MolokaiChildren;

    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;
	not_done = true;
	boat_is_on_oahu = true;
	lock = new Lock();
	children_on_boat = 0;

	total_children = children;
	total_adults = adults;
	child_on_Oahu = new Condition(lock);
	child_on_Molokai = new Condition(lock);
	adult_on_Oahu = new Condition(lock);
	adult_on_Molokai = new Condition(lock);
	MolokaiChildren = 0;
	MolokaiAdults = 0;

	// Instantiate global variables here
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	/*Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();
	*/
	Runnable r_child = new Runnable() { 
		public void run() {
			ChildItinerary();
		}
	}; 

	Runnable r_adult = new Runnable() {
		public void run() {
			AdultItinerary();
		}
	}; 

	for (int i = 0; i < adults; i++) {
		new KThread(r_adult).setName("Adult " + Integer.toString
		(i + 1)).fork();
	} 

	for (int i = 0; i < children; i++) {
		new KThread(r_child).setName("Child " + Integer.toString
			(i + 1)).fork();
	}
	
	while (not_done)
		KThread.yield();   
    }

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    	lock.acquire();

    	// while there are still adults not asleep on Molokai
    	while (not_done) {
    	    while(!boat_is_on_oahu || total_children - MolokaiChildren > 1 || children_on_boat != 0){
    	    	child_on_Oahu.wakeAll();
    	        adult_on_Oahu.sleep();
    	    }
    	
	bg.AdultRowToMolokai();
    	MolokaiAdults++;
    	boat_is_on_oahu = false;
    	child_on_Molokai.wake();
    	adult_on_Molokai.sleep();
	}
    }

    static void ChildItinerary(){
    	lock.acquire();

    	while (not_done) {
    		while (!boat_is_on_oahu) {
    			child_on_Oahu.sleep();
    		}


    		if (children_on_boat == 0) {
    			children_on_boat++;
    			child_on_Oahu.wakeAll();
    			child_on_Molokai.sleep();                

    			bg.ChildRowToOahu();
    			MolokaiChildren--;
    			boat_is_on_oahu = true;
    			children_on_boat = 0;

    			adult_on_Oahu.wakeAll();
    			child_on_Oahu.wakeAll();
    			child_on_Oahu.sleep();
    		}

    		else if (children_on_boat == 1) {
    				children_on_boat++;
    				bg.ChildRowToMolokai();
    				bg.ChildRideToMolokai();
    				boat_is_on_oahu = false;

    				MolokaiChildren +=2;

    				if (MolokaiChildren == total_children && MolokaiAdults == total_adults) {
    					not_done = false;
    					return;
    				} 
    				else {
    					bg.ChildRowToOahu();

    					MolokaiChildren--;
    					children_on_boat = 0;
    					boat_is_on_oahu = true;
    					adult_on_Oahu.wakeAll();
    					child_on_Oahu.wakeAll();
    					child_on_Oahu.sleep();
    				}
    		}
    	}
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
