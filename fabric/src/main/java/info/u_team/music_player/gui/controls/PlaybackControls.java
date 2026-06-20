package info.u_team.music_player.gui.controls;

import info.u_team.music_player.init.MusicPlayerResources;
import info.u_team.music_player.lavaplayer.api.queue.ITrackManager;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import info.u_team.music_player.musicplayer.MusicPlayerUtils;
import info.u_team.music_player.musicplayer.settings.Repeat;
import info.u_team.music_player.musicplayer.settings.Settings;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Reusable playback controls bar widget built with owo-lib.
 * Displays: shuffle, skip-back, play/pause, skip-forward, repeat buttons,
 * plus volume slider, track info labels, and settings button.
 */
public class PlaybackControls extends FlowLayout {
	
	private final ITrackManager trackManager;
	private final Settings settings;
	
	private FlowLayout buttonLayout;
	private boolean shuffleActive;
	private Repeat currentRepeat;
	private net.minecraft.resources.Identifier currentPlayTexture;
	private SliderComponent volumeSlider;
	private LabelComponent titleLabel;
	private LabelComponent authorLabel;
	private LabelComponent progressLabel;
	
	public PlaybackControls(Sizing horizontalSizing) {
		super(horizontalSizing, Sizing.fixed(50), Algorithm.HORIZONTAL);
		
		this.trackManager = MusicPlayerManager.getPlayer().getTrackManager();
		this.settings = MusicPlayerManager.getSettingsManager().getSettings();
		this.shuffleActive = settings.isShuffle();
		this.currentRepeat = settings.getRepeat();
		this.currentPlayTexture = getPlayPauseTexture();
		
		horizontalAlignment(HorizontalAlignment.CENTER);
		verticalAlignment(VerticalAlignment.CENTER);
		surface(Surface.flat(0xDD333333));
		padding(Insets.of(4));
		
		buildControls();
	}
	
	private void buildControls() {
		final int iconSize = 16;
		
		// Left side: Track info
		final FlowLayout infoLayout = UIContainers.verticalFlow(Sizing.fill(20), Sizing.content());
		infoLayout.gap(2);
		infoLayout.padding(Insets.left(4));
		
		titleLabel = UIComponents.label(Component.literal(getCurrentTitle()));
		titleLabel.color(Color.ofRgb(0xFFFF00));
		infoLayout.child(titleLabel);
		
		authorLabel = UIComponents.label(Component.literal(getCurrentAuthor()));
		authorLabel.color(Color.ofRgb(0xD86D1C));
		infoLayout.child(authorLabel);
		
		this.child(infoLayout);
		
		// Center: Playback buttons
		buttonLayout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
		buttonLayout.gap(4);
		buttonLayout.verticalAlignment(VerticalAlignment.CENTER);
		
		buildButtons();
		
		this.child(buttonLayout);
		
		// Right side: Volume + Settings
		final FlowLayout rightLayout = UIContainers.horizontalFlow(Sizing.fill(35), Sizing.content());
		rightLayout.gap(4);
		rightLayout.horizontalAlignment(HorizontalAlignment.RIGHT);
		rightLayout.verticalAlignment(VerticalAlignment.CENTER);
		rightLayout.padding(Insets.right(4));
		
		// Volume slider
		volumeSlider = UIComponents.slider(Sizing.fill(50));
		volumeSlider.value(settings.getVolume() / 100.0);
		volumeSlider.message(value -> Component.literal("Volume: " + (int)(Double.parseDouble(value) * 100) + "%"));
		volumeSlider.onChanged().subscribe(value -> {
			final int vol = (int)(value * 100);
			settings.setVolume(vol);
			MusicPlayerManager.getPlayer().setVolume(vol);
		});
		rightLayout.child(volumeSlider);
		
		// Home (Playlists) button
		final ButtonComponent homeBtn = UIComponents.button(Component.literal("Home"), button -> {
			if (!(Minecraft.getInstance().screen instanceof info.u_team.music_player.gui.MusicPlayerScreen)) {
				Minecraft.getInstance().setScreen(new info.u_team.music_player.gui.MusicPlayerScreen());
			}
		});
		homeBtn.sizing(Sizing.fixed(40), Sizing.fixed(20));
		rightLayout.child(homeBtn);
		
		// Settings button
		final TextureComponent settingsBtn = UIComponents.texture(MusicPlayerResources.TEXTURE_SETTINGS, 0, 0, iconSize, iconSize, iconSize, iconSize);
		settingsBtn.sizing(Sizing.fixed(iconSize));
		settingsBtn.tooltip(Component.literal("Settings"));
		settingsBtn.mouseDown().subscribe((event, fromKeyboard) -> {
			playClickSound();
			Minecraft.getInstance().setScreen(new info.u_team.music_player.gui.settings.MusicPlayerSettingsScreen(
					Minecraft.getInstance().screen
			));
			return true;
		});
		rightLayout.child(settingsBtn);
		
		this.child(rightLayout);
	}
	
