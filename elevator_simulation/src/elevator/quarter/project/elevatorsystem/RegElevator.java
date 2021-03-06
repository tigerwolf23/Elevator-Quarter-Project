package elevator.quarter.project.elevatorsystem;

import elevator.quarter.project.structures.*;
import elevator.quarter.project.population.Movable;
import elevator.quarter.project.Definitions;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 
 * @author Craig & ZGraceffa
 */
public class RegElevator implements Elevator, Runnable, Definitions
{
    private final int ELEVATOR_CAPACITY = 20;
    
    //current floor & queue information
    private Floor currentFloor;
    private ArrayList<Floor> destinations;
    private static int elevatorIDCounter = 1;
    private int elevatorID;
    
    //people currently on the elevator
    private ArrayList<Movable> movablesOnElevator;
    
    //other elevator components
    private FloorPanel elevatorFloorPanel;
    
    //state variables
    private Door doorState;
    private ElevatorState elevatorState;
    private boolean running;
    
    
    public RegElevator()
    {
        doorState = Door.CLOSED;
        elevatorState = ElevatorState.IDLE;
        movablesOnElevator = new ArrayList<Movable>();
        destinations = new ArrayList<Floor>();
        elevatorID = elevatorIDCounter;
        elevatorIDCounter++;
    }
    
    //accessors
    /**
     * accessor for elevatorID
     * @return elevatorID
     */
    @Override
    public int getElevatorID()
    {
        return elevatorID;
    }
    
    /**
     * accessor for currentFloor.
     * @return floor object representing current floor.
     */
    @Override
    public Floor getCurrentFloor()
    {
        return currentFloor;
    }
    
    /**
     * The elevator's initial floor can be set only once with this method.
     * @param floorIn 
     */
    @Override
    public void initiallySetCurrentFloor()
    {
        if(currentFloor == null)
        {
            currentFloor = RegBuilding.getInstance().getFloorWithIndex(DEFAULT_FLOOR);
        }
    }
    
