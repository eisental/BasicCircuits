
package org.redstonechips.basiccircuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.redstonechips.chip.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class router extends Circuit {
    boolean[] register;

    int firstDataPin;

    Map<Integer, List<Integer>> routingTable;

    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==0 && (state || firstDataPin == 0)) {
            Arrays.fill(register, false);
            for (int i=0; i<inputlen-firstDataPin; i++) {
                List<Integer> outs = routingTable.get(i);
                if (outs!=null) { // when there's no table entry do nothing.
                    for (int k : outs) {
                        if (k==-1) {
                            if (chip.hasListeners()) debug("Routing data input " + i + " (" + (inputs[i+firstDataPin]?"on":"off") + ") to all outputs.");
                            for (int m=0; m<outputlen; m++)
                                register[m] = inputs[i+firstDataPin] || register[m];
                        } else {
                            if (chip.hasListeners()) debug("Routing data input " + i + " (" + (inputs[i+firstDataPin]?"on":"off") + ") to output " + k + ".");
                            register[k] = inputs[i+firstDataPin] || register[k];
                        }
                    }
                }
            }

            writeBits(register, 0, outputlen);
        }
        
    }

    @Override
    public Circuit init(String[] args) {
        routingTable = new HashMap<>();

        if (inputlen<=1) firstDataPin = 0;
        else firstDataPin = 1;

        for (String arg : args) {
            String[] split = arg.split(":");
            if (split.length!=2) return error("Bad routing entry: " + arg);

            try {
                Integer input = Integer.decode(split[0]);                
                Integer output;
                if (split[1].equalsIgnoreCase("all")) {
                    output = -1;
                } else
                    output = Integer.decode(split[1]);
                if (input>=inputlen-firstDataPin) return error("Data input " + input + " doesn't exist.");

                if (routingTable.containsKey(input)) {
                    routingTable.get(input).add(output);
                } else {
                    List<Integer> outs = new ArrayList<>();
                    outs.add(output);
                    routingTable.put(input, outs);
                }

                register = new boolean[outputlen];
            } catch (NumberFormatException ne) {
                return error("Bad routing entry: " + arg);
            }
        }

        return this;
    }
}
