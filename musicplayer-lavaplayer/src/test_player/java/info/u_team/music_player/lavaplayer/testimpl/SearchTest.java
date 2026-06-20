package info.u_team.music_player.lavaplayer.testimpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import info.u_team.music_player.lavaplayer.MusicPlayer;
import info.u_team.music_player.lavaplayer.api.search.ISearchResult;
import info.u_team.music_player.lavaplayer.api.search.ITrackSearch;

/**
 * Simple headless test to verify Lavaplayer track search works.
 * No audio output needed - just tests if we can resolve tracks.
 */
public class SearchTest {

	public static void main(String[] args) throws Exception {
		System.out.println("=== Lavaplayer Search Test ===");
		System.out.println();

		final MusicPlayer musicPlayer = new MusicPlayer();

		final ITrackSearch search = musicPlayer.getTrackSearch();

		// Test 1: YouTube search
		System.out.println("[Test 1] YouTube search: 'never gonna give you up'");
		testSearch(search, "ytsearch:never gonna give you up", "YouTube Search");

		// Test 2: Direct YouTube URL
		System.out.println("[Test 2] Direct YouTube URL: https://www.youtube.com/watch?v=dQw4w9WgXcQ");
		testSearch(search, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "YouTube Direct URL");

		// Test 3: SoundCloud search
		System.out.println("[Test 3] SoundCloud search: 'lofi hip hop'");
		testSearch(search, "scsearch:lofi hip hop", "SoundCloud Search");

		System.out.println();
		System.out.println("=== All tests completed ===");

		// Force exit since lavaplayer threads may keep JVM alive
		System.exit(0);
	}

	private static void testSearch(ITrackSearch search, String query, String testName) throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<ISearchResult> resultRef = new AtomicReference<>();

		search.getTracks(query, result -> {
			resultRef.set(result);
			latch.countDown();
		});

		boolean completed = latch.await(30, TimeUnit.SECONDS);

		if (!completed) {
			System.out.println("  [TIMEOUT] " + testName + " - No response within 30 seconds");
			System.out.println();
			return;
		}

		final ISearchResult result = resultRef.get();

		if (result == null) {
			System.out.println("  [FAIL] " + testName + " - Result is null");
			System.out.println();
			return;
		}

		if (result.hasError()) {
			System.out.println("  [FAIL] " + testName + " - Error: " + result.getErrorMessage());
			if (result.getStackTrace() != null) {
				for (StackTraceElement elem : result.getStackTrace()) {
					System.out.println("    at " + elem);
				}
			}
			System.out.println();
			return;
		}

		if (result.isList()) {
			final var tracks = result.getTrackList().getTracks();
			System.out.println("  [OK] " + testName + " - Found " + tracks.size() + " tracks (playlist/search)");
			for (int i = 0; i < Math.min(3, tracks.size()); i++) {
				final var info = tracks.get(i).getInfo();
				System.out.println("    " + (i + 1) + ". " + info.getAuthor() + " - " + info.getTitle()
						+ " [" + formatDuration(tracks.get(i).getDuration()) + "]");
			}
		} else {
			final var track = result.getTrack();
			final var info = track.getInfo();
			System.out.println("  [OK] " + testName + " - Found track: " + info.getAuthor() + " - " + info.getTitle()
					+ " [" + formatDuration(track.getDuration()) + "]");
		}
		System.out.println();
	}

	private static String formatDuration(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		seconds %= 60;
		return String.format("%d:%02d", minutes, seconds);
	}
}
