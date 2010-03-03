/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.moraleboost.lucene.analysis.ja;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

/**
 *
 * @author kariya
 */
public class TinySegmenterAnalyzer extends Analyzer {

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return new TinySegmenterTokenizer(reader);
    }

}
