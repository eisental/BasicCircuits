package org.tal.basiccircuits;

import java.util.*;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tal.basiccircuits.SignWriter.DisplayMode;
import org.tal.basiccircuits.SignWriter.Type;
import org.tal.redstonechips.bitset.BitSet7;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.RCTypeReceiver;
import org.tal.redstonechips.circuit.io.IOBlock;
import org.tal.redstonechips.wireless.Receiver;


/**
 *
 * @author Tal Eisenberg
 */
public class print extends Circuit implements RCTypeReceiver {
    private final static int clockPin = 0;
    private final static int scrollPin = 2;
    private final static int clearPin = 1;

    private int dataPin = 1;
    private SignWriter writer;
    
    @Override
    public void inputChange(int inIdx, boolean state) {
        if (inIdx==clockPin) {
            if (state) write(inputBits, dataPin, inputs.length-dataPin);
            
        } else if (inIdx==clearPin && (writer.getDisplayMode()==DisplayMode.scroll || 
                writer.getDisplayMode()==DisplayMode.add)) {
            if (state) clear();
            
        } else if (inIdx==scrollPin && writer.getDisplayMode()==DisplayMode.scroll) {
            if (state) writer.scroll(1);
            
        } else if (inputBits.get(clockPin))
            write(inputBits, dataPin, inputs.length-dataPin);
        
    }
    
    class WriteReceiver extends Receiver {
        
        @Override
        public void receive(BitSet7 bits) {
            write(bits, 0, getChannelLength());
        }
    }
    
    class ClearReceiver extends Receiver {

        @Override
        public void receive(BitSet7 bits) {
            if (bits.get(0)) writer.clear();
        }        
    }
    
    class ScrollReceiver extends Receiver {
        @Override
        public void receive(BitSet7 bits) {
            if (bits.get(0)) writer.scroll(1);
        }
        
    }
    
    private void clear() {
        if (hasListeners()) debug("Clearing signs.");
        writer.clear();
    }
    
    private void write(BitSet7 bits, int start, int length) {
        writer.write(bits, start, length);
        if (hasListeners()) {
            String[] lines = writer.getLines();
            debug("text:");
            debug(lines[0]);
            debug(lines[1]);
            debug(lines[2]);
            debug(lines[3]);
        }
    }
    
    @Override
    public void type(String[] words, Player player) {
        if (words.length==0) return;

        String text = "";
        for (String word : words)
            text += word + " ";
        writer.write(text.substring(0, text.length()-1));
    }
    
    @Override
    public boolean init(CommandSender sender, String[] args) {        
        String channel = null;
        Type type = Type.num;
        DisplayMode display = DisplayMode.replace;
        
        if (args.length>0) {            
            if (args[args.length-1].startsWith("#")) { // channel arg
                channel = args[args.length-1].substring(1);
            }
            
            if (args.length>=(channel!=null?2:1)) {
                try {
                    type = Type.valueOf(args[0]);
                } catch (IllegalArgumentException ie) {
                    error(sender, "Unknown type: " + args[0]);
                    return false;
                }
            }

            if (args.length>=(channel!=null?3:2)) {
                try {
                    display = DisplayMode.valueOf(args[1]);
                } catch (IllegalArgumentException ie) {
                    error(sender, "Unknown display arg: " + args[1]);
                    return false;
                }
            }
        }

        if (channel==null && !checkInputs(sender, display)) return false;

        if (interfaceBlocks.length==0) {
            error(sender, "Expecting at least 1 interface block.");
            return false;
        }

        if (display==DisplayMode.replace) dataPin = 1;
        else if (display==DisplayMode.add) dataPin = 2;
        else if (display==DisplayMode.scroll) dataPin = 3;
                
        writer = SignWriter.getSignWriter(display, type, IOBlock.locationsOf(interfaceBlocks));        
        if (writer.getSigns().isEmpty()) {
            error(sender, "Couldn't find any signs attached to the chip interface blocks.");
            return false;
        } else {
            List<Location> str = new ArrayList<Location>();
            str.addAll(Arrays.asList(structure));
            str.addAll(writer.getSigns());
            this.structure = str.toArray(new Location[str.size()]);
            info(sender, "Found " + writer.getSigns().size() + " sign(s) to print on.");
        }
        

        redstoneChips.addRCTypeReceiver(activationBlock, this);
        
        if (channel!=null && !initReceiver(sender, channel, type)) return false;
        
        return true;
    }

    private boolean checkInputs(CommandSender sender, DisplayMode display) {
        if (display==DisplayMode.replace && inputs.length<2) {
            error(sender, "Expecting at least 2 inputs. 1 clock pin and 1 data pin.");
            return false;
        } else if (display==DisplayMode.add && inputs.length<3) {
            error(sender, "Expecting at least 3 inputs. 1 clock pin, 1 clear pin and 1 data pin.");
            return false;
        } else if (display==DisplayMode.scroll && inputs.length<4) {
            error(sender, "Expecting at least 4 inputs. 1 clock pin, 1 clear pin, 1 scroll pin and 1 data pin.");
            return false;
        } else return true;
    }
    
    private boolean initReceiver(CommandSender sender, String channel, Type type) {
        try {
            Receiver writeReceiver = new WriteReceiver();
            writeReceiver.init(sender, channel, type==Type.ascii?8:32, this);
            
            if (writer.getDisplayMode()==DisplayMode.add) {
                Receiver clearReceiver = new ClearReceiver();
                clearReceiver.init(sender, channel, 1, this);
                
                writeReceiver.setStartBit(writeReceiver.getStartBit()+1);
            } else if (writer.getDisplayMode()==DisplayMode.scroll) {
                Receiver clearReceiver = new ClearReceiver();
                clearReceiver.init(sender, channel, 1, this);
                
                Receiver scrollReceiver = new ScrollReceiver();
                scrollReceiver.init(sender, channel, 1, this);
                scrollReceiver.setStartBit(scrollReceiver.getStartBit()+1);

                writeReceiver.setStartBit(writeReceiver.getStartBit()+2);                
                        
            }
                
            return true;
        } catch (IllegalArgumentException e) {
            error(sender, e.getMessage());
            return false;
        }        
    }
    
    @Override
    public void setInternalState(Map<String, String> state) {
        Object text = state.get("text");

        if (text!=null) 
            writer.setText(text.toString());
    }

    @Override
    public Map<String, String> getInternalState() {
        Map<String,String> state = new HashMap<String,String>();
        state.put("text", writer.getText());
        return state;
    }

    @Override
    protected boolean isStateless() {
        return false;
    }    
}
