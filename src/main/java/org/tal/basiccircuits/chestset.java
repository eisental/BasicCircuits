package org.tal.basiccircuits;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.tal.redstonechips.circuit.Circuit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;

/**
*
* @author needspeed10
*/
public class chestset extends Circuit {
	Block blockchest;
	BlockState bschest;
 	Chest chest;
 	Inventory invchest;
 	ItemStack[] itemstack;
 	CommandSender playa;
	@Override
    public void inputChange(int index, boolean state) 
	{		
		Block down = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.DOWN);
    	Block up = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.UP);
    	Block east = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.EAST);
    	Block west = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.WEST);
    	Block north = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.NORTH);
    	Block south = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.SOUTH);
    	if (down.getType()==Material.CHEST)blockchest = down;
    	if (up.getType()==Material.CHEST)blockchest = up;
    	if (north.getType()==Material.CHEST)blockchest = north;
    	if (east.getType()==Material.CHEST)blockchest = east;
    	if (west.getType()==Material.CHEST)blockchest = west;
    	if (south.getType()==Material.CHEST)blockchest = south;
    	bschest = blockchest.getState();
    	chest = (Chest)bschest;
    	invchest = chest.getInventory();
		if(index == 0 && state)
		{
			int val = 0;
	        for (int i=0; i<inputs.length-1; i++) {
	            if (inputBits.get(i+1)) val += Math.pow(2,i);}
	        if(Material.getMaterial(val).getId() > 0)
	        {
	        	invchest.addItem(new ItemStack(val,1));	
	        }		
		}
		
    }

    @Override
    protected boolean init(CommandSender sender, String[] strings) 
    {
    	playa = sender;
    	if (this.interfaceBlocks.length!=1) 
    	{
            error(sender, "Expecting just 1 interface block.");
            return false;
        }                
        return true;
    }
}
