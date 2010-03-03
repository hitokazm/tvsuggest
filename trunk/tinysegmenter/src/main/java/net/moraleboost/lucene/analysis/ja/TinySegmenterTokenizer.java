/*
 **
 **  Mar. 24, 2009
 **
 **  The author disclaims copyright to this source code.
 **  In place of a legal notice, here is a blessing:
 **
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 **
 **                                         Stolen from SQLite :-)
 **  Any feedback is welcome.
 **  Kohei TAKETA <k-tak@void.in>
 **
 */
package net.moraleboost.lucene.analysis.ja;

import java.io.IOException;
import java.io.Reader;

import net.moraleboost.io.BasicCodePointReader;
import net.moraleboost.tinysegmenter.TinySegmenter;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttributeImpl;

/**
 * {@link TinySegmenter}��p���ē��͂𕪂�����������Tokenizer�B
 * 
 * @author taketa
 *
 */
public class TinySegmenterTokenizer extends Tokenizer
{
    private TinySegmenter segmenter = null;
    
    public TinySegmenterTokenizer(Reader in)
    {
        super(in);
        segmenter = new TinySegmenter(new BasicCodePointReader(in));
    }
    
    public Token next(Token reusableToken) throws IOException
    {
        TinySegmenter.Token baseToken = segmenter.next();
        
        if (baseToken == null) {
            return null;
        } else {
            return reusableToken.reinit(
                    baseToken.str,
                    (int)baseToken.start,
                    (int)baseToken.end);
        }
    }

    @Override
    public boolean incrementToken() throws IOException {
        TinySegmenter.Token baseToken = segmenter.next();

        if (baseToken == null) {
            return false;
        } else {
            clearAttributes();
            TermAttributeImpl term = new TermAttributeImpl();
            term.setTermBuffer(baseToken.str);
            addAttributeImpl(term);
            return true;
        }
    }
}
