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

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author willi
 */
public class WikipediaPage {
    protected final String pageTitle;       // name of the the wiki-page
    protected final String revNumber;        // revision ID of the page
    private final String mainPageText;      // main article text from page'
    
    private ArrayList<String> categories;   // categoriesList listed on the page
    private ArrayList<String> citations;    // citations used on the page
    private ArrayList<String> anchors;      // hyperlinks used on the page
    
    public WikipediaPage(Node pageNode) {
        // cast to an Element for using: getElementsByTagName(String name)
        Element page = (Element) pageNode;
        //
        this.pageTitle = parsePageTitle(page);
        this.revNumber = parseRevisionNum(page);
        //
        char[] pageTextCharArray = parseMainPageText(page).toCharArray();
        //
        this.categories = parseTextForCategories(pageTextCharArray);
        this.citations = parseTextForCitations(pageTextCharArray);
        this.anchors = parseTextForAnchors(pageTextCharArray);
        //
        this.mainPageText 
                = normalizeWikiPageTextForPOSTagging(pageTextCharArray);
        //
    }
    
    public ArrayList<String> getCategories() { return this.categories; }
    public ArrayList<String> getCitations() { return this.citations; }
    public ArrayList<String> getAnchors() { return this.anchors; }
    public String getText() { return this.mainPageText; }
    
    private String parsePageTitle(Element page) {
        return page.getElementsByTagName("title").item(0).getTextContent();
    }
    
    private String parseRevisionNum(Element page) {
        return page.getElementsByTagName("id").item(0).getTextContent();
    }
    
    private String parseMainPageText(Element page) {
        return page.getElementsByTagName("text").item(0).getTextContent();
    }
    
    private ArrayList<String> parseTextForCategories(char[] symbols) {
        
        ArrayList<String> categoriesList = new ArrayList<>();
        
        StringBuffer buff = null;
        boolean reading = false;
        char current, next;
        
        for (int i = 0; i < symbols.length - 1; i++) {
            
            current = symbols[i];
            next = symbols[i + 1];
            
            if (current == '[' && next == '[') {
                
                buff = new StringBuffer();
                reading = true;
                i++; // step over second brace
                
            } else if (current == ']' && next == ']') {
                
                String possibleCategory = buff.toString();
                String categoryPrefix = "Category:";
                
                if (possibleCategory.startsWith(categoryPrefix)) {
                    categoriesList.add(
                        possibleCategory.substring(categoryPrefix.length()));
                }
                
                reading = false;
                
            } else if (reading) buff.append(current);
        }
        
        return categoriesList;
    }
    
    private ArrayList<String> parseTextForCitations(char[] symbols) {
        
        ArrayList<String> citationsList = new ArrayList<>();
        
        StringBuffer buff = null;
        boolean reading = false;
        int braceCount = 0;
        char current, next;
        
        for (int i = 0; i < symbols.length - 1; i++) {
            
            current = symbols[i];
            next = symbols[i + 1];
            
            if (current == '{') braceCount++;
            if (current == '}') braceCount--;
            
            if (current == '{' && next == '{') {
                
                buff = new StringBuffer();
                reading = true;
                braceCount++; // add second brace to count
                i++; // step over second brace
                
            } else if (current == '}' && next == '}') {
                
                String possibleCitation = buff.toString();
                String citationPrefix = "cite";
                
                if (possibleCitation.startsWith(citationPrefix)) {
                    String titlePrefix = "title";
                    String titlePostfix = "|";
                    
                    int titleStartIndex 
                            = possibleCitation.indexOf(titlePrefix)
                            + titlePrefix.length();
                    
                    int titleEndIndex = possibleCitation.indexOf(
                            titlePostfix,       // the character '|'
                            titleStartIndex);   // index after "title"
                    
                    if (titleStartIndex > 0 && titleEndIndex > 0) {
                        citationsList.add(possibleCitation.substring(
                            titleStartIndex,    // front of title
                            titleEndIndex));    // end of title
                    }
                }
                
                reading = false;
                
            } else if (reading) buff.append(current);
        }
        
        return citationsList;
    }
    
    private ArrayList<String> parseTextForAnchors(char[] symbols) {
        
        ArrayList<String> anchorsList = new ArrayList<>();
        
        StringBuffer buff = null;
        boolean reading = false;
        int braceCount = 0;
        char current, next;
        
        for (int i = 0; i < symbols.length - 1; i++) {
            
            current = symbols[i];
            next = symbols[i + 1];
            
            if (current == '{' && !reading) braceCount++;
            if (current == '}' && !reading) braceCount--;
            if (braceCount > 0) continue;
            
            if (current == '[' && next == '[') {
                
                buff = new StringBuffer();
                reading = true;
                i++; // step over second brace
                
            } else if (current == ']' && next == ']') {
                
                String possibleAnchor = buff.toString();
                String categoryPrefix = "Category:";
                
                if (possibleAnchor.startsWith(categoryPrefix)) {
                    // a category link, already parsed from text ...
                    // set reading to false and then continue ...
                    reading = false;
                    continue;
                }
                
                int bar = possibleAnchor.indexOf('|');
                if (bar > 0) {
                    anchorsList.add(possibleAnchor.substring(0, bar));
                } else {
                    anchorsList.add(possibleAnchor);
                }
                
                reading = false;
                
            } else if (reading) buff.append(current);
        }
        
        return anchorsList;
    }
    
    private String normalizeWikiPageTextForPOSTagging(char[] symbols) {
        
        StringBuffer buff = new StringBuffer();
        int braceCount = 0;
        char current;
        
        for (int i = 0; i < symbols.length; i++) {
            
            current = symbols[i];
            
            if (current == '{') braceCount++;
            if (current == '}') braceCount--;
            if (braceCount > 0) continue;
            
            int ascii = (int)current;
            boolean digit = (ascii >= (int)'0') && (ascii <= (int)'9');
            boolean upper = (ascii >= (int)'A') && (ascii <= (int)'Z');
            boolean lower = (ascii >= (int)'a') && (ascii <= (int)'z');
            boolean space = (ascii == (int)' ');
            
            if (digit || upper || lower || space) buff.append(current);
            
        }
        
        return buff.toString();
    }
}
