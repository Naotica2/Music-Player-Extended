package info.u_team.music_player.gui;

import info.u_team.music_player.gui.controls.PlaybackControls;
import info.u_team.music_player.init.MusicPlayerResources;
import info.u_team.music_player.lavaplayer.api.queue.ITrackManager;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import info.u_team.music_player.musicplayer.playlist.Playlist;
import info.u_team.music_player.musicplayer.playlist.Playlists;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Main Music Player Extended screen - shows the list of playlists
 * with controls to add, remove, play, and manage them.
 * Built entirely with owo-lib for responsive layout.
 */
public class MusicPlayerScreen extends Screen {
	
	private OwoUIAdapter<FlowLayout> uiAdapter;
	private FlowLayout rootLayout;
	private PlaybackControls controlsBar;
	private FlowLayout playlistListLayout;
	
	private final Screen parentScreen;
	
	public MusicPlayerScreen() {
		this(null);
	}
	
	public MusicPlayerScreen(Screen parentScreen) {
		super(Component.translatable("gui.musicplayer.title"));
		this.parentScreen = parentScreen;
	}
	
	@Override
	protected void init() {
		super.init();
		
		// Create root adapter
		uiAdapter = OwoUIAdapter.create(this, UIContainers::verticalFlow);
		rootLayout = uiAdapter.rootComponent;
		rootLayout.horizontalAlignment(HorizontalAlignment.CENTER);
		rootLayout.surface(Surface.VANILLA_TRANSLUCENT);
		rootLayout.padding(Insets.of(20));
		
		// Header: Title
		final LabelComponent header = UIComponents.label(Component.literal("Music Player Extended"));
		header.color(Color.ofRgb(0xFFFF00));
		rootLayout.child(header);
		rootLayout.child(UIComponents.box(Sizing.fill(100), Sizing.fixed(2)));
		
		// Controls bar at top
		controlsBar = new PlaybackControls(Sizing.fill(100));
		rootLayout.child(controlsBar);
		
		// Separator
		rootLayout.child(UIComponents.box(Sizing.fill(100), Sizing.fixed(2)));
		
		// Add playlist input row
		final FlowLayout addRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		addRow.gap(4);
		addRow.verticalAlignment(VerticalAlignment.CENTER);
		addRow.padding(Insets.of(2));
		
		final var nameInput = UIComponents.textBox(Sizing.fill(60));
		nameInput.setSuggestion("Add playlist...");
		nameInput.onChanged().subscribe(val -> {
			nameInput.setSuggestion(val.isEmpty() ? "Add playlist..." : "");
		});
		addRow.child(nameInput);
		
		final ButtonComponent addButton = UIComponents.button(Component.literal("+"), button -> {
			final String name = nameInput.getValue();
			if (!name.isEmpty()) {
				MusicPlayerManager.getPlaylistManager().getPlaylists().add(new Playlist(name));
				nameInput.setValue("");
				nameInput.setSuggestion("Add playlist...");
				rebuildPlaylistList();
			}
		});
		addButton.sizing(Sizing.fixed(24), Sizing.fixed(20));
		addRow.child(addButton);
		
		rootLayout.child(addRow);
		
		// Scrollable playlist area
		final ScrollContainer<FlowLayout> scrollArea = UIContainers.verticalScroll(
				Sizing.fill(100), Sizing.fill(60),
				UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
		);
		scrollArea.surface(Surface.flat(0xAA202020));
		scrollArea.padding(Insets.of(2));
		
		playlistListLayout = (FlowLayout) scrollArea.child();
		
		rootLayout.child(scrollArea);
		
		// Bottom nav bar
		final FlowLayout bottomBar = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
		bottomBar.horizontalAlignment(HorizontalAlignment.CENTER);
		bottomBar.gap(8);
		
		final ButtonComponent closeBtn = UIComponents.button(Component.literal("X"), button -> {
			this.onClose();
		});
		closeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
		bottomBar.child(closeBtn);
		
		rootLayout.child(bottomBar);
		
		// Build the playlist entries
		rebuildPlaylistList();
		
		uiAdapter.inflateAndMount();
	}
	
	private void rebuildPlaylistList() {
		if (playlistListLayout == null) return;
		
		playlistListLayout.clearChildren();
		
		final Playlists playlists = MusicPlayerManager.getPlaylistManager().getPlaylists();
		final ITrackManager trackManager = MusicPlayerManager.getPlayer().getTrackManager();
		
		for (int i = 0; i < playlists.size(); i++) {
			final Playlist playlist = playlists.get(i);
			final int index = i;
			
			final FlowLayout entry = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(28));
			entry.gap(4);
			entry.verticalAlignment(VerticalAlignment.CENTER);
			entry.padding(Insets.horizontal(4).withTop(2).withBottom(2));
			entry.surface(Surface.flat(0xCC404040));
			
			// Playlist name
			final LabelComponent nameLabel = UIComponents.label(
					Component.literal(playlist.getName() != null && !playlist.getName().isEmpty()
							? playlist.getName() : "No name")
			);
			nameLabel.color(Color.WHITE);
			entry.child(nameLabel);
			
			// Entry count
			final LabelComponent countLabel = UIComponents.label(
					Component.literal("(" + playlist.getEntrySize() + " " +
							(playlist.getEntrySize() == 1 ? "Entry" : "Entries") + ")")
			);
			countLabel.color(Color.ofRgb(0xAAAAAA));
			entry.child(countLabel);
			
			// Group buttons into a right-aligned flow
			final FlowLayout buttons = UIContainers.horizontalFlow(Sizing.content(), Sizing.fill(100));
			buttons.gap(4);
			buttons.verticalAlignment(VerticalAlignment.CENTER);
			buttons.positioning(io.wispforest.owo.ui.core.Positioning.relative(100, 50));
			buttons.padding(Insets.right(4));
			
			// Play button
			final ButtonComponent playBtn = UIComponents.button(Component.literal(">"), button -> {
				playlist.load(() -> {
					if (playlist.getFirstTrack().getRight() != null) {
						playlists.setPlaying(playlist);
						playlists.setPlayingLock();
						playlist.setPlayable(playlist.getFirstTrack().getLeft(), playlist.getFirstTrack().getRight());
						trackManager.setTrackQueue(playlist);
						trackManager.start();
					}
				});
			});
			playBtn.sizing(Sizing.fixed(24), Sizing.fixed(20));
			buttons.child(playBtn);
			
			// Open playlist button
			final ButtonComponent openBtn = UIComponents.button(Component.literal("..."), button -> {
				minecraft.setScreen(new MusicPlayerPlaylistScreen(playlist, this));
			});
			openBtn.sizing(Sizing.fixed(28), Sizing.fixed(20));
			buttons.child(openBtn);
			
			// Remove button
			final ButtonComponent removeBtn = UIComponents.button(Component.literal("X"), button -> {
				playlists.remove(playlist);
				rebuildPlaylistList();
			});
			removeBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
			buttons.child(removeBtn);
			
			entry.child(buttons);
			
			playlistListLayout.child(entry);
		}
	}
	

	@Override
	public void onClose() {
		this.minecraft.setScreen(parentScreen);
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
