/**
 *  StoreDraftService
 *  Copyright by Michael Christen, @0rb1t3r
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.susi.server.api.cms;

import ai.susi.DAO;
import ai.susi.json.JsonObjectWithDefault;
import ai.susi.server.APIException;
import ai.susi.server.APIHandler;
import ai.susi.server.AbstractAPIHandler;
import ai.susi.server.Authorization;
import ai.susi.server.Query;
import ai.susi.server.ServiceResponse;
import ai.susi.server.UserRole;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpServletResponse;

import java.util.Iterator;
import java.util.Random;


/**
 * This endpoint accepts either 2 parameters: id and object or only one parameter: object
 * If only the object is given, then a new ID is generated on-the-fly.
 * It is recommended to use only IDs generated by this API.
 * The parameter "object" must be a json object as string.
 * http://localhost:4000/cms/storeDraft.json?object={"test":"123"}
 * http://localhost:4000/cms/storeDraft.json?object={"tset":"123"}&id=aabbcc
 */
public class StoreDraftService extends AbstractAPIHandler implements APIHandler {


    private static final long serialVersionUID = -1960932190918215684L;
    private static final Random random = new Random(System.currentTimeMillis());
    
    @Override
    public UserRole getMinimalUserRole() {
        return UserRole.USER;
    }

    @Override
    public JSONObject getDefaultPermissions(UserRole baseUserRole) {
        return null;
    }

    @Override
    public String getAPIPath() {
        return "/cms/storeDraft.json";
    }

    @Override
    public ServiceResponse serviceImpl(Query call, HttpServletResponse response, Authorization authorization, final JsonObjectWithDefault permissions) throws APIException {

        JSONObject json = new JSONObject();
        JSONObject object = new JSONObject();

        object.put("category", call.get("category", ""));
        object.put("language", call.get("language", ""));
        object.put("name", call.get("name", ""));
        object.put("buildCode", call.get("buildCode", ""));
        object.put("designCode", call.get("designCode", ""));
        object.put("configCode", call.get("configCode", ""));
        object.put("image", call.get("image", ""));

        String id = call.get("id", "");

        Iterator<String> keys = object.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            String value = object.getString(key);
            // throw api exception if any of the keys have empty values
            if(value.length()==0) {
                throw new APIException(422, "some values of object are empty");
            }
        }
 
        if (id.length() == 0) {
        	// generate an id.
        	id = Integer.toHexString(random.nextInt(Integer.MAX_VALUE)).toLowerCase();
        }
        
        DAO.storeDraft(authorization.getIdentity(), id, new DAO.Draft(object));

        json.put("accepted", true);
        json.put("message", "draft stored");
        json.put("id", id);

        return new ServiceResponse(json);
    }
}
