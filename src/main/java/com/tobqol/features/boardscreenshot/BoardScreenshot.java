/*
 * Copyright (c) 2025, contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tobqol.features.boardscreenshot;

import com.tobqol.TheatreQOLConfig;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpritePixels;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
public class BoardScreenshot
{
	private static final int BOARD_WIDGET_GROUP = 459;
	private static final int BOARD_WIDGET_CHILD = 1;

	// Negative IDs are used for custom sprite overrides — chosen to not conflict with TOA (-420/-421).
	private static final int CAMERA_SPRITE_IDX = -422;
	private static final int CAMERA_HOVER_SPRITE_IDX = -423;

	private static final BufferedImage CAMERA_IMG =
		ImageUtil.loadImageResource(BoardScreenshot.class, "camera.png");
	private static final BufferedImage WHITE_CAMERA_IMG =
		ImageUtil.recolorImage(CAMERA_IMG, Color.WHITE);

	@Inject
	private Client client;

	@Inject
	private TheatreQOLConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ImageCapture imageCapture;

	@Inject
	private DrawManager drawManager;

	@Inject
	private EventBus eventBus;

	private Widget button = null;
	private boolean autoScreenshotTaken = false;

	public void startUp()
	{
		eventBus.register(this);
		clientThread.invokeLater(this::createButton);
	}

	public void shutDown()
	{
		eventBus.unregister(this);
		clientThread.invokeLater(() ->
		{
			removeCameraIconOverride();
			button = null;
		});
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != BOARD_WIDGET_GROUP)
		{
			return;
		}

		clientThread.invokeLater(this::createButton);

		if (config.boardScreenshotAuto() && !autoScreenshotTaken)
		{
			autoScreenshotTaken = true;
			clientThread.invokeLater(() -> screenshot(false));
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Reset so the next raid gets an auto-screenshot too.
		if (event.getVarbitId() == Varbits.THEATRE_OF_BLOOD && event.getValue() == 0)
		{
			autoScreenshotTaken = false;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!TheatreQOLConfig.GROUP_NAME.equals(event.getGroup()))
		{
			return;
		}

		switch (event.getKey())
		{
			case "boardScreenshotEnable":
			case "boardScreenshotWhiteIcon":
				clientThread.invokeLater(() ->
				{
					removeCameraIconOverride();
					button = null;
					createButton();
				});
				break;
		}
	}

	private void createButton()
	{
		if (!config.boardScreenshotEnable())
		{
			return;
		}

		Widget parent = client.getWidget(BOARD_WIDGET_GROUP, BOARD_WIDGET_CHILD);
		if (parent == null)
		{
			return;
		}


		// Don't add the button twice if the widget script fires more than once.
		Widget[] existing = parent.getDynamicChildren();
		if (existing != null)
		{
			for (Widget child : existing)
			{
				if (child.equals(button))
				{
					return;
				}
			}
		}

		boolean hasCameraSprite = addCameraIconOverride();

		button = parent.createChild(-1, hasCameraSprite ? WidgetType.GRAPHIC : WidgetType.TEXT);
		// Sized to match the close button (child 11, 26x23), placed to its left.
		int buttonWidth = hasCameraSprite ? 26 : 30;
		button.setOriginalHeight(23);
		button.setOriginalWidth(buttonWidth);
		button.setOriginalX(Math.max(0, parent.getWidth() - 21 - 16 - buttonWidth));
		button.setOriginalY(6);

		if (hasCameraSprite)
		{
			button.setSpriteId(CAMERA_SPRITE_IDX);
		}
		else
		{
			button.setText("[ss]");
			button.setTextColor(0xFFFFFF);
			button.setFontId(496); // RuneScape small
		}

		button.setHasListener(true);
		button.setAction(0, "Screenshot Board");
		button.setAction(1, "Copy to clipboard");
		button.setOnOpListener((JavaScriptCallback) e ->
		{
			// op=1 is left-click, op=2 is the right-click "Copy to clipboard" entry.
			if (e.getOp() == 1)
			{
				clientThread.invokeLater(() -> screenshot(false));
			}
			else if (e.getOp() == 2)
			{
				clientThread.invokeLater(() -> screenshot(true));
			}
		});

		if (hasCameraSprite)
		{
			button.setOnMouseOverListener((JavaScriptCallback) e -> button.setSpriteId(CAMERA_HOVER_SPRITE_IDX));
			button.setOnMouseLeaveListener((JavaScriptCallback) e -> button.setSpriteId(CAMERA_SPRITE_IDX));
		}

		button.revalidate();
	}

	private boolean addCameraIconOverride()
	{
		client.getWidgetSpriteCache().reset();

		if (client.getIndexSprites() == null)
		{
			return false;
		}

		SpritePixels[] emptyBox;
		SpritePixels[] selectedBox;
		try
		{
			emptyBox = client.getSprites(client.getIndexSprites(), SpriteID.OptionsBoxes.EMPTY, 0);
			selectedBox = client.getSprites(client.getIndexSprites(), SpriteID.OptionsBoxes.SELECTED, 0);
		}
		catch (Exception e)
		{
			log.warn("Failed to load options box sprites for board screenshot button", e);
			return false;
		}

		if (emptyBox == null || emptyBox[0] == null || selectedBox == null || selectedBox[0] == null)
		{
			return false;
		}

		BufferedImage camera = config.boardScreenshotWhiteIcon() ? WHITE_CAMERA_IMG : CAMERA_IMG;

		client.getSpriteOverrides().put(CAMERA_SPRITE_IDX,
			ImageUtil.getImageSpritePixels(overlapImages(camera, emptyBox[0].toBufferedImage()), client));
		client.getSpriteOverrides().put(CAMERA_HOVER_SPRITE_IDX,
			ImageUtil.getImageSpritePixels(overlapImages(camera, selectedBox[0].toBufferedImage()), client));

		return true;
	}

	private static BufferedImage overlapImages(BufferedImage foreground, BufferedImage background)
	{
		final int padding = 4;
		int maxW = background.getWidth() - padding * 2;
		int maxH = background.getHeight() - padding * 2;

		int fgW = foreground.getWidth();
		int fgH = foreground.getHeight();

		if (fgW > maxW || fgH > maxH)
		{
			double scale = Math.min((double) maxW / fgW, (double) maxH / fgH);
			fgW = (int) (fgW * scale);
			fgH = (int) (fgH * scale);
		}

		int x = background.getWidth() / 2 - fgW / 2;
		int y = background.getHeight() / 2 - fgH / 2;

		BufferedImage combined = new BufferedImage(background.getWidth(), background.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = combined.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(background, 0, 0, null);
		g.drawImage(foreground, x, y, fgW, fgH, null);
		g.dispose();
		return combined;
	}

	private void removeCameraIconOverride()
	{
		client.getWidgetSpriteCache().reset();
		client.getSpriteOverrides().remove(CAMERA_SPRITE_IDX);
		client.getSpriteOverrides().remove(CAMERA_HOVER_SPRITE_IDX);
	}

	// -------------------------------------------------------------------------
	// Screenshot capture
	// -------------------------------------------------------------------------

	private void screenshot(boolean clipboardOnly)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		Widget widget = client.getWidget(BOARD_WIDGET_GROUP, BOARD_WIDGET_CHILD);
		if (widget == null || widget.isHidden())
		{
			return;
		}

		// Prefer the parent so the header row is included in the capture.
		Widget captureWidget = widget;
		Widget parent = widget.getParent();
		if (parent != null && !parent.isHidden() && parent.getWidth() > 0 && parent.getHeight() > 0)
		{
			captureWidget = parent;
		}

		final Widget capture = captureWidget;

		// Hide the button so it doesn't end up in the screenshot.
		if (button != null)
		{
			button.setHidden(true);
			button.revalidate();
		}

		// Wait for the next rendered frame so we get everything the game drew.
		drawManager.requestNextFrameListener(image ->
		{
			clientThread.invokeLater(() ->
			{
				if (button != null)
				{
					button.setHidden(false);
					button.revalidate();
				}
			});
			// getCanvasLocation() is unreliable for some containers, so walk the tree manually.
			int wx = 0;
			int wy = 0;
			for (Widget node = capture; node != null; node = node.getParent())
			{
				wx += node.getRelativeX();
				wy += node.getRelativeY();
			}
			int ww = capture.getWidth();
			int wh = capture.getHeight();

			if (ww <= 0 || wh <= 0)
			{
				return;
			}

			int frameW = image.getWidth(null);
			int frameH = image.getHeight(null);

			// HiDPI: frame is in physical pixels, widget coords are logical.
			double scaleX = (double) frameW / client.getCanvasWidth();
			double scaleY = (double) frameH / client.getCanvasHeight();

			int x = (int) Math.round(wx * scaleX);
			int y = (int) Math.round(wy * scaleY);
			int w = (int) Math.round(ww * scaleX);
			int h = (int) Math.round(wh * scaleY);

			x = Math.max(0, x);
			y = Math.max(0, y);
			w = Math.min(w, frameW - x);
			h = Math.min(h, frameH - y);

			if (w <= 0 || h <= 0)
			{
				return;
			}

			BufferedImage frame = new BufferedImage(frameW, frameH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = frame.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();

			BufferedImage cropped = frame.getSubimage(x, y, w, h);

			if (clipboardOnly)
			{
				copyImageToClipboard(cropped);
			}
			else
			{
				imageCapture.saveScreenshot(cropped, "tob-board", "tob-board", true, false);
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "TOB board screenshot saved.", null);
			}
		});
	}

	// -------------------------------------------------------------------------
	// Clipboard
	// -------------------------------------------------------------------------

	private void copyImageToClipboard(BufferedImage image)
	{
		if (System.getProperty("os.name", "").toLowerCase().contains("mac"))
		{
			copyImageToClipboardMacOS(image);
		}
		else
		{
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(new TransferableImage(image), null);
		}
	}

	private void copyImageToClipboardMacOS(BufferedImage image)
	{
		try
		{
			File tmp = File.createTempFile("tob_board_", ".png");
			tmp.deleteOnExit();
			ImageIO.write(image, "png", tmp);

			String[] cmd = {
				"osascript", "-e",
				"set the clipboard to (read (POSIX file \""
					+ tmp.getAbsolutePath()
					+ "\") as «class PNGf»)"
			};
			new ProcessBuilder(cmd).start().waitFor();
			tmp.delete();
		}
		catch (Exception e)
		{
			log.warn("Failed to copy board screenshot to clipboard via osascript, falling back to AWT", e);
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			cb.setContents(new TransferableImage(image), null);
		}
	}

	private static class TransferableImage implements Transferable
	{
		private static final DataFlavor PNG_FLAVOR;

		static
		{
			try
			{
				PNG_FLAVOR = new DataFlavor("image/png");
			}
			catch (ClassNotFoundException e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}

		private final BufferedImage image;

		TransferableImage(BufferedImage image)
		{
			this.image = image;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
		{
			if (flavor.equals(PNG_FLAVOR) && image != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				return new ByteArrayInputStream(baos.toByteArray());
			}
			if (flavor.equals(DataFlavor.imageFlavor) && image != null)
			{
				return image;
			}
			throw new UnsupportedFlavorException(flavor);
		}

		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[]{PNG_FLAVOR, DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return flavor.equals(PNG_FLAVOR) || flavor.equals(DataFlavor.imageFlavor);
		}
	}
}
