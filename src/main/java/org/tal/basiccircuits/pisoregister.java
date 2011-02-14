package org.tal.basiccircuits;


import org.bukkit.entity.Player;
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
    private BitSet7 register;
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
    protected boolean init(Player player, String[] args) {
        if (inputs.length<3) {
            error(player, "Expecting at least 3 inputs.");
            return false;
        } if (outputs.length!=1) {
            error(player, "Expecting exactly 1 output. ");
            return false;
        }

        register = new BitSet7(inputs.length-2);

        return true;
    }

}
