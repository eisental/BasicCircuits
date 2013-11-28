
package org.tal.basiccircuits;

import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class repeater extends Circuit {
    int outputSets;
    int outputSetSize;
    
    @Override
    public void inputChange(int idx, boolean state) {
        if (outputSets==0)
            for (int i=0; i<outputs.length; i++) sendOutput(i, state);
        else
            for (int j=0; j<outputSets; j++)
                sendOutput(j*outputSetSize+idx, state);
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) { 
        if (inputs.length<1) {
            error(sender, "Expecting at least 1 input pin.");
            return false;
        }
        if (outputs.length<1) {
            error(sender, "Expecting at least 1 output pin.");
            return false;
        }
        
        if (inputs.length == 1)
            outputSets = 0; //optimize for original function
        else
            outputSets = outputs.length/inputs.length;
        
        if (outputSets != 0) {
            if (outputs.length != inputs.length*outputSets) {
                error(sender, "Tried to split "+outputs.length+" into "+outputSets+", expected "+(inputs.length*outputSets)+" outputs.");
                return false;
            }
            outputSetSize = outputs.length/outputSets;
            if (outputSets==1)
                info(sender, "Repeating "+outputs.length+" bits.");
            else
                info(sender, "Splitting "+inputs.length+" bits into "+outputs.length+", "+outputSets+" sets");
        } else {
            outputSetSize = 0; //not used
            if (outputs.length==1)
                info(sender, "Repeating 1 bit.");
            else
                info(sender, "Splitting 1 bit into "+outputs.length+".");
        }
        
        return true;
    }
}
