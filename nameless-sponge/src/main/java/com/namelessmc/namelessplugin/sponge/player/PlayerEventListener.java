package com.namelessmc.namelessplugin.sponge.player;

import java.io.File;
import java.io.IOException;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.namelessmc.namelessplugin.sponge.NamelessPlugin;
import com.namelessmc.namelessplugin.sponge.utils.PermissionHandler;
import com.namelessmc.namelessplugin.sponge.utils.RequestUtil;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

/*
 *  Bungeecord Version by IsS127
 */

public class PlayerEventListener {

	NamelessPlugin plugin;

	/*
	 *  Constructer
	 */
	public PlayerEventListener(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
	}

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event){
		Player player = event.getTargetEntity();

	    if(!plugin.getAPIUrl().isEmpty()){
		    try {
				userFileCheck(player);
				userNameCheck(player);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    userGetNotification(player);
		    userGroupSync(player);
		
	   }
	}
	
	/*
	 * User Notifications.
	 */
	public void userGetNotification(Player player){
		RequestUtil request = new RequestUtil(plugin);
		if(plugin.getConfig().getNode("join-notifications").getBoolean()){
			
			try {
				request.getNotifications(player.getUniqueId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * User Group Synchronization.
	 */
	public void userGroupSync(Player player){
		RequestUtil request = new RequestUtil(plugin);
		PermissionHandler phandler = new PermissionHandler(plugin);
		if(plugin.getConfig().getNode("group-synchronization").getBoolean()){
			try {
				for(ConfigurationNode cfgGroupId : phandler.getConfig().getNode("permissions").getChildrenList()){
					phandler.getConfig().getNode("permissions" ,cfgGroupId);
					if(request.getGroup(player.getName()).equals(cfgGroupId)){
						return;
					} else if(player.hasPermission(cfgGroupId.getString())){
						String[] groupId = cfgGroupId.getPath().toString().split(".");
						request.setGroup(player.getName(), groupId[1]);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/*
	 * Check if the user exists in the Players Information File.
	 */
	
	public void userFileCheck(Player player) throws IOException{
		// Check if user does NOT contain information in the Players Information file. 
		// If so, add him.
		File iFile = new File(new File("config", "NamelessPlugin"), "playersInformation.yml");
		YAMLConfigurationLoader fileLoader = YAMLConfigurationLoader.builder().setPath(iFile.toPath()).build();
		ConfigurationNode yFile;
		yFile = fileLoader.load();
		if(yFile.getNode(player.getUniqueId().toString()).isVirtual()){
			plugin.getLogger().info(Text.of(TextColors.GREEN, player.getName(), TextColors.RED, " does not contain in the Player Information File..").toPlain());
			plugin.getLogger().info(Text.of(TextColors.DARK_GREEN, "Adding ", TextColors.GREEN, player.getName(), TextColors.DARK_GREEN, " to the Player Information File.").toPlain());
			yFile.getNode(player.getUniqueId().toString() + ".Username").setValue(player.getName());
			try {
				fileLoader.save(yFile);
				plugin.getLogger().info(Text.of(TextColors.DARK_GREEN, "Added ", TextColors.GREEN, player.getName(), TextColors.DARK_GREEN," to the Player Information File.").toPlain());
			} catch (IOException e) {
				plugin.getLogger().info(Text.of(TextColors.RED, "Could not add ", TextColors.GREEN, player.getName(), TextColors.RED, " to the Player Information File!").toPlain());
				e.printStackTrace();
			}
		}
	}
	
	/*
	 *  Update username on Login
	 */
	public void userNameCheck(Player player) throws IOException{
		File iFile = new File(new File("config", "NamelessPlugin"), "playersInformation.yml");
		YAMLConfigurationLoader fileLoader = YAMLConfigurationLoader.builder().setPath(iFile.toPath()).build();
		ConfigurationNode yFile;
		yFile = fileLoader.load();
		
		// Check if user has changed Username
		// If so, change the username in the Players Information File. (NOT COMPLETED)
		// And change the username on the website.
		if(plugin.getConfig().getNode("update-username").getBoolean()){
		if(!yFile.getString(player.getUniqueId() + ".Username").equals(player.getName())){
			plugin.getLogger().info(Text.of(TextColors.RED, "Detected that ",  TextColors.GREEN, player.getName(), TextColors.RED, " has changed his/her username!").toPlain());
			plugin.getLogger().info(Text.of(TextColors.DARK_GREEN, "Changing ", TextColors.GREEN, player.getName(), "s", TextColors.DARK_GREEN, "username.").toPlain());

			String previousUsername = yFile.getNode(player.getUniqueId() + ".Username").getString();
			String newUsername = player.getName();
			yFile.getNode(player.getUniqueId() + ".PreviousUsername").setValue(previousUsername);
			yFile.getNode(player.getUniqueId() + ".Username").setValue(newUsername);
			try {
				fileLoader.save(yFile);
				plugin.getLogger().info(Text.of(TextColors.DARK_GREEN, "Changed ", TextColors.GREEN, player.getName(), "s", TextColors.DARK_GREEN, "username in the Player Information File.").toPlain());
			} catch (IOException e) {
				plugin.getLogger().info(Text.of(TextColors.RED, "Could not change ", TextColors.GREEN, player.getName(), "s", TextColors.RED, "username in the Player Information File.").toPlain());
				e.printStackTrace();
			}

			// Changing username on Website here.
			RequestUtil request = new RequestUtil(plugin);
			try {
				if(!player.getName().equals(request.getUserName(player.getUniqueId().toString()))){
					request.updateUserName(player.getUniqueId().toString(), newUsername);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}

}
