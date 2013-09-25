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

package eu.nerdz.api.impl.fastreverse.tests;

import eu.nerdz.api.Nerdz;

public class SendMessage {

    public static void main(String[] args) {

        try {

			if (args.length != 4) {
				System.err.println("usage: <classinvocation> username password touser message");
				return;
			}

            Nerdz.getImplementation("fastreverse.FastReverse").newMessenger(args[0], args[1]).sendMessage(args[2], args[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
