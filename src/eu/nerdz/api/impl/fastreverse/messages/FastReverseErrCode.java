/*
 * This file is part of NerdzApi-java.
 *
 *     NerdzApi-java is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation either version 3 of the License or
 *     (at your option) any later version.
 *
 *     NerdzApi-java is distributed in the hope that it will be useful
 *     but WITHOUT ANY WARRANTY without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with NerdzApi-java.  If not see <http://www.gnu.org/licenses/>.
 *
 *     (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
 */

package eu.nerdz.api.impl.fastreverse.messages;

/*
 This file is part of NerdzApi-java.

    NerdzApi-java is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation either version 3 of the License or
    (at your option) any later version.

    NerdzApi-java is distributed in the hope that it will be useful
    but WITHOUT ANY WARRANTY without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NerdzApi-java.  If not see <http://www.gnu.org/licenses/>.

    (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
*/



/**
 Error codes to be returned by FastFetch API.
 */
public enum FastReverseErrCode {

    /**
     * Unknown error code.
     */
    UNKNOWN(-0x1, "unknown error"),
    
    /**
     * Everything is good. Pretty useless.
     */
    NOTHING_WRONG(0x0, "everything is good"),
    
    /**
     * The user is not logged in.
     */
    NOT_LOGGED(0x1, "user not logged in"),
    
    /**
     * The user has not provided an action.
     */
    NO_ACTION(0x2, "no action provided to FastFetch"),
    
    /**
     * The user has provided an invalid or unknown action.
     */
    INVALID_ACTION(0x3, "invalid or unknown action"),
    
    /**
     * The server is not passing a good moment. Please leave him alone.
     */
    SERVER_FAILURE(0x4, "server is not really ok at the moment"),
    
    /**
     * The request is malformed.
     */
    WRONG_REQUEST(0x5, "malformed request"),
    
    /**
     * The user has not provided an user id to be used with the the given action.
     */
    NO_OTHER_ID(0x6, "no other id given"),
    
    /**
     * The user has provided a limit which is higher than the max.
     */
    LIMIT_EXCEEDED(0x7, "maximum limit exceeded");

    private int mErrorCode;
    private String mDescription;

    private FastReverseErrCode(int errorCode, String description) {

    }

    private int getErrorCode() {
        return this.mErrorCode;
    }

    @Override
    public String toString() {
        return "ErrCode " + this.mErrorCode + ": " + this.mDescription;
    }

    public static FastReverseErrCode fromCode(int errorCode) {
        for (FastReverseErrCode elem : FastReverseErrCode.values()){
            if (elem.mErrorCode == errorCode) {
                return elem;
            }
        }
        return FastReverseErrCode.UNKNOWN;
    }

}


