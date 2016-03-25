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
package xmlparser;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Wrapper Class for the Wikipedia Special Export XML Data.
 * 
 * @author W. Hatfield
 * @author U. Jaimini
 * @author U. Panjala
 */
public class xmlPage {
    protected final String pageTitle;       // name of the the wiki-page
    protected final String revNumber;        // revision ID of the page

    private ArrayList<String> mainPageText;      // main article text from page'
    private ArrayList<String> categories;   // categoriesList listed on the page
    private ArrayList<String> citations;    // citations used on the page
    private ArrayList<String> anchors;      // hyperlinks used on the page
    
    public xmlPage(Node pageNode, String pageType) {
        // cast to an Element for using: getElementsByTagName(String name)
        Element page = (Element) pageNode;
        //
        this.pageTitle = parsePageTitle(page);
        this.revNumber = parseRevisionNum(page);
        //
        if(pageType == "category" || pageType == "article") {
          this.categories = parseCategoryTags(page);            
        }
        if(pageType == "citation" || pageType == "article") {
        this.citations = parseCitationTags(page);
        }
        if(pageType == "anchor" || pageType == "article") {
        this.anchors = parseAnchorTags(page);
        }
        if(pageType == "text" || pageType == "article") {
        this.mainPageText = parseTextTags(page);
        }
    }
    
    public ArrayList<String> getCategories() { return this.categories; }
    public ArrayList<String> getCitations() { return this.citations; }
    public ArrayList<String> getAnchors() { return this.anchors; }
    public ArrayList<String> getText() { return this.mainPageText; }
    
    private String parsePageTitle(Element page) {
        return page.getElementsByTagName("title").item(0).getTextContent();
    }
    
    private String parseRevisionNum(Element page) {
        return page.getElementsByTagName("rev").item(0).getTextContent();
    }
    
    private ArrayList<String> parseCategoryTags(Element page) {
        
            NodeList al;
            // get a nodelist of elements (wiki pages)
            al = page.getElementsByTagName("category");
            // get the list for storing articles
            ArrayList<String> list = new ArrayList<>();

            // convert NodeList to ArrayList<WikiArticle>
            for (int i = 0; i < al.getLength(); i++) {
                list.add(al.item(i).getTextContent());
            }        
        return list;
    }
    
    private ArrayList<String> parseCitationTags(Element page) {
        
            NodeList al;
            // get a nodelist of elements (wiki pages)
            al = page.getElementsByTagName("citation");
            // get the list for storing articles
            ArrayList<String> list = new ArrayList<>();

            // convert NodeList to ArrayList<WikiArticle>
            for (int i = 0; i < al.getLength(); i++) {
                list.add(al.item(i).getTextContent());
            }        
        return list;
    }
    
    private ArrayList<String> parseAnchorTags(Element page) {
        
            NodeList al;
            // get a nodelist of elements (wiki pages)
            al = page.getElementsByTagName("anchor");
            // get the list for storing articles
            ArrayList<String> list = new ArrayList<>();

            // convert NodeList to ArrayList<WikiArticle>
            for (int i = 0; i < al.getLength(); i++) {
                list.add(al.item(i).getTextContent());
            }        
        return list;
    }
    
    private ArrayList<String> parseTextTags(Element page) {
        
            NodeList al;
            // get a nodelist of elements (wiki pages)
            al = page.getElementsByTagName("text");
            // get the list for storing articles
            ArrayList<String> list = new ArrayList<>();

            // convert NodeList to ArrayList<WikiArticle>
            for (int i = 0; i < al.getLength(); i++) {
                list.add(al.item(i).getTextContent());
            }        
        return list;
    }

}
