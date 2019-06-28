package data;

import lombok.Data;

/**
 * @description: TODO
 * @author: KimJun
 * @date: 07/12/18 16:27
 */
@Data
public class TicketEntity {
    //车次
    private String trainNo;

    //发车站
    private String startStation;

    //目的站
    private String destStation;

    //发车时间
    private String startTime;

    //到达时间
    private String arrTime;

    //总共时间
    private String costTime;

    //是否可以购买
    private String canBuy;

    //发车日期
    private String startDate;

    //软卧
    private String solfSleep;

    //硬座
    private String hardSeat;

    //无座
    private String noSeat;

    //硬卧
    private String hardSleep;

    //一等座
    private String firstClassSeat;

    //二等座
    private String secondClassSeat;

    //特等座
    private String principalSeat;

    @Override
    public String toString() {
        return "车次:" + this.getTrainNo() + "\n" +
                "日期:" + this.startDate +
                " 发车站:" + this.startStation +
                " 终点站:" + this.destStation +
                " 发车时间: " + this.startTime +
                " 到站时间: " + this.arrTime +
                " 历时：" + this.costTime +
                " 商务座：" + this.principalSeat +
                " 一等座: " + this.firstClassSeat +
                " 二等座：" + this.secondClassSeat;
    }

}
