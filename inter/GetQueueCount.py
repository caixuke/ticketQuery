# coding=utf-8
import datetime
import sys
import time
from collections import OrderedDict
import wrapcache

from config.ticketConf import _get_yaml
from inter.ConfirmSingleForQueue import confirmSingleForQueue


def conversion_int(str):
    return int(str)


class getQueueCount:
    def __init__(self, session, is_need_code, ifShowPassCodeTime, set_type, station_dates, train_no, ticket_peoples,
                 ticketInfoForPassengerForm, token, oldPassengerStr, passengerTicketStrList):
        self.station_dates = station_dates
        self.session = session
        self.is_need_code = is_need_code
        self.ifShowPassCodeTime = ifShowPassCodeTime
        self.set_type = set_type
        self.train_no = train_no
        self.ticket_peoples = ticket_peoples
        self.ticket_black_list = {}
        self.ticketInfoForPassengerForm = ticketInfoForPassengerForm
        self.token = token
        self.oldPassengerStr = oldPassengerStr
        self.passengerTicketStrList = passengerTicketStrList

    def data_par(self):
        """
        参数结构
        自动提交代码接口-autoSubmitOrderRequest
            - 字段说明
                - secretStr 车票代码
                - train_date 乘车日期
                - tour_flag 乘车类型
                - purpose_codes 学生还是成人
                - query_from_station_name 起始车站
                - query_to_station_name 结束车站
                - cancel_flag 默认2，我也不知道干嘛的
                - bed_level_order_num  000000000000000000000000000000
                - passengerTicketStr   乘客乘车代码
                - oldPassengerStr  乘客编号代码
        :return:
        """

        if sys.version_info.major is 2:
            new_train_date = filter(None, str(time.asctime(time.strptime(self.station_dates, "%Y-%m-%d"))).split(" "))
        else:
            new_train_date = list(filter(None, str(time.asctime(time.strptime(self.station_dates, "%Y-%m-%d"))).split(" ")))
        data = OrderedDict()
        data['train_date'] = "{0} {1} 0{2} {3} 00:00:00 GMT+0800 (中国标准时间)".format(
            new_train_date[0],
            new_train_date[1],
            new_train_date[2],
            new_train_date[4],
        ),
        data['train_no'] = self.ticketInfoForPassengerForm['queryLeftTicketRequestDTO']['train_no'],
        data['stationTrainCode'] = self.ticketInfoForPassengerForm['queryLeftTicketRequestDTO'][
                                       'station_train_code'],
        data['seatType'] = self.set_type,
        data['fromStationTelecode'] = self.ticketInfoForPassengerForm['queryLeftTicketRequestDTO'][
                                          'from_station'],
        data['toStationTelecode'] = self.ticketInfoForPassengerForm['queryLeftTicketRequestDTO']['to_station'],
        data['leftTicket'] = self.ticketInfoForPassengerForm['leftTicketStr'],
        data['purpose_codes'] = self.ticketInfoForPassengerForm['purpose_codes'],
        data['train_location'] = self.ticketInfoForPassengerForm['train_location'],
        data['REPEAT_SUBMIT_TOKEN'] = self.token,
        return data

    def sendGetQueueCount(self):
        """
        # 模拟查询当前的列车排队人数的方法
        # 返回信息组成的提示字符串
        :return:
        """
        getQueueCountResult = self.session.httpClint.send(self.session.urls["getQueueCountUrl"], self.data_par())
        if "status" in getQueueCountResult and getQueueCountResult["status"] is True:
            if "countT" in getQueueCountResult["data"]:
                ticket = getQueueCountResult["data"]["ticket"]
                ticket_split = sum(map(conversion_int, ticket.split(","))) if ticket.find(",") != -1 else ticket
                countT = getQueueCountResult["data"]["countT"]
                # if int(countT) is 0:
                print(u"排队成功, 你排在: {1}位, 当前余票还剩余: {0} 张，其中二等票{2}张，无座{3}张".format(ticket_split, countT, ticket.split(",")[0], ticket.split(",")[1]))
                csf = confirmSingleForQueue(self.session, self.ifShowPassCodeTime, self.is_need_code, self.token,
                                            self.set_type, self.ticket_peoples, self.ticketInfoForPassengerForm,
                                            self.oldPassengerStr, self.passengerTicketStrList)
                # csf.sendConfirmSingleForQueue()
            #     else:
            #         print(u"当前排队人数: {1} 当前余票还剩余:{0} 张，继续排队中".format(ticket_split, countT))
            else:
                print(u"排队发现未知错误{0}，将此列车 {1}加入小黑屋".format(getQueueCountResult, self.train_no))
                wrapcache.set(key=self.train_no, value=datetime.datetime.now(),
                              timeout=int(_get_yaml()["ticket_black_list_time"]) * 60)
        elif "messages" in getQueueCountResult and getQueueCountResult["messages"]:
            print(u"排队异常，错误信息：{0}, 将此列车 {1}加入小黑屋".format(getQueueCountResult["messages"][0], self.train_no))
            wrapcache.set(key=self.train_no, value=datetime.datetime.now(),
                          timeout=int(_get_yaml()["ticket_black_list_time"]) * 60)
        else:
            if "validateMessages" in getQueueCountResult and getQueueCountResult["validateMessages"]:
                print(str(getQueueCountResult["validateMessages"]))
                wrapcache.set(key=self.train_no, value=datetime.datetime.now(),
                              timeout=int(_get_yaml()["ticket_black_list_time"]) * 60)
            else:
                print(u"未知错误 {0}".format("".join(getQueueCountResult)))
