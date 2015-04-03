/*
Copyright (C) 2015 Electronic Arts Inc.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1.  Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
2.  Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
3.  Neither the name of Electronic Arts, Inc. ("EA") nor the names of
    its contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY ELECTRONIC ARTS AND ITS CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL ELECTRONIC ARTS OR ITS CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ea.orbit.actors.test;


import com.ea.orbit.actors.IActor;
import com.ea.orbit.actors.OrbitStage;
import com.ea.orbit.actors.test.actors.ISomeMatch;
import com.ea.orbit.actors.test.actors.ISomePlayer;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PersistenceTest extends ActorBaseTest
{

    @Test
    public void checkWritesTest() throws ExecutionException, InterruptedException
    {
        OrbitStage stage1 = createStage();
        assertEquals(0, fakeDatabase.values().size());
        ISomeMatch someMatch = IActor.getReference(ISomeMatch.class, "300");
        ISomePlayer somePlayer = IActor.getReference(ISomePlayer.class, "101");
        someMatch.addPlayer(somePlayer).get();
        assertTrue(fakeDatabase.values().size() > 0);
    }

    @Test
    public void checkReads() throws ExecutionException, InterruptedException
    {
        {
            // adding some state and then tearing down the cluster.
            OrbitStage stage1 = createStage();
            assertEquals(0, fakeDatabase.values().size());
            ISomeMatch someMatch = IActor.getReference(ISomeMatch.class, "300");
            ISomePlayer somePlayer = IActor.getReference(ISomePlayer.class, "101");
            someMatch.addPlayer(somePlayer).get();
            assertTrue(fakeDatabase.values().size() > 0);
            stage1.stop();
        }
        {
            OrbitStage stage2 = createStage();
            ISomeMatch someMatch_r2 = IActor.getReference(ISomeMatch.class, "300");
            ISomePlayer somePlayer_r2 = IActor.getReference(ISomePlayer.class, "101");
            assertEquals(1, someMatch_r2.getPlayers().get().size());
            assertEquals(somePlayer_r2, someMatch_r2.getPlayers().get().get(0));
        }
    }
}
