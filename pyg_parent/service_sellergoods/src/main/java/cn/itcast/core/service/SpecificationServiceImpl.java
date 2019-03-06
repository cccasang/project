package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Spec;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationDao specificationDao;

    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    //查询所有数据
    @Override
    public List<Specification> findAll() {
        return specificationDao.selectByExample(null);
    }


    //分页显示数据
    @Override
    public PageResult findPage(Specification specification,Integer page, Integer rows) {
        //创建查询对象
        SpecificationQuery query = new SpecificationQuery();
        if (specification != null){
            SpecificationQuery.Criteria criteria = query.createCriteria();
            if (specification.getSpecName() != null && !"".equals(specification.getSpecName())){
                criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
            }
        }

        //使用pageHelper进行分页
        PageHelper.startPage(page,rows);
        //通过dao层查询数据库查到所有数据,并强转为page集合
        Page<Specification> specificationPageList =(Page<Specification>)specificationDao.selectByExample(query);
        //返回pageResult
        return new PageResult(specificationPageList.getTotal(),specificationPageList.getResult());
    }

    //添加数据
    @Override
    public void add(Spec spec) {
        //先保存规格对象
        Specification specification = spec.getSpecification();
        specificationDao.insertSelective(specification);
        //再保存规格选项对象
        List<SpecificationOption> optionList = spec.getSpecificationOptionList();
        for (SpecificationOption option : optionList) {
            //维护多对一关系
            option.setSpecId(specification.getId());
            specificationOptionDao.insertSelective(option);
        }
    }

    //查询修改的回显数据
    @Override
    public Spec findOne(Long id) {
        Spec spec = new Spec();
        //规格
        spec.setSpecification(specificationDao.selectByPrimaryKey(id));
        //规格选项集合
        SpecificationOptionQuery example = new SpecificationOptionQuery();
        example.createCriteria().andSpecIdEqualTo(id);
        spec.setSpecificationOptionList(specificationOptionDao.selectByExample(example));

        return spec;
    }

    //修改数据
    @Override
    public void update(Spec spec) {
       //specificationDao.updateByPrimaryKeySelective(spec);
        //先保存规格
        Specification specification = spec.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);
        //删除掉关联的规格选项
        SpecificationOptionQuery example = new SpecificationOptionQuery();
        example.createCriteria().andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(example);
        //将规格选项再次保存到数据库中
        List<SpecificationOption> optionList = spec.getSpecificationOptionList();
        for (SpecificationOption option : optionList) {
            //维护多对一关系
            option.setSpecId(specification.getId());
            specificationOptionDao.insertSelective(option);
        }

    }

    //批量删除
    @Override
    public void delete(Long[] ids) {
        if (ids != null){
            for (Long id : ids) {
                //删除掉规格
                specificationDao.deleteByPrimaryKey(id);
                //并且删除掉当前规格下的规格选项
                SpecificationOptionQuery example = new SpecificationOptionQuery();
                example.createCriteria().andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(example);
            }
        }
    }

    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
