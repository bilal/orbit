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
import com.ea.orbit.actors.annotation.NoIdentity;
import com.ea.orbit.actors.runtime.OrbitActor;
import com.ea.orbit.concurrent.Task;

import org.junit.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExceptionalTest extends ActorBaseTest
{
    public interface IExceptionalThing extends IActor
    {
        Task<String> justRespond();

        Task<String> justThrowAnException();
    }

    public static class ExceptionalThing extends OrbitActor implements IExceptionalThing
    {
        public Task<String> justRespond()
        {
            return Task.fromValue("resp");
        }

        public Task<String> justThrowAnException()
        {
            throw new RuntimeException("as requested, one exception!");
        }
    }

    @Test
    public void noException() throws ExecutionException, InterruptedException
    {
        OrbitStage stage1 = createStage();
        final IExceptionalThing ref = IActor.getReference(IExceptionalThing.class, "0");
        assertEquals("resp", ref.justRespond().join());
    }

    @Test(expected = CompletionException.class)
    public void withException() throws ExecutionException, InterruptedException
    {
        OrbitStage stage1 = createStage();
        final IExceptionalThing ref = IActor.getReference(IExceptionalThing.class, "0");
        ref.justThrowAnException().join();
    }

    @Test
    public void catchingTheException() throws ExecutionException, InterruptedException
    {
        OrbitStage stage1 = createStage();
        final IExceptionalThing ref = IActor.getReference(IExceptionalThing.class, "0");
        try
        {
            ref.justThrowAnException().join();
            fail("should have thrown an exception");
        }
        catch (CompletionException ex)
        {
            assertTrue(ex.getCause() instanceof RuntimeException);
            assertEquals("as requested, one exception!", ex.getCause().getMessage());
        }
    }

    @Test
    public void checkingTheException() throws ExecutionException, InterruptedException
    {
        OrbitStage stage1 = createStage();
        final IExceptionalThing ref = IActor.getReference(IExceptionalThing.class, "0");
        final Task<String> fut = ref.justThrowAnException();
        // TODO: check this.
        final Throwable ex = fut.handle((r, e) -> e.getCause()).join();
        assertTrue(fut.isCompletedExceptionally());
        assertTrue(ex instanceof RuntimeException);
        assertEquals("as requested, one exception!", ex.getMessage());
    }
}
