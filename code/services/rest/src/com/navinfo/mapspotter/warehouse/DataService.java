package com.navinfo.mapspotter.warehouse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.mapspotter.foundation.util.JsonUtil;
import com.navinfo.mapspotter.foundation.util.ZipFileUtil;
import com.navinfo.mapspotter.process.storage.crud.InfoMongoUpdate;
import com.navinfo.mapspotter.warehouse.connection.DBPool;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.*;

/**
 * Created by SongHuiXing on 6/24 0024.
 */
@Path("/data")
public class DataService {
    private InfoMongoUpdate infomationUpdator =
            new InfoMongoUpdate(DBPool.getInstance().getMongo().getMongo());

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDefaultData() {
        return "Hello, this is data service";
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/info/{id}")
    public JSONObject getInfoDetail(@PathParam("id") String infoId){
        return infomationUpdator.getInformation(infoId);
    }

    @POST
    @Consumes("text/plain;charset=UTF-8")
    @Produces("application/json")
    @Path("/info/upload")
    public String uploadInfomations(final InputStream in) throws IOException {
        int totalCount = 0, failCount = 0;

        String errmsg = "Failed to upload:";

        ArrayList<String> sucessids = new ArrayList<>();
        ArrayList<String> failedids = new ArrayList<>();
        ArrayList<String> wrongformats = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"))){

            String jsonLine = null;

            while(null != (jsonLine = br.readLine())){
                try{
                    JSONObject info = JSONObject.parseObject(jsonLine);

                    Map.Entry<String, Boolean> result;

                    if(info.containsKey("c_isAdopted")){
                        result = infomationUpdator.updateExtension(info);
                    } else {
                        result = infomationUpdator.insertInfo(info);
                    }

                    if(result.getValue()){
                        sucessids.add(result.getKey());
                    } else {
                        failedids.add(result.getKey());
                    }
                } catch (Exception e){
                    wrongformats.add(jsonLine);
                }
            }
        }

        JSONObject result = new JSONObject();
        result.put("sucess_ids", sucessids);
        result.put("failed_ids", failedids);
        result.put("wrong_format", wrongformats);

        return result.toJSONString();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/info/grid/{num}")
    public String getInfosFromGrid(@PathParam("num") String gridNum){
        return infomationUpdator.getInfomation(gridNum).toJSONString();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/info/grids")
    public String getInfosFromGrids(@QueryParam("grids") String gridNums,
                                    @Context ServletContext context){

        JSONObject res = new JSONObject();

        try {
            List<String> grids = JsonUtil.getInstance().readStringArray(gridNums);

            StringBuilder stringBuilder = new StringBuilder();
            for (String grid : grids){
                JSONArray infos = infomationUpdator.getInfomation(grid);
                if(infos.size() == 0)
                    continue;

                stringBuilder.append(infos.toJSONString());
                stringBuilder.append('\n');
            }

            InputStream in = context.getResourceAsStream("/fileresource.properties");
            Properties properties = new Properties();
            properties.load(in);

            String realpath = properties.getProperty("info_zipfile");
            String virtualPath = properties.getProperty("URL_InfoBase");

            ByteArrayInputStream inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));

            Date now = new Date();
            String zipfile = now.getTime() + ".zip";

            ZipFileUtil.zip(realpath + "/" + zipfile,
                            inputStream,
                            "info.json");

            res.put("errcode", 0);
            res.put("errmsg", "sucess");
            res.put("data", virtualPath + "/" + zipfile);
        } catch (Exception e) {
            res.put("errcode", -1);
            res.put("errmsg", e.getMessage());
            res.put("data", "");
        }

        return res.toJSONString();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("/info/grids/hasinfo")
    public String queryGridsStatus(@QueryParam("grids") String gridNums){

        JSONObject res = new JSONObject();

        try {
            List<String> grids = JsonUtil.getInstance().readStringArray(gridNums);

            Map<String, Boolean> status = new HashMap<>();

            StringBuilder stringBuilder = new StringBuilder();
            for (String grid : grids){
                boolean has = infomationUpdator.isThereAvaliableInfo(grid);

                status.put(grid, has);
            }

            res.put("errcode", 0);
            res.put("errmsg", "sucess");
            res.put("data", status);
        } catch (Exception e) {
            res.put("errcode", -1);
            res.put("errmsg", e.getMessage());
            res.put("data", "");
        }

        return res.toJSONString();
    }
}
