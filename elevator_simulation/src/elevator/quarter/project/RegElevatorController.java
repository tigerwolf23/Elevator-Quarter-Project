
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elevator.quarter.project;
import java.util.ArrayList;

/**
 *
 * @author ZGraceffa & Craig
 */
public class RegElevatorController implements ElevatorController, Lift
{
    private ArrayList<Elevator> elevators;
    private static RegElevatorController instance = null;
    
    private RegElevatorController()
    {
        
    }
    
    /**
     * This is the singleton getInstance() method that returns the only instance of RegElevatorController or creates it if it does not yet exist.
     * @return 
     */
    public static RegElevatorController getInstance()
    {
        if(instance == null)
        {
            synchronized(ElevatorController.class)
            {
                if(instance == null)
                {
                    instance = new RegElevatorController();
                }
            }
        }
        return instance;
    }
    
    /*/**
     * This method recieves a request from an elevator CallBox, it intelligently 
     * decides which elevator to send but takes NO ACTION; it then tells the 
     * private up/down methods to somehow service that request.
     * @param aFloor
     * @param direction
     */
   /* @Override
    public void request(Floor aFloor, int direction)
    {
        Elevator sendElevator;
        
       
        for(int i = 0; i < elevators.size(); i++)
        {
            if (elevators[i].getstate() == ElevatorState.IDLE)
            {
                sendElevator = elevators[i];
                
            }
        }
        
    }*/
    
    /**
     * Simple ElevatorController method that does not intelligently choose which elevator to send. This function is for part 2 of the project only.
     * @param floorIn
     * @param elevatorIn 
     */
    @Override
    public void request(Floor floorIn, Elevator elevatorIn)
    {
        elevatorIn.addFloorToDestList(floorIn);
        
        
        
        /*
        //if the elevator is currently on the requested floor, ignore.
        if(elevatorIn.getCurrentFloor().getFloorID() == floorIn.getFloorID())
        {
            System.out.println("The requested elevator is already on the requested floor. Doors opening.");
            elevatorIn.doorOpen();
        }
        //if the elevator is below the requested floor, send the elevator up
        else if(elevatorIn.getCurrentFloor().getFloorID() < floorIn.getFloorID())
        {
            sendElevatorUp(floorIn, elevatorIn);
        }        
        //if the elevator is above the requested floor, send the elevator down
        else if(elevatorIn.getCurrentFloor().getFloorID() > floorIn.getFloorID())
        {
            sendElevatorDown(floorIn, elevatorIn);
        }
        */
    }
    
    /**
     * 
     * @param whichFloor
     * @param whichElevator 
     */
    private void sendElevatorUp(Floor whichFloor, Elevator whichElevator)
    {
        //if elevator is already headed in correct direction hasn't passed desired floor, simply add floor to destination list.
        
        
    }

    /**
     * 
     * @param whichFloor
     * @param whichElevator 
     */
    private void sendElevatorDown(Floor whichFloor, Elevator whichElevator)
    {
        
    }
}