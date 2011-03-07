package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;
import org.tal.redstonechips.circuit.Circuit;
import org.tal.redstonechips.circuit.ReceivingCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 * // dyes wool if present on output block
 * @author Tal Eisenberg
 */
public class pixel extends Circuit implements ReceivingCircuit {
    private boolean indexedColor = false;
    private byte[] colorIndex;
    private String broadcastChannel = null;
    private static BlockFace[] faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    
    @Override
    public void inputChange(int inIdx, boolean on) {
        if (inputs.length==1) {
            updatePixel();
        } else if (inIdx==0 && on) { // clock pin
            updatePixel();
        }
    }

    @Override
    protected boolean init(CommandSender sender, String[] args) {
        // needs to have 5 inputs 1 clock 4 data

        if (args.length>0) {
            List<Byte> colorList = new ArrayList<Byte>();
            for (int i=0; i<args.length; i++) {
                try {
                    colorList.add(DyeColor.valueOf(args[i].toUpperCase()).getData());
                } catch (IllegalArgumentException ie) {
                    // not dye color
                    try {
                        int val = Integer.decode(args[i]);
                        colorList.add((byte)val);
                    } catch (NumberFormatException ne) {
                        // not dye number also, treat as broadcast channel if last;
                        if (i==args.length-1) broadcastChannel = args[i];
                        else {
                            error(sender, "Unknown color name: " + args[i]);
                            return false;
                        }
                    }
                }

            }

            if (!colorList.isEmpty()) {
                colorIndex = new byte[colorList.size()];
                for (int i=0; i<colorList.size(); i++)
                    colorIndex[i] = colorList.get(i);
                indexedColor = true;
            }

            if (broadcastChannel!=null) {
                info(sender, "Pixel will listen on broadcast channel " + broadcastChannel + ".");
                redstoneChips.addReceiver(this);
            }
        }



        if (inputs.length>5) {
            error(sender, "Too many inputs. Requires 1 clock pin and no more than 4 data pins.");
            return false;
        }

        if (this.interfaceBlocks.length==0) {
            error(sender, "Expecting at least 1 interface block.");
            return false;
        }
        return true;
    }

    private void updatePixel() {        
        int val;
        if (inputs.length<=1) val = BitSetUtils.bitSetToUnsignedInt(inputBits, 0, inputBits.length());
        else val = BitSetUtils.bitSetToUnsignedInt(inputBits, 1, inputBits.length()-1);

        byte color;

        if (indexedColor) {
            int index = val;
            if (index>=colorIndex.length) {
                if (hasDebuggers()) debug("Color index out of bounds: " + index);
                return;
            }
            color = colorIndex[index];
        } else 
            color = (byte)val;

        if (hasDebuggers()) debug("Setting pixel color to " + DyeColor.getByData(color));

        for (Location l : interfaceBlocks)
            colorBlocks(world.getBlockAt(l), color);

    }

    private void colorBlocks(Block block, byte color) {
        List<Block> wool = new ArrayList<Block>();
        findWoolAround(block.getLocation().toVector(), block, wool, 3, 0);
        for (Block b : wool) {
            b.setData(color);
        }
}

    @Override
    public void receive(BitSet7 bits) {
        // if we have 0 or 1 inputs there's no clock to adjust. just use the incoming bits.        
        if (inputs.length<=1) {
            inputBits = bits.get(0, (inputs.length==0?5:inputs.length));
        }  else {
            for (int i=0; i<bits.length(); i++)
                inputBits.set(i+1, bits.get(i));
            inputBits.clear(0);
        }
        updatePixel();
    }

    @Override
    public String getChannel() {
        return broadcastChannel;
    }

    private void findWoolAround(Vector origin, Block b, List<Block> wool, int range, int curDist) {
        if (curDist>=range) {
            return;
        } else {
            curDist++;
            for (BlockFace face : faces) {
                Block f = b.getFace(face);
                if (f.getType()==Material.WOOL) {
                    if (!wool.contains(f) && origin.distanceSquared(f.getLocation().toVector())<4)
                        wool.add(f);
                    findWoolAround(origin, f, wool, range, curDist);
                }
            }
        }
    }
}
