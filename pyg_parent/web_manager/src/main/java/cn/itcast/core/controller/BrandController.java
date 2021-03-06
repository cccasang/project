package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//品牌管理
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    //查询品牌所有数据
    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

    /**
     * 分页查询
     * @param page  当前页
     * @param rows  每页显示多少条数据
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(Integer page, Integer rows){
        return brandService.findPage(null,page,rows);
    }


    /**
     * 添加品牌数据
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
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
    public Brand findOne(Long id){
        return brandService.findOne(id);

    }

    /**
     * 修改数据
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody  Brand brand){
        try {
            brandService.update(brand);
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
            brandService.delete(ids);
            return new Result(true,"删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"删除失败!");

        }
    }

    /**
     * 高级分页查询
     * @param brand 品牌查询对象
     * @param page  当前页
     * @param rows  每页显示的条数
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Brand brand,Integer page, Integer rows){
       return brandService.findPage(brand,page,rows);
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
