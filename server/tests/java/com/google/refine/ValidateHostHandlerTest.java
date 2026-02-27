package com.google.refine;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * Mockito-based test suite for ValidateHostHandler.
 * 
 * This class tests the Host header validation feature that prevents DNS rebinding attacks.
 * It uses Mockito to mock servlet objects and verify the handler's behavior when processing
 * HTTP requests with different Host header values.
 * 
 * Tests cover:
 * - Rejection of invalid hosts with 404 response
 * - Delegation to wrapped handler for valid hosts
 * - Support for loopback addresses (IPv4 and IPv6)
 * - Support for localhost
 * - Case-insensitive hostname matching
 * - Handling of ports in Host header
 * - Null Host header handling
 */
public class ValidateHostHandlerTest {

    /**
     * Test Case 1: Invalid Host Header Sends 404 Error
     * 
     * Scenario: Request with Host header that doesn't match expected hostname
     * Expected Behavior: 
     *   - sendError(404, "Invalid hostname") is called on response
     *   - Wrapped handler is never called (no delegation)
     * 
     * Security Implication: Prevents DNS rebinding attacks by rejecting requests
     * from unauthorized hostnames
     */
    @Test
    public void testInvalidHostSends404ErrorAndDoesNotDelegate() throws IOException, ServletException {
        // Setup
        String expectedHost = "expected.example.com";
        ValidateHostHandler handler = new ValidateHostHandler(expectedHost);
        
        // Mock the wrapped handler
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        // Mock servlet objects
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // Configure mocks to return invalid host
        Mockito.when(request.getHeader("Host")).thenReturn("malicious.attacker.com");
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        // Should send 404 error
        Mockito.verify(response, Mockito.times(1))
                .sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid hostname");
        
        // Should NOT call the wrapped handler
        Mockito.verifyNoInteractions(wrappedHandler);
    }

    /**
     * Test Case 2: Valid Host Header Delegates to Wrapped Handler
     * 
     * Scenario: Request with Host header matching expected hostname (with port)
     * Expected Behavior:
     *   - Wrapped handler's handle() method is called
     *   - sendError() is never called on response
     * 
     * Security Implication: Allows legitimate requests from the expected hostname
     * to proceed to the application
     */
    @Test
    public void testValidHostDelegatesToWrappedHandlerAndDoesNotSendError() throws IOException, ServletException {
        // Setup
        String expectedHost = "expected.example.com";
        ValidateHostHandler handler = new ValidateHostHandler(expectedHost);
        
        // Mock the wrapped handler
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        // Mock servlet objects
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // Configure mocks to return valid host with port
        Mockito.when(request.getHeader("Host")).thenReturn("expected.example.com:3333");
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        // Should call the wrapped handler with correct parameters
        Mockito.verify(wrappedHandler, Mockito.times(1))
                .handle("/api/endpoint", baseRequest, request, response);
        
        // Should NOT send error
        Mockito.verify(response, Mockito.never())
                .sendError(Mockito.anyInt(), Mockito.anyString());
    }

    /**
     * Test Case 3: Loopback IPv4 Address is Allowed
     * 
     * Scenario: Request from loopback IPv4 address (127.0.0.1)
     * Expected Behavior:
     *   - Wrapped handler is called (access allowed)
     *   - No error response is sent
     * 
     * Security Implication: Allows localhost access without specifying expected hostname,
     * useful for local development and testing
     */
    @Test
    public void testLoopbackIPv4IsAllowed() throws IOException, ServletException {
        // Setup
        ValidateHostHandler handler = new ValidateHostHandler("expected.example.com");
        
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // Loopback address should always be allowed
        Mockito.when(request.getHeader("Host")).thenReturn("127.0.0.1:3333");
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        Mockito.verify(wrappedHandler, Mockito.times(1))
                .handle("/api/endpoint", baseRequest, request, response);
        Mockito.verify(response, Mockito.never())
                .sendError(Mockito.anyInt(), Mockito.anyString());
    }

