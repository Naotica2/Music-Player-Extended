package info.u_team.music_player.gui;

import info.u_team.music_player.gui.controls.PlaybackControls;
import info.u_team.music_player.gui.search.MusicPlayerSearchScreen;
import info.u_team.music_player.gui.util.GuiTrackUtils;
import info.u_team.music_player.lavaplayer.api.audio.IAudioTrack;
import info.u_team.music_player.lavaplayer.api.queue.ITrackManager;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import info.u_team.music_player.musicplayer.playlist.LoadedTracks;
import info.u_team.music_player.musicplayer.playlist.Playlist;
import info.u_team.music_player.musicplayer.playlist.Playlists;
import info.u_team.music_player.util.WrappedObject;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Collection;

/**
 * Playlist detail view for Music Player Extended.
 * Shows all tracks within a single playlist with play, move, and remove controls.
 * Built entirely with owo-lib.
 */
public class MusicPlayerPlaylistScreen extends Screen {
	
	private final Playlist playlist;
	private final Screen parentScreen;
	private OwoUIAdapter<FlowLayout> uiAdapter;
	private FlowLayout trackListLayout;
	private PlaybackControls controlsBar;
	
	public MusicPlayerPlaylistScreen(Playlist playlist, Screen parentScreen) {
		super(Component.literal(playlist.getName()));
		this.playlist = playlist;
		this.parentScreen = parentScreen;
	}
	
	@Override
	protected void init() {
		super.init();
		
		uiAdapter = OwoUIAdapter.create(this, UIContainers::verticalFlow);
		final FlowLayout root = uiAdapter.rootComponent;
		root.horizontalAlignment(HorizontalAlignment.CENTER);
		root.surface(Surface.VANILLA_TRANSLUCENT);
		root.padding(Insets.of(20));
		root.gap(4);
		
		// Header
		final LabelComponent header = UIComponents.label(
				Component.literal("Playlist: " + (playlist.getName() != null ? playlist.getName() : "Unnamed"))
		);
		header.color(Color.ofRgb(0xFFFF00));
		root.child(header);
		
		// Controls bar
		controlsBar = new PlaybackControls(Sizing.fill(100));
		root.child(controlsBar);
		
		// Action buttons row
		final FlowLayout actionRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		actionRow.gap(8);
		
		final ButtonComponent addTracksBtn = UIComponents.button(Component.literal("Add Tracks"), button -> {
			minecraft.setScreen(new MusicPlayerSearchScreen(this, playlist));
		});
		addTracksBtn.sizing(Sizing.fixed(100), Sizing.fixed(20));
		actionRow.child(addTracksBtn);
		
		final ButtonComponent playAllBtn = UIComponents.button(Component.literal("Play All"), button -> {
			playPlaylist();
		});
		playAllBtn.sizing(Sizing.fixed(80), Sizing.fixed(20));
		actionRow.child(playAllBtn);
		
		root.child(actionRow);
		
		// Track list scroll area
		final ScrollContainer<FlowLayout> scrollArea = UIContainers.verticalScroll(
				Sizing.fill(100), Sizing.fill(65),
				UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
		);
		scrollArea.surface(Surface.flat(0xAA202020));
		scrollArea.padding(Insets.of(2));
		
		trackListLayout = (FlowLayout) scrollArea.child();
		
		root.child(scrollArea);
		
		// Bottom nav
		final FlowLayout bottomBar = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
		bottomBar.horizontalAlignment(HorizontalAlignment.CENTER);
		bottomBar.gap(8);
		
		final ButtonComponent backBtn = UIComponents.button(Component.literal("Back"), button -> {
			minecraft.setScreen(parentScreen);
		});
		backBtn.sizing(Sizing.fixed(100), Sizing.fixed(20));
		bottomBar.child(backBtn);
		
		root.child(bottomBar);
		
		// Load tracks if not loaded
		if (!playlist.isLoaded()) {
			final LabelComponent loadingLabel = UIComponents.label(Component.literal("Loading tracks..."));
			loadingLabel.color(Color.ofRgb(0xFFFF00));
			trackListLayout.child(loadingLabel);
			
			playlist.load(() -> minecraft.execute(this::rebuildTrackList));
		} else {
			rebuildTrackList();
		}
		
		uiAdapter.inflateAndMount();
	}
	
