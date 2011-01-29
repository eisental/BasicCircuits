package org.tal.basiccircuits;

import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;

/**
 *
 * @author Matthew Peychich
 */
public class srnor extends Circuit {

    @Override
    public void inputChange(int inIdx, boolean newLevel) {
      // only update outputs when an input goes low to high.
      if(newLevel){
        for(int i = 0; i < inputs.length; i++){
          sendOutput(i, true);
        }
        sendOutput(inIdx, false);
      }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=outputs.length) {
            error(player, "Expecting the same number of inputs and outputs.");
            return false;
        }

        return true;
    }

}
