package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.util.List;
import java.util.Map;

@Service
public class SolrManagerServiceImpl implements SolrManagerService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private ItemDao itemDao;

    @Override
    public void importItemToSolr(Long goodsId) {
        //1.查询库存表所有已通过审核的库存数据
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        //查询状态为已审核的
        criteria.andStatusEqualTo("1");
        //查询商品id为指定商品的库存数据
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(query);
        //解析库存集合
        if (itemList != null){
            for (Item item : itemList) {
                //获取规格json字符串
                String specJsonStr = item.getSpec();
                //规格json字符串解析成map对象
                Map<String,String> map = JSON.parseObject(specJsonStr, Map.class);
                item.setSpecMap(map);


            }
        }
        //2.将库存集合数据存入索引库
        solrTemplate.saveBeans(itemList);
        //3.提交
        solrTemplate.commit();
    }

    @Override
    public void deleteItemByGoodsId(Long goodsId) {
        //创建查询对象
        Query query = new SimpleQuery();
        //创建条件查询对象
        Criteria criteria = new Criteria("item_goodsid").is(goodsId);
        query.addCriteria(criteria);
        //根据查询条件删除,这里的删除对象是根据商品id删除
        solrTemplate.delete(query);
        //提交
        solrTemplate.commit();
    }
}