	private void rebuildTrackList() {
		if (trackListLayout == null) return;
		trackListLayout.clearChildren();
		
		final Collection<LoadedTracks> loadedTracks = playlist.getLoadedTracks();
		int index = 0;
		
		for (LoadedTracks loaded : loadedTracks) {
			final int trackIndex = index++;
			
			final FlowLayout entry = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
			entry.gap(4);
			entry.verticalAlignment(VerticalAlignment.CENTER);
			entry.padding(Insets.horizontal(4).withTop(2).withBottom(2));
			entry.surface(Surface.flat(0xCC404040));
			
			if (loaded.hasError()) {
				final LabelComponent errorLabel = UIComponents.label(Component.literal("Error: " + loaded.getErrorMessage()));
				errorLabel.color(Color.ofRgb(0xFF5555));
				entry.child(errorLabel);
			} else {
				// Track title
				final String title = loaded.getTitle() != null ? loaded.getTitle() : "Unknown";
				final LabelComponent titleLabel = UIComponents.label(
						Component.literal(title.length() > 40 ? title.substring(0, 40) + "..." : title)
				);
				titleLabel.color(Color.WHITE);
				entry.child(titleLabel);
				
				// Group buttons into a right-aligned flow
				final FlowLayout buttons = UIContainers.horizontalFlow(Sizing.content(), Sizing.fill(100));
				buttons.gap(4);
				buttons.verticalAlignment(VerticalAlignment.CENTER);
				buttons.positioning(io.wispforest.owo.ui.core.Positioning.relative(100, 50));
				buttons.padding(Insets.right(4));
				
				// Play button
				final ButtonComponent playBtn = UIComponents.button(Component.literal(">"), button -> {
					playTrack(loaded);
				});
				playBtn.sizing(Sizing.fixed(24), Sizing.fixed(20));
				buttons.child(playBtn);
				
				// Move up
				final ButtonComponent upBtn = UIComponents.button(Component.literal("^"), button -> {
					playlist.move(loaded.getUri(), 1);
					rebuildTrackList();
				});
				upBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
				buttons.child(upBtn);
				
				// Move down
				final ButtonComponent downBtn = UIComponents.button(Component.literal("v"), button -> {
					playlist.move(loaded.getUri(), -1);
					rebuildTrackList();
				});
				downBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
				buttons.child(downBtn);
				
				// Remove
				final ButtonComponent removeBtn = UIComponents.button(Component.literal("X"), button -> {
					playlist.remove(loaded.getUri());
					rebuildTrackList();
				});
				removeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
				buttons.child(removeBtn);
				
				entry.child(buttons);
			}
			
			trackListLayout.child(entry);
		}
		
		if (loadedTracks.isEmpty()) {
			trackListLayout.child(UIComponents.label(Component.literal("No tracks in this playlist")));
		}
	}
	
	private void playPlaylist() {
		if (!playlist.isLoaded()) return;
		
		final Playlists playlists = MusicPlayerManager.getPlaylistManager().getPlaylists();
		final ITrackManager trackManager = MusicPlayerManager.getPlayer().getTrackManager();
		
		if (playlist.getFirstTrack().getRight() != null) {
			playlists.setPlaying(playlist);
			playlists.setPlayingLock();
			playlist.setPlayable(playlist.getFirstTrack().getLeft(), playlist.getFirstTrack().getRight());
			trackManager.setTrackQueue(playlist);
			trackManager.start();
		}
	}
	
	private void playTrack(LoadedTracks loaded) {
		final Playlists playlists = MusicPlayerManager.getPlaylistManager().getPlaylists();
		final ITrackManager trackManager = MusicPlayerManager.getPlayer().getTrackManager();
		
		final IAudioTrack track = loaded.getFirstTrack();
		if (track != null) {
			playlists.setPlaying(playlist);
			playlists.setPlayingLock();
			playlist.setPlayable(loaded, track);
			trackManager.setTrackQueue(playlist);
			trackManager.start();
		}
	}
	

	@Override
	public void tick() {
		super.tick();
		if (controlsBar != null) {
			controlsBar.tick();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
	

}
