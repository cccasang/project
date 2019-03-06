package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Spec;
import cn.itcast.core.pojo.specification.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    public List<Specification> findAll();

    public PageResult findPage(Specification specification, Integer page, Integer rows);

    public void add(Spec spec);

    public Spec findOne(Long id);

    public void update(Spec spec);

    public void delete(Long[] ids);

    public List<Map> selectOptionList();


}
