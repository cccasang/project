package cn.itcast.core.service;

import cn.itcast.core.common.Constants;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateDao typeTemplateDao;

    @Autowired
    private SpecificationOptionDao optionDao;

    @Autowired
    private RedisTemplate redisTemplate;

    //查询所有数据
    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateDao.selectByExample(null);
    }


    //分页显示数据
    @Override
    public PageResult findPage(TypeTemplate typeTemplate,Integer page, Integer rows) {

        /**
         * 查询模板所有数据,以模板id作为key,对应的品牌集合作为value,存入Redis中
         * 查询模板所有数据,以模板id作为key,对应的规格集合作为value,存入Redis中
         */

        List<TypeTemplate> templateList = typeTemplateDao.selectByExample(null);
        if (templateList != null){
            for (TypeTemplate template : templateList) {
                //1.以模板id作为key,对应的品牌集合作为value,存入Redis中
                String brandIdsJsonStr = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandIdsJsonStr, Map.class);
                redisTemplate.boundHashOps(Constants.REDIS_BRAND_LIST).put(template.getId(),brandList);
                //2.以模板id作为key,对应的规格集合作为value,存入Redis中
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps(Constants.REDIS_SPEC_LIST).put(template.getId(),specList);

            }
        }


        //创建查询对象
        TypeTemplateQuery query = new TypeTemplateQuery();
        if (typeTemplate != null){
            TypeTemplateQuery.Criteria criteria = query.createCriteria();
            if (typeTemplate.getName() != null && !"".equals(typeTemplate.getName())){
                criteria.andNameLike("%"+typeTemplate.getName()+"%");
            }

        }

        //使用pageHelper进行分页
        PageHelper.startPage(page,rows);
        //通过dao层查询数据库查到所有数据,并强转为page集合
        Page<TypeTemplate> typeTemplatePageList =(Page<TypeTemplate>)typeTemplateDao.selectByExample(query);
        //返回pageResult
        return new PageResult(typeTemplatePageList.getTotal(),typeTemplatePageList.getResult());
    }

    //添加数据
    @Override
    public void add(TypeTemplate typeTemplate) {
        //直接添加
//        typeTemplateDao.insert(typeTemplate);
        //先判断属性是否为null
        typeTemplateDao.insertSelective(typeTemplate);
    }

    //查询修改的回显数据
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    //修改数据
    @Override
    public void update(TypeTemplate typeTemplate) {
       typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    //批量删除
    @Override
    public void delete(Long[] ids) {
        if (ids != null){
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map> findBySpecList(Long id) {
        //1.根据模板id查询模板对象
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        //2.从模板对象中获取规格的json字符串
        String jsonStr = typeTemplate.getSpecIds();
        //3.解析规格json字符串
        List<Map> maps = JSON.parseArray(jsonStr, Map.class);
        //4.遍历规格集合
        if (maps != null){
            for (Map map : maps) {
                Long specId =Long.parseLong(String.valueOf(map.get("id")));
                SpecificationOptionQuery query = new SpecificationOptionQuery();
                SpecificationOptionQuery.Criteria criteria = query.createCriteria();
                criteria.andSpecIdEqualTo(specId);
                List<SpecificationOption> optionList = optionDao.selectByExample(query);
                map.put("options",optionList);

            }
        }
        //5.遍历过程中,根据规格id,查询对应的规格选项集合

        //6.将规格选项集合,封装回规格集合中

        return maps;
    }
}
