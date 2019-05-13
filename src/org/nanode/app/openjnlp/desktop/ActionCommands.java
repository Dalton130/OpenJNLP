/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/ 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is openjnlp.nanode.org code.
 *
 * The Initial Developer of the Original Code is Nanode LLC. Portions created by Nanode are
 * Copyright (C) 2001 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.app.openjnlp.desktop;


public interface ActionCommands {
    static final int CMD_NONE = -1;
    static final int CMD_MAX = 27;

    static final int CMD_QUIT = 0;
    static final int CMD_ABOUT = 1;
    static final int CMD_PREFS = 2;
    static final int CMD_UNDO = 3;
    static final int CMD_CUT = 4;
    static final int CMD_COPY = 5;
    static final int CMD_PASTE = 6;
    static final int CMD_DELETE = 7;
    static final int CMD_ALL = 8;
    static final int CMD_GET = 9;
    static final int CMD_INFO = 10;
    static final int CMD_RUN = 11;
    static final int CMD_CLOSE = 12;
    static final int CMD_ZOOM = 13;
    static final int CMD_MINI = 14;
    static final int CMD_FRONT = 15;
    static final int CMD_PAGE = 16;
    static final int CMD_PRINT = 17;
    static final int CMD_CONSOLE = 18;
    static final int CMD_NEW = 19;
    static final int CMD_OPEN = 20;
    static final int CMD_SAVE = 21;
    static final int CMD_SAVE_AS = 22;
    static final int CMD_REVERT = 23;
    static final int CMD_SETUP = 24;
    static final int CMD_CLEAR = 25;
    static final int CMD_ADD = 26;
}