	/**
	 * Called every tick to refresh the display state.
	 */
	public void tick() {
		if (trackManager.getCurrentTrack() == null) {
			titleLabel.text(Component.literal(""));
			authorLabel.text(Component.literal(""));
		} else {
			titleLabel.text(Component.literal(getCurrentTitle()));
			authorLabel.text(Component.literal(getCurrentAuthor()));
		}
		
		net.minecraft.resources.Identifier newPlay = getPlayPauseTexture();
		Repeat newRepeat = settings.getRepeat();
		boolean newShuffle = settings.isShuffle();
		
		if (!newPlay.equals(currentPlayTexture) || newRepeat != currentRepeat || newShuffle != shuffleActive) {
			currentPlayTexture = newPlay;
			currentRepeat = newRepeat;
			shuffleActive = newShuffle;
			buttonLayout.clearChildren();
			buildButtons();
		}
	}
	
	private String getCurrentTitle() {
		if (trackManager.getCurrentTrack() == null) return "";
		return info.u_team.music_player.gui.util.GuiTrackUtils.trimToWith(
				trackManager.getCurrentTrack().getInfo().getFixedTitle(), 200);
	}
	
	private String getCurrentAuthor() {
		if (trackManager.getCurrentTrack() == null) return "";
		return info.u_team.music_player.gui.util.GuiTrackUtils.trimToWith(
				trackManager.getCurrentTrack().getInfo().getFixedAuthor(), 200);
	}
	
	private void buildButtons() {
		final int iconSize = 16;
		
		// Shuffle
		final TextureComponent shuffleButton = UIComponents.texture(MusicPlayerResources.TEXTURE_SHUFFLE, 0, 0, iconSize, iconSize, iconSize, iconSize);
		shuffleButton.sizing(Sizing.fixed(iconSize));
		shuffleButton.tooltip(shuffleActive ? Component.literal("Shuffle: ON") : Component.literal("Shuffle: OFF"));
		shuffleButton.mouseDown().subscribe((event, fromKeyboard) -> {
			playClickSound();
			settings.setShuffle(!shuffleActive);
			return true;
		});
		buttonLayout.child(shuffleButton);
		
		// Skip Back
		final TextureComponent skipBack = UIComponents.texture(MusicPlayerResources.TEXTURE_SKIP_BACK, 0, 0, iconSize, iconSize, iconSize, iconSize);
		skipBack.sizing(Sizing.fixed(iconSize));
		skipBack.tooltip(Component.literal("Skip Back"));
		skipBack.mouseDown().subscribe((event, fromKeyboard) -> {
			playClickSound();
			if (trackManager.getCurrentTrack() != null) {
				MusicPlayerUtils.skipBack();
			}
			return true;
		});
		buttonLayout.child(skipBack);
		
		// Play/Pause
		final TextureComponent playPauseButton = UIComponents.texture(currentPlayTexture, 0, 0, iconSize, iconSize, iconSize, iconSize);
		playPauseButton.sizing(Sizing.fixed(iconSize));
		playPauseButton.tooltip(Component.literal("Play / Pause"));
		playPauseButton.mouseDown().subscribe((event, fromKeyboard) -> {
			playClickSound();
			if (trackManager.getCurrentTrack() != null) {
				trackManager.setPaused(!trackManager.isPaused());
			}
			return true;
		});
		buttonLayout.child(playPauseButton);
		
		// Skip Forward
		final TextureComponent skipForward = UIComponents.texture(MusicPlayerResources.TEXTURE_SKIP_FORWARD, 0, 0, iconSize, iconSize, iconSize, iconSize);
		skipForward.sizing(Sizing.fixed(iconSize));
		skipForward.tooltip(Component.literal("Skip Forward"));
		skipForward.mouseDown().subscribe((event, fromKeyboard) -> {
			playClickSound();
			if (trackManager.getCurrentTrack() != null) {
				MusicPlayerUtils.skipForward();
			}
			return true;
		});
		buttonLayout.child(skipForward);
		
		// Repeat
		final TextureComponent repeatButton = UIComponents.texture(currentRepeat.getResource(), 0, 0, iconSize, iconSize, iconSize, iconSize);
		repeatButton.sizing(Sizing.fixed(iconSize));
		repeatButton.tooltip(Component.literal("Repeat: " + currentRepeat.name()));
		repeatButton.mouseDown().subscribe((event, fromKeyboard) -> {
			playClickSound();
			settings.setRepeat(Repeat.forwardCycle(currentRepeat));
			return true;
		});
		buttonLayout.child(repeatButton);
	}

	private void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(
			net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F)
		);
	}
	
	private net.minecraft.resources.Identifier getPlayPauseTexture() {
		if (trackManager.getCurrentTrack() == null) {
			return MusicPlayerResources.TEXTURE_STOP;
		}
		return trackManager.isPaused() ? MusicPlayerResources.TEXTURE_PLAY : MusicPlayerResources.TEXTURE_PAUSE;
	}
}
