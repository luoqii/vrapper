package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;

public interface Selection extends TextObject, TextRange {
    public boolean isReversed();
    public Position getFrom();
    public Position getTo();
    /** Returns the name of the mode in which this selection was made. */
    public String getModeName();
}
