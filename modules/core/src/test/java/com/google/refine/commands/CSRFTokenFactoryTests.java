
package com.google.refine.commands;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.Instant;

import org.testng.annotations.Test;

public class CSRFTokenFactoryTests {

    static class CSRFTokenFactoryStub extends CSRFTokenFactory {

        public CSRFTokenFactoryStub(long timeToLive, int tokenLength) {
            super(timeToLive, tokenLength);
        }

        public void tamperWithToken(String token, Instant newGenerationTime) {
            tokenCache.asMap().put(token, newGenerationTime);
        }
    }

    /**
     * Stub that replaces random token generation with a deterministic value.
     * Useful when we want predictable tests that do not depend on RNG output.
     */
    static class DeterministicCSRFTokenFactoryStub extends CSRFTokenFactory {

        private final String fixedToken;

        public DeterministicCSRFTokenFactoryStub(long timeToLive, int tokenLength, String fixedToken) {
            super(timeToLive, tokenLength);
            this.fixedToken = fixedToken;
        }

        @Override
        public String getFreshToken() {
            tokenCache.asMap().put(fixedToken, now());
            return fixedToken;
        }
    }

    static class TimeControlledCSRFTokenFactoryStub extends CSRFTokenFactory {

        private Instant currentTime;

        public TimeControlledCSRFTokenFactoryStub(long timeToLive, int tokenLength, Instant startTime) {
            super(timeToLive, tokenLength);
            this.currentTime = startTime;
        }

        public void setCurrentTime(Instant currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        protected Instant now() {
            return currentTime;
        }
    }

    @Test
    public void testGenerateValidToken() {
        CSRFTokenFactory factory = new CSRFTokenFactory(10, 25);
        // Generate a fresh token
        String token = factory.getFreshToken();
        // Immediately after, the token is still valid
        assertTrue(factory.validToken(token));
        // The token has the right length
        assertEquals(25, token.length());
    }

    @Test
    public void testGenerateValidTokenWithStubbedMethod() {
        String fixedToken = "StubbedTokenValue1234567890";
        DeterministicCSRFTokenFactoryStub stub = new DeterministicCSRFTokenFactoryStub(10, fixedToken.length(), fixedToken);

        // Uses the stubbed getFreshToken() instead of real random generation
        String token = stub.getFreshToken();

        assertEquals(token, fixedToken);
        assertTrue(stub.validToken(token));
        assertEquals(fixedToken.length(), token.length());
    }

    @Test
    public void testInvalidToken() {
        CSRFTokenFactory factory = new CSRFTokenFactory(10, 25);
        assertFalse(factory.validToken("bogusToken"));
    }

    @Test
    public void testTokenExpirationBoundaryWithControlledTime() {
        Instant t0 = Instant.parse("2026-02-27T20:00:00Z");
        TimeControlledCSRFTokenFactoryStub factory = new TimeControlledCSRFTokenFactoryStub(10, 25, t0);

        String token = factory.getFreshToken();

        factory.setCurrentTime(t0.plusSeconds(9));
        assertTrue(factory.validToken(token));

        // At exactly TTL seconds, token should be expired (strictly after cutoff is required)
        factory.setCurrentTime(t0.plusSeconds(10));
        assertFalse(factory.validToken(token));
    }

    @Test
    public void testOldToken() {
        CSRFTokenFactoryStub stub = new CSRFTokenFactoryStub(10, 25);
        // Generate a fresh token
        String token = stub.getFreshToken();
        // Manually change the generation time
        stub.tamperWithToken(token, Instant.now().minusSeconds(100));
        // The token should now be invalid
        assertFalse(stub.validToken(token));
    }
}
