/**
 * Treats documents as text.  Parses out only DOCNO.
 * XML Tags are discarded
 */

package org.apache.lucenesandbox.xmlindexingdemo;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.io.*;
import java.util.*;


public class XMLDocumentHandlerTxt
{    
    private Document doc;
    private ArrayList docs;
    
    /**
     * @param xmlFile
     * @param docTag - Tag that indicates document
     */
    public XMLDocumentHandlerTxt(File xmlFile) throws IOException
    {
        docs = new ArrayList();
        parse(xmlFile);
    }
    
    public void parse( File file) throws IOException
    {
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        StringBuffer strb = new StringBuffer(100);
	String docline = "";	
        boolean intext = false;
	boolean indocline = false;

        while ( reader.ready() )
        {
            String line = reader.readLine();
            if ( line.indexOf( "</DOCNO>" ) != -1 ) {
		docline += line;
		doc = createDoc( docline );
		docs.add( doc );
		docline = "";
		indocline = false;
	    } else if ( line.indexOf( "<DOCNO>" ) != -1 ) {
                docline += line;
                indocline = true;
	    } else if (indocline) {
		docline += line.replaceAll("[!]", "");
	    } else if ( line.toUpperCase().indexOf("<TEXT>") != -1 ) {
		intext = true;
	    } else if (line.toUpperCase().indexOf("</TEXT>") != -1 ) {
		intext = false;
	    }
            // Add contents to a doc
            else if ( doc != null && intext) {
                // Strip out xml tags
                String txt = stripTagsOut( line );
		
		strb.append( " " + txt + " " );
            }

            // If we hit the end of doc then add text to it
            if ( line.indexOf( "</DOC>" ) != -1 )
            {
                doc.add(new Field("text", strb.toString(), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
                strb = new StringBuffer(100);
            }
        }
        reader.close();
    }
    
    public String stripTagsOut( String str)
    {
        boolean ignore = false;
        StringBuffer strb = new StringBuffer();
        for ( int i = 0; i < str.length(); i++ )
        {
            char ch = str.charAt( i );
            if ( ch == '<' )
            {
                ignore = true;
            }
            else if ( ch == '>' )
            {
                ignore = false;
            }
            else if ( !ignore )
            {
                strb.append( ch );
            }
        }
        // Strip out some html tags - &blank; &hyph; &amp; &sect;        
        String strP = strb.toString();
        strP = strP.replaceAll( "&blank;|&hyph;|&amp;|&sect;|&bull;", " " );        
        
        return strP;
    }
    
    /**
     * Creates empty doc from a str of form:
     * <DOCNO> FBIS3-1 </DOCNO>
     */
    public Document createDoc( String str )
    {
        // DocNo is a second token
        StringTokenizer tknzr = new StringTokenizer( str, " \t\n\r\f<>" );
        tknzr.nextToken();
        String docNo = tknzr.nextToken().trim();
        Document doc = new Document();
        //noc.add( Field.Text( "DOCNO", docNo ) );
        doc.add(new Field("DOCNO", docNo, Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }
    
    
    public ArrayList getDocuments()
    {
        return docs;
    }
}
