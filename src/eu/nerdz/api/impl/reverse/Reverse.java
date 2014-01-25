package eu.nerdz.api.impl.reverse;

import java.io.IOException;

import eu.nerdz.api.Application;
import eu.nerdz.api.Application.Features;
import eu.nerdz.api.HttpException;
import eu.nerdz.api.LoginException;
import eu.nerdz.api.Nerdz;
import eu.nerdz.api.UserInfo;
import eu.nerdz.api.WrongUserInfoTypeException;
import eu.nerdz.api.impl.reverse.AbstractReverseApplication.ReverseUserInfo;
import eu.nerdz.api.impl.reverse.messages.ReverseMessenger;
import eu.nerdz.api.messages.Messenger;

/**
 * Reverse Nerdz implementation.
 * This instance can be instantiated with eu.nerdz.api.Nerdz.getImplementation("reverse.Reverse");
 */

@SuppressWarnings("unused")
class Reverse extends Nerdz {

    @SuppressWarnings("unused")
    public Reverse() {}

    @Override
    public String serializeToString(UserInfo userInfo) throws WrongUserInfoTypeException {

        ReverseUserInfo reverseUserInfo = (ReverseUserInfo)  userInfo;

        return reverseUserInfo.getUsername() + ' ' + reverseUserInfo.getNerdzIdCookie().getValue() + ' ' + reverseUserInfo.getNerdzU();
    }

    @Override
    public UserInfo deserializeFromString(String data) throws WrongUserInfoTypeException {

        String[] fields = new String[3];

        int tok = data.lastIndexOf(' '), precTok = tok;

        //Why? Because the string is serialized in format "name id token". If name is a letter, and id a single digit,
        //then the shortest valid string possibile is something like "a 1 c". Observe that the last space is in position 3.
        if (tok < 3) {
            throw new WrongUserInfoTypeException("Data passed is not a Reverse serialization");
        }

        fields[2] = data.substring(tok + 1);

        tok = data.lastIndexOf(' ', precTok - 1);

        //Same as before, now it must be at least at position 1.
        if (tok < 1) {
            throw new WrongUserInfoTypeException("Data passed is not a Reverse serialization");
        }

        fields[1] = data.substring(tok + 1, precTok);

        fields[0] = data.substring(0, tok);

        try {
            Integer.parseInt(fields[1]);
        } catch (NumberFormatException e) {
            throw new WrongUserInfoTypeException("Not numeric userID; this is not a Reverse serialization");
        }

        return new ReverseUserInfo(fields[0], fields[1], fields[2]);
    }

    @Override
    public UserInfo logAndGetInfo(String userName, String password) throws IOException, HttpException, LoginException {
        return new ReverseMessenger(userName, password).getUserInfo();
    }

    @Override
    public Application newApplication(UserInfo userInfo, Features... instanceFeatures) throws IOException, HttpException, LoginException, WrongUserInfoTypeException {
        if (instanceFeatures.length == 1 && instanceFeatures[0] == Features.MESSENGER) {
            Messenger instance = new ReverseMessenger(userInfo);
            instance.checkValidity();
            return instance;
        } else {
            throw new UnsupportedOperationException("Only MESSENGER is supported by Reverse at the moment. Sorry.");
        }
    }

    @Override
    public Application restoreApplication(UserInfo userInfo, Features... instanceFeatures) throws WrongUserInfoTypeException {
        if (instanceFeatures.length == 1 && instanceFeatures[0] == Features.MESSENGER) {
            return new ReverseMessenger(userInfo);
        } else {
            throw new UnsupportedOperationException("Only MESSENGER is supported by Reverse at the moment. Sorry.");
        }
    }

}
