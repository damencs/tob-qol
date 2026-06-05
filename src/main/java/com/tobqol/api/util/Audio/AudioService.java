/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.tobqol.api.util.Audio;

import com.tobqol.TheatreQOLConfig;
import com.tobqol.TheatreQOLPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

@Singleton
@Slf4j
public class AudioService
{
	@Inject
	private TheatreQOLConfig config;

	@Inject
	private AudioPlayer audioPlayer;

	public void playSotetegBallAlarm()
	{
		playClip(AudioClips.SOTETSEG_BALL, config.sotetsegSoundClipVolume());
	}

	public void playXarpusSheeshAlarm()
	{
		playClip(AudioClips.XARPUS_SHEESH, config.xarpusSoundClipVolume());
	}

	public void playVerzikBallAlarm()
	{
		playClip(AudioClips.VERZIK_BALL, config.verzikSoundClipVolume());
	}

	private void playClip(AudioClips clip, int volume)
	{
		float gain = volume / 2f - 45f;

		try
		{
			log.debug("Playing clip: {} with gain: {}", clip.getFileName(), gain);
			audioPlayer.play(TheatreQOLPlugin.class, clip.getFileName(), gain);
		}
		catch (IOException | UnsupportedAudioFileException | LineUnavailableException e)
		{
			log.warn("Failed to play clip: {}", clip.getFileName(), e);
		}
	}
}
