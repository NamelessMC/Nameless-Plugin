package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.NamelessException;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class WebsendConsoleCapture {

	private final List<String> LOG_LINES = new ArrayList<>();

	private @Nullable LoggingOutputStream replacedOutStream = null;
	private @Nullable LoggingOutputStream replacedErrStream = null;

	void start() {
		replacedOutStream = new LoggingOutputStream(System.out);
		System.setOut(new PrintStream(replacedOutStream));

		replacedErrStream = new LoggingOutputStream(System.err);
		System.setErr(new PrintStream(replacedErrStream));
	}

	void stop() {
		// When this plugin is disabled, our output stream may still be stored by something.
		// As far as I know there's no way to prevent this. As a hacky workaround, remove all
		// functionality from our custom output stream and make it pass through only.
		if (replacedOutStream != null) {
			replacedOutStream.disable();
		}
		if (replacedErrStream != null) {
			replacedErrStream.disable();
		}
	}

	void sendLogLines() {
		final List<String> linesToSend;
		synchronized (LOG_LINES) {
			// Copy to second array to break console for as little time as possible
			linesToSend = new ArrayList<>(LOG_LINES);
			LOG_LINES.clear();
		}

		NamelessPlugin.getInstance().getNamelessApi().ifPresent(api -> {
			int serverId = NamelessPlugin.getInstance().getConfig().getInt("server-id");
			if (serverId <= 0) {
				NamelessPlugin.getInstance().getLogger().warning("server-id is not configured");
				return;
			}
			try {
				api.websend().sendConsoleLog(serverId, linesToSend);
			} catch (NamelessException e) {
				e.printStackTrace();
			}
		});
	}

	private class LoggingOutputStream extends OutputStream {

		private static final int DEFAULT_LINE_BUFFER_SIZE = 4096;

		private final @NotNull PrintStream originalStream;

		private byte[] lineBuffer = new byte[DEFAULT_LINE_BUFFER_SIZE];
		private int lineBufferPos = 0; // position for next element

		private boolean disabled = false;

		public LoggingOutputStream(@NotNull PrintStream originalStream) {
			super();
			this.originalStream = originalStream;
		}

		@Override
		public void write(int i) {
			originalStream.write(i);

			if (disabled) {
				return;
			}

			synchronized (LOG_LINES) {
				if (i == '\n') {
					byte[] bytes = new byte[lineBufferPos];
					System.arraycopy(lineBuffer, 0, bytes, 0, lineBufferPos);
					LOG_LINES.add(new String(bytes));
					this.lineBufferPos = 0;
					if (lineBuffer.length > DEFAULT_LINE_BUFFER_SIZE) {
						lineBuffer = new byte[DEFAULT_LINE_BUFFER_SIZE];
					}
				} else {
					if (lineBufferPos >= lineBuffer.length) {
						byte[] newBuffer = new byte[lineBuffer.length * 2];
						System.arraycopy(lineBuffer, 0, newBuffer, 0, lineBuffer.length);
						this.lineBuffer = newBuffer;
					}

					this.lineBuffer[lineBufferPos++] = (byte) i;
				}
			}
		}

		public void disable() {
			synchronized (LOG_LINES) {
				Validate.isTrue(!disabled);
				this.disabled = true;
				this.lineBuffer = null;
			}
		}
	}

}