    /**
     * Test Case 4: Localhost Hostname is Allowed
     * 
     * Scenario: Request using "localhost" hostname
     * Expected Behavior:
     *   - Wrapped handler is called (access allowed)
     *   - No error response is sent
     * 
     * Security Implication: Allows localhost connections for local development
     */
    @Test
    public void testLocalhostIsAllowed() throws IOException, ServletException {
        // Setup
        ValidateHostHandler handler = new ValidateHostHandler("expected.example.com");
        
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // localhost should always be allowed
        Mockito.when(request.getHeader("Host")).thenReturn("localhost:3333");
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        Mockito.verify(wrappedHandler, Mockito.times(1))
                .handle("/api/endpoint", baseRequest, request, response);
        Mockito.verify(response, Mockito.never())
                .sendError(Mockito.anyInt(), Mockito.anyString());
    }

    /**
     * Test Case 5: IPv6 Loopback Address is Allowed
     * 
     * Scenario: Request from IPv6 loopback address ([::1])
     * Expected Behavior:
     *   - Wrapped handler is called (access allowed)
     *   - No error response is sent
     * 
     * Security Implication: Supports IPv6 localhost connections
     */
    @Test
    public void testIPv6LoopbackIsAllowed() throws IOException, ServletException {
        // Setup
        ValidateHostHandler handler = new ValidateHostHandler("expected.example.com");
        
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // IPv6 loopback should be allowed
        Mockito.when(request.getHeader("Host")).thenReturn("[::1]:3333");
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        Mockito.verify(wrappedHandler, Mockito.times(1))
                .handle("/api/endpoint", baseRequest, request, response);
        Mockito.verify(response, Mockito.never())
                .sendError(Mockito.anyInt(), Mockito.anyString());
    }

    /**
     * Test Case 6: Case-Insensitive Hostname Matching
     * 
     * Scenario: Request with hostname in different case (UPPERCASE)
     * Expected Behavior:
     *   - Wrapped handler is called (matches ignoring case)
     *   - No error response is sent
     * 
     * Security Implication: Makes validation robust against case variations
     */
    @Test
    public void testHostnameMatchingIsCaseInsensitive() throws IOException, ServletException {
        // Setup
        ValidateHostHandler handler = new ValidateHostHandler("expected.example.com");
        
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // Different case should still match
        Mockito.when(request.getHeader("Host")).thenReturn("EXPECTED.EXAMPLE.COM");
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        Mockito.verify(wrappedHandler, Mockito.times(1))
                .handle("/api/endpoint", baseRequest, request, response);
        Mockito.verify(response, Mockito.never())
                .sendError(Mockito.anyInt(), Mockito.anyString());
    }

    /**
     * Test Case 7: Null Host Header is Rejected
     * 
     * Scenario: Request with null Host header
     * Expected Behavior:
     *   - sendError(404, "Invalid hostname") is called
     *   - Wrapped handler is never called
     * 
     * Security Implication: Prevents requests without Host header from being processed
     */
    @Test
    public void testNullHostHeaderSends404Error() throws IOException, ServletException {
        // Setup
        ValidateHostHandler handler = new ValidateHostHandler("expected.example.com");
        
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // Null host header
        Mockito.when(request.getHeader("Host")).thenReturn(null);
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        Mockito.verify(response, Mockito.times(1))
                .sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid hostname");
        Mockito.verifyNoInteractions(wrappedHandler);
    }

    /**
     * Test Case 8: Valid Host Without Port
     * 
     * Scenario: Request with valid hostname but no port number
     * Expected Behavior:
     *   - Wrapped handler is called
     *   - No error response is sent
     * 
     * Security Implication: Supports both port and non-port Host headers
     */
    @Test
    public void testValidHostWithoutPortIsAllowed() throws IOException, ServletException {
        // Setup
        ValidateHostHandler handler = new ValidateHostHandler("expected.example.com");
        
        Handler wrappedHandler = Mockito.mock(Handler.class);
        handler.setHandler(wrappedHandler);
        
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        
        // Host without port number
        Mockito.when(request.getHeader("Host")).thenReturn("expected.example.com");
        
        // Execute
        handler.handle("/api/endpoint", baseRequest, request, response);
        
        // Verify
        Mockito.verify(wrappedHandler, Mockito.times(1))
                .handle("/api/endpoint", baseRequest, request, response);
        Mockito.verify(response, Mockito.never())
                .sendError(Mockito.anyInt(), Mockito.anyString());
    }
}
