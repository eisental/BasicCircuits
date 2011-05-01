package org.tal.basiccircuits;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.tal.redstonechips.circuit.Circuit;

/**
*
* @author needspeed10
*/
public class commandex extends Circuit implements CommandSender{

	 String command = "";
	 String commander = "";
	 CommandSender sendi;
	 @Override
	 public void inputChange(int index, boolean state) 
	 {
		 if(state && index==0)
	     {
			 if(getServer().getPlayer(commander)!=null&&commander!="")sendi = getServer().getPlayer(commander);
			 else sendi = this;
			 getServer().dispatchCommand(sendi, command);
	     }
	 }
	    
	 @Override
	 protected boolean init(CommandSender sender, String[] strings)
	 {
		 for(int i=1;i<strings.length;i++) command += strings[i]+" ";
		 if(strings[0].equals("noplayer"))commander = "noplayer";
		 else if(strings.length >= 1&&getServer().getPlayer(strings[0])!=null)commander = getServer().getPlayer(strings[0]).getName();
		 if(sender!=null)info(sender,"Sender: " + commander + " Command: /" + command);
		 return true;
	 }

	 @Override
	 public Server getServer() {
	  return redstoneChips.getServer();
	 }
	 @Override
	 public boolean isOp() {
	  return true;
	 }
	 @Override
	 public void sendMessage(String arg0) {
	  return;
	 }
}