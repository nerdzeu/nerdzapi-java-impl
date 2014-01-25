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


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

import eu.nerdz.api.BadStatusException;
import eu.nerdz.api.ContentException;
import eu.nerdz.api.HttpException;
import eu.nerdz.api.LoginException;
import eu.nerdz.api.UserInfo;
import eu.nerdz.api.UserNotFoundException;
import eu.nerdz.api.WrongUserInfoTypeException;
import eu.nerdz.api.impl.reverse.AbstractReverseApplication;
import eu.nerdz.api.messages.ConversationHandler;
import eu.nerdz.api.messages.Message;
import eu.nerdz.api.messages.Messenger;

/**
 * Reverse implementation of eu.nerdz.api.messages.Messenger.
 */
public class ReverseMessenger extends AbstractReverseApplication implements Messenger {

    /**
     *
     */
    private static final long serialVersionUID = -1627978816568946110L;
    //The only ConversationHandler of the instance.
    private ConversationHandler mConversationHandler;

    /**
     * Creates a ReverseMessenger, initializing the underlining AbstractReverseApplication.
     *
     * @param user     the username
     * @param password the password
     * @throws IOException
     * @throws HttpException
     * @throws LoginException
     */
    public ReverseMessenger(String user, String password) throws IOException, HttpException {
        super(user, password);
        this.setHandler(new ReverseConversationHandler(this));
    }

    /**
     * Creates a ReverseMessenger from existing login data, initializing the underlining AbstractReverseApplication.
     *
     * @param loginData existing login data
     * @throws WrongUserInfoTypeException if loginData is not an instance of AbstractReverseApplication.ReverseUserInfo
     */
    public ReverseMessenger(UserInfo loginData) throws WrongUserInfoTypeException {
        super(loginData);
        this.setHandler(new ReverseConversationHandler(this));
    }

    //Setting the handler
    protected void setHandler(ConversationHandler handler) {

        //The mConversationHandler instance is set.
        this.mConversationHandler = handler;
    }

    @Override
    public ConversationHandler getConversationHandler() {
        return this.mConversationHandler;
    }

    @Override
    public Message sendMessage(String to, String message) throws IOException, HttpException, ContentException, BadStatusException, UserNotFoundException {

        HashMap<String, String> form = new HashMap<String, String>(2);

        form.put("to", to);
        form.put("message", message);

        try {
            JSONObject response = new JSONObject(this.post("/pages/pm/send.json.php", form, AbstractReverseApplication.NERDZ_DOMAIN_NAME + "/pm.php"));

            if (!(response.getString("status").equals("ok") && response.getString("message").equals("OK"))) {
                throw new BadStatusException("wrong status couple (" + response.getString("status") + ", " + response.getString("message") + "), message not sent");
            }
        } catch (JSONException e) {
            throw new ContentException("Error while parsing JSON in new messages: " + e.getLocalizedMessage());
        }

        Date now = new Date();

        return new ReverseMessage(new ReverseConversation(to, this.getUserIdForName(to), now), this.getUserInfo(), now, message, false);

    }

    @Override
    public int newMessages() throws IOException, HttpException, ContentException {
        try {
            return new JSONObject(this.get("/pages/pm/notify.json.php")).getInt("message");
        } catch (JSONException e) {
            throw new ContentException("Error while parsing JSON in new messages: " + e.getLocalizedMessage());
        }
    }

    //here you can see some fastfetch awesomeness.
    @Override
    public int getUserIdForName(String userName) throws UserNotFoundException, IOException, HttpException, ContentException {

        int result;

        try {
            result = (new JSONObject(this.get("/fastfetch.json.php?action=getid&username=" + URLEncoder.encode(userName, "UTF-8")))).getInt("id");
        } catch (JSONException e) {
           throw new ContentException("Error while parsing JSON in new messages: " + e.getLocalizedMessage());
        }
        return result;
    }

    @Override
    public Features[] getSupportedFeatures() {
        return new Features[]{ Features.MESSENGER };
    }

}
