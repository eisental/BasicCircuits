package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
*
* @author needspeed10
*/
public class pulsar extends Circuit {
int ticks;
    @Override
    public void inputChange(int index, boolean state) 
    {
    	if(state && index == 0)
    	{
    		int val = 0;
    		for (int i=0; i<inputs.length-1; i++) 
    		{
    			if (inputBits.get(i+1)) val += Math.pow(2,i);
    			ticks = val;
    		}
    		for(int i=0;i < ticks;i++)
    		{
    			sendOutput(0,true);
    			sendOutput(0,false);
    		}
    	}
    }

    @Override
    protected boolean init(CommandSender sender, String[] strings) 
    {
        return true;
    }

}
