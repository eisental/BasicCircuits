package org.tal.basiccircuits;


import java.util.BitSet;
import org.bukkit.entity.Player;
import org.tal.redstonechips.Circuit;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class pisoregister extends Circuit {
    private static final int clockPin = 0;
    private static final int writePin = 1;
    private BitSet register;
    private boolean shift = false;
    private int curIdx = 0;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inIdx==writePin) {
            shift = high;
            if (shift) write();
        } else if (inIdx==clockPin && shift) {
            sendOutput(0, register.get(curIdx));
            curIdx++;
            if (curIdx>=inputs.length-2) curIdx = 0;

        }
    }

    private void write() {
        for (int i=2; i<inputs.length; i++) {
            register.set(i-2, inputBits.get(i));
        }
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

        register = new BitSet(inputs.length-2);

        return true;
    }

}
