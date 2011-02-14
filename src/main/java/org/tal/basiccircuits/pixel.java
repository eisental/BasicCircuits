package org.tal.basiccircuits;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
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

    @Override
    public void inputChange(int inIdx, boolean on) {
        if (inputs.length==1) {
            updatePixel();
        } else if (inIdx==0 && on) { // clock pin
            updatePixel();
        }
    }

    @Override
    protected boolean init(Player player, String[] args) {
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
                        // not dye number also, treat as brodcast channel if last;
                        if (i==args.length-1) broadcastChannel = args[i];
                        else {
                            error(player, "Unknown color name: " + args[i]);
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
                info(player, "Pixel will listen on broadcast channel " + broadcastChannel + ".");
                redstoneChips.addReceiver(this);
            }
        }



        if (inputs.length>5) {
            error(player, "Too many inputs. Requires 1 clock pin and no more than 4 data pins.");
            return false;
        }

        if (this.interfaceBlocks.length==0) {
            error(player, "Expecting at least 1 interface block.");
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

        for (BlockVector v : interfaceBlocks)
            colorBlocks(world.getBlockAt(v.getBlockX(),v.getBlockY(),v.getBlockZ()), color);

    }

    private void colorBlocks(Block block, byte color) {
        Block down = block.getFace(BlockFace.DOWN);
        Block east = block.getFace(BlockFace.EAST);
        Block north = block.getFace(BlockFace.NORTH);
        Block northEast = block.getFace(BlockFace.NORTH_EAST);
        Block northWest = block.getFace(BlockFace.NORTH_WEST);
        Block south = block.getFace(BlockFace.SOUTH);
        Block southEast = block.getFace(BlockFace.SOUTH_EAST);
        Block southWest = block.getFace(BlockFace.SOUTH_WEST);
        Block up = block.getFace(BlockFace.UP);
        Block west = block.getFace(BlockFace.WEST);

        Block upeast = up.getFace(BlockFace.EAST);
        Block upnorth = up.getFace(BlockFace.NORTH);
        Block upnorthEast = up.getFace(BlockFace.NORTH_EAST);
        Block upnorthWest = up.getFace(BlockFace.NORTH_WEST);
        Block upsouth = up.getFace(BlockFace.SOUTH);
        Block upsouthEast = up.getFace(BlockFace.SOUTH_EAST);
        Block upsouthWest = up.getFace(BlockFace.SOUTH_WEST);
        Block upwest = up.getFace(BlockFace.WEST);

        Block downeast = down.getFace(BlockFace.EAST);
        Block downnorth = down.getFace(BlockFace.NORTH);
        Block downnorthEast = down.getFace(BlockFace.NORTH_EAST);
        Block downnorthWest = down.getFace(BlockFace.NORTH_WEST);
        Block downsouth = down.getFace(BlockFace.SOUTH);
        Block downsouthEast = down.getFace(BlockFace.SOUTH_EAST);
        Block downsouthWest = down.getFace(BlockFace.SOUTH_WEST);
        Block downwest = down.getFace(BlockFace.WEST);

        if (down.getType()==Material.WOOL)
            down.setData(color);
        if (east.getType()==Material.WOOL)
            east.setData(color);
        if (north.getType()==Material.WOOL)
            north.setData(color);
        if (northEast.getType()==Material.WOOL)
            northEast.setData(color);
        if (northWest.getType()==Material.WOOL)
            northWest.setData(color);
        if (south.getType()==Material.WOOL)
            south.setData(color);
        if (southEast.getType()==Material.WOOL)
            southEast.setData(color);
        if (west.getType()==Material.WOOL)
            west.setData(color);
        if (up.getType()==Material.WOOL)
            up.setData(color);
        if (southWest.getType()==Material.WOOL)
            southWest.setData(color);

        if (upeast.getType()==Material.WOOL)
            upeast.setData(color);
        if (upnorth.getType()==Material.WOOL)
            upnorth.setData(color);
        if (upnorthEast.getType()==Material.WOOL)
            upnorthEast.setData(color);
        if (upnorthWest.getType()==Material.WOOL)
            upnorthWest.setData(color);
        if (upsouth.getType()==Material.WOOL)
            upsouth.setData(color);
        if (upsouthEast.getType()==Material.WOOL)
            upsouthEast.setData(color);
        if (upwest.getType()==Material.WOOL)
            upwest.setData(color);
        if (upsouthWest.getType()==Material.WOOL)
            upsouthWest.setData(color);

        if (downeast.getType()==Material.WOOL)
            downeast.setData(color);
        if (downnorth.getType()==Material.WOOL)
            downnorth.setData(color);
        if (downnorthEast.getType()==Material.WOOL)
            downnorthEast.setData(color);
        if (downnorthWest.getType()==Material.WOOL)
            downnorthWest.setData(color);
        if (downsouth.getType()==Material.WOOL)
            downsouth.setData(color);
        if (downsouthEast.getType()==Material.WOOL)
            downsouthEast.setData(color);
        if (downwest.getType()==Material.WOOL)
            downwest.setData(color);
        if (downsouthWest.getType()==Material.WOOL)
            downsouthWest.setData(color);
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
}
