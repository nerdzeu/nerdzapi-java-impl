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

import java.io.IOException;

import eu.nerdz.api.HttpException;
import eu.nerdz.api.UserInfo;
import eu.nerdz.api.WrongUserInfoTypeException;
import eu.nerdz.api.impl.reverse.messages.ReverseMessenger;

public class FastReverseMessenger extends ReverseMessenger {

    public FastReverseMessenger(String user, String password) throws IOException, HttpException {
        super(user, password);
        this.setHandler(new FastReverseConversationHandler(this));
    }

    public FastReverseMessenger(UserInfo loginData) throws WrongUserInfoTypeException {
        super(loginData);
        this.setHandler(new FastReverseConversationHandler(this));
    }

}
