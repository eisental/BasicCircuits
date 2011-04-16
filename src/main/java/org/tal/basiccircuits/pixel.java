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
import org.tal.redstonechips.channels.ReceivingCircuit;
import org.tal.redstonechips.util.BitSet7;
import org.tal.redstonechips.util.BitSetUtils;

/**
 * // dyes wool if present on output block
 * @author Tal Eisenberg
 */
public class pixel extends ReceivingCircuit {
    private boolean indexedColor = false;
    private byte[] colorIndex;
    private int distance = 3;
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
            String channelString = null;

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
                        if (i==args.length-1) channelString = args[i];
                        else if ((args[i].startsWith("d{") || args[i].startsWith("dist{")) && args[i].endsWith("}")) {
                            try {
                                distance = Integer.decode(args[i].substring(args[i].indexOf("{")+1, args[i].length()-1));
                            } catch (NumberFormatException ne2) {
                                error(sender, "Bad distance argument: " + args[i] + ". Expecting d{<distance>} or dist{<distance>}.");
                                return false;
                            }
                        } else {
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

            if (channelString!=null) {
                parseChannelString(channelString);
                info(sender, "Pixel will listen on broadcast channel " + getChannel().name + ".");
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
        if (!isCircuitChunkLoaded()) return;

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
        findWoolAround(block.getLocation().toVector(), block, block, wool, distance, 0);
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

    private void findWoolAround(Vector origin, Block originBlock, Block b, List<Block> wool, int range, int curDist) {
        if (curDist>=range) {
            return;
        } else {
            curDist++;
            for (BlockFace face : faces) {
                Block f = b.getFace(face);
                if (f.getType()==Material.WOOL) {
                    if (!wool.contains(f) && origin.distanceSquared(f.getLocation().toVector())<4 && f!=originBlock)
                        wool.add(f);
                    findWoolAround(origin, originBlock, f, wool, range, curDist);
                }
            }
        }
    }

    @Override
    public void circuitShutdown() {
        if (getChannel() != null) redstoneChips.removeReceiver(this);
    }

    @Override
    public int getLength() {
        if (!indexedColor)
            return 4;
        else {
            return (int)Math.ceil(Math.log(colorIndex.length)/Math.log(2));
        }
    }
}
