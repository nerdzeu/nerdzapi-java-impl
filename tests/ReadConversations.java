import eu.nerdz.api.messages.Conversation;
import eu.nerdz.api.messages.ConversationHandler;
import eu.nerdz.api.Nerdz;
import eu.nerdz.api.messages.Message;
import eu.nerdz.api.messages.Messenger;

public class ReadConversations {

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
                for(Message message : conversationHandler.getMessages(conversation))
                    System.out.println(message);
                System.out.println();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
