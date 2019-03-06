package cn.itcast.core.listener;

import cn.itcast.core.service.SolrManagerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 自定义监听器,监听来自于消息服务器发送来的消息,也就是商品id
 * 根据商品id到solr服务器中删除对应的数据,完成下架操作
 */
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private SolrManagerService solrManagerService;

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage)message;
        try {
            //1.获取消息,也就是商品id
            String goodsId = activeMQTextMessage.getText();
            //2.根据商品id到solr服务器中删除对应的数据,完成下架操作
            solrManagerService.deleteItemByGoodsId(Long.parseLong(goodsId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
