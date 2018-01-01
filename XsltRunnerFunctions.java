package org.exist.xquery.modules.odf;

import com.sun.jndi.toolkit.url.Uri;
import org.exist.storage.serializers.Serializer;
import org.exist.xquery.BasicFunction;
import org.apache.log4j.Logger;
import org.exist.xquery.value.FunctionParameterSequenceType;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.exist.dom.persistent.BinaryDocument;
import org.exist.dom.QName;
import org.exist.dom.persistent.DocumentImpl;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.lock.Lock;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.modules.ModuleUtils;
import org.exist.xquery.value.AnyURIValue;
import org.exist.xquery.value.Base64BinaryValueType;
import org.exist.xquery.value.BinaryValue;
import org.exist.xquery.value.BinaryValueFromInputStream;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.StringValue;
import org.xml.sax.SAXException;

import org.odftoolkit.odfdom.pkg.OdfPackage;
import org.odftoolkit.odfdom.pkg.manifest.OdfFileEntry;

import org.w3c.dom.Element; // Maybe

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
//            Element content = ((Element)args[1].itemAt(0));
//            BinaryValue[] newData = getBinaryData(args[2]);
//            BinaryValue content = (BinaryValue) args[1].itemAt(0);
            BinaryValue content = (BinaryValue)args[1].itemAt(0);
            result = replaceContent(sourceODFPackage_uri, content);
        }
//        else if (isCalledAs(UPDATE_ENTRIES)) {
//            XmldbURI uri = ((AnyURIValue)args[0].itemAt(0)).toXmldbURI();
//            String[] paths = getPaths(args[1]);
//            BinaryValue[] newData = getBinaryData(args[2]);
//            result = updateZip(uri, paths, newData);
//        }



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

            // Serialize the content file - but this is old.
//            if (doc.getResourceType() == DocumentImpl.XML_FILE) {
//                // xml file
//                Serializer serializer = context.getBroker().getSerializer();
//                serializer.setUser(context.getUser());
//                serializer.setProperty("omit-xml-declaration", "no");
//                getDynamicSerializerOptions(serializer);
//                String strDoc = serializer.serialize(doc);
//                value = strDoc.getBytes();
//            }
    // TEST 1: Given the INPUT FILE, simply return it as an Output.

            // the loadPackage method can take a File or an InputStream
            URL url = new URL(aInputFile);
            //File file = new File(url.openStream());

            OdfPackage aInputPkg = OdfPackage.loadPackage(url.openStream());

            OdfPackage aOutputPkg = aInputPkg; // a copy? See XSLTRunner line 215

            OutputStream outputStream = null;

            // try simply returning this aOutputPkg
            String aPathInPackage = "content.xml";
            String aMediaType ="text/xml";
//            OdfFileEntry aFileEntry =  aInputPkg.getFileEntry(aPathInPackage);
//            if( aFileEntry != null )
//                aMediaType = aFileEntry.getMediaTypeString();

            OutputStream aOutputStream = aOutputPkg.insertOutputStream(aPathInPackage, aMediaType );

            // DO it the old way (see also GZipFunction)
//            content.getStringValue();
            logger.warn(content.convertTo(Type.BASE64_BINARY));
            content.streamBinaryTo(aOutputStream); // was streamBinaryTo
            aOutputStream.flush();
            content.close();




            // write the content into the outputStream
            // get an input stream for the content
            // Files.copy will copy an inputStream to a Path, or a Path to an outputstream
            // can i turn the content into a Path? Not sure - it's BinaryDoc
            // Path contentPath = new Path()
            // Files.copy(content.getInputStream(),outputStream);
//            Files.copy(content,aOutputStream);
            // So - either receive a full binary here and simply construct a File,
            // Or - receive an Element here and Serialize into a File

            // how do you add theContentFile to the output?

            // SAVE the Package to a File or Stream
            aOutputPkg.save(aOutputStream);
            aOutputStream.close();

            return BinaryValueFromInputStream.getInstance(context, new Base64BinaryValueType(), aOutputPkg.getInputStream());
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
