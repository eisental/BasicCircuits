package org.tal.basiccircuits;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
*
* @author needspeed10
*/
public class reseter extends Circuit
{
	int type;
	@Override
    public void inputChange(int index, boolean state) 
	{				
		if(state)
		{
			Block target = null;
			Block down = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.DOWN);
			Block up = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.UP);
			Block east = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.EAST);
			Block west = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.WEST);
			Block north = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.NORTH);
			Block south = world.getBlockAt(interfaceBlocks[0]).getFace(BlockFace.SOUTH);
			if (down.getType()==Material.WALL_SIGN)target = down;
			if (up.getType()==Material.WALL_SIGN)target = up;
			if (north.getType()==Material.WALL_SIGN)target = north;
			if (east.getType()==Material.WALL_SIGN)target = east;
			if (west.getType()==Material.WALL_SIGN)target = west;
			if (south.getType()==Material.WALL_SIGN)target = south;
			switch (type)
			{
				case 0:
					reset(redstoneChips.getCircuitManager().getCircuitByActivationBlock(target),target);
					break;
				case 1:
					activate(target);		
					break;
				case 2:
					deactivate(redstoneChips.getCircuitManager().getCircuitByActivationBlock(target));
					break;
			}
    	}
    }
    
    public void reset(Circuit cr,Block block) 
    {        
        deactivate(cr);
        activate(block);
    }
    public void deactivate(Circuit ci)
    {
    	try{redstoneChips.getCircuitManager().destroyCircuit(ci, null,false);}catch(Exception e){};
    }
    public void activate(Block bl)
    {
    	try
    	{
    		redstoneChips.getCircuitManager().checkForCircuit(bl,null,redstoneChips.getPrefs().getInputBlockType(),redstoneChips.getPrefs().getOutputBlockType(),redstoneChips.getPrefs().getInterfaceBlockType());
    	}
    	catch(Exception e){};
    }
    
    @Override
    protected boolean init(CommandSender sender, String[] args)
    {
    	if (this.interfaceBlocks.length<1) 
    	{
            error(sender, "Expecting at least 1 interface block.");
            return false;
        }           
    	else 
    	{
    		if(args[0].equalsIgnoreCase("reset"))type = 0;
    		if(args[0].equalsIgnoreCase("activate"))type = 1;
    		if(args[0].equalsIgnoreCase("deactivate"))type = 2;
    	}
        return true;
    }

}