package com.google.refine.commands;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.testng.annotations.Test;

public class CommandMockingTests {

    @Test
    public void respondNoJsonpExceptionWrapsErrorForValidCallback() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("callback")).thenReturn("myCallback_123");

        StringWriter body = new StringWriter();
        PrintWriter writer = new PrintWriter(body);
        when(response.getWriter()).thenReturn(writer);

        Command.respondNoJsonpException(request, response);
        writer.flush();

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).getWriter();

        String output = body.toString();
        assertTrue(output.startsWith("myCallback_123("));
        assertTrue(output.contains("JSONP is not supported for this command"));
        assertTrue(output.endsWith(")"));
    }

    @Test
    public void respondNoJsonpExceptionSkipsBodyForInvalidCallbackIdentifier() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getParameter("callback")).thenReturn("bad-callback-name");

        Command.respondNoJsonpException(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response, never()).getWriter();
    }
}
