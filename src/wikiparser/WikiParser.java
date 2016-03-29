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

package wikiparser;

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
public class WikiParser {
    
    /**
     * Takes an XML file name as the only argument, specifically one that was
     * downloaded from https://en.wikipedia.org/wiki/Special:Export , which then
     * uses the DOM to parse the file and create WikiArticle objects that are
     * added to the list and returned to the caller.
     * 
     * @param xmlFN: the XML path/file name
     * @return list: a list of WikiArticles
     */
    private static ArrayList<WikipediaPage> importWikiXMLFile(String xmlFN) {
        
        ArrayList<WikipediaPage> list;
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
                WikipediaPage newWikiPage = new WikipediaPage(nl.item(i));
                list.add(newWikiPage);
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
    private static Document makeDocument(ArrayList<WikipediaPage> wikiList, int docType) {
        
        String xmlRootElement = "WikipediaPageParseData";
        
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document doc;
        Element root;
        
        try {
            // get the factory, builder, and new document
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            doc = db.newDocument();
            // get and then set the ROOT NODE of doc
            root = doc.createElement(xmlRootElement);
            doc.appendChild(root);
            // iterate through list appending to doc
            for (WikipediaPage wiki : wikiList) {
                // create wikipedia page element and append it to the root
                Element page = doc.createElement("page");
                root.appendChild(page);
                // create title element set value and append it to the article
                Element title = doc.createElement("title");
                title.appendChild(doc.createTextNode(wiki.pageTitle));
                page.appendChild(title);
                // create waNum element set value and append it to the article
                Element waNum = doc.createElement("rev");
                waNum.appendChild(doc.createTextNode(wiki.revNumber));
                page.appendChild(waNum);
                // iterate the articles list of links and append them
                switch (docType) {
                    case 1: {
                        for (String categoryString : wiki.getCategories()) {
                            Element cat = doc.createElement("category");
                            cat.appendChild(doc.createTextNode(categoryString));
                            page.appendChild(cat);
                        }
                        break;
                    }
                    case 2: {
                        for (String citationSring : wiki.getCitations()) {
                            Element cit = doc.createElement("citation");
                            cit.appendChild(doc.createTextNode(citationSring));
                            page.appendChild(cit);
                        }
                        break;
                    }
                    case 3: {
                        for (String anchorString : wiki.getAnchors()) {
                            Element anc = doc.createElement("anchor");
                            anc.appendChild(doc.createTextNode(anchorString));
                            page.appendChild(anc);
                        }
                        break;
                    }
                    case 4: {
                        Element txt = doc.createElement("text");
                        txt.appendChild(doc.createTextNode(wiki.getText()));
                        page.appendChild(txt);
                        break;
                    }
                    case -1: {
                        // add Categories, Citations, Anchors, and Text
                        for (String categoryString : wiki.getCategories()) {
                            Element cat = doc.createElement("category");
                            cat.appendChild(doc.createTextNode(categoryString));
                            page.appendChild(cat);
                        }
                        for (String citationSring : wiki.getCitations()) {
                            Element cit = doc.createElement("citation");
                            cit.appendChild(doc.createTextNode(citationSring));
                            page.appendChild(cit);
                        }
                        for (String anchorString : wiki.getAnchors()) {
                            Element anc = doc.createElement("anchor");
                            anc.appendChild(doc.createTextNode(anchorString));
                            page.appendChild(anc);
                        }
                        Element txt = doc.createElement("text");
                        txt.appendChild(doc.createTextNode(wiki.getText()));
                        page.appendChild(txt);
                        break;
                    }
                }
            } /* ALL ARTICLES NOW ADDED TO THE DOCUMENT OBJECT */
            
            return doc;
            
        } catch (ParserConfigurationException ex) {
            System.err.println("ERROR: " + ex.getMessage());
            System.err.println("!! Doc Creation Failed !!");
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
        // TODO code application logic here
        
        /**
         * CHANGE THIS FILENAME TO YOUR XML FILE TO PARSE
         * PLACE YOUR XML FILE INTO THE xmlInput FOLDER
         */
        String fileName = "xmlInput/WikiParseTestFile.xml";
        
        ArrayList<WikipediaPage> list = importWikiXMLFile(fileName);
        Document doc = makeDocument(list, -1);
        fileName = "xmlOutput/articleOuput.xml";
        writeDocumentToXMLFile(doc, fileName);
        
        for (int i = 1; i <= 4; i++) {
            doc = makeDocument(list, i);
            switch (i) {
                case 1: fileName = "xmlOutput/pageCategoryDocument.xml";
                break;
                case 2: fileName = "xmlOutput/pageCitationDocument.xml";
                break;
                case 3: fileName = "xmlOutput/pageAnchorDocument.xml";
                break;
                case 4: fileName = "xmlOutput/pageTextDocument.xml";
            }
            writeDocumentToXMLFile(doc, fileName);
        }
        
        
    }
    
}
