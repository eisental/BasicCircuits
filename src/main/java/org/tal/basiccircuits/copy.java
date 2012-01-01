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
public class copy extends Circuit {
	
	Block blockchest1;
 	BlockState bschest1;
 	Chest chest1;
 	Inventory invchest1;
 	Block[] blocks;
 	BlockState[] states;
 	Chest[] chests;
 	Inventory[] inventories;
 	ItemStack[] itemstack1;
    CommandSender playa;
 	@Override
    public void inputChange(int index, boolean state) {    	
 		
 		if(index == 1 && state)
 		{
 			for(int i = 1; i < interfaceBlocks.length; i++)
 	    	{
 	    	    inventories[i].clear();
 	    	    chests[i].update();
 	    	}
 		}
 		if(index == 0 && state)
 		{
 			Block down = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.DOWN);
 	    	Block up = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.UP);
 	    	Block east = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.EAST);
 	    	Block west = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.WEST);
 	    	Block north = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.NORTH);
 	    	Block south = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.SOUTH);
 	    	if (down.getType()==Material.CHEST)blockchest1 = down;
 	    	if (up.getType()==Material.CHEST)blockchest1 = up;
 	    	if (north.getType()==Material.CHEST)blockchest1 = north;
 	    	if (east.getType()==Material.CHEST)blockchest1 = east;
 	    	if (west.getType()==Material.CHEST)blockchest1 = west;
 	    	if (south.getType()==Material.CHEST)blockchest1 = south;
 	    	bschest1 = blockchest1.getState();
 	    	chest1= (Chest)bschest1;
 	    	invchest1 = chest1.getInventory();
 	    	blocks = new Block[interfaceBlocks.length];
 	    	states = new BlockState[blocks.length];
 	    	chests = new Chest[blocks.length];
 	    	inventories = new Inventory[blocks.length];
 	    	itemstack1 = invchest1.getContents();
 	    	for(int i = 0; i < interfaceBlocks.length; i++)
 	    	{
 	    		Block down2 = world.getBlockAt(interfaceBlocks[i]).getFace(BlockFace.DOWN);
 	        	Block up2 = world.getBlockAt(interfaceBlocks[i]).getFace(BlockFace.UP);
 	        	Block east2 = world.getBlockAt(interfaceBlocks[i]).getFace(BlockFace.EAST);
 	        	Block west2 = world.getBlockAt(interfaceBlocks[i]).getFace(BlockFace.WEST);
 	        	Block north2 = world.getBlockAt(interfaceBlocks[i]).getFace(BlockFace.NORTH);
 	        	Block south2 = world.getBlockAt(interfaceBlocks[i]).getFace(BlockFace.SOUTH);
 	        	if (down2.getType()==Material.CHEST)blocks[i] = down2;
 	        	if (up2.getType()==Material.CHEST)blocks[i] = up2;
 	        	if (north2.getType()==Material.CHEST)blocks[i] = north2;
 	        	if (east2.getType()==Material.CHEST)blocks[i] = east2;
 	        	if (west2.getType()==Material.CHEST)blocks[i] = west2;
 	        	if (south2.getType()==Material.CHEST)blocks[i] = south2;
 	    		states[i] = blocks[i].getState();
 	    		try{chests[i] = (Chest)states[i];}
 	    		catch(Exception e) {info(playa,"" + blocks[i]);}
 	    		inventories[i] = chests[i].getInventory();
 	    		inventories[i].setContents(itemstack1);
 				chests[i].update(); 	    	    
 	    	}
    	}
    }

    @Override
    protected boolean init(CommandSender sender, String[] strings) 
    {
        if (this.interfaceBlocks.length<2) {
            error(sender, "Expecting at least 2 interface block.");
            return false;
        }        
        playa = sender;
        return true;
        
    }

}
