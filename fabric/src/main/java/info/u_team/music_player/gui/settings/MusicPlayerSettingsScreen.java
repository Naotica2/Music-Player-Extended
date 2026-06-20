package info.u_team.music_player.gui.settings;

import info.u_team.music_player.gui.MusicPlayerScreen;
import info.u_team.music_player.init.MusicPlayerResources;
import info.u_team.music_player.musicplayer.MusicPlayerManager;
import info.u_team.music_player.musicplayer.settings.IngameOverlayPosition;
import info.u_team.music_player.musicplayer.settings.Settings;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SliderComponent;
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

/**
 * Settings screen for Music Player Extended.
 * Provides toggles and sliders for all player settings,
 * built with owo-lib for responsive layout.
 */
public class MusicPlayerSettingsScreen extends Screen {
	
	private final Screen parentScreen;
	private OwoUIAdapter<FlowLayout> uiAdapter;
	
	public MusicPlayerSettingsScreen(Screen parentScreen) {
		super(Component.literal("Settings"));
		this.parentScreen = parentScreen;
	}
	
	@Override
	protected void init() {
		super.init();
		
		final Settings settings = MusicPlayerManager.getSettingsManager().getSettings();
		
		uiAdapter = OwoUIAdapter.create(this, UIContainers::verticalFlow);
		final FlowLayout root = uiAdapter.rootComponent;
		root.horizontalAlignment(HorizontalAlignment.CENTER);
		root.surface(Surface.VANILLA_TRANSLUCENT);
		root.padding(Insets.of(20));
		root.gap(6);
		
		// Header
		final LabelComponent header = UIComponents.label(Component.literal("Music Player Extended - Settings"));
		header.color(Color.ofRgb(0xFFFF00));
		root.child(header);
		
		// Scrollable settings area
		final ScrollContainer<FlowLayout> scroll = UIContainers.verticalScroll(
				Sizing.fill(100), Sizing.fill(80),
				UIContainers.verticalFlow(Sizing.fill(100), Sizing.content())
		);
		scroll.surface(Surface.flat(0xAA202020));
		scroll.padding(Insets.of(8));
		
		final FlowLayout settingsList = (FlowLayout) scroll.child();
		settingsList.gap(8);
		
		// Toggle: Ingame overlay
		settingsList.child(createToggleRow("Ingame Overlay", settings.isShowIngameOverlay(), enabled -> {
			settings.setShowIngameOverlay(enabled);
		}));
		
		// Toggle: Menu overlay
		settingsList.child(createToggleRow("Menu Overlay", settings.isShowIngameMenueOverlay(), enabled -> {
			settings.setShowIngameMenueOverlay(enabled);
		}));
		
		// Toggle: Keybinds in GUI
		settingsList.child(createToggleRow("Keybinds in GUI", settings.isKeyWorkInGui(), enabled -> {
			settings.setKeyWorkInGui(enabled);
		}));
		
		// Toggle: Overlay position
		final FlowLayout posRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		posRow.gap(8);
		posRow.verticalAlignment(VerticalAlignment.CENTER);
		
		posRow.child(UIComponents.label(Component.literal("Overlay Position:")));
		
		final ButtonComponent posBtn = UIComponents.button(
				Component.literal(getPositionName(settings.getIngameOverlayPosition())),
				button -> {
					final IngameOverlayPosition newPos = IngameOverlayPosition.forwardCycle(settings.getIngameOverlayPosition());
					settings.setIngameOverlayPosition(newPos);
					button.setMessage(Component.literal(getPositionName(newPos)));
				}
		);
		posBtn.sizing(Sizing.fixed(120), Sizing.fixed(20));
		posRow.child(posBtn);
		
		settingsList.child(posRow);
		
		// Speed slider
		final FlowLayout speedRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		speedRow.gap(8);
		speedRow.verticalAlignment(VerticalAlignment.CENTER);
		speedRow.child(UIComponents.label(Component.literal("Speed:")));
		
		final SliderComponent speedSlider = UIComponents.slider(Sizing.fill(60));
		speedSlider.value(MusicPlayerManager.getPlayer().getSpeed() / 10.0);
		speedSlider.message(val -> Component.literal(String.format("%.1fx", Double.parseDouble(val) * 10.0)));
		speedSlider.onChanged().subscribe(val -> {
			MusicPlayerManager.getPlayer().setSpeed((float)(val * 10.0));
		});
		speedRow.child(speedSlider);
		settingsList.child(speedRow);
		
		// Pitch slider
		final FlowLayout pitchRow = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		pitchRow.gap(8);
		pitchRow.verticalAlignment(VerticalAlignment.CENTER);
		pitchRow.child(UIComponents.label(Component.literal("Pitch:")));
		
		final SliderComponent pitchSlider = UIComponents.slider(Sizing.fill(60));
		pitchSlider.value(MusicPlayerManager.getPlayer().getPitch() / 10.0);
		pitchSlider.message(val -> Component.literal(String.format("%.1fx", Double.parseDouble(val) * 10.0)));
		pitchSlider.onChanged().subscribe(val -> {
			MusicPlayerManager.getPlayer().setPitch((float)(val * 10.0));
		});
		pitchRow.child(pitchSlider);
		settingsList.child(pitchRow);
		
		root.child(scroll);
		
		// Back button
		final ButtonComponent backBtn = UIComponents.button(Component.literal("Back"), button -> {
			minecraft.setScreen(parentScreen != null ? parentScreen : new MusicPlayerScreen());
		});
		backBtn.sizing(Sizing.fixed(100), Sizing.fixed(20));
		root.child(backBtn);
		
		uiAdapter.inflateAndMount();
	}
	
	private FlowLayout createToggleRow(String label, boolean initialValue, java.util.function.Consumer<Boolean> onChange) {
		final FlowLayout row = UIContainers.horizontalFlow(Sizing.fill(100), Sizing.fixed(24));
		row.gap(8);
		row.verticalAlignment(VerticalAlignment.CENTER);
		
		row.child(UIComponents.label(Component.literal(label)));
		
		final ButtonComponent toggle = UIComponents.button(
				Component.literal(initialValue ? "ON" : "OFF"),
				button -> {
					// Toggle is tricky - we track state via the label
					final boolean current = button.getMessage().getString().equals("ON");
					final boolean newVal = !current;
					button.setMessage(Component.literal(newVal ? "ON" : "OFF"));
					onChange.accept(newVal);
				}
		);
		toggle.sizing(Sizing.fixed(60), Sizing.fixed(20));
		row.child(toggle);
		
		return row;
	}
	
	private String getPositionName(IngameOverlayPosition pos) {
		return switch (pos) {
			case UP_LEFT -> "Top Left";
			case UP_RIGHT -> "Top Right";
			case DOWN_RIGHT -> "Bottom Right";
			case DOWN_LEFT -> "Bottom Left";
		};
	}
	

	@Override
	public boolean isPauseScreen() {
		return false;
	}
	

}
