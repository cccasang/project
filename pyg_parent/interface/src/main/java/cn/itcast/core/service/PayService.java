package cn.itcast.core.service;

import cn.itcast.core.pojo.log.PayLog;

import java.util.Map;

public interface PayService {

    /**
     * 根据支付单号和总金额,调用微信统一下单接口,生成支付链接
     *
     * @param out_trade_no 支付单号
     * @param total_fee    总金额
     * @return 返回支付链接等map信息
     */
    public Map createNative(String out_trade_no, String total_fee);

    /**
     * 查询接口,根据支付单号,查询是否支付成功
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 根据当前登录的用户名到redis中获取支付日志对象
     * @param userName
     * @return
     */
    public PayLog findPayLogFromRedisByUserName(String userName);


    }