package info.u_team.music_player.gui.search;

import info.u_team.music_player.gui.controls.PlaybackControls;
import info.u_team.music_player.lavaplayer.api.audio.IAudioTrack;
import info.u_team.music_player.lavaplayer.api.search.ISearchResult;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import info.u_team.music_player.musicplayer.playlist.Playlist;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Search/Add tracks screen for Music Player Extended.
 * Allows adding tracks via URL, file, folder, and YouTube search.
 * Built entirely with owo-lib.
 */
public class MusicPlayerSearchScreen extends Screen {
	
	private final Screen parentScreen;
	private final Playlist targetPlaylist;
	private OwoUIAdapter<FlowLayout> uiAdapter;
	private FlowLayout searchResultsLayout;
	private PlaybackControls controlsBar;
	
	public MusicPlayerSearchScreen(Screen parentScreen, Playlist targetPlaylist) {
		super(Component.literal("Add Tracks"));
		this.parentScreen = parentScreen;
		this.targetPlaylist = targetPlaylist;
	}
	
	@Override
	protected void init() {
		super.init();
		
		uiAdapter = OwoUIAdapter.create(this, UIContainers::verticalFlow);
		final FlowLayout root = uiAdapter.rootComponent;
		root.horizontalAlignment(HorizontalAlignment.CENTER);
		root.surface(Surface.VANILLA_TRANSLUCENT);
		root.padding(Insets.of(20));
		root.gap(6);
		
		// Header
		final LabelComponent header = UIComponents.label(Component.literal("Add New Tracks"));
		header.color(Color.ofRgb(0xFFFF00));
		root.child(header);
		
		// Controls bar
		controlsBar = new PlaybackControls(Sizing.fill(100));
		root.child(controlsBar);
		
		// URL input row
		final FlowLayout urlRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		urlRow.gap(4);
		urlRow.verticalAlignment(VerticalAlignment.CENTER);
		
		urlRow.child(UIComponents.label(Component.literal("Enter URL to track:")));
		
		final var urlInput = UIComponents.textBox(Sizing.fill(60));
		urlInput.setSuggestion("https://...");
		urlInput.onChanged().subscribe(val -> {
			urlInput.setSuggestion(val.isEmpty() ? "https://..." : "");
		});
		urlRow.child(urlInput);
		
		final ButtonComponent addUrlBtn = UIComponents.button(Component.literal("+"), button -> {
			final String url = urlInput.getValue();
			if (!url.isEmpty()) {
				addTrackFromUrl(url);
				urlInput.setValue("");
				urlInput.setSuggestion("https://...");
			}
		});
		addUrlBtn.sizing(Sizing.fixed(24), Sizing.fixed(20));
		urlRow.child(addUrlBtn);
		
		root.child(urlRow);
		
		// File/Folder buttons row
		final FlowLayout fileRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		fileRow.gap(8);
		
		final ButtonComponent loadFileBtn = UIComponents.button(Component.literal("Load File"), button -> {
			// Open file dialog - uses JFileChooser on the desktop
			openFileDialog(false);
		});
		loadFileBtn.sizing(Sizing.fixed(100), Sizing.fixed(20));
		fileRow.child(loadFileBtn);
		
		final ButtonComponent loadFolderBtn = UIComponents.button(Component.literal("Load Folder"), button -> {
			openFileDialog(true);
		});
		loadFolderBtn.sizing(Sizing.fixed(100), Sizing.fixed(20));
		fileRow.child(loadFolderBtn);
		
		root.child(fileRow);
		
		// Search row
		final FlowLayout searchRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		searchRow.gap(4);
		searchRow.verticalAlignment(VerticalAlignment.CENTER);
		
		searchRow.child(UIComponents.label(Component.literal("Search:")));
		
		final var searchInput = UIComponents.textBox(Sizing.fill(60));
		searchInput.setSuggestion("Search YouTube...");
		searchInput.onChanged().subscribe(val -> {
			searchInput.setSuggestion(val.isEmpty() ? "Search YouTube..." : "");
		});
		searchRow.child(searchInput);
		
		final ButtonComponent searchBtn = UIComponents.button(Component.literal("Search"), button -> {
			final String query = searchInput.getValue();
			if (!query.isEmpty()) {
				searchTracks("ytsearch:" + query);
				searchInput.setValue("");
				searchInput.setSuggestion("Search YouTube...");
			}
		});
		searchBtn.sizing(Sizing.fixed(60), Sizing.fixed(20));
		searchRow.child(searchBtn);
		
		root.child(searchRow);
		
		// Search results area
		final ScrollContainer<FlowLayout> scrollArea = UIContainers.verticalScroll(
				Sizing.fill(100), Sizing.fill(50),
				UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
		);
		scrollArea.surface(Surface.flat(0xAA202020));
		scrollArea.padding(Insets.of(2));
		
		searchResultsLayout = (FlowLayout) scrollArea.child();
		
		root.child(scrollArea);
		
		// Bottom nav
		final FlowLayout bottomBar = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
		bottomBar.horizontalAlignment(HorizontalAlignment.CENTER);
		
		final ButtonComponent backBtn = UIComponents.button(Component.literal("Back"), button -> {
			minecraft.setScreen(parentScreen);
		});
		backBtn.sizing(Sizing.fixed(100), Sizing.fixed(20));
		bottomBar.child(backBtn);
		
		root.child(bottomBar);
		
		uiAdapter.inflateAndMount();
	}
	
