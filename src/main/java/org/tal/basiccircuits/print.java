package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.tal.basiccircuits.SignWriter.DisplayMode;
import org.tal.basiccircuits.SignWriter.Type;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.io.InterfaceBlock;
import org.tal.redstonechips.circuit.RCTypeReceiver;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.Locations;
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
    private Receiver receiver;
    
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
    
    class PrintReceiver extends Receiver {
        @Override
        public void receive(BitSet7 bits) {
            if ((writer.getDisplayMode()==DisplayMode.scroll || writer.getDisplayMode()==DisplayMode.add) && bits.get(clearPin-1))
                clear();
            if (writer.getDisplayMode()==DisplayMode.scroll && bits.get(scrollPin-1))
                writer.scroll(1);

            write(bits, dataPin-1, getChannelLength()-(dataPin-1));
        }
    }
    
    private void clear() {
        if (hasDebuggers()) debug("Clearing signs.");
        writer.clear();
    }
    
    private void write(BitSet7 bits, int start, int length) {
        writer.write(bits, start, length);
        if (hasDebuggers()) {
            String[] lines = writer.getLines();
            debug("printed:");
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
        
        if (channel!=null && !initReceiver(sender, channel, type)) return false;
        
        List<Location> signList = findSigns();
        if (signList.isEmpty()) {
            error(sender, "Couldn't find any signs attached to the chip interface blocks.");
            return false;
        } else {
            List<Location> str = new ArrayList<Location>();
            str.addAll(Arrays.asList(structure));
            str.addAll(signList);
            this.structure = str.toArray(new Location[str.size()]);
            info(sender, "Found " + signList.size() + " sign(s) to print on.");
        }
        
        writer = new SignWriter(display, type, signList);
        redstoneChips.addRCTypeReceiver(activationBlock, this);
        
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
            receiver = new PrintReceiver();
            int len = dataPin-1 + (type==Type.ascii?8:32);
            receiver.init(sender, channel, len, this);
            return true;
        } catch (IllegalArgumentException e) {
            error(sender, e.getMessage());
            return false;
        }        
    }
    
    private List<Location> findSigns() {
        List<Location> signs = new ArrayList<Location>();

        for (InterfaceBlock ib : interfaceBlocks) {
            Location loc = ib.getLocation();
            Location north = Locations.getFace(loc, BlockFace.NORTH);
            Location south = Locations.getFace(loc, BlockFace.SOUTH);
            Location west = Locations.getFace(loc, BlockFace.WEST);
            Location east = Locations.getFace(loc, BlockFace.EAST);
            Location up = Locations.getFace(loc, BlockFace.UP);

            Block i = loc.getBlock();
            if (checkBlock(i, north)) { signs.add(north); }
            if (checkBlock(i, south)) { signs.add(south); }
            if (checkBlock(i, west)) { signs.add(west); }
            if (checkBlock(i, east)) { signs.add(east); }
            if (checkBlock(i, up)) { signs.add(up); }
        }
        
        return signs;
    }
    
    private boolean checkBlock(Block i, Location s) {
        // TODO: Check whether this method loads the chunk or not.
        Block sign = s.getBlock();
        MaterialData data = sign.getState().getData();
        if (data instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign)data;
            if (sign.getRelative(signData.getAttachedFace()).equals(i)) // make sure the sign is actually attached to the interface block.
                return true;
            else return false;

        } else return false;
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
