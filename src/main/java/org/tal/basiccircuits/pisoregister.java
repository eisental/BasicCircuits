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
public class pisoregister extends Circuit {
    private static final int clockPin = 0;
    private static final int writePin = 1;
    private BitSet7 register = new BitSet7();
    private boolean shift = false;
    private int curIdx = 0;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inIdx==writePin) {
            shift = high;
            if (shift) write();
            else if (hasDebuggers()) debug("Switched to shift mode.");
        } else if (inIdx==clockPin && !shift && high) {
            if (hasDebuggers()) debug("Reading bit " + curIdx + ": " + register.get(curIdx));
            sendOutput(0, register.get(curIdx));
            curIdx++;
            if (curIdx>=inputs.length-2) curIdx = 0;

        }
    }

    private void write() {
        for (int i=2; i<inputs.length; i++) {
            register.set(i-2, inputBits.get(i));
        }
        if (hasDebuggers()) debug("writing " + BitSetUtils.bitSetToBinaryString(register, 0, inputs.length-2) + " to register");
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        if (inputs.length<3) {
            error(sender, "Expecting at least 3 inputs.");
            return false;
        } if (outputs.length!=1) {
            error(sender, "Expecting exactly 1 output. ");
            return false;
        }

        if (sender!=null) resetOutputs();
        return true;
    }

    @Override
    protected boolean isStateless() {
        return false;
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<String,String>();
        state.put("curidx", Integer.toString(curIdx));
        state.put("shift", Boolean.toString(shift));
        BitSetUtils.bitSetToMap(state, "register", register, inputs.length-2);
        return state;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        if (state.containsKey("curidx"))
            curIdx = Integer.decode(state.get("curidx"));
        if (state.containsKey("shift"))
            shift = Boolean.parseBoolean(state.get("shift"));
        if (state.containsKey("register"))
            register = BitSetUtils.mapToBitSet(state, "register");

        if (curIdx>0) {
            sendOutput(0, register.get(curIdx-1));
        }

    }

}
