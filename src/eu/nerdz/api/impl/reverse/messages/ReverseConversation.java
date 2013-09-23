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

import eu.nerdz.api.messages.Conversation;

class ReverseConversation implements Conversation {

    /**
     *
     */
    private static final long serialVersionUID = -6815925809655616400L;
    private final String mOther;
    private int mUserID;
    private Date mLastDate;

    public ReverseConversation(String userName, int userID, Date lastDate) {
        this.mOther = userName;
        this.mUserID = userID;
        this.mLastDate = lastDate;
    }

    @Override
    public int getOtherID() {

        return this.mUserID;

    }

    @Override
    public String getOtherName() {
        return this.mOther;
    }

    @Override
    public Date getLastDate() {
        return this.mLastDate;
    }

    @Override
    public String toString() {
        return this.mOther + " (" + this.mUserID + ") , last contact on " + this.mLastDate;
    }
}
