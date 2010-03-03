/*
 **
 **  Feb. 17, 2009
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

/**
 * �����Unicode�R�[�h�|�C���g���擾���邽�߂̃N���X�B
 * �T���Q�[�g�y�A�𐳂����F������B
 */
public interface CodePointReader
{
    /**
     * �s���ȃT���Q�[�g�y�A��u�����邽�߂̑�֕������Z�b�g����B
     * ���̃��\�b�h���Ăяo���Ȃ��ꍇ�̊���l�́A
     * �u{@value #DEFAULT_ALTERNATIVE_CODEPOINT}�v�ł���B
     * 
     * @param cp
     *            ��֕����̃R�[�h�|�C���g
     */
    public abstract void setAlternationCodePoint(int cp);

    /**
     * �s���ȃT���Q�[�g�y�A��u�����邽�߂̑�֕������擾����B
     * 
     * @return ��֕����̃R�[�h�|�C���g
     */
    public abstract int getAlternationCodePoint();

    /**
     * �L�����N�^�X�g���[�����̌��݂̈ʒu��Ԃ��B
     * �R�[�h�|�C���g�P�ʂłȂ�char�P�ʂŐ�����̂ŁA
     * �T���Q�[�g�y�A���o������ƁA�ʒu��2�傫���Ȃ�B
     * 
     * @return �L�����N�^�X�g���[�����̈ʒu�B
     */
    public abstract long getPosition();

    /**
     * ���̃R�[�h�|�C���g���擾����B
     * 
     * @return Unicode�R�[�h�|�C���g�B
     */
    public abstract int read() throws IOException;
}
