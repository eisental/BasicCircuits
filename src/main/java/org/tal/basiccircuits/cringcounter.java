package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
*
* @author needspeed10
*/
public class cringcounter extends Circuit{
    BitSet7 register;
    CommandSender sendi;
    int count = 0,value;
    @Override
    public void inputChange(int inIdx, boolean newLevel) {
    	if (newLevel) {
            if (inIdx==0)
            {                
            	count += value;
                if(!(count> -1 && count < outputs.length))
                {
                	if(value>0) count = count-outputs.length;
                	else if(value<0) count = count+outputs.length;        	
                }
                register.clear();
                register.set(count);
            }
            else if (inIdx==1) 
            {
            	register.clear();
            	if(value> 0)register.set(count=0);
            	else register.set(count=outputs.length-1);
            }
            sendBitSet(register);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
    	if (inputs.length==0) {
            error(sender, "Expecting at least 1 clock input. A 2nd reset input pin is optional.");
            return false;
        } else if (outputs.length==0) {
            error(sender, "Expecting at least 1 output.");
            return false;
        }    	
        value = Integer.decode(args[0]);
        register = new BitSet7(outputs.length);
        sendi = sender;
        if(value > 0)register.set(0);else {register.set(outputs.length-1);count = outputs.length-1;}
    	sendBitSet(register);
        return true;
    }

}