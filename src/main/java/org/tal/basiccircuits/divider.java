package org.tal.basiccircuits;


import java.util.BitSet;
import java.util.Map;
import org.bukkit.entity.Player;
import org.tal.redstonechips.BitSetCircuit;
import org.tal.redstonechips.Circuit;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tal Eisenberg
 */
public class divider extends BitSetCircuit {
    int constant = 0;

    @Override
    protected void bitSetChanged(int bitSetIdx, BitSet set) {
        int div = Circuit.bitSetToUnsignedInt(inputBitSets[0], 0, wordlength);
        if (inputBitSets.length>1) {
            for (int i=1; i<inputBitSets.length; i++) {
                int num = Circuit.bitSetToUnsignedInt(inputBitSets[i], 0, wordlength);
                if (num==0) { div = 0; break; }
                div = Math.round(div/num);
            }
        }

        div = Math.round(div / constant);
        this.sendInt(0, outputs.length, div);
    }

    @Override
    public boolean init(Player player, String[] args) {
        if (!super.init(player, args)) return false;
        if (args.length>0) {
            try {
                constant = Integer.decode(args[0]);
                if (constant == 0) {
                    player.sendMessage("Can't divide by zero.");
                    return false;
                } else return true;
            } catch (NumberFormatException ne) {
                player.sendMessage("Bad argument: " + args[0] + " expected a number.");
                return false;
            }
        } else constant = 1;

        return true;
    }

    @Override
    public void loadState(Map<String, String> state) {
        String sconst = state.get("constant");
        if (sconst==null) return;

        constant = Integer.decode(sconst);
        super.loadState(state);
    }

    @Override
    public Map<String, String> saveState() {
        Map<String,String> map = super.saveState();
        map.put("constant", ""+constant);
        return map;
    }

}
