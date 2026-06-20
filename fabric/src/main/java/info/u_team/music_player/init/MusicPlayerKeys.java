package info.u_team.music_player.init;

import static info.u_team.music_player.init.MusicPlayerLocalization.KEY_CATEGORY;
import static info.u_team.music_player.init.MusicPlayerLocalization.KEY_OPEN;
import static info.u_team.music_player.init.MusicPlayerLocalization.KEY_PAUSE;
import static info.u_team.music_player.init.MusicPlayerLocalization.KEY_SKIP_BACK;
import static info.u_team.music_player.init.MusicPlayerLocalization.KEY_SKIP_FORWARD;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class MusicPlayerKeys {
	
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(info.u_team.music_player.MusicPlayerReference.MODID, "category"));
	
	public static final KeyMapping OPEN = new KeyMapping(KEY_OPEN, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, CATEGORY);
	public static final KeyMapping PAUSE = new KeyMapping(KEY_PAUSE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_8, CATEGORY);
	public static final KeyMapping SKIP_FORWARD = new KeyMapping(KEY_SKIP_FORWARD, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_9, CATEGORY);
	public static final KeyMapping SKIP_BACK = new KeyMapping(KEY_SKIP_BACK, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_7, CATEGORY);
	
	public static void register() {
		KeyMappingHelper.registerKeyMapping(OPEN);
		KeyMappingHelper.registerKeyMapping(PAUSE);
		KeyMappingHelper.registerKeyMapping(SKIP_FORWARD);
		KeyMappingHelper.registerKeyMapping(SKIP_BACK);
	}
}
