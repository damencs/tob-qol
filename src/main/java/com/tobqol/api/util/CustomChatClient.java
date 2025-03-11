/*
 * Copyright (c) 2025, Damen <gh: damencs>
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
package com.tobqol.api.util;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.tobqol.loottracking.LootItems;
import com.tobqol.loottracking.LootTrackingMemory;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.chat.Duels;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CustomChatClient
{
    private final OkHttpClient client;
    private final HttpUrl apiBase;
    private final Gson gson;

    @Inject
    private CustomChatClient(OkHttpClient client, @Named("runelite.api.base") HttpUrl apiBase, Gson gson)
    {
        this.client = client;
        this.apiBase = apiBase;
        this.gson = gson;
    }

    public boolean submitMemory(String username, LootTrackingMemory memory) throws IOException
    {
        String lastPersonalItem;

        if (memory.getLastPersonalItem() == null || memory.getLastPersonalItem().isEmpty())
        {
            lastPersonalItem = "0";
        }
        else
        {
            lastPersonalItem = Integer.toString(LootItems.getItemLookupByName().get(memory.getLastPersonalItem()));
        }

        HttpUrl url = apiBase.newBuilder()
                .addPathSegment("chat")
                .addPathSegment("duels")
                .addQueryParameter("name", username)
                .addQueryParameter("wins", Integer.toString(memory.getCountSincePersonal()))
                .addQueryParameter("losses", Integer.toString(memory.getCountSinceOther()))
                .addQueryParameter("winningStreak", lastPersonalItem)
                .addQueryParameter("losingStreak", "0")
                .build();

        Request request = new Request.Builder()
                .post(RequestBody.create(null, new byte[0]))
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute())
        {
            return response.isSuccessful();
        }
    }

    public LootTrackingMemory getMemory(String username) throws IOException
    {
        HttpUrl url = apiBase.newBuilder()
                .addPathSegment("chat")
                .addPathSegment("duels")
                .addQueryParameter("name", username)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("unable to lookup memory");
            }

            InputStream in = response.body().byteStream();
            Duels duels = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), Duels.class);

            String lastPersonalItem;

            if (duels.getWinningStreak() == 0)
            {
                lastPersonalItem = "n/a";
            }
            else
            {
                lastPersonalItem = LootItems.getItemLookup().get(duels.getWinningStreak());
            }

            return new LootTrackingMemory(duels.getWins(), lastPersonalItem, duels.getLosses());
        }
        catch (JsonParseException ex)
        {
            throw new IOException(ex);
        }
    }
}
