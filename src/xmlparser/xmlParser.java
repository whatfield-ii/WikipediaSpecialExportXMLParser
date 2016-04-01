/*
* Copyright (c) 2016 William Hatfield, Utkarshani Jaimini, Uday Sagar Panjala.
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions: 
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/
//Add to xmlparser package - to be changed to xml and postagger parser
package xmlparser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The purpose of this project is take a XML file from the Wikipedia Special
 * Export tool, and parse the data into 4 (or 5) different XML files.
 * 
 * Each one will contain different types of data parsed from the same source:
 * categories, citations, anchor-text, and the main article text, or all.
 * 
 * @author W. Hatfield
 * @author U. Jaimini
 * @author U. Panjala
 */
public class xmlParser {
    
    /**
     * Takes an XML file name as the only argument, specifically one that was
     * downloaded from https://en.wikipedia.org/wiki/Special:Export , which then
     * uses the DOM to parse the file and create WikiArticle objects that are
     * added to the list and returned to the caller.
     * 
     * @param xmlFN: the XML path/file name
     * @return list: a list of WikiArticles
     */
    private static ArrayList<xmlPage> importAnchorXMLFile(String xmlFN, String pageType) {
        
        ArrayList<xmlPage> list;
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document doc;
        NodeList nl;
        
        try {
            // get the NEW document builder factory
            dbf = DocumentBuilderFactory.newInstance();
            // use factory to get instance of document builder
            db = dbf.newDocumentBuilder();
            // use builder to get instance of document
            doc = db.parse(xmlFN);
            // get a nodelist of elements (wiki pages)
            nl = doc.getElementsByTagName("page");
            // get the list for storing articles
            list = new ArrayList<>();

            // convert NodeList to ArrayList<WikiArticle>
            for (int i = 0; i < nl.getLength(); i++) {
                xmlPage newXmlPage = new xmlPage(nl.item(i), pageType);
                list.add(newXmlPage);
            }
            
            System.out.println("Success Parsing XML!");
            return list;
            
        } catch (ParserConfigurationException
                | SAXException
                | IOException ex) {
            System.err.println("ERROR: " + ex.getMessage());
            System.err.println("!! Failed XML Parsing !!");
        }
        return null;
    }
    
    /**
     * 
     * @param wikiList - the array list of wikipedia pages
     * @param docType - the type of document to build
     *      1: xml DOM w/Categories
     *      2: xml DOM w/Citations
     *      3: xml DOM w/Anchors
     *      4: xml DOM w/Text
     *     -1: xml DOM w/All Tags
     * @return a document including tags, determined by docType
     */
    private static File makeTextFile(ArrayList<xmlPage> wikiList, int docType, String fileName) {

        BufferedWriter output = null;     
        
        try {
            File file = new File(fileName);
            output = new BufferedWriter(new FileWriter(file));

            // iterate through list appending to doc
            for (xmlPage wiki : wikiList) {
                // iterate the articles list of links and append them
                switch (docType) {
                    case 1: {
                        for (String categoryString : wiki.getCategories()) {
                             output.write(categoryString);
                        }
                        break;
                    }
                    case 2: {
                        for (String citationSring : wiki.getCitations()) {
                             output.write(citationSring);
                        }
                        break;
                    }
                    case 3: {
                        for (String anchorString : wiki.getAnchors()) {
                             output.write(anchorString);
                        }
                        break;
                    }
                    case 4: {
                        for (String textString : wiki.getText()) {
                             output.write(textString);
                        }
                        break;
                    }
                    case -1: {
                        // add Categories, Citations, Anchors, and Text
                        for (String categoryString : wiki.getCategories()) {
                             output.write(categoryString);
                        }

                        for (String citationSring : wiki.getCitations()) {
                             output.write(citationSring);
                        }

                        for (String anchorString : wiki.getAnchors()) {
                             output.write(anchorString);
                        }
                        for (String textString : wiki.getText()) {
                             output.write(textString);
                        }
                        break;
                    }
                }
            } /* ALL ARTICLES NOW ADDED TO THE DOCUMENT OBJECT */
            
            return file;
            
        }  catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            //if ( output != null ) output.close();
        }
        
        return null;
    }
    
    /**
     * 
     * @param doc the DOM/XML document to write to a file
     * @param fn the filename of the XML file to create
     */
    private static void writeDocumentToXMLFile(Document doc, String fn) {
        
        TransformerFactory tf;
        Transformer transx;
        DOMSource source;
        
        try {
            tf = TransformerFactory.newInstance();
            transx = tf.newTransformer();
            source = new DOMSource(doc);
            File xmlFile = new File(fn);
            
            StreamResult sRes = new StreamResult(xmlFile); // saving to xml file
            
            transx.transform(source, sRes);
            System.out.println("XML File Saved: " + xmlFile.getAbsolutePath());
            
        } catch (TransformerConfigurationException ex) {
            System.err.println("ERROR 1: " + ex.getMessage());
            System.err.println("!! XML Creation Failed !!");
        } catch (TransformerException ex) {
            System.err.println("ERROR 2: " + ex.getMessage());
            System.err.println("!! XML Creation Failed !!");
        }
    }
    
    /**
     * THE MAIN METHOD. <-- String fileName designates input for now. -->
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        /**
         * CHANGE THIS FILENAME TO YOUR XML FILE TO PARSE
         * PLACE YOUR XML FILE INTO THE xmlInput FOLDER
         */
        String pageArticleFileName = "xmlOutput/articleOuput.xml";
        String pageCategoryFileName = "xmlOutput/pageCategoryDocument.xml";
        String pageCitationFileName = "xmlOutput/pageCitationDocument.xml";
        String pageAnchorFileName = "xmlOutput/pageAnchorDocument.xml";
        String pageTextFileName = "xmlOutput/pageTextDocument.xml"; 
        
        String pageArticleOutputFileName = "POSTaggerInput/pageArticleDocument.txt";
        String pageCategoryOutputFileName = "POSTaggerInput/pageCategoryDocument.txt";        
        String pageCitationOutputFileName = "POSTaggerInput/pageCitationDocument.txt";
        String pageAnchorOutputFileName = "POSTaggerInput/pageAnchorDocument.txt";
        String pageTextOutputFileName = "POSTaggerInput/pageTextDocument.txt";         
        
        ArrayList<xmlPage> ArticlePagelist = importAnchorXMLFile(pageArticleFileName, "article");
        File articlePageFile = makeTextFile(ArticlePagelist, -1, pageArticleOutputFileName);
        
        ArrayList<xmlPage> CategoryPagelist = importAnchorXMLFile(pageCategoryFileName, "category");
        File categoryPageFile = makeTextFile(CategoryPagelist, 1, pageCategoryOutputFileName);
        
        ArrayList<xmlPage> CitationPagelist = importAnchorXMLFile(pageCitationFileName, "citation");
        File citationPageFile = makeTextFile(CitationPagelist, 2, pageCitationOutputFileName);
        
        ArrayList<xmlPage> AnchorPagelist = importAnchorXMLFile(pageAnchorFileName, "anchor");
        File anchorPageFile = makeTextFile(AnchorPagelist, 3, pageAnchorOutputFileName);
        
        ArrayList<xmlPage> TextPagelist = importAnchorXMLFile(pageTextFileName, "text");
        File textPageFile = makeTextFile(TextPagelist, 4, pageTextOutputFileName);
           
    }
    
}
