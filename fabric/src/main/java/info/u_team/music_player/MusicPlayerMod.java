package info.u_team.music_player;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import info.u_team.music_player.config.ClientConfig;
import info.u_team.music_player.dependency.DependencyManager;
import info.u_team.music_player.init.MusicPlayerEvents;
import info.u_team.music_player.init.MusicPlayerKeys;
import info.u_team.music_player.musicplayer.MusicPlayerInitManager;
import net.fabricmc.api.ClientModInitializer;

/**
 * Main Fabric client entrypoint for the Music Player mod.
 * 
 * Initialization order:
 * 1. Set HTTP user-agent (required for some streaming sources)
 * 2. Load client configuration from disk
 * 3. Load Lavaplayer dependencies via custom classloader
 * 4. Initialize the music player engine
 * 5. Register keybinds (Fabric KeyBindingHelper)
 * 6. Register event handlers (Fabric API events)
 */
public class MusicPlayerMod implements ClientModInitializer {
	
	public static final String MODID = MusicPlayerReference.MODID;
	public static final Logger LOGGER = LogUtils.getLogger();
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Music Player Extended (Fabric)");
		
		// Set HTTP agent for streaming source compatibility
		System.setProperty("http.agent", "Chrome");
		
		// Load configuration
		ClientConfig.load();
		
		// Load Lavaplayer dependencies into custom classloader
		DependencyManager.load();
		
		// Initialize the music player engine
		MusicPlayerInitManager.register();
		
		// Register keybinds via Fabric API
		MusicPlayerKeys.register();
		
		// Register Fabric event handlers
		MusicPlayerEvents.register();
		
		LOGGER.info("Music Player Extended (Fabric) initialized successfully");
	}
}
