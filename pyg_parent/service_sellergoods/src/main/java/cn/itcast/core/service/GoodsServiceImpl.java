package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao goodsDescDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private SellerDao  sellerDao;

    @Autowired
    private JmsTemplate  jmsTemplate;

    //注入上架业务发送的目标
    @Autowired
    private ActiveMQTopic topicPageAndSolrDestination;

    //注入上架业务发送的目标
    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;


    @Override
    public void add(GoodsEntity goodsEntity) {
        //添加商品对象
        //商品状态默认为0,未审核
        goodsEntity.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(goodsEntity.getGoods());
        //添加商品详情对象
        //设置商品主键id作为商品详情的主键id 商品和商品详情是一对一关系
        goodsEntity.getGoodsDesc().setGoodsId(goodsEntity.getGoods().getId());
        goodsDescDao.insertSelective(goodsEntity.getGoodsDesc());
        //添加库存集合对象
        insertItemList(goodsEntity);


    }

    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        //创建查询对象
        GoodsQuery query = new GoodsQuery();
        //创建where条件查询对象
        GoodsQuery.Criteria criteria = query.createCriteria();
        if (goods != null){
            if (goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus())){
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (goods.getGoodsName() != null && !"".equals(goods.getGoodsName())){
                criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }
            if (!"admin".equals(goods.getSellerId()) && !"wc".equals(goods.getSellerId())
                    && !"".equals(goods.getSellerId()) && goods.getSellerId() != null){
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
        }
        Page<Goods> goodsList =(Page<Goods>)goodsDao.selectByExample(query);
        return new PageResult(goodsList.getTotal(),goodsList.getResult());
    }

    @Override
    public GoodsEntity findOne(Long id) {
        //1.根据商品id查询商品对象
        Goods goods = goodsDao.selectByPrimaryKey(id);
        //2.根据商品id查询商品详情对象
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        //3.根据商品id查询库存集合对象
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);
        //4.将查询到的所有对象封装到实体中
        GoodsEntity goodsEntity = new GoodsEntity();
        goodsEntity.setGoods(goods);
        goodsEntity.setGoodsDesc(goodsDesc);
        goodsEntity.setItemList(itemList);


        return goodsEntity;
    }

    /**
     * 更新商品
     * @param goodsEntity
     */
    @Override
    public void update(GoodsEntity goodsEntity) {

        //1.保存修改商品对象
        goodsDao.updateByPrimaryKeySelective(goodsEntity.getGoods());

        //2.保存修改商品详情对象
        goodsDescDao.updateByPrimaryKeySelective(goodsEntity.getGoodsDesc());

        //3.根据商品id删除对应的库存数据
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(goodsEntity.getGoods().getId());
        itemDao.deleteByExample(query);

        //4.将页面传入的新的库存集合数据添加到数据库中
        insertItemList(goodsEntity);

    }

    @Override
    public void delete(Long[] ids) {
        if (ids  != null){
            for (final Long id : ids) {
                /**
                 * 1.根据商品id 到数据库中逻辑删除商品
                 */
                Goods goods = new Goods();
                goods.setId(id);
                goods.setIsDelete("1");
                goodsDao.updateByPrimaryKeySelective(goods);

                /**
                 * 2.将商品id作为消息发送给消息服务器的下架队列中
                 */
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));
                    }
                });
            }
        }
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null){
            for (final Long id : ids) {
                //1.根据商品id改变商品表中的审核状态码
                Goods goods = new Goods();
                goods.setId(id);
                goods.setAuditStatus(status);
                goodsDao.updateByPrimaryKeySelective(goods);

                //2 .根据商品id改变库存表中的审核状态码
                Item item = new Item();
                item.setStatus(status);

                ItemQuery query = new ItemQuery();
                ItemQuery.Criteria criteria = query.createCriteria();
                criteria.andGoodsIdEqualTo(id);
                itemDao.updateByExampleSelective(item,query);

                //3.如果审核状态为 审核已通过,则将商品id作为消息发送给消息服务器
                jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));

                    }
                });

            }
        }
    }


    /**
     * 初始化item库存对象属性的值
     * @param goodsEntity  页面传入的大对象
     * @param item  需要初始化的库存对象
     * @return
     */
    private Item setItemValues(GoodsEntity goodsEntity,Item item){

        item.setGoodsId(goodsEntity.getGoods().getId());

        item.setUpdateTime(new Date());

        item.setCreateTime(new Date());

        item.setCategoryid(goodsEntity.getGoods().getCategory3Id());

        ItemCat itemCat = itemCatDao.selectByPrimaryKey(goodsEntity.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        Brand brand = brandDao.selectByPrimaryKey(goodsEntity.getGoods().getBrandId());
        item.setBrand(brand.getName());

        item.setSellerId(goodsEntity.getGoods().getSellerId());

        Seller seller = sellerDao.selectByPrimaryKey(goodsEntity.getGoods().getSellerId());
        item.setSeller(seller.getName());

        String imgJsonStr = goodsEntity.getGoodsDesc().getItemImages();
        List<Map> maps = JSON.parseArray(imgJsonStr, Map.class);
        if (maps != null){
            item.setImage(String.valueOf(maps.get(0).get("url")));
        }

        item.setStatus("0");

        return item;
    }

    /**
     * 添加库存集合对象
     * @param goodsEntity
     */
    public void insertItemList(GoodsEntity  goodsEntity){
        if ("1".equals(goodsEntity.getGoods().getIsEnableSpec())){
            //在添加商品的时候是否启用规格为勾选状态 有规格 有库存对象
            if (goodsEntity.getItemList() !=  null){
                for (Item item : goodsEntity.getItemList()) {
                    //初始化库存对象的属性值
                    item = setItemValues(goodsEntity, item);


                    String title = goodsEntity.getGoods().getGoodsName();
                    String specJsonStr = item.getSpec();
                    Map<String,String> specMap = JSON.parseObject(specJsonStr, Map.class);
                    Collection<String> values = specMap.values();
                    for (String value : values) {
                        title +=" "+value;
                    }
                    item.setTitle(title);
                    itemDao.insertSelective(item);

                }
            }
        }else {
            //在添加商品的时候是否启用规格为未勾选状态 没有规格 没有库存对象
            //初始化库存对象的属性值
            Item item = new Item();
            item = setItemValues(goodsEntity, item);

            //初始化商品名为库存标题
            item.setTitle(goodsEntity.getGoods().getGoodsName());

            //初始化价格
            item.setPrice(new BigDecimal("0"));
            //初始化规格
            item.setSpec("{}");
            //初始化数量
            item.setNum(0);
            //初始化是否默认
            item.setIsDefault("1");
            itemDao.insertSelective(item);

        }

    }

}
