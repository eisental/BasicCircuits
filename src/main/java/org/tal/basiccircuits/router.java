
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

    int firstDataPin;

    Map<Integer, List<Integer>> routingTable;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        if (inIdx==0 && (newLevel || firstDataPin == 0)) {
            register.clear();
            for (int i=0; i<inputs.length-firstDataPin; i++) {
                List<Integer> outs = routingTable.get(i);
                if (outs!=null) { // when there's no table entry do nothing.
                    for (int k : outs) {
                        if (k==-1) {
                            if (hasDebuggers()) debug("Routing data input " + i + " (" + (inputBits.get(i+firstDataPin)?"on":"off") + ") to all outputs.");
                            for (int m=0; m<outputs.length; m++)
                                register.set(m, inputBits.get(i+firstDataPin) || register.get(m));
                        } else {
                            if (hasDebuggers()) debug("Routing data input " + i + " (" + (inputBits.get(i+firstDataPin)?"on":"off") + ") to output " + k + ".");
                            register.set(k, inputBits.get(i+firstDataPin) || register.get(k));
                        }
                    }
                }
            }

            sendBitSet(0, outputs.length, register);
        }
        
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        routingTable = new HashMap<Integer, List<Integer>>();

        if (inputs.length<=1) firstDataPin = 0;
        else firstDataPin = 1;

        for (String arg : args) {
            String[] split = arg.split(":");
            if (split.length!=2) {
                error(sender, "Bad routing entry: " + arg);
                return false;
            }

            try {
                Integer input = Integer.decode(split[0]);                
                Integer output;
                if (split[1].equalsIgnoreCase("all")) {
                    output = -1;
                } else
                    output = Integer.decode(split[1]);
                if (input>=inputs.length-firstDataPin) error(sender, "Data input " + input + " doesn't exist");

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

        return true;
    }

}
