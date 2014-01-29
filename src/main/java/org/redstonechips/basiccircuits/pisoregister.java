package org.redstonechips.basiccircuits;


import java.util.HashMap;
import java.util.Map;
import org.redstonechips.Serializer;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.util.BooleanArrays;


/**
 *
 * @author Tal Eisenberg
 */
public class pisoregister extends Circuit {
    private static final int clockPin = 0;
    private static final int writePin = 1;
    private boolean[] register;
    private boolean shift = false;
    private int curIdx = 0;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==writePin) {
            shift = state;
            if (shift) writeRegister();
            else if (chip.hasListeners()) debug("Switched to shift mode.");
        } else if (inIdx==clockPin && !shift && state) {
            if (chip.hasListeners()) debug("Reading bit " + curIdx + ": " + register[curIdx]);
            write(register[curIdx], 0);
            curIdx++;
            if (curIdx>=inputlen-2) curIdx = 0;

        }
    }

    private void writeRegister() {
        System.arraycopy(inputs, 2, register, 0, register.length);

        if (chip.hasListeners()) debug("writing " + BooleanArrays.toPrettyString(register) + " to register");
    }

    @Override
    public Circuit init(String[] args) {
        if (inputlen<3) return error("Expecting at least 3 inputs.");
        if (outputlen!=1) return error("Expecting exactly 1 output. ");

        register = new boolean[inputlen-2];
        
        if (activator!=null) clearOutputs();
        return this;
    }

    @Override
    public boolean isStateless() {
        return false;
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<>();
        state.put("curidx", Integer.toString(curIdx));
        state.put("shift", Boolean.toString(shift));
        Serializer.booleanArrayToMap(state, "register", register, register.length);
        return state;
    }

    @Override
    public void setInternalState(Map<String, String> state) {
        if (state.containsKey("curidx"))
            curIdx = Integer.decode(state.get("curidx"));
        if (state.containsKey("shift"))
            shift = Boolean.parseBoolean(state.get("shift"));
        if (state.containsKey("register"))
            register = Serializer.mapToBooleanArray(state, "register");

        if (curIdx>0) {
            write(register[curIdx-1], 0);
        }

    }
}
