package info.u_team.music_player.init;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import info.u_team.music_player.gui.MusicPlayerScreen;
import info.u_team.music_player.lavaplayer.api.queue.ITrackManager;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import info.u_team.music_player.musicplayer.MusicPlayerUtils;
import info.u_team.music_player.musicplayer.SettingsManager;
import info.u_team.music_player.musicplayer.settings.IngameOverlayPosition;
import info.u_team.music_player.render.RenderOverlayMusicDisplay;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.PauseScreen;

/**
 * Handles all client-side events for the Music Player mod.
 * Rewritten from the original to use only Fabric API events,
 * replacing uteamcore's ClientEvents.
 */
public class MusicPlayerEvents {
	
	private static final SettingsManager settingsManager = MusicPlayerManager.getSettingsManager();
	
	private static RenderOverlayMusicDisplay overlayRender;
	
	/**
	 * Called from KeyboardHandlerMixin after every key press.
	 * Handles global keybinds when no GUI is open.
	 */
	public static void onKeyInput() {
		handleKeyboard(false, -1, -1);
	}
	
	/**
	 * Handles keybinds. Returns true if the key was consumed.
	 */
	private static boolean handleKeyboard(boolean gui, int keyCode, int scanCode) {
		final ITrackManager manager = MusicPlayerManager.getPlayer().getTrackManager();
		
		if (isKeyDown(MusicPlayerKeys.OPEN, gui, keyCode, scanCode)) {
			final Minecraft mc = Minecraft.getInstance();
			if (!(mc.screen instanceof MusicPlayerScreen)) {
				mc.setScreen(new MusicPlayerScreen());
			}
			return true;
		} else if (isKeyDown(MusicPlayerKeys.PAUSE, gui, keyCode, scanCode)) {
			if (manager.getCurrentTrack() != null) {
				manager.setPaused(!manager.isPaused());
			}
			return true;
		} else if (isKeyDown(MusicPlayerKeys.SKIP_FORWARD, gui, keyCode, scanCode)) {
			if (manager.getCurrentTrack() != null) {
				MusicPlayerUtils.skipForward();
			}
			return true;
		} else if (isKeyDown(MusicPlayerKeys.SKIP_BACK, gui, keyCode, scanCode)) {
			if (manager.getCurrentTrack() != null) {
				MusicPlayerUtils.skipBack();
			}
			return true;
		}
		return false;
	}
	
	private static boolean isKeyDown(KeyMapping binding, boolean gui, int keyCode, int scanCode) {
		if (gui) {
			final Key key = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
			return key != InputConstants.UNKNOWN && key.equals(KeyMappingHelper.getBoundKeyOf(binding));
		} else {
			return binding.consumeClick();
		}
	}
	
	/**
	 * Called from GuiMixin to render the in-game HUD overlay.
	 */
	public static void onRenderGameOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker partialTick) {
		final Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null) {
			if (settingsManager.getSettings().isShowIngameOverlay()) {
				final IngameOverlayPosition position = settingsManager.getSettings().getIngameOverlayPosition();
				
				if (overlayRender == null) {
					overlayRender = new RenderOverlayMusicDisplay();
				}
				
				final Window window = mc.getWindow();
				final int screenWidth = window.getGuiScaledWidth();
				final int screenHeight = window.getGuiScaledHeight();
				
				final int height = overlayRender.getHeight();
				final int width = overlayRender.getWidth();
				
				final int x;
				if (position.isLeft()) {
					x = 3;
				} else {
					x = screenWidth - 3 - width;
				}
				
				final int y;
				if (position.isUp()) {
					y = 3;
				} else {
					y = screenHeight - 3 - height;
				}
				
				final org.joml.Matrix3x2fStack poseStack = guiGraphics.pose();
				
				poseStack.pushMatrix();
				poseStack.translate(x, y);
				overlayRender.extractRenderState(guiGraphics, 0, 0, partialTick.getGameTimeDeltaPartialTick(false));
				poseStack.popMatrix();
			}
		}
	}
	
	/**
	 * Registers all Fabric event listeners.
	 * Replaces the old uteamcore ClientEvents with pure Fabric API.
	 */
	public static void register() {
		// Register keyboard input event
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			onKeyInput();
		});

		// Register pause screen overlay (controls bar at top of pause menu)
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof PauseScreen) {
				if (settingsManager.getSettings().isShowIngameMenueOverlay()) {
					io.wispforest.owo.ui.core.OwoUIAdapter<io.wispforest.owo.ui.container.FlowLayout> adapter = 
							io.wispforest.owo.ui.core.OwoUIAdapter.create(screen, io.wispforest.owo.ui.container.UIContainers::verticalFlow);
					adapter.rootComponent.horizontalAlignment(io.wispforest.owo.ui.core.HorizontalAlignment.CENTER);
					adapter.rootComponent.verticalAlignment(io.wispforest.owo.ui.core.VerticalAlignment.TOP);
					
					info.u_team.music_player.gui.controls.PlaybackControls controls = new info.u_team.music_player.gui.controls.PlaybackControls(io.wispforest.owo.ui.core.Sizing.fill(100));
					adapter.rootComponent.child(controls);
					adapter.inflateAndMount();
					
					// Register per-screen tick
					ScreenEvents.afterTick(screen).register(s -> {
						controls.tick();
					});
				}
			}
		});
	}
	
}
