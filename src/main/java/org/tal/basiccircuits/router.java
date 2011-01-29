/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Tal Eisenberg
 */
public class router extends Circuit {
    Map<Integer, List<Integer>> routingTable;

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
        List<Integer> outs = routingTable.get(inIdx);
        if (outs==null) // just update the respective output.
            sendOutput(inIdx, newLevel);
        else {
            for (int i : outs)
                sendOutput(i, newLevel);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        routingTable = new HashMap<Integer, List<Integer>>();

        for (String arg : args) {
            String[] split = arg.split(":");
            if (split.length!=2) {
                error(player, "Bad routing entry: " + arg);
                return false;
            }

            try {
                Integer input = Integer.decode(split[0]);
                Integer output = Integer.decode(split[1]);
                if (routingTable.containsKey(input)) {
                    routingTable.get(input).add(output);
                } else {
                    List<Integer> outs = new ArrayList<Integer>();
                    outs.add(output);
                    routingTable.put(input, outs);
                }
            } catch (NumberFormatException ne) {
                error(player, "Bad routing entry: " + arg);
            }
        }
        System.out.println(routingTable);
        return true;
    }

}
