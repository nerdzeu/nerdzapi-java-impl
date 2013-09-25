/*
 * This file is part of NerdzApi-java.
 *
 *     NerdzApi-java is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     NerdzApi-java is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with NerdzApi-java.  If not, see <http://www.gnu.org/licenses/>.
 *
 *     (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
 */

package eu.nerdz.api.impl.fastreverse.messages;

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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nerdz.api.ContentException;
import eu.nerdz.api.HttpException;
import eu.nerdz.api.impl.reverse.messages.ReverseConversationHandler;
import eu.nerdz.api.impl.reverse.messages.ReverseMessenger;
import eu.nerdz.api.messages.Conversation;
import eu.nerdz.api.messages.Message;

public class FastReverseConversationHandler extends ReverseConversationHandler {


    public FastReverseConversationHandler(ReverseMessenger messenger) {
        super(messenger);
    }

    @Override
    public List<Conversation> getConversations() throws IOException, HttpException, ContentException {

        String response = this.mMessenger.get("/fastfetch.json.php?action=conversations");

        try {

            JSONObject errorObj = new JSONObject(response); //if this raises an exception, it's not an error response
            throw new ContentException(FastReverseErrCode.fromCode(errorObj.getInt("error")).toString());


        } catch (JSONException e) {

            try {

                JSONArray jsonResponse;

                jsonResponse = new JSONArray(response);

                int length = jsonResponse.length();

                List<Conversation> conversations = new ArrayList<Conversation>(length);

                for (int i = 0; i < length; ++i) {

                    JSONObject conversation = jsonResponse.getJSONObject(i);

                    conversations.add(new FastReverseConversation(

                            conversation.getString("name"),
                            conversation.getInt("id"),
                            new Date(conversation.getLong("last_timestamp") * 1000L),
                            FastReverseConversationHandler.removeBbcode(conversation.getString("last_message")),
                            conversation.getInt("last_sender") != this.mMessenger.getUserID()

                    ));
                }

                return conversations;
            } catch (JSONException e1) {

                throw new ContentException("Invalid json response from FastFetch");

            }
        }

    }

    @Override
    protected Pair<List<Message>, Boolean> getMessagesAndCheck(Conversation conversation, int start, int howMany) throws IOException, HttpException, ContentException {

        if (howMany > 30) {
            howMany = 30;
        }

        if (start == 0 && howMany == 1 && conversation instanceof FastReverseConversation) { //FastReverseConversations contain the content of the last message - so we take this fast path.

            Pair<String,Boolean> lastInfo = ((FastReverseConversation) conversation).getLastMessageInfo();

            boolean lastWasOther = lastInfo.getRight();

            List<Message> lastMessage = new ArrayList<Message>(1);
            lastMessage.add( new FastReverseMessage(

                                    lastWasOther ? conversation.getOtherName() : this.mMessenger.getUsername(),
                                    lastInfo.getLeft(),
                                    lastWasOther ? conversation.getOtherID() : this.mMessenger.getUserID(),
                                    conversation.getLastDate(),
                                    false

            ));

            return new ImmutablePair<List<Message>, Boolean>(lastMessage,false);
        }

        String response = this.mMessenger.get("/fastfetch.json.php?action=messages&otherid=" + conversation.getOtherID() + "&start=" + start + "&limit=" + (howMany + 1));

        try {

            JSONObject errorObj = new JSONObject(response); //if this raises an exception, it's not an error response
            throw new ContentException(FastReverseErrCode.fromCode(errorObj.getInt("error")).toString());


        } catch (JSONException e) {

            try {

                JSONArray jsonResponse = new JSONArray(response);

                int length = jsonResponse.length();

                boolean hasMore = length > howMany; //if howMany is n, previously i've tried to fetch n + 1; if the number of elements fetched is really n + 1 this means that there are more messages.

                length = hasMore ? howMany : length;

                LinkedList<Message> conversationList = new LinkedList<Message>();

                for (int i = 0; i < length; ++i) {

                    JSONObject conversationJson = jsonResponse.getJSONObject(i);

                    boolean sent = conversationJson.getBoolean("sent");

                    conversationList.addFirst(new FastReverseMessage(
                            sent ? this.mMessenger.getUsername() : conversation.getOtherName(),
                            FastReverseConversationHandler.removeBbcode(conversationJson.getString("message")),
                            sent ? this.mMessenger.getUserID() : conversation.getOtherID(),
                            new Date(conversationJson.getLong("timestamp") * 1000L),
                            conversationJson.getBoolean("read")
                    ));
                }

                return new ImmutablePair<List<Message>, Boolean>(conversationList, hasMore);
            } catch (JSONException e1) {

                e1.printStackTrace();

                throw new ContentException("Invalid json response from FastFetch");

            }
        }

    }

    private static String removeBbcode(String message) {
        return FastReverseConversationHandler.removeImages(FastReverseConversationHandler.removeYoutubeVideos(message));
    }

    private static String removeSingle(String regex, String message) {

        Matcher matcher = Pattern.compile(regex,Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result,matcher.group(1));
        }

        matcher.appendTail(result);

        return result.toString();
    }

    private static String removeImages(String message) {
        return FastReverseConversationHandler.removeSingle("\\[img\\](.*?)\\[/img\\]", message);
    }

    private static String removeYoutubeVideos(String message) {
        return FastReverseConversationHandler.removeSingle(
                "\\[yt\\](.*?)\\[/yt\\]",
                FastReverseConversationHandler.removeSingle(
                        "\\[youtube\\](.*?)\\[/youtube\\]",
                        message
                )

        );
    }

}