	private void addTrackFromUrl(String url) {
		if (targetPlaylist == null || !targetPlaylist.isLoaded()) return;
		
		MusicPlayerManager.getPlayer().getTrackSearch().getTracks(url, result -> {
			if (!result.hasError()) {
				if (!result.isList()) {
					targetPlaylist.add(result.getTrack());
				} else {
					targetPlaylist.add(result.getTrackList());
				}
			}
		});
	}
	
	private void searchTracks(String query) {
		searchResultsLayout.clearChildren();
		
		final LabelComponent loadingLabel = UIComponents.label(Component.literal("Searching..."));
		loadingLabel.color(Color.ofRgb(0xFFFF00));
		searchResultsLayout.child(loadingLabel);
		
		MusicPlayerManager.getPlayer().getTrackSearch().getTracks(query, result -> {
			minecraft.execute(() -> {
				searchResultsLayout.clearChildren();
				
				if (result.hasError()) {
					searchResultsLayout.child(UIComponents.label(Component.literal("Error: " + result.getErrorMessage())));
					return;
				}
				
				if (result.isList()) {
					final List<IAudioTrack> tracks = result.getTrackList().getTracks();
					for (IAudioTrack track : tracks) {
						addSearchResultEntry(track);
					}
				} else {
					addSearchResultEntry(result.getTrack());
				}
			});
		});
	}
	
	private void addSearchResultEntry(IAudioTrack track) {
		final FlowLayout entry = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		entry.gap(4);
		entry.verticalAlignment(VerticalAlignment.CENTER);
		entry.padding(Insets.horizontal(4));
		entry.surface(Surface.flat(0xCC404040));
		
		final String title = track.getInfo().getFixedTitle();
		final LabelComponent titleLabel = UIComponents.label(
				Component.literal(title.length() > 50 ? title.substring(0, 50) + "..." : title)
		);
		titleLabel.color(Color.WHITE);
		entry.child(titleLabel);
		
		final String duration = info.u_team.music_player.gui.util.GuiTrackUtils.getFormattedDuration(track);
		final LabelComponent durationLabel = UIComponents.label(Component.literal(duration));
		durationLabel.color(Color.ofRgb(0xFFFF00));
		entry.child(durationLabel);
		
		// Group buttons into a right-aligned flow
		final FlowLayout buttons = UIContainers.horizontalFlow(Sizing.content(), Sizing.fill(100));
		buttons.gap(4);
		buttons.verticalAlignment(VerticalAlignment.CENTER);
		buttons.positioning(io.wispforest.owo.ui.core.Positioning.relative(100, 50));
		buttons.padding(Insets.right(4));
		
		final ButtonComponent addBtn = UIComponents.button(Component.literal("+"), button -> {
			if (targetPlaylist != null && targetPlaylist.isLoaded()) {
				targetPlaylist.add(track);
				button.active = false;
				button.setMessage(Component.literal("Added"));
			}
		});
		addBtn.sizing(Sizing.fixed(32), Sizing.fixed(20));
		buttons.child(addBtn);
		
		entry.child(buttons);
		
		searchResultsLayout.child(entry);
	}
	
	private void openFileDialog(boolean folder) {
		// File dialog implementation using AWT/Swing (runs on separate thread)
		new Thread(() -> {
			final javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
			chooser.setFileSelectionMode(folder
					? javax.swing.JFileChooser.DIRECTORIES_ONLY
					: javax.swing.JFileChooser.FILES_ONLY);
			chooser.setDialogTitle(folder ? "Select Folder" : "Select Music File");
			
			if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				final String path = file.getAbsolutePath();
				minecraft.execute(() -> addTrackFromUrl(path));
			}
		}).start();
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
