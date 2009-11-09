//
// $Id: AppInserterServlet.java 18229 2009-10-01 18:19:09Z jamie $

package com.threerings.msoy.web.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.mortbay.jetty.servlet.DefaultServlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.io.StreamUtil;
import com.samskivert.velocity.VelocityUtil;

import com.threerings.msoy.group.server.ThemeLogic;
import com.threerings.msoy.group.server.persist.ThemeRecord;
import com.threerings.msoy.group.server.persist.ThemeRepository;
import com.threerings.msoy.web.gwt.ArgNames;

@Singleton
public class ThemedTemplateServlet extends DefaultServlet
{
    @Override
    protected void doGet (HttpServletRequest req, HttpServletResponse rsp)
        throws ServletException, IOException
    {
        String themeIdStr = req.getParameter(ArgNames.THEME);
        int themeId = (themeIdStr != null) ? Integer.parseInt(themeIdStr) : 0;
        ThemeRecord themeRec = (themeId != 0) ? _themeRepo.loadTheme(themeId) : null;

        VelocityContext ctx = new VelocityContext();
        if (themeRec != null) {
            ctx.put("logoUrl", themeRec.toLogo().getMediaPath());
            ctx.put("backgroundColor", hexColor(themeRec.backgroundColor));
        } else {
            ctx.put("logoUrl", DEFAULT_LOGO_URL);
            ctx.put("backgroundColor", hexColor(ThemeRecord.DEFAULT_BACKGROUND_COLOR));
        }

        try {
            PrintWriter pout = new PrintWriter(rsp.getOutputStream());
            VelocityEngine ve = VelocityUtil.createEngine();
            String URI = req.getRequestURI();
            ve.mergeTemplate("rsrc" + URI + ".tmpl", "UTF-8", ctx, pout);
            StreamUtil.close(pout);

        } catch (Exception ex) {
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected static String hexColor (int rgb)
    {
        String str = ("000000" + Integer.toHexString(rgb));
        return str.substring(str.length() - 6);
    }

    @Inject ThemeRepository _themeRepo;
    @Inject ThemeLogic _themeLogic;

    protected static final String DEFAULT_LOGO_URL = "/images/header/header_logo.png";
}