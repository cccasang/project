package cn.itcast.core.service;

import cn.itcast.core.common.Constants;
import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.pojo.entity.PageResult;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult search(Integer page, Integer rows, Content content) {
        PageHelper.startPage(page,rows);
        Page<Content> contentList =(Page<Content>)contentDao.selectByExample(null);
        return new PageResult(contentList.getTotal(),contentList.getResult());
    }

    @Override
    public void add(Content content) {

        //1.根据广告的分类id删除redis中对应的广告数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
        //2.将广告数据添加到redis中
        contentDao.insertSelective(content);
    }

    @Override
    public Content findOne(Long id) {
        return contentDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(Content content) {
        //1.根据页面传入的广告对象的主键id,到数据库中查询对应的广告对象
        Content oldContent = findOne(content.getId());
        //2.根据数据库中查询对应的广告对象中的分类id,删除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(oldContent.getCategoryId());
        //3.根据页面传入的新的广告对象中的分类id,删除redis中对应的广告集合数据
        redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
        //4.将页面传入的新的广告对象保存到数据库中
        contentDao.updateByPrimaryKeySelective(content);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids !=  null){
            for (Long id : ids) {
                //1.根据广告id到数据库中查询广告对象
                Content content = findOne(id);
                //2.根据广告对象中的分类id删除redis中对应的广告集合数据
                redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).delete(content.getCategoryId());
                //3.根据广告id删除数据库中对应的广告数据
                contentDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        ContentQuery query = new ContentQuery();
        ContentQuery.Criteria criteria = query.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<Content> contentList = contentDao.selectByExample(query);
        return contentList;
    }

    @Override
    public List<Content> findByCategoryIdFromRedis(Long categoryId) {
        List<Content> contentList = (List<Content>)redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).get(categoryId);
        if (contentList != null  && !"".equals(contentList)){
            return contentList;

        }else {
            contentList = findByCategoryId(categoryId);
            redisTemplate.boundHashOps(Constants.REDIS_CONTENT_LIST).put(categoryId,contentList);
            return contentList;

        }
    }
}
