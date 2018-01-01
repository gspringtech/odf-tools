package org.exist.xquery.modules.odf;

import org.apache.log4j.Logger;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;
import org.exist.xquery.modules.mail.*;

import java.util.List;
import java.util.Map;

public class XsltRunnerModule extends AbstractInternalModule {
    private final static Logger LOG = Logger.getLogger( XsltRunnerModule.class );

    public final static String NAMESPACE_URI = "http://pekoe.io/odf-toolkit-wrapper";

    public final static String PREFIX = "odf-tools";
    // JavaMail-based from 2009-03-14
    // makes the need for versioning of the functions obvious too /ljo
    public final static String INCLUSION_DATE = "2017-12-30";
    public final static String RELEASED_IN_VERSION = "eXist-2.2";

    private final static FunctionDef[] functions = {
            new FunctionDef(XsltRunnerFunctions.signatures[0], XsltRunnerFunctions.class)
    };

//    public final static String SESSIONS_CONTEXTVAR = "_eXist_mail_sessions";
//    public final static String STORES_CONTEXTVAR = "_eXist_mail_stores";
//    public final static String FOLDERS_CONTEXTVAR = "_eXist_mail_folders";
//    public final static String FOLDERMSGLISTS_CONTEXTVAR = "_eXist_folder_message_lists";
//    public final static String MSGLISTS_CONTEXTVAR = "_eXist_mail_message_lists";

//    private static long currentSessionHandle = System.currentTimeMillis();


    public XsltRunnerModule(Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }


    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }


    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }


    @Override
    public String getDescription() {
        return "A module for performing actions on ODF files";
    }

    @Override
    public String getReleaseVersion() {
        return RELEASED_IN_VERSION;
    }

    //***************************************************************************
    //*
    //*    Session Methods
    //*
    //***************************************************************************/

}
