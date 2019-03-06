package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandDao brandDao;

    //查询所有数据
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }


    //分页显示数据
    @Override
    public PageResult findPage(Brand brand,Integer page, Integer rows) {
        //创建查询对象
        BrandQuery query = new BrandQuery();
        if (brand != null){
            BrandQuery.Criteria criteria = query.createCriteria();
            if (brand.getName() != null && !"".equals(brand.getName())){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar())){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }

        //使用pageHelper进行分页
        PageHelper.startPage(page,rows);
        //通过dao层查询数据库查到所有数据,并强转为page集合
        Page<Brand> brandPageList =(Page<Brand>)brandDao.selectByExample(query);
        //返回pageResult
        return new PageResult(brandPageList.getTotal(),brandPageList.getResult());
    }

    //添加数据
    @Override
    public void add(Brand brand) {
        //直接添加
//        brandDao.insert(brand);
        //先判断属性是否为null
        brandDao.insertSelective(brand);
    }

    //查询修改的回显数据
    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    //修改数据
    @Override
    public void update(Brand brand) {
       brandDao.updateByPrimaryKeySelective(brand);
    }

    //批量删除
    @Override
    public void delete(Long[] ids) {
        if (ids != null){
            for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
