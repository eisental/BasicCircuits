package org.tal.basiccircuits;


import org.bukkit.entity.Player;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.util.BitSet7;

/**
 *
 * @author Tal Eisenberg
 */
public class shiftregister extends Circuit {
    private BitSet7 register;

    @Override
    public void inputChange(int inIdx, boolean high) {
        if (inIdx==0 && high) { // clock
            Circuit.shiftLeft(register, outputs.length);
            register.set(0, inputBits.get(1));
            sendBitSet(register);
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
        if (inputs.length!=2) {
            error(player, "Expecting two inputs. ");
            return false;
        }


        register = new BitSet7(outputs.length);
        return true;

    }


}
