package service;

import data.TicketEntity;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 打印动车或高铁的余票信息
 */
public class TicketQueryService {

    /**
     * 出发站 VBB代表哈尔滨西站
     */
    private static String From_Station = "CTH";

    /**
     * 终点站 GZQ代表广州东
     */
    private static String To_Station = "HGH";

    /**
     * 想要乘坐的列车号
     */
    private static String TrainNo = "G7678";

    /**
     * 出发的时间
     */
    private static String Date = "2019-06-30";

    /**
     * 成人票
     */
    private static String Purpose_codes = "ADULT";


    private static CloseableHttpResponse getHttpResponse() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建参数队列
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("leftTicketDTO.train_date", Date));
        params.add(new BasicNameValuePair("leftTicketDTO.from_station", From_Station));
        params.add(new BasicNameValuePair("leftTicketDTO.to_station", To_Station));
        params.add(new BasicNameValuePair("purpose_codes", Purpose_codes));

        //参数转换为字符串
        String paramsStr = EntityUtils.toString(new UrlEncodedFormEntity(params, "UTF-8"));
        String url = "https://kyfw.12306.cn/otn/leftTicket/query" + "?" + paramsStr;
        System.out.println(url);
        // 创建httpget
        HttpGet httpget = new HttpGet(url);
        // 执行get请求.
        return httpclient.execute(httpget);
    }

    /**
     * 发送 get请求
     */
    private static String doGet() throws IOException {
        final CloseableHttpResponse response = getHttpResponse();
        // 获取响应实体
        HttpEntity entity = response.getEntity();
        // 打印响应状态
        String code = response.getStatusLine().toString();
        if (entity != null) {
            // 打印响应内容
            String allData = EntityUtils.toString(entity);
            JSONObject json = new JSONObject(allData);
            String data = json.get("data").toString();
            JSONObject jsonOfData = new JSONObject(data);
            String resultOfData = jsonOfData.get("result").toString();
            String[] lineRecords = resultOfData.split("\\,");
            System.out.println(" ----> " + lineRecords.length);
            for (final String lineRecord : lineRecords) {
                String[] message = lineRecord.split("\\|");
                if (message.length < 10) {
                    System.out.println(message);
                    continue;
                }
                TicketEntity oneLineTicketEntity = new TicketEntity();
                oneLineTicketEntity.setTrainNo(message[3]);
                oneLineTicketEntity.setStartStation(message[6]);
                oneLineTicketEntity.setDestStation(message[7]);
                oneLineTicketEntity.setStartTime(message[8]);
                oneLineTicketEntity.setCostTime(message[10]);
                oneLineTicketEntity.setStartDate(message[13]);
                oneLineTicketEntity.setArrTime(message[9]);
                oneLineTicketEntity.setNoSeat(message[26]);
                oneLineTicketEntity.setSolfSleep(message[23]);
                oneLineTicketEntity.setHardSeat(message[29]);
                oneLineTicketEntity.setHardSleep(message[28]);
                oneLineTicketEntity.setSecondClassSeat(message[30]);
                oneLineTicketEntity.setFirstClassSeat(message[31]);
                oneLineTicketEntity.setPrincipalSeat(message[32]);
                // 动车或高铁
                if (oneLineTicketEntity.getTrainNo().contains("D") ||
                        oneLineTicketEntity.getTrainNo().contains("G")) {
                    System.out.println(oneLineTicketEntity.toString());
                }
            }
        }
        return code;
    }

    public static void main(String[] args) throws Exception {
        int num = 1;
        while (num-- > 0) {
            String code1 = doGet();
            Thread.sleep(6000);
            if (!code1.contains("200")) {
                //被反爬后 休眠一分钟
                Thread.sleep(60000);
                System.out.println("系统限制");
            }
        }
    }
}

