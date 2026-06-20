package info.u_team.music_player.render;

import com.mojang.blaze3d.vertex.PoseStack;

import info.u_team.music_player.gui.util.GuiTrackUtils;
import info.u_team.music_player.init.MusicPlayerColors;
import info.u_team.music_player.lavaplayer.api.audio.IAudioTrack;
import info.u_team.music_player.lavaplayer.api.queue.ITrackManager;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;

/**
 * Renders a small overlay displaying the currently playing track info.
 * Rewritten from the original to use vanilla Font rendering instead of
 * uteamcore's ScrollingText and ScalableText.
 */
public class RenderOverlayMusicDisplay implements Renderable {
	
	private final ITrackManager manager;
	
	private final int width;
	private final int height;
	
	public RenderOverlayMusicDisplay() {
		manager = MusicPlayerManager.getPlayer().getTrackManager();
		height = 35;
		width = 120;
	}
	
	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		final IAudioTrack track = manager.getCurrentTrack();
		if (track == null) {
			return;
		}
		
		final Font font = Minecraft.getInstance().font;
		
		// Background
		guiGraphics.fill(0, 0, width, height, 0xFF212121);
		
		// Title (truncated to fit width)
		final String title = GuiTrackUtils.trimToWith(track.getInfo().getFixedTitle(), width - 6);
		guiGraphics.text(font, title, 3, 2, MusicPlayerColors.YELLOW, false);
		
		// Author (truncated, rendered slightly smaller via pose scaling)
		final String author = GuiTrackUtils.trimToWith(track.getInfo().getFixedAuthor(), (int) ((width - 6) / 0.75F));
		final org.joml.Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		poseStack.translate(3, 12);
		poseStack.scale(0.75F, 0.75F);
		guiGraphics.text(font, author, 0, 0, MusicPlayerColors.YELLOW, false);
		poseStack.popMatrix();
		
		// Progress bar background
		guiGraphics.fill(6, 23, width - 6, 26, 0xFF555555);
		
		// Progress bar fill
		final double progress;
		if (track.getInfo().isStream()) {
			progress = 0.5;
		} else {
			progress = (double) track.getPosition() / track.getDuration();
		}
		guiGraphics.fill(6, 23, 6 + (int) ((width - 12) * progress), 26, 0xFF3E9100);
		
		// Position text (small, left-aligned)
		final String position = GuiTrackUtils.getFormattedPosition(track);
		poseStack.pushMatrix();
		poseStack.translate(6, 28);
		poseStack.scale(0.5F, 0.5F);
		guiGraphics.text(font, position, 0, 0, MusicPlayerColors.YELLOW, false);
		poseStack.popMatrix();
		
		// Duration text (small, right-aligned)
		final String duration = GuiTrackUtils.getFormattedDuration(track);
		final int durationWidth = font.width(duration);
		poseStack.pushMatrix();
		poseStack.translate(width - 6 - (durationWidth * 0.5F), 28);
		poseStack.scale(0.5F, 0.5F);
		guiGraphics.text(font, duration, 0, 0, MusicPlayerColors.YELLOW, false);
		poseStack.popMatrix();
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
}
