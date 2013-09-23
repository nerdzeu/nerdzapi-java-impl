import eu.nerdz.api.Nerdz;
import eu.nerdz.api.messages.Conversation;
import eu.nerdz.api.messages.ConversationHandler;
import eu.nerdz.api.messages.Messenger;

public class OneConversation {

    public static void main(String[] args) {

        try {

			if (args.length != 2) {
				System.err.println("usage: <classinvocation> username password");
				return;
			}
				

            Messenger messenger = Nerdz.getImplementation("reverse.Reverse").newMessenger(args[0], args[1]);
            ConversationHandler conversationHandler = messenger.getConversationHandler();

            for (Conversation conversation : conversationHandler.getConversations()) {

                System.out.println(conversation.toString() + "\n");
                System.out.println(conversationHandler.getLastMessage(conversation).toString() + '\n');

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
