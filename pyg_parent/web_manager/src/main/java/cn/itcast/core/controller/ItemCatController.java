package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//品牌管理
@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference
    private ItemCatService itemCatService;

    //查询品牌所有数据
    @RequestMapping("/findAll")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();
    }

    /**
     * 分页查询
     * @param page  当前页
     * @param rows  每页显示多少条数据
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(Integer page, Integer rows){
        return itemCatService.findPage(null,page,rows);
    }


    /**
     * 添加品牌数据
     * @param itemCat
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ItemCat itemCat){
        try {
            itemCatService.add(itemCat);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**
     * 回显修改数据
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public ItemCat findOne(Long id){
        return itemCatService.findOne(id);

    }

    /**
     * 修改数据
     * @param itemCat
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody  ItemCat itemCat){
        try {
            itemCatService.update(itemCat);
            return new Result(true,"修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败!");
        }
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            itemCatService.delete(ids);
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"删除失败!");

        }
    }

    /**
     * 高级分页查询
     * @param itemCat 品牌查询对象
     * @param page  当前页
     * @param rows  每页显示的条数
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody ItemCat itemCat,Integer page, Integer rows){
       return itemCatService.findPage(itemCat,page,rows);
    }


    /**
     * 查询该分类下的子分类列表
     * @param parentId
     * @return
     */
    @RequestMapping("/findByParentId")
    public List<ItemCat> findListByParentId(Long parentId) {
        return itemCatService.findListByParentId(parentId);
    }





}
