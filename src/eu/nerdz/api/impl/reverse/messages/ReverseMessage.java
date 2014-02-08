/*
 This file is part of NerdzApi-java.

    NerdzApi-java is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NerdzApi-java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NerdzApi-java.  If not, see <http://www.gnu.org/licenses/>.

    (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
*/

package eu.nerdz.api.impl.reverse.messages;

import java.util.Date;

import eu.nerdz.api.UserInfo;
import eu.nerdz.api.messages.Conversation;
import eu.nerdz.api.messages.Message;

/**
 * Created by marco on 7/18/13.
 */
public class ReverseMessage implements Message {

    /**
     *
     */
    private static final long serialVersionUID = 2838062641195124239L;
    private final String mContent;
    private final ReverseConversation mConversation;
    private final UserInfo mThisUserInfo; //Info about the user logged on the device on which this message has been created
    private final Date mDate;
    private final boolean mReceived;

    /**
     * Creates a ReverseMessage.
     * @param conversation a Conversation in which this message must be used. Must be castable to Reverse
     * @param thisUserInfo UserInfo about the current user
     * @param date date of creation/reception of this message
     * @param content a message
     */
    public ReverseMessage(Conversation conversation, UserInfo thisUserInfo, Date date, String content, boolean received) {

        this.mConversation = (ReverseConversation) conversation;
        this.mContent = content;
        this.mThisUserInfo = thisUserInfo;
        this.mDate = date;
        this.mReceived = received;

        if (conversation.getLastDate().compareTo(date) == -1) {
            conversation.updateConversation(this);
        }
    }

    @Override
    public Conversation thisConversation() {
        return this.mConversation;
    }

    @Override
    public boolean received() {
        return false;
    }

    @Override
    public boolean read() {
        return false;
    }

    @Override
    public String getContent() {
        return this.mContent;
    }

    @Override
    public Date getDate() {
        return this.mDate;
    }

    @Override
    public String toString() {
        return (this.mReceived ? this.mConversation.getOtherName() : this.mThisUserInfo.getUsername()) + " (" + this.mDate + "): " + this.mContent;
    }
}
