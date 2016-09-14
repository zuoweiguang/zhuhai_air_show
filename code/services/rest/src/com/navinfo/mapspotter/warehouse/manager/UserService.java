package com.navinfo.mapspotter.warehouse.manager;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.io.MongoDBCursor;
import com.navinfo.mapspotter.foundation.io.util.MongoOperator;
import com.navinfo.mapspotter.foundation.util.StringUtil;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;

import java.security.NoSuchAlgorithmException;

/**
 * Created by cuiliang on 2016/6/12.
 */
@Path("/user")
public class UserService extends ServiceAbstract{

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultView() {
        return "Hello, this is user service";
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces("text/plain;charset=UTF-8")
    public Response login(String json) {
        JSONObject output = new JSONObject();

        JSONObject input = JSONObject.parseObject(json);

        String username = input.getString("username");
        String password = input.getString("password");

        MongoOperator condition = new MongoOperator();

        condition.and(MongoOperator.FilterType.EQ, "username", username);
        condition.and(MongoOperator.FilterType.EQ, "password", StringUtil.encoderByMd5(password));
        if (null == username || "".equals(username)) {
            output.put("errcode", 1);
            output.put("errmsg", "username is null");
            return buildResponse(output.toJSONString());
        } else if (null == password || "".equals(password)) {
            output.put("errcode", 2);
            output.put("errmsg", "password is null");
            return buildResponse(output.toJSONString());
        } else {
            return buildResponse(query("user", condition));
        }
    }


    @GET
    @Path("/getUser")
    @Produces("text/plain;charset=UTF-8")
    public String getUser() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return "user";
    }

    public void insertUser(String username, String password){
        Document document = new Document();
        document.put("id",StringUtil.uuid().replace("-",""));
        document.put("username",username);
        document.put("password",StringUtil.encoderByMd5(password));
        database.insert("user", document);
    }
    public static void main(String args[]){
        UserService service = new UserService();
//        service.insertUser("info","info");
//        service.insertUser("mgw","mgw");
//        service.insertUser("cl","cl");
//        service.insertUser("xjl","xjl");
//        service.insertUser("zmr","zmr");
//        service.insertUser("gyk","gyk");
//        service.insertUser("shx","shx");
//        service.insertUser("zhy","zhy");
//        service.insertUser("sqh","sqh");
//        service.insertUser("zj","zj");
//        service.insertUser("ll","ll");
        service.insertUser("zhanghaiyan","zhanghaiyan");
//        System.out.println(service.login("{json: {username: \"admin\",password: \"admin\"}}"));
    }
}
