import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserTool {
    private final JSONArray jsonArray;
    private static String splitChar=" |,|.|:|，|。|："; //用于分割的字符

    //读取json地址文件
    public ParserTool(String filename){
        String data=readFileData(filename,"gbk");
        jsonArray=new JSONArray(data);
    }

    //文件转String
    private String readFileData(String filename,String charSetName){
        try {
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(filename),charSetName));
            StringBuffer buffer=new StringBuffer();
            String line=null;
            while ((line=reader.readLine())!=null){
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //移除用于分割的字符
    private String replaceSplitChar(String data){
        String[]split=splitChar.split("\\|");
        for (String s :split){
            data=data.replace(s,"");
        }
        return data;
    }

    //地址补全,优先匹配有省份的情况
    //无法补全只有镇的情况
    //每个项目用空格分割
    //返回json信息
    private String[] parserLocation(String data){
        Pattern p = Pattern.compile("["+splitChar+"]"+"[\u4e00-\u9fa5|\\d|a-z|A-Z]{8,50}"+"["+splitChar+"]");
        Matcher m = p.matcher(data);
        if(m.find())data= replaceSplitChar(m.group());
        String location=findProvince(data);
        if(location==null) location=findNoProvince(data);
        if (location==null)return null;
        return location.split(" ");
    }

    //根据补全的地址解析详细地址
    private String parserDetail(String data,String[] location){
        if(location==null)return null;
        String index=location[location.length-1];
        int pos = data.indexOf(index);
        if (pos==-1)return null;
        pos+=index.length();
        String sub=data.substring(pos,pos+index.length());
        Pattern p = Pattern.compile(sub+".*?["+splitChar+"]");
        Matcher m = p.matcher(data);
        sub=null;
        if(m.find()) sub=replaceSplitChar(m.group());
        return sub;
    }

    //如果地址以省份开头
    //省+市 返回 省+市
    //省+县 返回 省+市+县
    //省+市+县 返回 省+市+县
    //省+市+镇 返回 省+市+县+镇
    //省+市+县+镇 返回 省+市+县+镇
    private String findProvince(String location){
        for (int i=0;i<jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String province =jsonObject.getString("name");
            //必须以省份开头
            if(location.startsWith(province)){
                JSONArray cityArray=jsonObject.getJSONArray("children");
                for (int j =0;j<cityArray.length();j++){
                    JSONObject cityObject = cityArray.getJSONObject(j);
                    String city = cityObject.getString("name");
                    JSONArray countyArray=cityObject.getJSONArray("children");
                    for (int k =0 ;k<countyArray.length();k++){
                        JSONObject countyObject = countyArray.getJSONObject(k);
                        String county=countyObject.getString("name");
                        JSONArray townArray=countyObject.getJSONArray("children");
                        for (int l=0;l<townArray.length();l++){
                            JSONObject townObject = townArray.getJSONObject(l);
                            String town =townObject.getString("name");
                            //省+市+县+镇
                            //省+市+镇
                            //省+县+镇
                            //返回省+市+县+镇
                            if(location.startsWith(province+city+county+town)
                                    ||location.startsWith(province+city+town)
                                    ||location.startsWith(province+county+town)
                            ){
                                return province+" "+city+" "+county+" "+town;
                            }
                        }

                        //省+市+县
                        //省+县
                        //返回省+市+县
                        if(location.startsWith(province+city+county)||
                                location.startsWith(province+county)) {
                            return province+" "+city+" "+county;
                        }
                    }
                    //省+市,不需要补全,返回省+市
                    if(location.startsWith(province+city)) return province+" "+city;
                }
            }
        }
        return null;
    }

    //如果地址非省份开头
    //市 返回 省+市
    //县 返回 省+市+县
    //市+县 返回 省+市+县
    //市+镇 返回 省+市+县+镇
    //市+县+镇 返回 省+市+县+镇
    private String findNoProvince(String location){
        for (int i=0;i<jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String province =jsonObject.getString("name");
            JSONArray cityArray=jsonObject.getJSONArray("children");
            for (int j =0;j<cityArray.length();j++){
                JSONObject cityObject = cityArray.getJSONObject(j);
                String city = cityObject.getString("name");
                JSONArray countyArray=cityObject.getJSONArray("children");
                for (int k =0 ;k<countyArray.length();k++){
                    JSONObject countyObject = countyArray.getJSONObject(k);
                    String county=countyObject.getString("name");
                    JSONArray townArray=countyObject.getJSONArray("children");
                    for (int l=0;l<townArray.length();l++){
                        JSONObject townObject = townArray.getJSONObject(l);
                        String town =townObject.getString("name");
                        //市+县+镇
                        //市+镇
                        //县+镇
                        //返回市+县+镇
                        if(location.startsWith(city+county+town)
                                ||location.startsWith(city+town)
                                ||location.startsWith(county+town)

                        ){
                            return province+" "+city+" "+county+" "+town;
                        }
                    }

                    //市+县
                    //县
                    //返回省+市+县
                    if(location.startsWith(city+county)||
                            location.startsWith(county)) {
                        return province+" "+city+" "+county;
                    }
                }
                //市,返回省+市
                if(location.startsWith(city)) return province+" "+city;
            }
        }
        return null;
    }

    //解析手机号码
    //1+10位数字
    private String parserMobile(String data) {
        Pattern p = Pattern.compile("["+splitChar+"]"+"1\\d{10}"+"["+splitChar+"]");
        Matcher m = p.matcher(data);
        if(m.find())return replaceSplitChar(m.group());
        return null;
    }

    //解析座机号码
    //优先级:
    //4位-7位
    //3位-8位
    //7-8位
    private String parserPhone(String data) {
        Pattern p = Pattern.compile("["+splitChar+"]"+"\\d{4}-"+"\\d{7}"+"["+splitChar+"]");
        Matcher m = p.matcher(data);
        if(m.find())return replaceSplitChar(m.group());

        p=Pattern.compile("["+splitChar+"]"+"\\d{3}-"+"\\d{8}"+"["+splitChar+"]");
        m =p.matcher(data);
        if(m.find())return replaceSplitChar(m.group());

        p=Pattern.compile("["+splitChar+"]"+"\\d{7,8}"+"["+splitChar+"]");
        m =p.matcher(data);
        if(m.find())return replaceSplitChar(m.group());

        return null;
    }

    //解析姓名
    //2-4位汉字
    private String parserName(String data){
        Pattern p = Pattern.compile("["+splitChar+"]"+"[\u4e00-\u9fa5]{2,4}"+"["+splitChar+"]");
        Matcher m = p.matcher(data);
        if(m.find())return replaceSplitChar(m.group());
        return null;
    }

    //返回最终解析结果
    public String parser(String data){
        data=" "+data+" ";
        String[] location=parserLocation(data);
        String detail=parserDetail(data,location);
        String name =parserName(data);
        String mobile=parserMobile(data);
        String phone=parserPhone(data);
        return (new ParserData(location,detail,name,mobile,phone)).toString();
    }
}

class ParserData{
    private String[] location;//地址数组
    private String detail;//详细地址
    private String name;//姓名
    private String mobile;//座机
    private String phone;//手机
    public ParserData(){}
    public ParserData(String[] location, String detail, String name, String mobile, String phone) {
        this.location = location;
        this.detail = detail;
        this.name = name;
        this.mobile = mobile;
        this.phone = phone;
    }

    public String[] getLocation() {
        return location;
    }

    public void setLocation(String[] location) {
        this.location = location;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        Gson gson=new Gson();
        return gson.toJson(this);
    }
}

