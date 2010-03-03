/*
 **
 **  Feb. 1, 2009
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
package net.moraleboost.io;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * Reader�����b�v���āA�ЂƂ���Unicode�R�[�h�|�C���g��
 * �擾���邽�߂̃f�R���[�^�N���X�B�T���Q�[�g�y�A�𐳂����F������B<br>
 * 
 * �s���ȃT���Q�[�g�y�A�́A{@link #getAlternativeCodePoint()}�œ�����
 * ��փR�[�h�|�C���g�ɒu�������B
 */
public class BasicCodePointReader implements CodePointReader
{
    /**
     * �s���ȃT���Q�[�g�y�A��u�����镶���̊���l�B
     */
    public static final int DEFAULT_ALTERNATION_CODEPOINT = '��';

    private PushbackReader reader = null;
    private long position = 0;
    private int alternationCodePoint = DEFAULT_ALTERNATION_CODEPOINT;
    private boolean eos = false;

    /**
     * �R�[�h�|�C���g�C�e���[�^���\�z����B
     * 
     * @param sequence
     *            �\�[�X�ƂȂ�char�̃V�[�P���X
     */
    public BasicCodePointReader(Reader reader)
    {
        this.reader = new PushbackReader(reader, 1);
    }

    public void setAlternationCodePoint(int cp)
    {
        this.alternationCodePoint = cp;
    }

    public int getAlternationCodePoint()
    {
        return alternationCodePoint;
    }

    public long getPosition()
    {
        return position;
    }

    public int read() throws IOException
    {
        int ci;
        char c, c2;

        if (eos) {
            return -1;
        }

        ci = reader.read();
        ++position;

        if (ci < 0) {
            // end of character stream
            return -1;
        } else {
            c = (char)ci;
        }

        if (Character.isHighSurrogate(c)) {
            // ���̕���������
            ci = reader.read();
            ++position;
            if (ci < 0) {
                // �V�[�P���X��high surrogate�ŏI����Ă���B
                // ��֕�����Ԃ��Ƌ��ɁAEOS�t���O��ON�ɂ���B
                eos = true;
                --position;
                return alternationCodePoint;
            }

            c2 = (char)ci;
            if (Character.isLowSurrogate(c2)) {
                // �T���Q�[�g�y�A���R�[�h�|�C���g�ɕϊ����ĕԂ��B
                return Character.toCodePoint(c, c2);
            } else {
                // high surrogate�ɑ���char���Alow surrogate�łȂ��B
                // c2���v�b�V���o�b�N���đ�֕�����Ԃ��B
                reader.unread(c2);
                --position;
                return alternationCodePoint;
            }
        } else if (Character.isLowSurrogate(c)) {
            // �P�Ƃő��݂���low surrogate�𔭌��B
            // ��֕�����Ԃ��B
            return alternationCodePoint;
        } else {
            // ��{�����B���̂܂ܕԂ��B
            return c;
        }
    }
}