    /**
     * Continuously running thread method.
     */
    @Override
    public void run()
    {
        System.out.println("Elevator " + elevatorID + " running (On default floor: " + this.getCurrentFloor().getFloorID() +").");
        
	boolean waitEnded = false;
	boolean waitException = false;
        
        running = true;
	
	while(running)
	{
            //this block waits for an elevator timeout
            synchronized(this)
            {
                if(destinations.isEmpty())
		{
                    try
                    {
			wait(IDLE_WAIT_TIME/SCALE_FACTOR);
			waitEnded = true;
                    }
                    catch(InterruptedException ex)
                    {
                        waitException = true;
                        ex.printStackTrace();
                    }
                }
            }
            //sync break allows destinations to be added during idle timeout
            synchronized(this)
            {
                //this means you timed out, so return to default floor
                if(destinations.isEmpty() && waitEnded)
                {
                    try
                    {   
                        if(currentFloor.getFloorID() != DEFAULT_FLOOR)
                        {
                            addDestination(DEFAULT_FLOOR);
                        }
                        
                        waitEnded = false;//resetting wait flag
                    }
                    catch (InvalidFloorRequestException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }//end sync block
            synchronized(this)
            {
                //this means there are still floors left in the destination list to visit.
                if(!destinations.isEmpty())
                {
                    try
                    {
                        Thread.sleep(TIME_PER_FLOOR/SCALE_FACTOR);
                    }
                    catch(InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    
                    /*
                     * the up/down decision must be made here (can only be set if the elevator is currently idle)
                     * this section assumes that the arraylist of destinations has already been sorted, meaning the first destination in the arraylist should be the closest, either up or down.
                     */
                }
            }
            synchronized(this)
            {
                    //determines whether the elevator is going up or down
                    if(elevatorState == ElevatorState.IDLE && !destinations.isEmpty())
                    {
                        if(this.getCurrentFloor().getFloorID() != destinations.get(0).getFloorID())
                        {
                            if(this.getCurrentFloor().getFloorID() > destinations.get(0).getFloorID())
                            {
                                elevatorState = ElevatorState.GOING_DOWN;
                                System.out.println("Elevator " + elevatorID + " going down." + printDestList());
                                waitEnded = false;
                            }
                            else if(this.getCurrentFloor().getFloorID() < destinations.get(0).getFloorID())
                            {
                                elevatorState = ElevatorState.GOING_UP;
                                System.out.println("Elevator " + elevatorID + " going up." + printDestList());
                                waitEnded = false;
                            }
                        }
                    }
            }
            synchronized(this)
            {
                     //increments or decrements the floor based on the previous logic block
                    if(elevatorState == ElevatorState.GOING_DOWN)
                    {
                        currentFloor = RegBuilding.getInstance().getNextLowerFloor(currentFloor);
                        
                        if(!this.getCurrentFloor().equals(destinations.get(0)))
                        {
                            System.out.println("Elevator " + elevatorID + " passing Floor " + currentFloor.getFloorID() + "." + printDestList());
                        }
                    }
                    else if(elevatorState == ElevatorState.GOING_UP)
                    {
                        currentFloor = RegBuilding.getInstance().getNextHigherFloor(currentFloor);
                        
                        if(!this.getCurrentFloor().equals(destinations.get(0)))
                        {
                            System.out.println("Elevator " + elevatorID + " passing Floor " + currentFloor.getFloorID() + "." + printDestList());
                        }
                    }
            }
            synchronized(this)
            {
                //if the elevator is at the destination with index 0, it has arrived at a destination
                if(currentFloor == destinations.get(0))
                {
                      elevatorArrived();
                }
            }//end sync     
        }//end while
    }//end run method

    /**
     * Shuts down elevator next time it is 'IDLE' and has no further destinations.
     */
    @Override
    public void endRun()
    {
        boolean readyToStop = false;
        while(!readyToStop)
        {
            if(elevatorState == ElevatorState.IDLE && destinations.isEmpty())//may need to add more checks
            {
                running = false;
                readyToStop = true;
                System.out.println("Elevator " + elevatorID + " is shut down!");
            }
        }
    }
    /**
     * Synchronized method to add floors to destination list.
     * @param floorIn 
     */
    @Override
    public synchronized void addDestination(int floorIn) throws InvalidFloorRequestException
    {   
        ///////////////////////////////
	//error checking
	//ensure that requested floor is within an acceptable min/max range
        System.out.println(elevatorState);
        if(floorIn > RegBuilding.getInstance().getFloorCount())
        {
            throw new InvalidFloorRequestException("Requested floor too large.");
        }
        else if(floorIn < 1)
        {
            throw new InvalidFloorRequestException("Requested floor too small.");
        }
	//check if requested floor is not in the current direction when going up
        else if(elevatorState == ElevatorState.GOING_UP && currentFloor.getFloorID() > floorIn)
        {
            throw new InvalidFloorRequestException("Requested floor not in current (upward) direction.");
        }
        //check if requested floor is not in the current direction when going down
        else if(elevatorState == ElevatorState.GOING_DOWN && currentFloor.getFloorID() < floorIn)
        {
            throw new InvalidFloorRequestException("Requested floor not in current (downward) direction.");
        }
	//check to see if the requested floor is already in the destinations list
        else if(destinations.indexOf(floorIn) > -1)
        {
            throw new InvalidFloorRequestException("Requested floor already in destinations list.");
        }
        else
        {
            //System.out.println("Indexof floorin : " + destinations.indexOf(floorIn));
            //add destination if requested floor passes all the error checking and elevator is not on current floor.
            if(this.getCurrentFloor().getFloorID() != floorIn)
            {
                destinations.add(getFloorInstanceOf(floorIn));
                
                Collections.sort(destinations, new RegFloor.compareFloors());
                
                if(elevatorState == ElevatorState.GOING_DOWN)
                {
                    Collections.reverse(destinations);
                }
                
                System.out.println("Added Floor " + floorIn + " to destinations list of Elevator " + elevatorID + "." + printDestList());
            }

            notifyAll(); //notify must be in a synchronized context (block or method)
        }
    }
    
    /**
     * This indicates that the elevator has arrived at its final destination. Takes care of clearing out the destination list, opening the doors, and other arrival tasks.
     */
    private void elevatorArrived()
    {
        elevatorState = ElevatorState.IDLE;
        System.out.println("Elevator " + elevatorID + " arrived at Floor " + currentFloor.getFloorID() + ".");
        destinations.remove(0);//removes destination just arrived at
        
        doorOpen();   
    }
    
    /**
     * Opens the elevator doors and prints a message.
     */
    @Override
    public void doorOpen()
    {
        if(doorState != Door.OPEN)
        {
            doorState = Door.OPEN;
            System.out.println("Elevator " + elevatorID + " doors open.");
            
            //open doors for 1 second
            try
            {
                Thread.sleep(TIME_BETWEEN_OPEN_CLOSE/SCALE_FACTOR);
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
            
            //close the doors after 1 second
            doorClose();
        }
    }

    /**
     * Closes the elevator doors and prints a message.
     */
    @Override
    public void doorClose()
    {
        if(doorState != Door.CLOSED)
        {
            doorState = Door.CLOSED;
            System.out.println("Elevator " + elevatorID + " doors closed.");
        }
    }
    
    /**
     * Takes an integer floor number and returns its corresponding floor object in the building's arraylist of floors.
     * @param floorNumIn
     * @return 
     */
    private Floor getFloorInstanceOf(int floorNumIn)
    {
        return RegBuilding.getInstance().getFloorWithIndex(floorNumIn);
    }
    
    /**
     * Creates a string containing the current destination list of this elevator.
     * @return destList
     */
    private String printDestList()
    {
        String destList = "No further destinations.";
        int limit = destinations.size();
        
        if(!destinations.isEmpty())
        {
            destList = " Full destination list : [";
            for(int i = 0; i < limit; i++)
            {
                destList += destinations.get(i).getFloorID();
                if((i + 1) != limit)
                    destList += ", ";
            }
            
            destList += "].";
        }
        
        return destList;
    }
    
   
    
    
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    //everything below this line is not part of submission 2
    
    /**
     * Checks that an entry request for the elevator is valid. First checks that the elevator is not at capacity, then checks that the Movable that made the request is currently on the same floor as the elevator.
     * @param movableIn
     * @throws OverCapacityException 
     */
    @Override
    public void entryRequest(Movable movableIn) throws OverCapacityException
    {
        /*
        if(movablesOnElevator.size() < ELEVATOR_CAPACITY)
        {
            if(((RegPerson)movableIn).getCurrentFloor().getFloorID() == this.currentFloor)
            {
                
            }
            else
            {
                //throw new MovableNotAvailableException("The requested movable is not on the same floor as the elevator.");
            }
        }
        else
        {
            throw new OverCapacityException("This elevator cannot accept any more movables.");
        }
        * 
        *         if(doorState == Door.OPEN)
        {
            
        }
        else
        {
            throw new ElevatorNotReadyException("This elevator's doors are closed and cannot accept movables.");
        }
        * */
        
    }

    @Override
    public void removalRequest(Movable movableIn)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * After confirming that the entry request is valid, this method will delete the movable that requested entry from its current floor and add it to the elevator. This method can potentially be delegated using the strategy pattern.
     * @param movableIn 
     */
    private void addMovable(Movable movableIn)
    {
        //error involves converting between int/ floor types. ALSO: why do i have to cast everything???
        
        //movableIn.getCurrentFloor().getMovablesOnFloor().delete(movableIn);
        movablesOnElevator.add(movableIn);
    }
    
    /**
     * After confirming that the exit request is valid, this method will delete the movable that requested to exit from the elevator and add it to the elevator's current floor. This method can potentially be delegated using the strategy pattern.
     * @param movableIn 
     */
    private void removeMovable(Movable movableIn)
    {
        movablesOnElevator.remove(movableIn);
        //currentFloor.getMovablesOnFloor().add(movableIn);
    }
}
