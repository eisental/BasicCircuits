package org.redstonechips.basiccircuits;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.redstonechips.Serializer;
import org.redstonechips.chip.Circuit;
import org.redstonechips.util.BooleanArrays;

/**
 *
 * @author Tal Eisenberg
 */
public class shiftregister extends Circuit {
    final static int ClockPin = 0;
    final static int ResetPin = 2;
    final static int DataPin = 1;
    
    private boolean shiftRight = false;
    
    private boolean[] register;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==0 && state) { // clock
            if (shiftRight) {
                BooleanArrays.shiftRight(register, register, false);
                register[outputlen-1] = inputs[DataPin];
            } else {
                BooleanArrays.shiftLeft(register, register);
                register[0] = inputs[DataPin];
            }
            writeBits(register);
            
        } else if (inIdx==2 && state) { // reset
            Arrays.fill(register, false);
            writeBits(register);
        }
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen!=2 && inputlen!=3) return error("Expecting 2 or 3 inputs. ");

        if (args.length>0) {
            if (args[0].equalsIgnoreCase("right")) shiftRight = true;                
            else if (args[0].equalsIgnoreCase("left")) shiftRight = false;                
            else return error("Invalid argument: " + args[0] + " expecting right|aright|left."); 
        }
        
        if (activator!=null) clearOutputs();
        register = new boolean[outputlen];
        
        return this;

    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<>();
        return Serializer.booleanArrayToMap(state, "register", register);
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        register = Serializer.mapToBooleanArray(state, "register");
        this.writeBits(register);
    }

    @Override
    public boolean isStateless() {
        return false;
    }
}
