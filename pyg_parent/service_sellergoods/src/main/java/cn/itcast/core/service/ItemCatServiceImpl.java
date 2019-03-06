package cn.itcast.core.service;

import cn.itcast.core.common.Constants;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private RedisTemplate redisTemplate;






    //查询所有数据ƒ
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }


    //分页显示数据
    @Override
    public PageResult findPage(ItemCat itemCat,Integer page, Integer rows) {
        //创建查询对象
        ItemCatQuery query = new ItemCatQuery();
        if (itemCat != null){
            ItemCatQuery.Criteria criteria = query.createCriteria();
            if (itemCat.getName() != null && !"".equals(itemCat.getName())){
                criteria.andNameLike("%"+itemCat.getName()+"%");
            }

        }

        //使用pageHelper进行分页
        PageHelper.startPage(page,rows);
        //通过dao层查询数据库查到所有数据,并强转为page集合
        Page<ItemCat> itemCatPageList =(Page<ItemCat>)itemCatDao.selectByExample(query);
        //返回pageResult
        return new PageResult(itemCatPageList.getTotal(),itemCatPageList.getResult());
    }

    //添加数据
    @Override
    public void add(ItemCat itemCat) {
        //直接添加
//        itemCatDao.insert(itemCat);
        //先判断属性是否为null
        itemCatDao.insertSelective(itemCat);
    }

    //查询修改的回显数据
    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    //修改数据
    @Override
    public void update(ItemCat itemCat) {
       itemCatDao.updateByPrimaryKeySelective(itemCat);
    }

    //批量删除
    @Override
    public void delete(Long[] ids) {
        if (ids != null){
            for (Long id : ids) {
                itemCatDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 根据传入的id获取该id下的所有分类列表数据
     * @param parentId
     * @return
     */
    @Override
    public List<ItemCat> findListByParentId(Long parentId) {
        /**
         * 查询分类所有数据,缓存到redis中,分类名称作为key,模板id作为value
         */
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList != null){
            for (ItemCat itemCat : itemCatList) {
                redisTemplate.boundHashOps(Constants.REDIS_CATEGORY_LIST).put(itemCat.getName(),itemCat.getTypeId());
            }
        }

        ItemCatQuery example = new ItemCatQuery();
        example.createCriteria().andParentIdEqualTo(parentId);
        return itemCatDao.selectByExample(example);
    }





}
