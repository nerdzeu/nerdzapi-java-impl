package eu.nerdz.api.impl.reverse.tests;

import eu.nerdz.api.messages.Conversation;
import eu.nerdz.api.messages.ConversationHandler;
import eu.nerdz.api.messages.Message;
import eu.nerdz.api.messages.Messenger;
import eu.nerdz.api.Nerdz;

public class Relogin {

    public static void main(String[] args) {

        try {

			if (args.length != 2) {
				System.err.println("usage: <classinvocation> username password");
				return;
			}
				

			Nerdz nerdz = Nerdz.getImplementation("reverse.Reverse");

            String data = nerdz.serializeToString(nerdz.logAndGetInfo(args[0], args[1]));
			System.out.println(data + '\n');
			Messenger messenger = nerdz.newMessenger(nerdz.deserializeFromString(data));
            ConversationHandler conversationHandler = messenger.getConversationHandler();

            for (Conversation conversation : conversationHandler.getConversations()) {

                System.out.println(conversation.toString() + "\n");
                for(Message message : conversationHandler.getMessages(conversation))
                    System.out.println(message);
                System.out.println();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
