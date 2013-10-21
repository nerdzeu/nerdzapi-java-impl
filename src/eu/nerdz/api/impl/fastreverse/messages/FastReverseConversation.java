package eu.nerdz.api.impl.fastreverse.messages;
/*
 This file is part of NerdzApiImpl.

    NerdzApiImpl is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NerdzApiImpl is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NerdzApiImpl.  If not, see <http://www.gnu.org/licenses/>.

    (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
*/

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;

import eu.nerdz.api.impl.reverse.messages.ReverseConversation;
import eu.nerdz.api.messages.Message;

public class FastReverseConversation extends ReverseConversation {

    private static final long serialVersionUID = -6815925809655654400L;

    private String mLastMessage;
    private boolean mLastWasOther;

    public FastReverseConversation(String userName, int userID, Date lastDate, String lastMessage, boolean lastWasOther, boolean newMessages) {
        super(userName, userID, lastDate);
        this.mLastMessage = lastMessage;
        this.mLastWasOther = lastWasOther;
        super.setHasNewMessages(newMessages);
    }

    public Pair<String,Boolean> getLastMessageInfo() {
        return new ImmutablePair<String, Boolean>(this.mLastMessage, this.mLastWasOther);
    }

    @Override
    public void updateConversation(Message message) {
        super.updateConversation(message);
        this.mLastMessage = message.getContent();
        this.mLastWasOther = message.received();
    }

}
