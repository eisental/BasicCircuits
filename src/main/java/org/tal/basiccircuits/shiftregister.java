package org.tal.basiccircuits;


import java.util.HashMap;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 *
 * @author Tal Eisenberg
 */
public class shiftregister extends Circuit {
    final static int ClockPin = 0;
    final static int ResetPin = 2;
    final static int DataPin = 1;
    
    private boolean shiftRight = false;
    
    private BitSet7 register;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inIdx==0 && high) { // clock
            if (shiftRight) {
                BitSetUtils.shiftRight(register, outputs.length, false);
                register.set(outputs.length-1, inputBits.get(DataPin));
            } else {
                BitSetUtils.shiftLeft(register, outputs.length);
                register.set(0, inputBits.get(DataPin));
            }
            sendBitSet(register);
            
        } else if (inIdx==2 && high) { // reset
            register.clear();
            sendBitSet(register);
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length!=2 && inputs.length!=3) {
            error(sender, "Expecting 2 or 3 inputs. ");
            return false;
        }

        if (args.length>0) {
            if (args[0].equalsIgnoreCase("right")) {
                shiftRight = true;
            } else if (args[0].equalsIgnoreCase("left")) {
                shiftRight = false;
            } else {
                error(sender, "Invalid argument: " + args[0] + " expecting right|aright|left.");
                return false;
            }
        }
        if (sender!=null) resetOutputs();
        register = new BitSet7(outputs.length);
        return true;

    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<String,String>();
        BitSetUtils.bitSetToMap(state, "register", register, outputs.length);
        return state;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        register = BitSetUtils.mapToBitSet(state, "register");
        outputBits = (BitSet7)register.clone();
    }

    @Override
    protected boolean isStateless() {
        return false;
    }

}
