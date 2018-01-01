package org.exist.xquery.modules.odf;

import org.apache.log4j.Logger;
import org.exist.dom.QName;
import org.exist.dom.persistent.BinaryDocument;
import org.exist.dom.persistent.DocumentImpl;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.lock.Lock;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.*;
import org.exist.xquery.value.*;
import org.odftoolkit.odfdom.pkg.OdfPackage;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class XsltRunnerFunctions extends BasicFunction{
    private static final Logger logger = Logger.getLogger(XsltRunnerFunctions.class);

    private final static FunctionParameterSequenceType HREF_PARAM = new FunctionParameterSequenceType("href",
            Type.ANY_URI, Cardinality.EXACTLY_ONE,
            "The URI for locating the Zip file");
    private final static String FILE_ENTRIES = "entries";
    private final static String REPLACE_CONTENT = "replace-content";


    public final static FunctionSignature signatures[] = {
            //odf-xslt:replace-content($OdfURI, $content) : base64-binary
            new FunctionSignature(
                    new QName(REPLACE_CONTENT, XsltRunnerModule.NAMESPACE_URI, XsltRunnerModule.PREFIX),
                    "Return a new ODF file where the content.xml has been replaced by the supplied XML document",
                    new SequenceType[]{
                            new FunctionParameterSequenceType("href", Type.ANY_URI, Cardinality.EXACTLY_ONE, "The URI for locating the ODF file"),
                            new FunctionParameterSequenceType("content", Type.BASE64_BINARY, Cardinality.ONE_OR_MORE,
                                    "The replacement ODF content."),

                    },
                    new FunctionReturnSequenceType(
                            Type.BASE64_BINARY, Cardinality.EXACTLY_ONE,
                            "The updated ODF document. The content will not be modified if the supplied $content is empty")
            )
    };

    public XsltRunnerFunctions(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {

        Sequence result = Sequence.EMPTY_SEQUENCE;
        if (isCalledAs(REPLACE_CONTENT)) {
            String sourceODFPackage_uri = ((AnyURIValue) args[0].itemAt(0)).getStringValue();
            BinaryValue content = (BinaryValue)args[1].itemAt(0);
            result = replaceContent(sourceODFPackage_uri, content);
        }




        return result;
    }

/*
    provide an InputFile URI for an ODF file.
    provide a content.xml file as a BinaryDocument (so the serialization happens in eXist)
    returns a BinaryDocument in a Sequence
 */
    private Sequence replaceContent(String aInputFile, BinaryValue content ) {
        // use the odfpackage to replace the content file
        try {
            // the loadPackage method can take a File or an InputStream
            URL url = new URL(aInputFile);

            OdfPackage aInputPkg = OdfPackage.loadPackage(url.openStream());
            OdfPackage aOutputPkg = aInputPkg; // Why? See XSLTRunner line 215 - Probably not necessary

            String aPathInPackage = "content.xml";
            String aMediaType ="text/xml";

            OutputStream aOutputStream = aOutputPkg.insertOutputStream(aPathInPackage, aMediaType);

            content.streamBinaryTo(aOutputStream); // was streamBinaryTo
            aOutputStream.flush();
            aOutputStream.close();
            content.close();

            ByteArrayOutputStream theOutputFile = new ByteArrayOutputStream();
            // SAVE the Package to a File or Stream - this becomes the NEW ODT
            aOutputPkg.save(theOutputFile);
            aOutputPkg.close();
            aInputPkg.close();

            return BinaryValueFromInputStream.getInstance(context, new Base64BinaryValueType(), new ByteArrayInputStream(theOutputFile.toByteArray()));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Sequence.EMPTY_SEQUENCE;
//        ODFXSLTRunner runner = new ODFXSLTRunner();
//
//        runner.runXSLT(  new File( aStyleSheet ), aParams,
//                new File( aInputFile), ODFXSLTRunner.INPUT_MODE_PACKAGE,
//                aOutputFile != null ? new File(aOutputFile) : null, ODFXSLTRunner.OUTPUT_MODE_COPY_INPUT_PACKAGE,
//                "content.xml", null,
//                false, logger );
    }

    private BinaryDocument getDoc(XmldbURI uri) throws PermissionDeniedException {

        DocumentImpl doc = context.getBroker().getXMLResource(uri, Lock.READ_LOCK);
        if(doc == null || doc.getResourceType() != DocumentImpl.BINARY_FILE) {
            return null;
        }

        return (BinaryDocument)doc;
    }
}
