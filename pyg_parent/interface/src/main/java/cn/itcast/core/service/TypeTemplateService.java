package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {

    public List<TypeTemplate> findAll();

    public PageResult findPage(TypeTemplate typeTemplate, Integer page, Integer rows);

    public void add(TypeTemplate typeTemplate);

    public TypeTemplate findOne(Long id);

    public void update(TypeTemplate typeTemplate);

    public void delete(Long[] ids);

    public List<Map> findBySpecList(Long id);

}
