package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {

    public List<ItemCat> findAll();

    public PageResult findPage(ItemCat itemCat, Integer page, Integer rows);

    public void add(ItemCat itemCat);

    public ItemCat findOne(Long id);

    public void update(ItemCat itemCat);

    public void delete(Long[] ids);

    public List<ItemCat> findListByParentId(Long parentId);
}
