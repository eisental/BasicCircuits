/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class router extends Circuit {
    BitSet7 register;

    Map<Integer, List<Integer>> routingTable;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==0 && newLevel) {
            register.clear();
            for (int i=0; i<inputs.length-1; i++) {
                List<Integer> outs = routingTable.get(i);
                if (outs!=null) { // when there's no table entry do nothing.
                    for (int k : outs) {
                        if (hasDebuggers()) debug("Routing data input " + i + " (" + (inputBits.get(i+1)?"on":"off") + ") to output " + k);
                        register.set(k, inputBits.get(i+1) || register.get(k));
                    }
                }
            }

            sendBitSet(0, outputs.length, register);
        }
        
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        routingTable = new HashMap<Integer, List<Integer>>();

        for (String arg : args) {
            String[] split = arg.split(":");
            if (split.length!=2) {
                error(sender, "Bad routing entry: " + arg);
                return false;
            }

            try {
                Integer input = Integer.decode(split[0]);
                Integer output = Integer.decode(split[1]);
                if (input>=inputs.length-1) error(sender, "Data input " + input + " doesn't exist");

                if (routingTable.containsKey(input)) {
                    routingTable.get(input).add(output);
                } else {
                    List<Integer> outs = new ArrayList<Integer>();
                    outs.add(output);
                    routingTable.put(input, outs);
                }

                register = new BitSet7(outputs.length);
            } catch (NumberFormatException ne) {
                error(sender, "Bad routing entry: " + arg);
            }
        }
        //System.out.println(routingTable);
        return true;
    }

}
