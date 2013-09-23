import eu.nerdz.api.Nerdz;

public class SendMessage {

    public static void main(String[] args) {

        try {

			if (args.length != 4) {
				System.err.println("usage: <classinvocation> username password touser message");
				return;
			}

            Nerdz.getImplementation("reverse.Reverse").newMessenger(args[0], args[1]).sendMessage(args[2], args[3]);            

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
