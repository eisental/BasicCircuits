
package org.redstonechips.basiccircuits;

import org.bukkit.entity.Player;
import org.redstonechips.RCTypeReceiver;
import org.redstonechips.circuit.Circuit;
import org.redstonechips.chip.io.InterfaceBlock;
import org.redstonechips.util.BooleanArrays;
import org.redstonechips.wireless.Transmitter;

/**
 *
 * @author Tal Eisenberg
 */
public class terminal extends Circuit implements RCTypeReceiver {
    private enum DataType { ascii, num }

    private boolean[] register;
    private DataType type = DataType.ascii;
    private boolean eot;
    
    private Transmitter transmitter;
    
    @Override
    public void input(boolean state, int inIdx) {
        if (inIdx==0 && state) {
            // clear pin
            this.clearOutputs();
        }
    }

    @Override
    public Circuit init(String[] args) {
        eot = false;
        String channelArg = null;
        
        if (args.length>0 && args[args.length-1].startsWith("#")) {
            // last argument is a channel name
            channelArg = args[args.length-1].substring(1);
            String[] newArgs = new String[args.length-1];
            if (newArgs.length>0)
                System.arraycopy(args, 0, newArgs, 0, newArgs.length);

            args = newArgs;            
        } 

        
        if (args.length>0) {
            try {
                type = DataType.valueOf(args[0]);
            } catch (IllegalArgumentException ie) {
                return error("Unknown data type argument: " + args[0]);
            }

            if (args.length>1) {
                if (args[1].equalsIgnoreCase("eot")) {
                    eot = true;
                } else 
                    return error("Unknown argument: " + args[1]);
            }
        }

        if (channelArg==null) {
            if (type==DataType.ascii && outputlen!=9 && outputlen!=10)
                return error("Expecting 9-10 outputs. 1 clock output, an optional end-of-text output and 8 data outputs.");
            else if (type==DataType.num && outputlen<2)
                return error("Expecting at least 2 outputs. 1 clock output and 1 or more data outputs.");            
        }

        if (chip.interfaceBlocks.length==0) return error("Expecting at least one interface block.");
            

        for (InterfaceBlock i : chip.interfaceBlocks)
            rc.addRCTypeReceiver(i.getLocation(), this);

        if (channelArg!=null) {
            int len;
            if (type==DataType.ascii) {
                if (eot) len = 9;
                else len = 8;
            } else {
                if (eot) len = 33;
                else len = 32;
            }
            
            transmitter = new Transmitter();
            transmitter.init(activator, channelArg, len, this);
        }
        
        return this;
    }

    @Override
    public void shutdown() {
        rc.removeRCTypeReceiver(this);
    }

    @Override
    public void type(String[] words, Player player) {
        if (words.length==0) {
            errorForSender(player, "Nothing to type.");
            return;
        }

        String typeString = "";

        for (String a : words) typeString += a + " ";
        
        if (type==DataType.ascii) {
            int datapin = (outputlen==9?1:2);

            for (int i=0; i<typeString.length()-1; i++) {
                register = BooleanArrays.fromInt(typeString.charAt(i), 8);
                if (chip.hasListeners()) debug("Sending " + BooleanArrays.toPrettyString(register, 0, 8) + "(" + (char)typeString.charAt(i) + ")");
                
                if (transmitter!=null) transmitter.transmit(register, 0, 8);
                else {
                    this.writeBits(register, datapin, 8);
                    this.write(true, 0);
                    this.write(false, 0);
                }
            }
           
            if (eot) {
                // send an EOT (end of text character - 0x3)                
                if (transmitter!=null) transmitter.transmit(0x3, 0, 8);
                else {
                    this.writeInt(0x3, datapin, 8);
                    this.write(true, 0);
                    this.write(false, 0);                    
                }
            }

            if (datapin==2 && transmitter==null) {
                // pulse the EOT output pin if exists.                
                this.write(true, 1);
                this.write(false, 1);
            }
        } else if (type==DataType.num) {
            try {
                int i = Integer.decode(typeString.trim());
                register = BooleanArrays.fromInt(i);
                if (chip.hasListeners()) debug("Sending " + BooleanArrays.toPrettyString(register, 0, outputlen-1));
                this.writeBits(register, 1, outputlen-1);
                
                if (transmitter!=null) 
                    transmitter.transmit(register, transmitter.getChannelLength());
                else {
                    this.write(true, 0);
                    this.write(false, 0);                    
                }
                
            } catch (NumberFormatException ne) {
                errorForSender(player, "Not a number: " + typeString.trim() + ". Use a 'ascii' sign argument to sending ascii characters.");
            }
        }

    }
}

